package com.pointofsale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.pointofsale.model.AuditLog;
import com.pointofsale.data.Database;
import java.util.List;
import com.pointofsale.helper.Helper;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.pointofsale.model.Session;
import com.pointofsale.model.SecuritySettings;


public class AuditLogger {

    public static void log(String action, String details) {
    SecuritySettings settings = Helper.getSettings();
    if (!settings.enableAuditLog) return;

    String username = Session.firstName + " " + Session.lastName;
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    String sql = "INSERT INTO AuditLogs (Username, Action, Details, Timestamp) VALUES (?, ?, ?, ?)";

    try (Connection conn = Database.createConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, username);
        stmt.setString(2, action);
        stmt.setString(3, details);
        stmt.setString(4, timestamp);

        stmt.executeUpdate();

    } catch (SQLException e) {
        e.printStackTrace(); // Replace with proper logging
    }
}
    
    public static List<AuditLog> getAllLogs() {
    List<AuditLog> logs = new ArrayList<>();
    String sql = "SELECT * FROM AuditLogs ORDER BY Timestamp DESC";

    try (Connection conn = Database.createConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         var rs = stmt.executeQuery()) {

        while (rs.next()) {
            AuditLog log = new AuditLog(
                    rs.getInt("Id"),
                    rs.getString("Username"),
                    rs.getString("Action"),
                    rs.getString("Details"),
                    rs.getString("Timestamp")
            );
            logs.add(log);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return logs;
}

}

