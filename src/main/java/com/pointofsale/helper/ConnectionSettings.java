package com.pointofsale.helper;

import com.pointofsale.data.Database;
import java.net.URI;
import java.sql.*;

/** User-selected endpoint is applied on restart, keeping in-flight requests on one server. */
public final class ConnectionSettings {
    public static String validate(String value) {
        String url = value == null ? "" : value.trim();
        try {
            URI uri = URI.create(url);
            if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null
                    || uri.getQuery() != null || uri.getFragment() != null
                    || uri.getPort() > 65535) throw new IllegalArgumentException();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Enter a valid HTTP or HTTPS base URL, including the API path, without credentials, query or fragment.");
        }
        return url.replaceAll("/+$", "");
    }

    private static Connection open() throws SQLException {
        Connection c = Database.createConnection();
        try (Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS ConnectionSettings (Id INTEGER PRIMARY KEY CHECK(Id=1), BaseUrl TEXT NOT NULL)");
        } catch (SQLException e) { c.close(); throw e; }
        return c;
    }

    public static String load() {
        try (Connection c = open(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT BaseUrl FROM ConnectionSettings WHERE Id=1")) {
            if (r.next()) return validate(r.getString(1));
        } catch (SQLException e) { throw new IllegalStateException("Cannot read API connection settings", e); }
        return validate(System.getProperty("pos.api.baseUrl", "https://eis-api.mra.mw/api/v1"));
    }

    public static void save(String value) throws SQLException {
        String url = validate(value);
        try (Connection c = open(); PreparedStatement s = c.prepareStatement(
                "INSERT INTO ConnectionSettings(Id,BaseUrl) VALUES(1,?) ON CONFLICT(Id) DO UPDATE SET BaseUrl=excluded.BaseUrl")) {
            s.setString(1, url); s.executeUpdate();
        }
    }
}
