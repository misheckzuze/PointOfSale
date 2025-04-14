package com.pointofsale.helper;

import javax.json.*;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.pointofsale.data.Database;

public class ActivationDataInserter {

    public static void insertActivationData(String responseBody) {
        try (JsonReader reader = Json.createReader(new StringReader(responseBody));
             Connection conn = Database.createConnection()) {

            JsonObject root = reader.readObject();
            JsonObject data = root.getJsonObject("data");
            if (data == null) return;

            // === TerminalSite ===
            JsonObject terminalSite = data
                    .getJsonObject("configuration")
                    .getJsonObject("terminalConfiguration")
                    .getJsonObject("terminalSite");

            String siteId = terminalSite.getString("siteId", "");
            String siteName = terminalSite.getString("siteName", "");

            String insertSiteSQL = "INSERT OR REPLACE INTO TerminalSites (SiteId, Name, Location) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSiteSQL)) {
                stmt.setString(1, siteId);
                stmt.setString(2, siteName);
                stmt.setString(3, ""); // No location info in response
                stmt.executeUpdate();
                System.out.println("✅ Inserted TerminalSite: " + siteId);
            }

            // === TaxRates ===
            JsonArray taxRates = data
                    .getJsonObject("configuration")
                    .getJsonObject("globalConfiguration")
                    .getJsonArray("taxrates");

            String insertTaxSQL = "INSERT OR REPLACE INTO TaxRates (Id, Name, ChargeMode, Ordinal, Rate) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertTaxSQL)) {
                for (JsonValue rateValue : taxRates) {
                    JsonObject rateObj = rateValue.asJsonObject();
                    String id = rateObj.getString("id", "");
                    String name = rateObj.getString("name", "");
                    String chargeMode = rateObj.getString("chargeMode", "");
                    int ordinal = rateObj.getInt("ordinal", 0);
                    double rate = rateObj.getJsonNumber("rate").doubleValue();

                    stmt.setString(1, id);
                    stmt.setString(2, name);
                    stmt.setString(3, chargeMode);
                    stmt.setInt(4, ordinal);
                    stmt.setDouble(5, rate);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                System.out.println("✅ Inserted TaxRates");
            }

            // === ActivatedTerminal ===
            JsonObject activatedTerminal = data.getJsonObject("activatedTerminal");
            String terminalId = activatedTerminal.getString("terminalId", "");
            int terminalPosition = activatedTerminal.getInt("terminalPosition", 0);
            int taxpayerId = activatedTerminal.getInt("taxpayerId", 0);
            String activationDate = activatedTerminal.getString("activationDate", "");

            JsonObject credentials = activatedTerminal.getJsonObject("terminalCredentials");
            String jwtToken = credentials.getString("jwtToken", "");
            String secretKey = credentials.getString("secretKey", "");

            String insertTerminalSQL = "INSERT OR REPLACE INTO ActivatedTerminal (TerminalId, TerminalPosition, TaxpayerId, ActivationDate, JwtToken, SecretKey) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertTerminalSQL)) {
                stmt.setString(1, terminalId);
                stmt.setInt(2, terminalPosition);
                stmt.setInt(3, taxpayerId);
                stmt.setString(4, activationDate);
                stmt.setString(5, jwtToken);
                stmt.setString(6, secretKey);
                stmt.executeUpdate();
                System.out.println("✅ Inserted ActivatedTerminal");
            }

            // === TerminalConfiguration ===
            JsonObject terminalConfig = data.getJsonObject("configuration").getJsonObject("terminalConfiguration");
            String terminalLabel = terminalConfig.getString("terminalLabel", "");
            boolean isActive = terminalConfig.getBoolean("isActiveTerminal", false);
            String email = terminalConfig.getString("emailAddress", "");
            String phone = terminalConfig.getString("phoneNumber", "");
            String tradingName = terminalConfig.getString("tradingName", "");
            String addressLine = terminalConfig.getJsonArray("addressLines").getString(0);

            String insertConfigSQL = "INSERT OR REPLACE INTO TerminalConfiguration (TerminalId, Label, IsActive, Email, Phone, TradingName, AddressLine) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertConfigSQL)) {
                stmt.setString(1, terminalId);
                stmt.setString(2, terminalLabel);
                stmt.setBoolean(3, isActive);
                stmt.setString(4, email);
                stmt.setString(5, phone);
                stmt.setString(6, tradingName);
                stmt.setString(7, addressLine);
                stmt.executeUpdate();
                System.out.println("✅ Inserted TerminalConfiguration");
            }

            // === OfflineLimit ===
            JsonObject offlineLimit = terminalConfig.getJsonObject("offlineLimit");
            int maxAge = offlineLimit.getInt("maxTransactionAgeInHours", 0);
            double maxAmount = offlineLimit.getJsonNumber("maxCummulativeAmount").doubleValue();

            String insertLimitSQL = "INSERT OR REPLACE INTO OfflineLimit (TerminalId, MaxTransactionAgeInHours, MaxCummulativeAmount) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertLimitSQL)) {
                stmt.setString(1, terminalId);
                stmt.setInt(2, maxAge);
                stmt.setDouble(3, maxAmount);
                stmt.executeUpdate();
                System.out.println("✅ Inserted OfflineLimit");
            }

            // === TaxpayerConfiguration & TaxOffice ===
            JsonObject taxpayerConfig = data.getJsonObject("configuration").getJsonObject("taxpayerConfiguration");
            String tin = taxpayerConfig.getString("tin", "");
            boolean isVATRegistered = taxpayerConfig.getBoolean("isVATRegistered", false);
            String taxOfficeCode = taxpayerConfig.getString("taxOfficeCode", "");

            JsonObject taxOffice = taxpayerConfig.getJsonObject("taxOffice");
            String officeName = taxOffice.getString("name", "");

            String insertTaxpayerSQL = "INSERT OR REPLACE INTO TaxpayerConfiguration (TaxpayerId, TIN, IsVATRegistered, TaxOfficeCode) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertTaxpayerSQL)) {
                stmt.setInt(1, taxpayerId);
                stmt.setString(2, tin);
                stmt.setBoolean(3, isVATRegistered);
                stmt.setString(4, taxOfficeCode);
                stmt.executeUpdate();
                System.out.println("✅ Inserted TaxpayerConfiguration");
            }

            String insertOfficeSQL = "INSERT OR REPLACE INTO TaxOffices (Code, Name) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertOfficeSQL)) {
                stmt.setString(1, taxOfficeCode);
                stmt.setString(2, officeName);
                stmt.executeUpdate();
                System.out.println("✅ Inserted TaxOffice");
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to insert activation data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void insertActivationCode(String activationCode) {
    try (Connection conn = Database.createConnection()) {
        // Insert data into the ActivationCode table
        String insertSQL = "INSERT OR REPLACE INTO ActivationCode (ActivationCode) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            stmt.setString(1, activationCode);
            stmt.executeUpdate();
            System.out.println("✅ Inserted ActivationCode: " + activationCode);
        }
    } catch (SQLException e) {
        System.err.println("❌ Failed to insert into ActivationCode table: " + e.getMessage());
        e.printStackTrace();
    }
}

}
