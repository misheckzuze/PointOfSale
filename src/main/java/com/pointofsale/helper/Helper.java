package com.pointofsale.helper;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.json.JsonObject;
import java.util.List;
import java.util.ArrayList;
import com.pointofsale.ProductLookupDialog;
import com.pointofsale.data.Database;


public class Helper {

    public static void updateActivateButtonState(TextField codeField, CheckBox checkbox, Button activateBtn) {
        boolean isCodeEntered = !codeField.getText().trim().isEmpty();
        boolean isAgreed = checkbox.isSelected();
        activateBtn.setDisable(!(isCodeEntered && isAgreed));
    }

    public static void handleActivation(TextField codeField, Label statusLabel) {
        String code = codeField.getText().trim();
        if (isValidActivationCode(code)) {
            statusLabel.setText("Activated");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            showAlert("Success", "Terminal activated successfully.", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Invalid Code", "Please enter a valid activation code (format: XXXX-XXXX-XXXX-XXXX).", Alert.AlertType.ERROR);
        }
    }

    public static boolean isValidActivationCode(String code) {
        return code.matches("[A-Za-z0-9]{4}(-[A-Za-z0-9]{4}){3}");
    }

    public static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ---- System Info ----

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static String getOSVersion() {
        return System.getProperty("os.version");
    }

    public static String getOSArchitecture() {
        return System.getProperty("os.arch");
    }

    public static String getJavaFXVersion() {
        try {
            return System.getProperty("javafx.runtime.version");
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public static String getAppVersion() {
        return "EIS_POINTOFSALE_V1";
    }

    public static String getOSBuild() {
        // OS Build might not be available in all systems, but we try to retrieve it
        String osBuild = System.getProperty("os.build");
        return (osBuild != null && !osBuild.isEmpty()) ? osBuild : "Unknown";
    }

    public static String getMacAddress() {
        try {
            // Get the network interfaces and loop through them to find the MAC address
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();
                byte[] mac = network.getHardwareAddress();

                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X", mac[i]));
                        if (i != mac.length - 1) {
                            sb.append(":");
                        }
                    }
                    return sb.toString();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
    
    public static String computeXSignature(String activationCode, String secretKey) {
    try {
        Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        sha512_HMAC.init(keySpec);
        byte[] hashBytes = sha512_HMAC.doFinal(activationCode.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }
    
   public static String getTerminalSiteName() {
    String siteName = "";
    try (Connection conn = Database.createConnection()) {
        String query = "SELECT Name FROM TerminalSites LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                siteName = rs.getString("Name");
            }
        }
    } catch (SQLException e) {
        System.err.println("❌ Failed to fetch Terminal Site: " + e.getMessage());
    }
    return siteName;
}
   
public static String getTerminalSiteId() {
    String siteId = "";
    try (Connection conn = Database.createConnection()) {
        String query = "SELECT SiteId FROM TerminalSites LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                siteId = rs.getString("SiteId");
            }
        }
    } catch (SQLException e) {
        System.err.println("❌ Failed to fetch Terminal Site: " + e.getMessage());
    }
    return siteId;
}
   
   public static String getTin() {
    String tin = "";
    try (Connection conn = Database.createConnection()) {
        String query = "SELECT TIN FROM TaxpayerConfiguration LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                tin = rs.getString("TIN");
            }
        }
    } catch (SQLException e) {
        System.err.println("❌ Failed to fetch TIN: " + e.getMessage());
    }
    return tin;
}

