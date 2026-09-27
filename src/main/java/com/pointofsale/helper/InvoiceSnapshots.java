package com.pointofsale.helper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pointofsale.data.Database;
import java.sql.*;
import java.util.function.Supplier;

/** Snapshot-first selection shared by all invoice resend paths. */
public final class InvoiceSnapshots {
    private InvoiceSnapshots() { }

    public static String forTransmission(String invoiceNumber, Supplier<String> legacyBuilder) throws SQLException {
        String payload;
        String signature;
        try (Connection connection = Database.createConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT Payload, OfflineTransactionSignature FROM Invoices WHERE InvoiceNumber = ?")) {
            statement.setString(1, invoiceNumber);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) throw new SQLException("Invoice not found: " + invoiceNumber);
                payload = row.getString(1);
                signature = row.getString(2);
            }
        }
        // Do not backfill old invoices or silently replace an invalid snapshot.
        if (payload == null || payload.isBlank()) payload = legacyBuilder.get();
        else if ((signature == null || signature.isBlank()) && !isTransmitted(invoiceNumber))
            throw new IllegalStateException("Offline signature is not ready; invoice remains pending.");
        return withSignature(payload, invoiceNumber, signature);
    }

    public static boolean isTransmitted(String invoiceNumber) throws SQLException {
        try (Connection connection = Database.createConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT State FROM Invoices WHERE InvoiceNumber=?")) {
            statement.setString(1, invoiceNumber);
            try (ResultSet row = statement.executeQuery()) { return row.next() && row.getInt(1) == 1; }
        }
    }

    public static String withSignature(String payload, String invoiceNumber, String signature) {
        JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
        if (!invoiceNumber.equals(json.getAsJsonObject("invoiceHeader").get("invoiceNumber").getAsString()))
            throw new IllegalArgumentException("Stored payload belongs to another invoice.");
        JsonObject summary = json.getAsJsonObject("invoiceSummary");
        if (summary == null || !json.has("invoiceLineItems"))
            throw new IllegalArgumentException("Stored invoice payload is incomplete.");
        if (signature != null && !signature.isBlank()) summary.addProperty("offlineSignature", signature);
        return json.toString();
    }
}
