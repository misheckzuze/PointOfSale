package com.pointofsale.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pointofsale.data.Database;
import com.pointofsale.model.*;
import org.junit.*;
import static org.junit.Assert.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class SynchronizationTest {
    private static Path directory;
    private static ServerSocket server;
    private static final AtomicInteger calls = new AtomicInteger();
    private static volatile String response = "{\"statusCode\":1,\"data\":{\"validationURL\":\"test\"}}";
    private static volatile int delay;
    private static volatile String submittedBody;
    private static final ObjectMapper JSON = new ObjectMapper();

    @BeforeClass public static void initialize() throws Exception {
        directory = Files.createTempDirectory("pos-sync-tests-");
        System.setProperty("pos.database.path", directory.resolve("test.db").toString());
        server = new ServerSocket(0, 10, InetAddress.getLoopbackAddress());
        System.setProperty("pos.api.baseUrl", "http://127.0.0.1:" + server.getLocalPort());
        Thread listener = new Thread(() -> {
            while (!server.isClosed()) {
                try {
                    Socket socket = server.accept();
                    Thread handler = new Thread(() -> respond(socket)); handler.setDaemon(true); handler.start();
                } catch (IOException ignored) { }
            }
        });
        listener.setDaemon(true); listener.start();
        // Exercise additive migration against the actual pre-release invoice schema.
        sql("CREATE TABLE Invoices (InvoiceNumber TEXT PRIMARY KEY,InvoiceDateTime TEXT,InvoiceTotal REAL,SellerTin TEXT,BuyerTin TEXT,TotalVAT REAL,OfflineTransactionSignature TEXT,SiteId TEXT,ValidationUrl TEXT,IsReliefSupply INTEGER,State INTEGER,PaymentId TEXT,AmountPaid REAL)");
        Database.initializeDatabase();
    }

    private static void respond(Socket socket) {
        try (Socket connection = socket) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            int length = 0; String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                if (line.toLowerCase(Locale.ROOT).startsWith("content-length:")) length = Integer.parseInt(line.substring(15).trim());
            }
            char[] body = new char[length]; int offset = 0;
            while (offset < length) { int n = reader.read(body, offset, length-offset); if (n < 0) break; offset += n; }
            submittedBody = new String(body); calls.incrementAndGet();
            Thread.sleep(delay);
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            OutputStream output = connection.getOutputStream();
            output.write(("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nConnection: close\r\nContent-Length: " + bytes.length + "\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
            output.write(bytes); output.flush();
        } catch (Exception ignored) { }
    }

    @Before public void reset() throws Exception {
        sql("DELETE FROM LineItems"); sql("DELETE FROM InvoiceTaxBreakDown"); sql("DELETE FROM InvoiceLevies");
        sql("DELETE FROM Invoices"); sql("DELETE FROM Products");
        delay = 0; response = "{\"statusCode\":1,\"data\":{\"validationURL\":\"test\"}}";
    }

    @AfterClass public static void cleanup() throws Exception {
        server.close();
        try (java.util.stream.Stream<Path> files = Files.walk(directory)) {
            for (Path file : (Iterable<Path>) files.sorted(Comparator.reverseOrder())::iterator) Files.deleteIfExists(file);
        }
    }

    static void sql(String sql) throws Exception { try (Connection c = Database.createConnection(); Statement s = c.createStatement()) { s.execute(sql); } }
    static String scalar(String query) throws Exception {
        try (Connection c = Database.createConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(query)) { return r.next() ? r.getString(1) : null; }
    }
    static String product(String code, int quantity, int price) {
        return "{\"productCode\":\""+code+"\",\"productName\":\"Test\",\"description\":\"Test\",\"quantity\":"+quantity+",\"price\":"+price+",\"siteId\":\"site\",\"taxRateId\":\"A\",\"isProduct\":true}";
    }
    static void upsert(String product, long revision) throws Exception { ProductSyncService.upsertBatch(Collections.singletonList(JSON.readTree(product)), "site", revision); }
    static void invoice(String number, String payload) throws Exception {
        try (Connection c = Database.createConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO Invoices(InvoiceNumber,State,OfflineTransactionSignature,Payload) VALUES (?,0,'signed',?)")) {
            s.setString(1, number); s.setString(2, payload); s.executeUpdate();
        }
    }
    static String payload(String number) {
        return "{\"invoiceHeader\":{\"invoiceNumber\":\""+number+"\",\"invoiceDateTime\":\"2025-03-01T12:00:00\"},\"invoiceLineItems\":[{\"unitPrice\":10.25,\"quantity\":2,\"discount\":0.5,\"total\":20}],\"invoiceSummary\":{\"invoiceTotal\":20,\"totalVAT\":2.45,\"amountTendered\":25},\"futureField\":{\"preserved\":true}}";
    }

    @Test public void migrationIsRepeatableAndLeavesLegacyRowsNull() throws Exception {
        invoice("old", null); Database.initializeDatabase();
        assertNull(scalar("SELECT Payload FROM Invoices WHERE InvoiceNumber='old'"));
        assertEquals("0", scalar("SELECT State FROM Invoices WHERE InvoiceNumber='old'"));
    }
    @Test public void snapshotsPreserveAllFieldsAndAddOnlySignature() throws Exception {
        String original = payload("snapshot"); invoice("snapshot", original);
        String sent = InvoiceSnapshots.forTransmission("snapshot", () -> { throw new AssertionError("Legacy builder ran"); });
        JsonObject expected = JsonParser.parseString(original).getAsJsonObject();
        expected.getAsJsonObject("invoiceSummary").addProperty("offlineSignature", "signed");
        assertEquals(expected, JsonParser.parseString(sent));
        assertEquals(original, scalar("SELECT Payload FROM Invoices"));
        assertEquals("0", scalar("SELECT State FROM Invoices"));
    }
    @Test public void nullSnapshotUsesLegacyBuilderWithoutBackfill() throws Exception {
        invoice("legacy", null); AtomicInteger built = new AtomicInteger();
        String sent = InvoiceSnapshots.forTransmission("legacy", () -> { built.incrementAndGet(); return payload("legacy"); });
        assertEquals(1, built.get()); assertTrue(sent.contains("signed")); assertNull(scalar("SELECT Payload FROM Invoices"));
    }
    @Test public void corruptSnapshotDoesNotFallBack() throws Exception {
        invoice("bad", "not JSON");
        assertThrows(RuntimeException.class, () -> InvoiceSnapshots.forTransmission("bad", () -> { throw new AssertionError(); }));
    }
    @Test public void mismatchedSnapshotDoesNotSubmit() throws Exception {
        invoice("one", payload("two"));
        assertThrows(IllegalArgumentException.class, () -> InvoiceSnapshots.forTransmission("one", () -> ""));
    }
    @Test public void unsignedSnapshotRemainsPending() throws Exception {
        invoice("unsigned", payload("unsigned")); sql("UPDATE Invoices SET OfflineTransactionSignature='' ");
        assertThrows(IllegalStateException.class, () -> InvoiceSnapshots.forTransmission("unsigned", () -> ""));
    }
    @Test public void savingInvoiceStoresSnapshotAtomically() throws Exception {
        InvoiceHeader header = new InvoiceHeader(); header.setInvoiceNumber("new"); header.setInvoiceDateTime("2026-09-24T12:00:00");
        header.setBuyerTIN("buyer"); header.setGlobalConfigVersion(42);
        LineItemDto line = new LineItemDto(); line.setProductCode("p"); line.setQuantity(2); line.setUnitPrice(10.25); line.setDiscount(0.5); line.setTotal(20); line.setTotalVAT(2.45);
        assertTrue(Helper.saveTransaction(header, Collections.singletonList(line), Collections.emptyList(), Collections.emptyList(),20,2.45,"signed","url",false,"Cash",25));
        JsonObject saved = JsonParser.parseString(scalar("SELECT Payload FROM Invoices")).getAsJsonObject();
        assertEquals("signed", saved.getAsJsonObject("invoiceSummary").get("offlineSignature").getAsString());
        assertEquals("signed", scalar("SELECT OfflineTransactionSignature FROM Invoices"));
        assertEquals(42, saved.getAsJsonObject("invoiceHeader").get("globalConfigVersion").getAsInt());
        assertEquals(25, saved.getAsJsonObject("invoiceSummary").get("amountTendered").getAsInt());
        assertEquals(0.5, saved.getAsJsonArray("invoiceLineItems").get(0).getAsJsonObject().get("discount").getAsDouble(),0);
        assertFalse(Helper.saveTransaction(header, Collections.singletonList(line), Collections.emptyList(), Collections.emptyList(),20,2.45,"signed","url",false,"Cash",25));
        assertEquals("1", scalar("SELECT COUNT(*) FROM LineItems"));
    }
    @Test public void offlineSaveRequiresSignatureButOnlineSaveDoesNot() throws Exception {
        InvoiceHeader header = new InvoiceHeader(); header.setInvoiceNumber("signature-check");
        for (String signature : new String[] {null, "", "   "}) {
            assertFalse(Helper.saveTransaction(header, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),20,0,signature,"",false,"Cash",20));
        }
        assertEquals("0", scalar("SELECT COUNT(*) FROM Invoices"));
        assertTrue(Helper.saveTransaction(header, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),20,0,"","server-url",true,"Cash",20));
        assertEquals("1", scalar("SELECT State FROM Invoices"));
    }
    @Test public void productUpdatesPreserveLocalDiscount() throws Exception {
        upsert(product("p",100,10), ProductSyncService.salesRevision()); sql("UPDATE Products SET Discount=5");
        upsert(product("p",120,20), ProductSyncService.salesRevision());
        assertEquals("20.0", scalar("SELECT Price FROM Products")); assertEquals("5.0", scalar("SELECT Discount FROM Products"));
        assertEquals("1", scalar("SELECT COUNT(*) FROM Products"));
    }
    @Test public void pendingSalesAreDeductedFromServerStock() throws Exception {
        upsert(product("p",100,10), ProductSyncService.salesRevision()); invoice("pending", null);
        sql("INSERT INTO LineItems(InvoiceNumber,ProductCode,Quantity,IsProduct) VALUES ('pending','p',3,1)");
        upsert(product("p",100,10), ProductSyncService.salesRevision());
        assertEquals("97.0", scalar("SELECT Quantity FROM Products"));
    }
    @Test public void concurrentSalePreventsStaleQuantityOverwrite() throws Exception {
        upsert(product("p",100,10), ProductSyncService.salesRevision()); long revision = ProductSyncService.salesRevision();
        invoice("concurrent", null); sql("UPDATE Products SET Quantity=96");
        upsert(product("p",100,25), revision);
        assertEquals("96.0", scalar("SELECT Quantity FROM Products")); assertEquals("25.0", scalar("SELECT Price FROM Products"));
    }
    @Test public void failedBatchRollsBackAndCanBeRetried() throws Exception {
        assertThrows(SQLException.class, () -> ProductSyncService.upsertBatch(Arrays.asList(JSON.readTree(product("a",10,2)), JSON.readTree("{}")), "site", ProductSyncService.salesRevision()));
        assertEquals("0", scalar("SELECT COUNT(*) FROM Products"));
        upsert(product("a",10,2), ProductSyncService.salesRevision()); assertEquals("1", scalar("SELECT COUNT(*) FROM Products"));
    }
    @Test public void largeCatalogueIsAppliedInBatchesAndIdempotent() throws Exception {
        Path file = directory.resolve("catalogue.json");
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("{\"data\":[");
            for (int i=0;i<1205;i++) { if (i>0) writer.write(','); writer.write(product("p"+i,100,10)); }
            writer.write("],\"statusCode\":1}");
        }
        assertEquals(1205, ProductSyncService.applyDownloadedCatalogue(file,"site",ProductSyncService.salesRevision()));
        assertEquals(1205, ProductSyncService.applyDownloadedCatalogue(file,"site",ProductSyncService.salesRevision()));
        assertEquals("1205", scalar("SELECT COUNT(*) FROM Products"));
    }
    @Test public void rejectedCatalogueDoesNotChangeDatabase() throws Exception {
        Path file = directory.resolve("rejected.json"); Files.writeString(file,"{\"data\":["+product("p",100,10)+"],\"statusCode\":-1}");
        assertThrows(IllegalArgumentException.class, () -> ProductSyncService.applyDownloadedCatalogue(file,"site",0));
        assertEquals("0",scalar("SELECT COUNT(*) FROM Products"));
    }
    @Test public void wrongSiteIsRejected() throws Exception {
        assertThrows(SQLException.class, () -> upsert(product("p",10,10).replace("\"site\"","\"other\""),ProductSyncService.salesRevision()));
    }
    @Test public void productHttpDownloadUsesCurrentSite() throws Exception {
        response="{\"statusCode\":1,\"data\":["+product("download",10,5)+"]}";
        assertEquals(1, new ProductSyncService().synchronize(new URI(System.getProperty("pos.api.baseUrl")+"/products"),"token","tin","site"));
        assertTrue(submittedBody.contains("site")); assertEquals("download",scalar("SELECT ProductCode FROM Products"));
    }
    @Test public void overlappingAndStaleSubmissionsSendOnce() throws Exception {
        invoice("once",payload("once")); delay=300;
        ApiClient client=new ApiClient(); String body=InvoiceSnapshots.forTransmission("once",()->"");
        int before=calls.get(); CountDownLatch done=new CountDownLatch(2);
        BiConsumer<Boolean, String> completion = new BiConsumer<Boolean, String>() {
            @Override public void accept(Boolean accepted, String validationUrl) { done.countDown(); }
        };
        client.submitTransactions(body, "test", completion);
        client.submitTransactions(body, "test", completion);
        assertTrue(done.await(10,TimeUnit.SECONDS)); assertEquals(before+1,calls.get());
        assertTrue(InvoiceSnapshots.isTransmitted("once"));
        CountDownLatch stale = new CountDownLatch(1);
        client.submitTransactions(body, "test", new BiConsumer<Boolean, String>() {
            @Override public void accept(Boolean accepted, String validationUrl) { stale.countDown(); }
        });
        assertTrue(stale.await(5,TimeUnit.SECONDS)); assertEquals(before+1,calls.get());
    }
    @Test public void duplicateServerResponseReconcilesPendingInvoice() throws Exception {
        invoice("accepted",payload("accepted")); response="{\"statusCode\":-2,\"remark\":\"Invoice Number already exists\"}";
        sql("UPDATE Invoices SET ValidationUrl='offline-receipt'");
        CountDownLatch done=new CountDownLatch(1); AtomicBoolean success=new AtomicBoolean();
        new ApiClient().submitTransactions(InvoiceSnapshots.forTransmission("accepted", () -> ""), "token",
                new BiConsumer<Boolean, String>() {
                    @Override public void accept(Boolean accepted, String validationUrl) {
                        success.set(accepted);
                        done.countDown();
                    }
                });
        assertTrue(done.await(10,TimeUnit.SECONDS)); assertTrue(success.get()); assertTrue(InvoiceSnapshots.isTransmitted("accepted"));
        Helper.updateValidationUrl("accepted", "");
        assertEquals("offline-receipt", scalar("SELECT ValidationUrl FROM Invoices"));
    }
    @Test public void manualSingleInvoiceUsesSnapshotWithoutRelationalLines() throws Exception {
        invoice("single", payload("single"));
        assertTrue(Helper.transmitInvoice("single"));
        assertTrue(submittedBody.contains("futureField"));
        assertTrue(submittedBody.contains("2025-03-01T12:00:00"));
    }
    @Test public void manualBatchUsesSnapshotWithoutRelationalLines() throws Exception {
        invoice("batch", payload("batch")); CountDownLatch done = new CountDownLatch(1);
        AtomicReference<List<String>> failed = new AtomicReference<>();
        Helper.retryPendingTransactions(null, errors -> { failed.set(errors); done.countDown(); });
        assertTrue(done.await(10,TimeUnit.SECONDS)); assertTrue(failed.get().isEmpty());
        assertTrue(submittedBody.contains("futureField"));
    }
    @Test public void automaticRetryUsesSnapshotWithoutRelationalLines() throws Exception {
        invoice("auto", payload("auto")); new ApiClient().retryPendingTransactions();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!InvoiceSnapshots.isTransmitted("auto") && System.nanoTime() < deadline) Thread.sleep(20);
        assertTrue(InvoiceSnapshots.isTransmitted("auto")); assertTrue(submittedBody.contains("futureField"));
    }
    @Test public void repeatedProductLinesDeductQuantityAtomically() throws Exception {
        upsert(product("p",100,10),ProductSyncService.salesRevision());
        InvoiceHeader header = new InvoiceHeader(); header.setInvoiceNumber("stock");
        LineItemDto line = new LineItemDto(); line.setProductCode("p"); line.setQuantity(2); line.setIsProduct(true); line.setTotal(20);
        assertTrue(Helper.saveTransaction(header,Arrays.asList(line,line),Collections.emptyList(),Collections.emptyList(),40,0,"signed","url",false,"Cash",40));
        assertEquals("96.0",scalar("SELECT Quantity FROM Products"));
    }
}
