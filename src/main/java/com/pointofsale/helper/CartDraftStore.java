package com.pointofsale.helper;

import com.google.gson.Gson;
import com.pointofsale.data.Database;
import com.pointofsale.model.CartDraft;
import java.sql.*;
import java.util.concurrent.*;

/** Serial background writes keep an older snapshot from overtaking a newer one. */
public class CartDraftStore {
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "cart-autosave"); t.setDaemon(true); return t;
    });
    private final String cashier;

    public CartDraftStore(String cashier) {
        if (cashier == null || cashier.isBlank()) throw new IllegalArgumentException("Cashier is required");
        this.cashier = cashier;
    }

    private Connection open() throws SQLException {
        Connection c = Database.createConnection();
        try (Statement s = c.createStatement()) {
            // FULL flushes the WAL on each commit, including after sudden power loss.
            s.execute("PRAGMA synchronous=FULL");
            s.execute("CREATE TABLE IF NOT EXISTS CartDrafts (Cashier TEXT PRIMARY KEY, Payload TEXT NOT NULL)");
        } catch (SQLException e) { c.close(); throw e; }
        return c;
    }

    public CompletableFuture<CartDraft> load() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection c = open(); PreparedStatement s = c.prepareStatement(
                    "SELECT Payload FROM CartDrafts WHERE Cashier=?")) {
                s.setString(1, cashier);
                try (ResultSet r = s.executeQuery()) {
                    if (!r.next()) return null;
                    CartDraft draft = new Gson().fromJson(r.getString(1), CartDraft.class);
                    if (draft == null || draft.lines == null) throw new IllegalStateException("Invalid saved cart");
                    if (draft.invoiceNumber != null) {
                        try (PreparedStatement invoice = c.prepareStatement("SELECT 1 FROM Invoices WHERE InvoiceNumber=?")) {
                            invoice.setString(1, draft.invoiceNumber);
                            try (ResultSet saved = invoice.executeQuery()) { if (saved.next()) return null; }
                        }
                    }
                    return draft;
                }
            } catch (Exception e) { throw new CompletionException(e); }
        }, WORKER);
    }

    public CompletableFuture<Void> save(String json) {
        return CompletableFuture.runAsync(() -> {
            try (Connection c = open(); PreparedStatement s = c.prepareStatement(
                    "INSERT INTO CartDrafts(Cashier,Payload) VALUES (?,?) " +
                    "ON CONFLICT(Cashier) DO UPDATE SET Payload=excluded.Payload")) {
                s.setString(1, cashier); s.setString(2, json); s.executeUpdate();
            } catch (SQLException e) { throw new CompletionException(e); }
        }, WORKER);
    }
}
