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
            s.execute("CREATE TABLE IF NOT EXISTS CancelledCartDrafts (Id INTEGER PRIMARY KEY AUTOINCREMENT, Cashier TEXT NOT NULL, InvoiceNumber TEXT, Payload TEXT NOT NULL, CancelledAt TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
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

    /** Keep the original payload for reconciliation; never cancel or delete a fiscal invoice. */
    public CompletableFuture<Void> cancel() {
        return CompletableFuture.runAsync(() -> {
            try (Connection c = open()) {
                c.setAutoCommit(false);
                try (PreparedStatement archive = c.prepareStatement("INSERT INTO CancelledCartDrafts(Cashier,InvoiceNumber,Payload) SELECT Cashier,json_extract(Payload,'$.invoiceNumber'),Payload FROM CartDrafts WHERE Cashier=?");
                     PreparedStatement clear = c.prepareStatement("DELETE FROM CartDrafts WHERE Cashier=?")) {
                    archive.setString(1, cashier); archive.executeUpdate();
                    clear.setString(1, cashier); clear.executeUpdate();
                    c.commit();
                } catch (Exception e) { c.rollback(); throw e; }
            } catch (Exception e) { throw new CompletionException(e); }
        }, WORKER);
    }

    public static boolean isCancelledNumber(String number) {
        // Creating the additive table also supports first launch of an existing database.
        try (Connection c = new CartDraftStore("lookup").open(); PreparedStatement s = c.prepareStatement(
                "SELECT 1 FROM CancelledCartDrafts WHERE InvoiceNumber=? LIMIT 1")) {
            s.setString(1, number);
            try (ResultSet r = s.executeQuery()) { return r.next(); }
        } catch (SQLException e) { throw new IllegalStateException("Cannot check cancelled invoice numbers", e); }
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
