package com.pointofsale.helper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pointofsale.data.Database;
import com.pointofsale.utils.ApiEndpoints;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/** Each installation polls its own authenticated site's catalogue. No JavaFX work here. */
public final class ProductSyncService {
    private static final ProductSyncService INSTANCE = new ProductSyncService();
    private static final ObjectMapper JSON = new ObjectMapper();
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "product-sync"); thread.setDaemon(true); return thread;
    });
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    private final AtomicLong revision = new AtomicLong();
    private CompletableFuture<Integer> inFlight;
    private boolean started;
    private int failures;
    private volatile String lastError = "";
    private volatile long lastSuccess;

    public static ProductSyncService getInstance() { return INSTANCE; }
    public long getRevision() { return revision.get(); }
    public long getLastSuccess() { return lastSuccess; }
    public String getLastError() { return lastError; }

    public synchronized void start() {
        if (started) return;
        started = true;
        worker.schedule(this::poll, ThreadLocalRandom.current().nextInt(5, 16), TimeUnit.SECONDS);
    }

    private void poll() {
        synchronizeNow().whenComplete((count, error) -> {
            long interval = Math.max(15, Long.getLong("pos.products.pollSeconds", 60L));
            long delay = error == null ? interval : Math.min(900, interval * (1L << Math.min(failures, 4)));
            synchronized (this) {
                if (started) worker.schedule(this::poll, delay + ThreadLocalRandom.current().nextInt(10), TimeUnit.SECONDS);
            }
        });
    }

    public synchronized CompletableFuture<Integer> synchronizeNow() {
        if (inFlight != null && !inFlight.isDone()) return inFlight;
        inFlight = CompletableFuture.supplyAsync(() -> {
            try {
                String token = Helper.getToken(), tin = Helper.getTin(), site = Helper.getTerminalSiteId();
                if (token == null || token.isBlank() || tin == null || tin.isBlank() || site == null || site.isBlank())
                    throw new IllegalStateException("Product sync is waiting for terminal activation.");
                int count = synchronize(URI.create(ApiEndpoints.BASE_URL + ApiEndpoints.GET_TERMINAL_SITE_PRODUCTS), token, tin, site);
                failures = 0; lastError = ""; lastSuccess = System.currentTimeMillis(); revision.incrementAndGet();
                System.out.println("Product sync completed: " + count + " products.");
                return count;
            } catch (Exception ex) {
                failures++; lastError = ex.getMessage();
                System.err.println("Product sync will retry: " + lastError);
                throw new CompletionException(ex);
            }
        }, worker);
        return inFlight;
    }

    public synchronized void stop() { started = false; worker.shutdownNow(); }

    int synchronize(URI endpoint, String token, String tin, String site) throws Exception {
        long salesRevision = salesRevision();
        Path download = Files.createTempFile("pos-products-", ".json");
        try {
            String body = JSON.createObjectNode().put("tin", tin).put("siteId", site).toString();
            HttpRequest request = HttpRequest.newBuilder(endpoint).timeout(Duration.ofMinutes(5))
                    .header("Authorization", "Bearer " + token).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<Path> response = http.send(request, HttpResponse.BodyHandlers.ofFile(download));
            if (response.statusCode() != 200) throw new IllegalStateException("Product server returned HTTP " + response.statusCode());
            return applyDownloadedCatalogue(download, site, salesRevision);
        } finally { Files.deleteIfExists(download); }
    }

    static long salesRevision() throws SQLException {
        try (Connection c = Database.createConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT SalesRevision FROM ProductSyncState WHERE Id = 1")) {
            return r.next() ? r.getLong(1) : 0;
        }
    }

    static int applyDownloadedCatalogue(Path file, String site, long salesRevision) throws Exception {
        // Validate the whole envelope before any writes, without retaining the catalogue in memory.
        boolean accepted = false, hasData = false;
        try (JsonParser parser = JSON.getFactory().createParser(file.toFile())) {
            if (parser.nextToken() != JsonToken.START_OBJECT) throw new IllegalArgumentException("Invalid product response.");
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken() == null) throw new IllegalArgumentException("Truncated product response.");
                String field = parser.currentName(); parser.nextToken();
                if ("statusCode".equals(field)) accepted = parser.getIntValue() == 1;
                if ("data".equals(field)) hasData = parser.currentToken() == JsonToken.START_ARRAY;
                parser.skipChildren();
            }
        }
        if (!accepted || !hasData) throw new IllegalArgumentException("Product catalogue was not accepted by the server.");
        int count = 0;
        int batchSize = Math.max(10, Math.min(1000, Integer.getInteger("pos.products.batchSize", 200)));
        List<JsonNode> batch = new ArrayList<>(batchSize);
        try (JsonParser parser = JSON.getFactory().createParser(file.toFile())) {
            parser.nextToken();
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken() == null) throw new IllegalArgumentException("Truncated product response.");
                String field = parser.currentName(); parser.nextToken();
                if ("data".equals(field)) {
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                        batch.add(JSON.readTree(parser));
                        if (batch.size() == batchSize) { upsertBatch(batch, site, salesRevision); count += batch.size(); batch.clear(); }
                    }
                } else parser.skipChildren();
            }
        }
        if (!batch.isEmpty()) { upsertBatch(batch, site, salesRevision); count += batch.size(); }
        return count;
    }

    static void upsertBatch(List<JsonNode> products, String site, long revision) throws SQLException {
        String sql = "INSERT INTO Products (ProductCode,ProductName,Description,Quantity,UnitOfMeasure,Price,SiteId,ProductExpiryDate,MinimumStockLevel,TaxRateId,IsProduct) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT(ProductCode) DO UPDATE SET "
                + "ProductName=excluded.ProductName,Description=excluded.Description,UnitOfMeasure=excluded.UnitOfMeasure,"
                + "Price=excluded.Price,SiteId=excluded.SiteId,ProductExpiryDate=excluded.ProductExpiryDate,"
                + "MinimumStockLevel=excluded.MinimumStockLevel,TaxRateId=excluded.TaxRateId,IsProduct=excluded.IsProduct,"
                + "Quantity=CASE WHEN (SELECT SalesRevision FROM ProductSyncState WHERE Id=1)=? THEN excluded.Quantity - "
                + "COALESCE((SELECT SUM(l.Quantity) FROM LineItems l JOIN Invoices i ON i.InvoiceNumber=l.InvoiceNumber "
                + "WHERE i.State=0 AND l.IsProduct=1 AND l.ProductCode=excluded.ProductCode),0) ELSE Products.Quantity END";
        try (Connection connection = Database.createConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (JsonNode product : products) {
                    String code = product.path("productCode").asText("");
                    if (code.isBlank() || !product.path("price").isNumber() || !product.path("quantity").isNumber())
                        throw new SQLException("Invalid product code, price or quantity in server catalogue.");
                    if (product.hasNonNull("siteId") && !site.equals(product.path("siteId").asText()))
                        throw new SQLException("Product belongs to a different terminal site.");
                    statement.setString(1, code); statement.setString(2, product.path("productName").asText(""));
                    statement.setString(3, product.path("description").asText("")); statement.setDouble(4, product.path("quantity").asDouble());
                    statement.setString(5, product.path("unitOfMeasure").asText("")); statement.setBigDecimal(6, product.path("price").decimalValue());
                    statement.setString(7, site); statement.setString(8, product.path("productExpiryDate").asText(null));
                    statement.setDouble(9, product.path("minimumStockLevel").asDouble()); statement.setString(10, product.path("taxRateId").asText(""));
                    statement.setInt(11, product.path("isProduct").asBoolean() ? 1 : 0); statement.setLong(12, revision);
                    statement.addBatch();
                }
                statement.executeBatch(); connection.commit();
            } catch (Exception ex) { connection.rollback(); throw ex; }
        }
    }
}