   public static String getTerminalLabel() {
    String label = "";
    try (Connection conn = Database.createConnection()) {
        String query = "SELECT Label FROM TerminalConfiguration LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                label = rs.getString("Label");
            }
        }
    } catch (SQLException e) {
        System.err.println("❌ Failed to fetch Terminal Label: " + e.getMessage());
    }
    return label;
   }
   
    public static String getTerminalId() {
        String terminalId = "";
        try (Connection conn = Database.createConnection()) {
            String query = "SELECT TerminalId FROM ActivatedTerminal LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                var rs = stmt.executeQuery();
                if (rs.next()) {
                    terminalId = rs.getString("TerminalId");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to get Terminal ID: " + e.getMessage());
        }
        return terminalId;
    }

    public static String getSecretKey() {
        String secretKey = "";
        try (Connection conn = Database.createConnection()) {
            String query = "SELECT SecretKey FROM ActivatedTerminal LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                var rs = stmt.executeQuery();
                if (rs.next()) {
                    secretKey = rs.getString("SecretKey");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to get Secret Key: " + e.getMessage());
        }
        return secretKey;
    }
    
    public static String getToken() {
        String token = "";
        try (Connection conn = Database.createConnection()) {
            String query = "SELECT JwtToken FROM ActivatedTerminal LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                var rs = stmt.executeQuery();
                if (rs.next()) {
                    token = rs.getString("JwtToken");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to get Secret Key: " + e.getMessage());
        }
        return token;
    }

    public static String getActivationCode() {
        String activationCode = "";
        try (Connection conn = Database.createConnection()) {
            String query = "SELECT ActivationCode FROM ActivationCode LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                var rs = stmt.executeQuery();
                if (rs.next()) {
                    activationCode = rs.getString("ActivationCode");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to get Activation Code: " + e.getMessage());
        }
        return activationCode;
    }

    public static boolean updateIsActiveInTerminalConfiguration(boolean isActive) {
    try (Connection conn = Database.createConnection()) {
        // Update statement to set IsActive value
        String query = "UPDATE TerminalConfiguration SET IsActive = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, isActive);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Return true if update was successful
        }
      } catch (SQLException e) {
        System.err.println("❌ Failed to update IsActive in TerminalConfiguration: " + e.getMessage());
        return false;
       }
    }
    
     public static void insertDefaultAdminIfNotExists() {
        String checkSql = "SELECT COUNT(*) FROM Users WHERE UserName = ?";
        String insertSql = "INSERT INTO Users (FirstName, LastName, UserName, Gender, PhoneNumber, EmailAddress, Address, Role, Password) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.createConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            checkStmt.setString(1, "admin");
            var rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                insertStmt.setString(1, "Admin");
                insertStmt.setString(2, "User");
                insertStmt.setString(3, "admin");
                insertStmt.setString(4, "MALE");
                insertStmt.setString(5, "0999123456");
                insertStmt.setString(6, "admin@example.com");
                insertStmt.setString(7, "Lilongwe");
                insertStmt.setString(8, "ADMIN");
                insertStmt.setString(9, "admin123");

                insertStmt.executeUpdate();
                System.out.println("✅ Default admin user created.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Failed to insert default admin user: " + e.getMessage());
        }
    }
     
   public static boolean isValidUser(String username, String password) {
    String query = "SELECT * FROM users WHERE username = ? AND password = ?";

    try (var conn = Database.createConnection();
         var stmt = conn.prepareStatement(query)) {

        stmt.setString(1, username);
        stmt.setString(2, password);

        try (var rs = stmt.executeQuery()) {
            return rs.next(); // returns true if user exists
        }

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
   
 public static boolean isTerminalActivated() {
    String query = "SELECT IsActive FROM TerminalConfiguration LIMIT 1";

    try (var conn = Database.createConnection();
         var stmt = conn.prepareStatement(query);
         var rs = stmt.executeQuery()) {

        if (rs.next()) {
            return rs.getInt("IsActive") == 1;
        } else {
            // No terminal configuration yet
            return false;
        }

        } catch (SQLException e) {
        e.printStackTrace();
        return false;
       }
    }
 public static void insertOrUpdateProduct(JsonObject product) {
    String sql = "INSERT OR REPLACE INTO Products (" +
            "ProductCode, ProductName, Description, Quantity, UnitOfMeasure, Price, SiteId, " +
            "ProductExpiryDate, MinimumStockLevel, TaxRateId, IsProduct) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = Database.createConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, product.getString("productCode", ""));
        pstmt.setString(2, product.getString("productName", ""));
        pstmt.setString(3, product.getString("description", ""));
        pstmt.setDouble(4, product.getJsonNumber("quantity").doubleValue());
        pstmt.setString(5, product.getString("unitOfMeasure", ""));
        pstmt.setDouble(6, product.getJsonNumber("price").doubleValue());
        pstmt.setString(7, product.getString("siteId", ""));
        pstmt.setString(8, product.getString("productExpiryDate", null));
        pstmt.setDouble(9, product.getJsonNumber("minimumStockLevel").doubleValue());
        pstmt.setString(10, product.getString("taxRateId", ""));
        pstmt.setInt(11, product.getBoolean("isProduct") ? 1 : 0);

        pstmt.executeUpdate();
        System.out.println("✅ Inserted/Updated product: " + product.getString("productCode"));
        } catch (Exception e) {
        System.err.println("❌ Failed to insert product: " + e.getMessage());
        e.printStackTrace();
        }
    }

public static List<ProductLookupDialog.Product> fetchAllProductsFromDB() {
    List<ProductLookupDialog.Product> products = new ArrayList<>();

    try (Connection conn = Database.createConnection()) {
        String query = "SELECT ProductCode, ProductName, Description, Price, TaxRateId, Quantity, UnitOfMeasure FROM Products";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             var rs = stmt.executeQuery()) {

            while (rs.next()) {
                String barcode = rs.getString("ProductCode");
                String name = rs.getString("ProductName");
                String desc = rs.getString("Description");
                double price = rs.getDouble("Price");
                String rate = rs.getString("TaxRateId");
                double quantity = rs.getDouble("Quantity");
                String unit = rs.getString("UnitOfMeasure");

                products.add(new ProductLookupDialog.Product(barcode, name, desc, price, rate, quantity, unit));
            }

        }
       } catch (SQLException e) {
        System.err.println("❌ Failed to load product data: " + e.getMessage());
       }

       return products;
      }


}
