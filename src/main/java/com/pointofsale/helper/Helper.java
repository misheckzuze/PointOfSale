package com.pointofsale.helper;

import javafx.scene.control.Button;
import java.util.function.Consumer;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import javax.crypto.Mac;
import java.util.Collections;
import javafx.util.Pair;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.concurrent.atomic.AtomicInteger;
import com.pointofsale.model.InvoiceSummary;
import com.google.gson.Gson;
import com.pointofsale.model.InvoicePayload;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Base64;
import java.time.temporal.ChronoUnit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javafx.scene.control.Alert;
import java.sql.SQLException;
import javafx.scene.control.ButtonType;
import javax.json.JsonObject;
import com.pointofsale.model.Product;
import com.pointofsale.model.ProductSale;
import com.pointofsale.model.TaxRates;
import com.pointofsale.model.SaleSummary;
import com.pointofsale.model.InvoiceDetails;
import com.pointofsale.model.Session;
import com.pointofsale.model.TaxTrend;
import com.pointofsale.model.TaxSummary;
import com.pointofsale.model.CategoryRevenue;
import com.pointofsale.model.InvoiceHeader;
import com.pointofsale.model.TaxBreakDown;
import com.pointofsale.model.LineItemDto;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;
import javafx.application.Platform;
import java.util.List;
import java.util.ArrayList;
import com.pointofsale.data.Database;
import java.time.format.DateTimeParseException;



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
   public static int getTaxpayerId() {
    int taxpayerId = 0;
    try (Connection conn = Database.createConnection()) {
        String query = "SELECT TaxpayerId FROM TaxpayerConfiguration LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                taxpayerId = rs.getInt("TaxpayerId");
            }
        }
    } catch (SQLException e) {
        System.err.println("❌ Failed to fetch TIN: " + e.getMessage());
    }
    return taxpayerId;
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
    
    public static int getTaxpayerVersion() {
    int version = 0;
    try (Connection conn = Database.createConnection()) {
        String query = "SELECT VersionNo FROM TaxpayerConfiguration LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                version = rs.getInt("VersionNo");
            }
        }
    } catch (SQLException e) {
        System.err.println("❌ Failed to get Taxpayer Version: " + e.getMessage());
    }
    return version;
    }
    
    public static int getTerminalVersion() {
    int version = 0;
    try (Connection conn = Database.createConnection()) {
        String query = "SELECT VersionNo FROM TerminalConfiguration LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                version = rs.getInt("VersionNo");
            }
        }
    } catch (SQLException e) {
        System.err.println("❌ Failed to get Terminal Version: " + e.getMessage());
    }
    return version;
}
    
  public static int getGlobalVersion() {
    int version = 0;
    try (Connection conn = Database.createConnection()) {
        String query = "SELECT VersionNo FROM GlobalConfiguration LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                version = rs.getInt("VersionNo");
            }
        }
    } catch (SQLException e) {
        System.err.println("❌ Failed to get Global Version: " + e.getMessage());
    }
    return version;
}
  
  public static int getOfflineTransactionLimit() {
    int offlineLimit = 0;
    try (Connection conn = Database.createConnection()) {
        String query = "SELECT MaxTransactionAgeInHours FROM OfflineLimit LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                offlineLimit = rs.getInt("MaxTransactionAgeInHours");
            }
        }
    } catch (SQLException e) {
        System.err.println("❌ Failed to get offline limit: " + e.getMessage());
    }
    return offlineLimit;
}
  
   public static LocalDateTime getLastSuccessfulSyncTimeFromInvoices() {
        String query = "SELECT MIN(InvoiceDateTime) FROM Invoices WHERE State = 0";

        try (Connection conn = Database.createConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             var rs = stmt.executeQuery()) {

            if (rs.next()) {
                String result = rs.getString(1);
                if (result != null) {
                    try {
                        return LocalDateTime.parse(result);
                    } catch (DateTimeParseException e) {
                        System.err.println("❌ Failed to parse InvoiceDateTime: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching last sync time: " + e.getMessage());
        }

        return null;
    }
   
   public static List<InvoiceDetails> getAllTransactions() {
    List<InvoiceDetails> transactions = new ArrayList<>();

    String query = """
        SELECT i.InvoiceNumber, i.InvoiceDateTime, i.BuyerTin, 
               COUNT(li.Id) AS ItemCount,
               i.InvoiceTotal, i.TotalVAT, i.State,
               i.ValidationUrl
        FROM Invoices i
        LEFT JOIN LineItems li ON i.InvoiceNumber = li.InvoiceNumber
        GROUP BY i.InvoiceNumber
        ORDER BY i.InvoiceDateTime DESC
    """;

    try (Connection conn = Database.createConnection();
         PreparedStatement stmt = conn.prepareStatement(query);
         var rs = stmt.executeQuery()) {

        while (rs.next()) {
            String invoiceNumber = rs.getString("InvoiceNumber");
            String invoiceDateTimeStr = rs.getString("InvoiceDateTime");
            String buyerTin = rs.getString("BuyerTin");
            int itemCount = rs.getInt("ItemCount");
            double invoiceTotal = rs.getDouble("InvoiceTotal");
            double totalVAT = rs.getDouble("TotalVAT");
            boolean transmitted = rs.getInt("State") != 0;
            String validationUrl = rs.getString("ValidationUrl");


            try {
                LocalDateTime invoiceDateTime = LocalDateTime.parse(invoiceDateTimeStr);
                transactions.add(new InvoiceDetails(
                        invoiceNumber,
                        invoiceDateTime,
                        buyerTin != null ? buyerTin : "",
                        itemCount,
                        invoiceTotal,
                        totalVAT,
                        transmitted,
                        validationUrl
                ));
            } catch (DateTimeParseException e) {
                System.err.println("❌ Skipping invoice due to invalid date: " + invoiceDateTimeStr);
            }
        }

    } catch (SQLException e) {
        System.err.println("❌ Failed to fetch transactions: " + e.getMessage());
    }

    return transactions;
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
 
    public static String getTrading() {
        String trading = "";
        String query = "SELECT TradingName FROM TerminalConfiguration LIMIT 1";

        try (var conn = Database.createConnection();
           var stmt = conn.prepareStatement(query);
           var rs = stmt.executeQuery()) {

           if (rs.next()) {
              trading = rs.getString("TradingName");
            }

        } catch (SQLException e) {
          e.printStackTrace();
        }

        return trading;
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

public static List<Product> fetchAllProductsFromDB() {
    List<Product> products = new ArrayList<>();

    try (Connection conn = Database.createConnection()) {
        String query = "SELECT ProductCode, ProductName, Description, Price, TaxRateId, Quantity, UnitOfMeasure, IsProduct FROM Products";
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
                boolean isProduct = rs.getInt("IsProduct") == 1; // Assuming IsProduct is stored as 1 (true) or 0 (false)

                // Create the Product object with the updated constructor
                products.add(new Product(barcode, name, desc, price, rate, quantity, unit, isProduct));
            }

        }
    } catch (SQLException e) {
        System.err.println("❌ Failed to load product data: " + e.getMessage());
    }

    return products;
}

public static Product fetchProductByBarcode(String barcode) {
    try (Connection conn = Database.createConnection()) {
        String query = "SELECT ProductCode, ProductName, Description, Price, TaxRateId, Quantity, UnitOfMeasure, IsProduct FROM Products WHERE ProductCode = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, barcode);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean isProduct = rs.getInt("IsProduct") == 1; // SQLite uses 0/1 for boolean
                    return new Product(
                        rs.getString("ProductCode"),
                        rs.getString("ProductName"),
                        rs.getString("Description"),
                        rs.getDouble("Price"),
                        rs.getString("TaxRateId"),
                        rs.getDouble("Quantity"),
                        rs.getString("UnitOfMeasure"),
                        isProduct
                    );
                }
            }
        }
    } catch (SQLException e) {
        System.err.println("❌ Error fetching product: " + e.getMessage());
    }
    return null;
}


public static void loadUserDetails() {
    String query = "SELECT FirstName, LastName, Role FROM users WHERE UserName = ? AND Password = ?";

    try (var conn = Database.createConnection();
         var stmt = conn.prepareStatement(query)) {

        stmt.setString(1, Session.currentUsername);
        stmt.setString(2, Session.currentPassword);

        try (var rs = stmt.executeQuery()) {
            if (rs.next()) {
                Session.firstName = rs.getString("FirstName");
                Session.lastName = rs.getString("LastName");
                Session.role = rs.getString("Role");
            }
        }

        } catch (SQLException e) {
        e.printStackTrace();
        }
    }

    public static String generateReceiptNumber(long taxpayerId, int terminalPosition, LocalDate transactionDate, long transactionCount) {
       long julianDate = toJulianDate(transactionDate);

       String base64Taxpayer = base10ToBase64(taxpayerId);
       String base64Position = base10ToBase64(terminalPosition);
       String base64Julian = base10ToBase64(julianDate);
       String base64Count = base10ToBase64(transactionCount);

        return base64Taxpayer + "-" + base64Position + "-" + base64Julian + "-" + base64Count;
   }
    
     public static long toJulianDate(LocalDate date) {
        return date.getLong(ChronoField.YEAR) * 1000 + date.getDayOfYear();
    }

    public static String base10ToBase64(long number) {
       String base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
       StringBuilder result = new StringBuilder();

      if (number == 0) return "A";

      while (number > 0) {
        int remainder = (int) (number % 64);
        result.insert(0, base64Chars.charAt(remainder));
        number /= 64;
      }

      return result.toString();
    }

    public static int getTerminalPosition() {
    int terminalPosition = -1; // default if not found

    try (Connection conn = Database.createConnection()) {
        String query = "SELECT TerminalPosition FROM ActivatedTerminal";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                terminalPosition = rs.getInt("TerminalPosition");
            }
        }
        } catch (SQLException e) {
          System.err.println("❌ Failed to fetch Terminal Position: " + e.getMessage());
        }

         return terminalPosition;
    }
    
    public static InvoiceDetails getLastInvoiceDetails() {
    InvoiceDetails invoiceDetails = null;

    String query = "SELECT InvoiceNumber, InvoiceDateTime FROM Invoices ORDER BY InvoiceDateTime DESC LIMIT 1";

    try (Connection conn = Database.createConnection();
         PreparedStatement stmt = conn.prepareStatement(query);
         var rs = stmt.executeQuery()) {

        if (rs.next()) {
            invoiceDetails = new InvoiceDetails();
            invoiceDetails.setInvoiceNumber(rs.getString("InvoiceNumber"));
            invoiceDetails.setInvoiceDateTime(LocalDateTime.parse(rs.getString("InvoiceDateTime")));
        } else {
            System.out.println("ℹ️ No records found in Invoices.");
        }
        } catch (SQLException e) {
        System.err.println("❌ Error fetching last invoice: " + e.getMessage());
        }

        return invoiceDetails;
    }
    
    public static long convertSequentialToBase10(String invoiceNumber) {
    if (invoiceNumber == null || !invoiceNumber.contains("-")) return 0;

    String[] parts = invoiceNumber.split("-");
    if (parts.length != 4) return 0;

    try {
        return base64ToBase10(parts[3]);
       } catch (Exception e) {
        System.err.println("❌ Failed to decode invoice serial: " + e.getMessage());
        return 0;
       }
   }

  public static long base64ToBase10(String base64) {
    String base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
    long result = 0;
    for (int i = 0; i < base64.length(); i++) {
        int index = base64Chars.indexOf(base64.charAt(i));
        if (index == -1) throw new IllegalArgumentException("Invalid Base64 character: " + base64.charAt(i));
        result = result * 64 + index;
    }
    return result;
}

   
public static double getTaxRateById(String taxRateId) {
    double rate = 0.0;

    String query = "SELECT Rate FROM TaxRates WHERE Id = ?";

    try (Connection conn = Database.createConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, taxRateId);
        try (var rs = stmt.executeQuery()) {
            if (rs.next()) {
                rate = rs.getDouble("Rate");
            } else {
                System.out.println("⚠️ No tax rate found for ID: " + taxRateId);
            }
        }
    } catch (SQLException e) {
        System.err.println("❌ Error fetching tax rate: " + e.getMessage());
    }

    return rate;
}

public static List<TaxRates> getTaxRates() {
   List<TaxRates> taxRates = new ArrayList<>();

    try (Connection conn = Database.createConnection()) {
         String query = "SELECT Id, Rate FROM TaxRates";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             var rs = stmt.executeQuery()) {

            while (rs.next()) {
                 String taxRateId = rs.getString("Id");
                double rate = rs.getDouble("Rate");
                taxRates.add(new TaxRates (taxRateId, rate));
            }

        }
    } catch (SQLException e) {
        System.err.println("❌ Failed to load product data: " + e.getMessage());
    }

    return taxRates ;
}


public static boolean isVATRegistered() {
    String query = "SELECT IsVATRegistered FROM TaxpayerConfiguration LIMIT 1";

    try (var conn = Database.createConnection();
         var stmt = conn.prepareStatement(query);
         var rs = stmt.executeQuery()) {

        if (rs.next()) {
            return rs.getInt("IsVATRegistered") == 1;
        } else {
            // No terminal configuration yet
            return false;
        }

        } catch (SQLException e) {
        e.printStackTrace();
        return false;
       }
    }

   
 public static LineItemDto convertProductToLineItemDto(Product product) {
    double unitPrice = product.getPrice();
    double quantity = product.getQuantity();
    double discount = product.getDiscount();
    double total = unitPrice * quantity;

    String taxRateId = product.getTaxRate();
    double taxRate = getTaxRateById(taxRateId);
    
    boolean isVATRegistered = isVATRegistered();
      
    double totalVAT = isVATRegistered ? (total * taxRate) / (100 + taxRate) : 0;

    return new LineItemDto(
        product.getBarcode(),
        product.getDescription(),
        unitPrice,
        quantity,
        discount,
        total,
        Math.round(totalVAT * 100.0) / 100.0,
        taxRateId
    );
}


public static List<TaxBreakDown> generateTaxBreakdown(List<LineItemDto> lineItems) {
        Map<String, TaxBreakDown> taxBreakdownMap = new HashMap<>();

        for (LineItemDto item : lineItems) {
            double taxableAmount = item.getTotal() - item.getTotalVAT();
            double taxAmount = item.getTotalVAT();
            String rateId = item.getTaxRateId();

            if (taxBreakdownMap.containsKey(rateId)) {
                TaxBreakDown existing = taxBreakdownMap.get(rateId);
                existing.setTaxableAmount(existing.getTaxableAmount() + taxableAmount);
                existing.setTaxAmount(existing.getTaxAmount() + taxAmount);
            } else {
                TaxBreakDown newBreakdown = new TaxBreakDown();
                newBreakdown.setRateId(rateId);
                newBreakdown.setTaxableAmount(taxableAmount);
                newBreakdown.setTaxAmount(taxAmount);

                taxBreakdownMap.put(rateId, newBreakdown);
            }
        }

        return new ArrayList<>(taxBreakdownMap.values());
    }

public static boolean saveTransaction(
        InvoiceHeader invoice,
        List<LineItemDto> lineItems,
        List<TaxBreakDown> taxBreakdowns,
        double total,
        double totalVAT,
        String offlineSignature,
        String validationUrl,
        boolean isTransmitted,
        String paymentId,
        double amountPaid
) {
    if (amountPaid == 0) {
        amountPaid = total;
    }

    String insertInvoiceQuery = "INSERT INTO Invoices (InvoiceNumber, InvoiceDateTime, InvoiceTotal, SellerTin, BuyerTin, TotalVAT, OfflineTransactionSignature, ValidationUrl, IsReliefSupply, State, PaymentId, AmountPaid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String insertLineItemQuery = "INSERT INTO LineItems (ProductCode, Description, Quantity, TaxRateID, Discount, UnitPrice, TotalPrice, DiscountAmount, VATRate, IsProduct, VATAmount, InvoiceNumber) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String insertTaxBreakdownQuery = "INSERT INTO InvoiceTaxBreakDown (InvoiceNumber, RateID, TaxableAmount, TaxAmount) VALUES (?, ?, ?, ?)";

    String updateProductQuery = "UPDATE Products SET Quantity = ? WHERE ProductCode = ?";

    String insertPaymentTypeQuery = "INSERT INTO PaymentType (PaymentId, Name, AmountPaid) VALUES (?, ?, ?)";

    try (Connection connection = Database.createConnection()) {
        connection.setAutoCommit(false);

        try (
            PreparedStatement invoiceStmt = connection.prepareStatement(insertInvoiceQuery);
            PreparedStatement paymentStmt = connection.prepareStatement(insertPaymentTypeQuery);
            PreparedStatement lineItemStmt = connection.prepareStatement(insertLineItemQuery);
            PreparedStatement updateProductStmt = connection.prepareStatement(updateProductQuery);
            PreparedStatement taxBreakdownStmt = connection.prepareStatement(insertTaxBreakdownQuery)
        ) {
            // Insert Invoice
            invoiceStmt.setString(1, invoice.getInvoiceNumber());
            invoiceStmt.setString(2, invoice.getInvoiceDateTime());
            invoiceStmt.setDouble(3, total);
            invoiceStmt.setString(4, invoice.getSellerTIN());
            invoiceStmt.setString(5, invoice.getBuyerTIN());
            invoiceStmt.setDouble(6, totalVAT);
            invoiceStmt.setString(7, offlineSignature);
            invoiceStmt.setString(8, validationUrl);
            invoiceStmt.setBoolean(9, invoice.isReliefSupply());
            invoiceStmt.setInt(10, isTransmitted ? 1 : 0);
            invoiceStmt.setString(11, paymentId);
            invoiceStmt.setDouble(12, amountPaid);
            invoiceStmt.executeUpdate();

            // Insert Payment Info
            paymentStmt.setString(1, paymentId);
            paymentStmt.setString(2, "CASH");
            paymentStmt.setDouble(3, amountPaid);
            paymentStmt.executeUpdate();

            // Insert Line Items
            for (LineItemDto item : lineItems) {
                if (item.isProduct()) {
                    double currentQty = getProductQuantity(item.getProductCode());
                    double newQty = currentQty - item.getQuantity();
                    updateProductStmt.setDouble(1, newQty);
                    updateProductStmt.setString(2, item.getProductCode());
                    updateProductStmt.executeUpdate();
                }

                double grossTotal = item.getTotal();             // Total price including VAT
                double vatAmount = item.getTotalVAT();           // VAT amount
                double netTotal = grossTotal - vatAmount;
                double vatRate = netTotal != 0 ? (vatAmount / netTotal) * 100.0 : 0.0;

                lineItemStmt.setString(1, item.getProductCode());
                lineItemStmt.setString(2, item.getDescription());
                lineItemStmt.setDouble(3, item.getQuantity());
                lineItemStmt.setString(4, item.getTaxRateId());
                lineItemStmt.setDouble(5, item.getDiscount());
                lineItemStmt.setDouble(6, item.getUnitPrice());
                lineItemStmt.setDouble(7, grossTotal);       // TotalPrice
                lineItemStmt.setDouble(8, item.getDiscount()); // DiscountAmount
                lineItemStmt.setDouble(9, vatRate);          // VATRate (calculated)
                lineItemStmt.setBoolean(10, item.isProduct());
                lineItemStmt.setDouble(11, vatAmount);        // VATAmount
                lineItemStmt.setString(12, invoice.getInvoiceNumber());
                lineItemStmt.executeUpdate();
            }

            // Insert Tax Breakdown
            for (TaxBreakDown tax : taxBreakdowns) {
                taxBreakdownStmt.setString(1, invoice.getInvoiceNumber());
                taxBreakdownStmt.setString(2, tax.getRateId());
                taxBreakdownStmt.setDouble(3, tax.getTaxableAmount());
                taxBreakdownStmt.setDouble(4, tax.getTaxAmount());
                taxBreakdownStmt.executeUpdate();
            }

            connection.commit();
            return true;

        } catch (SQLException ex) {
            connection.rollback();
            System.err.println("❌ Error during transaction save: " + ex.getMessage());
            return false;
        }
    } catch (SQLException e) {
        System.err.println("❌ DB Connection error: " + e.getMessage());
        return false;
    }
}



public static double getProductQuantity(String productCode) {
    try (Connection conn = Database.createConnection()) {
        String query = "SELECT Quantity FROM Products WHERE ProductCode = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, productCode);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("Quantity");
                }
            }
        }
    } catch (SQLException e) {
        System.err.println("❌ Error fetching product quantity: " + e.getMessage());
    }
    return 0.0;
}
public static void markAsTransmitted(String invoiceNumber) {
    try (Connection connection = Database.createConnection()) {
        String updateQuery = "UPDATE Invoices SET State = 1 WHERE InvoiceNumber = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, invoiceNumber);
            stmt.executeUpdate();
        }
    } catch (SQLException e) {
        System.err.println("❌ Error updating transmission state: " + e.getMessage());
    }
}

public static void updateValidationUrl(String invoiceNumber, String validationUrl) {
    try (Connection connection = Database.createConnection()) {
        String updateQuery = "UPDATE Invoices SET ValidationUrl = ? WHERE InvoiceNumber = ?"; 
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, validationUrl);
            stmt.setString(2, invoiceNumber);
            stmt.executeUpdate();
        }
    } catch (SQLException e) {
        System.err.println("❌ Error updating validation URL: " + e.getMessage());
    }
}

public static InvoiceHeader getInvoiceHeader(String invoiceNumber, String buyerTIN, String buyerAuthorizationCode, String paymentMethod) {
    InvoiceHeader invoiceHeader = new InvoiceHeader();
    invoiceHeader.setInvoiceNumber(invoiceNumber);
    invoiceHeader.setInvoiceDateTime(LocalDateTime.now().toString());
    invoiceHeader.setSellerTIN(getTin());
    invoiceHeader.setBuyerTIN(buyerTIN != null && !buyerTIN.isEmpty() ? buyerTIN : "");
    invoiceHeader.setBuyerAuthorizationCode(buyerAuthorizationCode != null ? buyerAuthorizationCode : "");
    invoiceHeader.setSiteId(getTerminalSiteId());
    invoiceHeader.setGlobalConfigVersion(getGlobalVersion());
    invoiceHeader.setTaxpayerConfigVersion(getTaxpayerVersion());
    invoiceHeader.setTerminalConfigVersion(getTerminalVersion());
    invoiceHeader.setReliefSupply(false);
    invoiceHeader.setVat5CertificateDetails(null);
    invoiceHeader.setPaymentMethod(paymentMethod);
    return invoiceHeader;
}


public static List<LineItemDto> getLineItems(String invoiceNumber) {
    List<LineItemDto> items = new ArrayList<>();

    String query = "SELECT ProductCode, Description, UnitPrice, Quantity, TaxRateID, Discount, " +
                   "TotalPrice, VATAmount, IsProduct FROM LineItems WHERE InvoiceNumber = ?";

    try (Connection connection = Database.createConnection();
         PreparedStatement stmt = connection.prepareStatement(query)) {

        stmt.setString(1, invoiceNumber);
        var rs = stmt.executeQuery();

        while (rs.next()) {
            LineItemDto item = new LineItemDto(); 
            item.setProductCode(rs.getString("ProductCode"));
            item.setDescription(rs.getString("Description"));
            item.setUnitPrice(rs.getDouble("UnitPrice"));
            item.setQuantity(rs.getDouble("Quantity"));
            item.setTaxRateId(rs.getString("TaxRateID"));
            item.setDiscount(rs.getDouble("Discount"));
            item.setTotal(rs.getDouble("TotalPrice"));       
            item.setTotalVAT(rs.getDouble("VATAmount"));
            item.setIsProduct(rs.getBoolean("IsProduct"));
            items.add(item);
        }

    } catch (SQLException ex) {
        System.err.println("❌ Error fetching line items: " + ex.getMessage());
    }

    return items;
}


public static void createTerminalBlockingReasonsTable() {
    String createTableSQL = 
    "CREATE TABLE IF NOT EXISTS TerminalBlockingReasons (\n" +
    "    Id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
    "    TerminalId TEXT NOT NULL,\n" +
    "    BlockingReason TEXT NOT NULL,\n" +
    "    IsUnblocked BOOLEAN NOT NULL,\n" +
    "    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP\n" +
    ");";

    try (Connection conn = Database.createConnection();
         PreparedStatement stmt = conn.prepareStatement(createTableSQL)) {
        stmt.execute();
        System.out.println("✅ TerminalBlockingReasons table ensured.");
    } catch (SQLException e) {
        System.err.println("❌ Table creation failed: " + e.getMessage());
    }
}

public static void saveBlockingReason(String terminalId, String reason, boolean isUnblocked) {
    String insertSQL = "INSERT INTO TerminalBlockingReasons (TerminalId, BlockingReason, IsUnblocked, CreatedAt) " +
                   "VALUES (?, ?, ?, datetime('now'));";

    try (Connection conn = Database.createConnection();
         PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
        stmt.setString(1, terminalId);
        stmt.setString(2, reason);
        stmt.setBoolean(3, isUnblocked);
        stmt.executeUpdate();
        System.out.println("✅ Blocking reason saved.");
    } catch (SQLException e) {
        System.err.println("❌ Failed to save blocking reason: " + e.getMessage());
    }
}

public static String getBlockingReason(String terminalId) {
    String query = "SELECT BlockingReason FROM TerminalBlockingReasons WHERE TerminalId = ? ORDER BY CreatedAt DESC LIMIT 1";
    try (Connection conn = Database.createConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, terminalId);
        var rs = stmt.executeQuery();
        if (rs.next()) return rs.getString("BlockingReason");
    } catch (SQLException e) {
        System.err.println("❌ Failed to fetch blocking reason: " + e.getMessage());
    }
    return null;
}

public static void deleteBlockingReason(String terminalId) {
    String deleteSQL = "DELETE FROM TerminalBlockingReasons WHERE TerminalId = ?";
    try (Connection conn = Database.createConnection();
         PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
        stmt.setString(1, terminalId);
        stmt.executeUpdate();
        System.out.println("✅ Blocking reason deleted.");
    } catch (SQLException e) {
        System.err.println("❌ Failed to delete blocking reason: " + e.getMessage());
    }
}

public static void checkAndHandleTerminalBlocking(Consumer<Boolean> onCheckComplete) {
    String terminalId = getTerminalId();
    String bearerToken = getToken();
    final boolean[] isUnblocked = {true};

    ApiClient apiClient = new ApiClient();

    apiClient.checkIfTerminalIsBlocked(terminalId, bearerToken, checkResult -> {
        if (checkResult == null || checkResult.isUnblocked == null) {
            String existingReason = getBlockingReason(terminalId);
            isUnblocked[0] = existingReason == null;
        } else {
            isUnblocked[0] = checkResult.isUnblocked;
        }

        if (!isUnblocked[0]) {
            createTerminalBlockingReasonsTable();
            String existingReason = getBlockingReason(terminalId);

            if (existingReason == null) {
                apiClient.fetchBlockingMessage(terminalId, bearerToken, blockingInfo -> {
                    if (blockingInfo != null) {
                        if (blockingInfo.isBlocked) {
                            String reason = blockingInfo.blockingReason != null
                                    ? blockingInfo.blockingReason
                                    : "No reason provided by server.";

                            saveBlockingReason(terminalId, reason, false);

                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.WARNING,
                                        "Terminal is blocked. Reason: " + reason, ButtonType.OK);
                                alert.setTitle("Terminal Blocked");
                                alert.showAndWait();
                            });

                            onCheckComplete.accept(false); // block payment
                        } else {
                            // Terminal is NOT blocked, override incorrect isUnblocked==false
                            deleteBlockingReason(terminalId);
                            onCheckComplete.accept(true); // allow payment
                        }
                    } else {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.WARNING,
                                    "Unable to verify terminal status. Please check your connection and try again.",
                                    ButtonType.OK);
                            alert.setTitle("Connection Error");
                            alert.showAndWait();
                        });

                        onCheckComplete.accept(false); // block payment
                    }
                });
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING,
                            "Terminal is blocked. Reason: " + existingReason, ButtonType.OK);
                    alert.setTitle("Terminal Blocked");
                    alert.showAndWait();
                });

                onCheckComplete.accept(false); // block payment
            }
        } else {
            deleteBlockingReason(terminalId);
            onCheckComplete.accept(true); // allow payment
        }
    });
}

public static boolean transmitInvoice(String invoiceNumber) {
    try (Connection connection = Database.createConnection()) {
        String query = "SELECT * FROM Invoices WHERE InvoiceNumber = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, invoiceNumber);

        try (var rs = stmt.executeQuery()) {
            if (rs.next()) {
                String paymentId = rs.getString("PaymentId");
                InvoiceHeader header = Helper.getInvoiceHeader(invoiceNumber, "", "", paymentId);
                List<LineItemDto> lineItems = Helper.getLineItems(invoiceNumber);

                InvoiceSummary invoiceSummary = new InvoiceSummary();
                invoiceSummary.setTaxBreakDown(Helper.generateTaxBreakdown(lineItems));
                invoiceSummary.setTotalVAT(rs.getDouble("TotalVAT"));
                invoiceSummary.setInvoiceTotal(rs.getDouble("InvoiceTotal"));
                invoiceSummary.setOfflineSignature("");

                InvoicePayload payload = new InvoicePayload();
                payload.setInvoiceHeader(header);
                payload.setInvoiceLineItems(lineItems);
                payload.setInvoiceSummary(invoiceSummary);

                String jsonPayload = new Gson().toJson(payload);
                String token = Helper.getToken();

                // Use a blocking mechanism to get result
                final boolean[] resultHolder = {false};
                final boolean[] completed = {false};

                ApiClient apiClient = new ApiClient();
                apiClient.submitTransactions(jsonPayload, token, (success, returnedValidationUrl) -> {
                    resultHolder[0] = success;
                    completed[0] = true;
                    if (success) {
                        Helper.updateValidationUrl(invoiceNumber, returnedValidationUrl);
                        Helper.markAsTransmitted(invoiceNumber);
                    }
                });

                // Wait for callback completion (simple way)
                int waitMs = 0;
                while (!completed[0] && waitMs < 5000) {
                    Thread.sleep(100); // max wait: 5s
                    waitMs += 100;
                }

                return resultHolder[0];
            }
        }
    } catch (Exception e) {
        System.err.println("❌ Error: " + e.getMessage());
    }
    return false;
}
public static void retryPendingTransactions(
        Consumer<Pair<Integer, String>> progressCallback,
        Consumer<List<String>> onComplete
) {
    String query = "SELECT * FROM Invoices WHERE State = 0";

    List<String> failedInvoices = Collections.synchronizedList(new ArrayList<>());
    List<String> pendingInvoices = new ArrayList<>();
    Map<String, Double> vatMap = new HashMap<>();
    Map<String, Double> totalMap = new HashMap<>();
    Map<String, String> paymentMap = new HashMap<>();

    try (Connection connection = Database.createConnection();
         PreparedStatement stmt = connection.prepareStatement(query);
         var rs = stmt.executeQuery()) {

        while (rs.next()) {
            String invoiceNumber = rs.getString("InvoiceNumber");
            pendingInvoices.add(invoiceNumber);
            vatMap.put(invoiceNumber, rs.getDouble("TotalVAT"));
            totalMap.put(invoiceNumber, rs.getDouble("InvoiceTotal"));
            paymentMap.put(invoiceNumber, rs.getString("PaymentId")); // ✅ extract PaymentId
        }

        int total = pendingInvoices.size();
        AtomicInteger counter = new AtomicInteger(0);
        String token = Helper.getToken(); // ✅ fetch once

        ExecutorService executor = Executors.newFixedThreadPool(5); // better than spawning threads

        for (String invoiceNumber : pendingInvoices) {
            executor.submit(() -> {
                try {
                    String paymentId = paymentMap.get(invoiceNumber);
                    InvoiceHeader header = Helper.getInvoiceHeader(invoiceNumber, "", "", paymentId);
                    List<LineItemDto> lineItems = Helper.getLineItems(invoiceNumber);

                    InvoiceSummary invoiceSummary = new InvoiceSummary();
                    invoiceSummary.setTaxBreakDown(Helper.generateTaxBreakdown(lineItems));
                    invoiceSummary.setTotalVAT(vatMap.getOrDefault(invoiceNumber, 0.0));
                    invoiceSummary.setInvoiceTotal(totalMap.getOrDefault(invoiceNumber, 0.0));
                    invoiceSummary.setOfflineSignature("");

                    InvoicePayload payload = new InvoicePayload();
                    payload.setInvoiceHeader(header);
                    payload.setInvoiceLineItems(lineItems);
                    payload.setInvoiceSummary(invoiceSummary);

                    String jsonPayload = new Gson().toJson(payload);

                    ApiClient apiClient = new ApiClient();
                    apiClient.submitTransactions(jsonPayload, token, (success, returnedValidationUrl) -> {
                        if (success) {
                            Helper.updateValidationUrl(invoiceNumber, returnedValidationUrl);
                            Helper.markAsTransmitted(invoiceNumber);
                            System.out.println("✅ Synced: " + invoiceNumber);
                        } else {
                            failedInvoices.add(invoiceNumber);
                            System.err.println("❌ Failed: " + invoiceNumber);
                        }

                        int current = counter.incrementAndGet();
                        if (progressCallback != null) {
                            progressCallback.accept(new Pair<>(current, invoiceNumber));
                        }
                        if (current == total && onComplete != null) {
                            onComplete.accept(failedInvoices);
                        }
                    });

                } catch (Exception e) {
                    failedInvoices.add(invoiceNumber);
                    System.err.println("❌ Exception for: " + invoiceNumber + " → " + e.getMessage());

                    int current = counter.incrementAndGet();
                    if (progressCallback != null) {
                        progressCallback.accept(new Pair<>(current, invoiceNumber));
                    }
                    if (current == total && onComplete != null) {
                        onComplete.accept(failedInvoices);
                    }
                }
            });

            Thread.sleep(400); // throttle if needed
        }

        executor.shutdown();

    } catch (SQLException | InterruptedException e) {
        System.err.println("❌ DB error: " + e.getMessage());
        if (onComplete != null) {
            onComplete.accept(pendingInvoices);
        }
    }
}

    /**
     * Get the store name
     */
    public static String getStoreName() {
        // Replace with your actual implementation or configuration
        return "ACME Point of Sale";
    }
    
    /**
     * Get the store address
     */
    public static String getStoreAddress() {
        // Replace with your actual implementation or configuration
        return "123 Main Street, City";
    }
    
    /**
     * Get the store phone number
     */
    public static String getStorePhone() {
        // Replace with your actual implementation or configuration
        return "+265 1234 5678";
    }
    
    public static List<SaleSummary> getDailySalesSummary(int daysBack) {
    String query = "SELECT " +
            "DATE(InvoiceDateTime) as SaleDate, " +
            "COUNT(*) as Transactions, " +
            "SUM(InvoiceTotal) as TotalRevenue, " +
            "SUM(TotalVAT) as TotalTax " +
            "FROM Invoices " +
            "WHERE DATE(InvoiceDateTime) >= DATE('now', ?) " +
            "GROUP BY DATE(InvoiceDateTime) " +
            "ORDER BY DATE(InvoiceDateTime) DESC";

    List<SaleSummary> summaries = new ArrayList<>();
    try (var conn = Database.createConnection();
         var stmt = conn.prepareStatement(query)) {

        stmt.setString(1, "-" + daysBack + " days");

        try (var rs = stmt.executeQuery()) {
            while (rs.next()) {
                String date = rs.getString("SaleDate");
                int transactions = rs.getInt("Transactions");
                double revenue = rs.getDouble("TotalRevenue");
                double tax = rs.getDouble("TotalTax");

                summaries.add(new SaleSummary(date, transactions, revenue, tax));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return summaries;
}
    
  public static double getTodaySalesTotal() {
        String query = "SELECT SUM(InvoiceTotal) FROM Invoices WHERE DATE(InvoiceDateTime) = DATE('now')";
        return fetchDouble(query);
    }

    public static double getYesterdaySalesTotal() {
        String query = "SELECT SUM(InvoiceTotal) FROM Invoices WHERE DATE(InvoiceDateTime) = DATE('now', '-1 day')";
        return fetchDouble(query);
    }

    public static double getMonthSalesTotal() {
        String query = "SELECT SUM(InvoiceTotal) FROM Invoices WHERE strftime('%Y-%m', InvoiceDateTime) = strftime('%Y-%m', 'now')";
        return fetchDouble(query);
    }

    public static double getLastMonthSalesTotal() {
        String query = "SELECT SUM(InvoiceTotal) FROM Invoices WHERE strftime('%Y-%m', InvoiceDateTime) = strftime('%Y-%m', 'now', '-1 month')";
        return fetchDouble(query);
    }

    public static int getTodayTransactionCount() {
        String query = "SELECT COUNT(*) FROM Invoices WHERE DATE(InvoiceDateTime) = DATE('now')";
        return (int) fetchDouble(query);
    }

    public static double getTodayTaxTotal() {
        String query = "SELECT SUM(TotalVAT) FROM Invoices WHERE DATE(InvoiceDateTime) = DATE('now')";
        return fetchDouble(query);
    }

    private static double fetchDouble(String query) {
        try (Connection conn = Database.createConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             var rs = stmt.executeQuery()) {

            return rs.next() ? rs.getDouble(1) : 0.0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public static String getPercentageChange(double current, double previous) {
        if (previous == 0) {
            return current == 0 ? "0%" : "+100%";
        }
        double change = ((current - previous) / previous) * 100;
        return String.format("%+.1f%%", change);
    }
    
    public static List<ProductSale> getTop10ProductSales() {
    String query = "SELECT li.ProductCode, p.ProductName, " +
            "SUM(li.Quantity) AS TotalQuantity, SUM(li.TotalPrice) AS TotalRevenue " +
            "FROM LineItems li " +
            "JOIN Products p ON li.ProductCode = p.ProductCode " +
            "GROUP BY li.ProductCode " +
            "ORDER BY TotalRevenue DESC " +
            "LIMIT 10";

    List<ProductSale> sales = new ArrayList<>();
    try (var conn = Database.createConnection();
         var stmt = conn.prepareStatement(query);
         var rs = stmt.executeQuery()) {

        while (rs.next()) {
            String name = rs.getString("ProductName");
            int quantity = rs.getInt("TotalQuantity");
            double revenue = rs.getDouble("TotalRevenue");
            double profit = revenue * 0.25; // Optional: estimated profit margin
            sales.add(new ProductSale(name, quantity, revenue, profit));
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return new ArrayList<>();  // Return an empty list in case of error
    }

    return sales;
}
 
public static List<CategoryRevenue> getSalesByCategory() {
    String query = "SELECT " +
            "CASE " +
            "WHEN p.Description LIKE '%food%' THEN 'Food & Beverages' " +
            "WHEN p.Description LIKE '%soap%' OR p.Description LIKE '%clean%' THEN 'Household Items' " +
            "WHEN p.Description LIKE '%shampoo%' OR p.Description LIKE '%cream%' THEN 'Personal Care' " +
            "ELSE 'Others' END AS Category, " +
            "SUM(li.TotalPrice) AS Revenue " +
            "FROM LineItems li " +
            "JOIN Products p ON li.ProductCode = p.ProductCode " +
            "GROUP BY Category";

    List<CategoryRevenue> categoryRevenueList = new ArrayList<>();
    try (var conn = Database.createConnection();
         var stmt = conn.prepareStatement(query);
         var rs = stmt.executeQuery()) {

        while (rs.next()) {
            String category = rs.getString("Category");
            double revenue = rs.getDouble("Revenue");
            // Add to CategoryRevenue list
            categoryRevenueList.add(new CategoryRevenue(category, revenue));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return categoryRevenueList;
}

public static List<TaxTrend> fetchTaxTrends(String startDate, String endDate) {
    String query = "SELECT strftime('%Y-%m-%d', InvoiceDateTime) AS Date, SUM(TotalVAT) AS VATAmount " +
                   "FROM Invoices " +
                   "WHERE InvoiceDateTime BETWEEN ? AND ? " +
                   "GROUP BY strftime('%Y-%m-%d', InvoiceDateTime) " +
                   "ORDER BY Date DESC";

    List<TaxTrend> trends = new ArrayList<>();
    
    try (Connection conn = Database.createConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, startDate);
        stmt.setString(2, endDate);
        
        try (var rs = stmt.executeQuery()) {
            while (rs.next()) {
                String date = rs.getString("Date");
                double vatAmount = rs.getDouble("VATAmount");
                trends.add(new TaxTrend(date, vatAmount));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return trends;
}

public static double fetchTotalVAT(String startDate, String endDate) {
        String query = "SELECT SUM(TotalVAT) AS totalVAT FROM Invoices WHERE InvoiceDateTime BETWEEN ? AND ?";
        return fetchDouble(query, startDate, endDate);
    }

    public static double fetchStandardRateVAT(String startDate, String endDate) {
        String query = "SELECT SUM(TotalVAT) AS totalVAT " +
                       "FROM Invoices i " +
                       "JOIN LineItems li ON i.InvoiceNumber = li.InvoiceNumber " +
                       "JOIN TaxRates tr ON li.TaxRateID = tr.Id " +
                       "WHERE tr.Rate = 16.5 AND i.InvoiceDateTime BETWEEN ? AND ?";
        return fetchDouble(query, startDate, endDate);
    }

    public static double fetchZeroRatedVAT(String startDate, String endDate) {
        String query = "SELECT SUM(TotalVAT) AS totalVAT " +
                       "FROM Invoices i " +
                       "JOIN LineItems li ON i.InvoiceNumber = li.InvoiceNumber " +
                       "JOIN TaxRates tr ON li.TaxRateID = tr.Id " +
                       "WHERE tr.Rate = 0 AND i.InvoiceDateTime BETWEEN ? AND ?";
        return fetchDouble(query, startDate, endDate);
    }

    public static double fetchExemptSales(String startDate, String endDate) {
        String query = "SELECT SUM(TotalVAT) AS totalVAT " +
                       "FROM Invoices i " +
                       "JOIN LineItems li ON i.InvoiceNumber = li.InvoiceNumber " +
                       "JOIN TaxRates tr ON li.TaxRateID = tr.Id " +
                       "WHERE tr.Name = 'Exempt' AND i.InvoiceDateTime BETWEEN ? AND ?";
        return fetchDouble(query, startDate, endDate);
    }

    public static double fetchDouble(String query, Object... params) {
        try (Connection conn = Database.createConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set parameters dynamically
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            // Execute the query and fetch the result
            try (var rs = stmt.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    public static ObservableList<TaxSummary> fetchTaxBreakdown() {
    String query = "SELECT tr.Name AS TaxRate, SUM(li.TotalPrice) AS TotalSales, SUM(li.VATAmount) AS TaxAmount " +
                   "FROM LineItems li " +
                   "JOIN TaxRates tr ON li.TaxRateID = tr.Id " +
                   "GROUP BY tr.Name";
    
    ObservableList<TaxSummary> taxData = FXCollections.observableArrayList();
    
    try (Connection conn = Database.createConnection();
         PreparedStatement stmt = conn.prepareStatement(query);
         var rs = stmt.executeQuery()) {
        
        while (rs.next()) {
            String taxRate = rs.getString("TaxRate");
            double totalSales = rs.getDouble("TotalSales");
            double taxAmount = rs.getDouble("TaxAmount");
            taxData.add(new TaxSummary(taxRate, totalSales, taxAmount));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return taxData;
}
    
public static double getSalesTotalByDateRange(String startDate, String endDate) {
    String query = "SELECT SUM(InvoiceTotal) FROM Invoices WHERE DATE(InvoiceDateTime) BETWEEN ? AND ?";
    return fetchDouble(query, startDate, endDate);
}

public static int getTransactionCountByDateRange(String startDate, String endDate) {
    String query = "SELECT COUNT(*) FROM Invoices WHERE DATE(InvoiceDateTime) BETWEEN ? AND ?";
    return (int) fetchDouble(query, startDate, endDate);
}

public static double getTaxTotalByDateRange(String startDate, String endDate) {
    String query = "SELECT SUM(TotalVAT) FROM Invoices WHERE DATE(InvoiceDateTime) BETWEEN ? AND ?";
    return fetchDouble(query, startDate, endDate);
}
private static double fetchDouble(String query, String startDate, String endDate) {
    try (Connection conn = Database.createConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, startDate);
        stmt.setString(2, endDate);

        try (var rs = stmt.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }

    } catch (SQLException e) {
        e.printStackTrace();
        return 0.0;
    }
}   

public static double getSalesTotalForPreviousPeriod(String startDate, String endDate) {
    // Parse the input strings into LocalDate
    LocalDate start = LocalDate.parse(startDate);
    LocalDate end = LocalDate.parse(endDate);

    // Calculate the duration of the current period
    long days = ChronoUnit.DAYS.between(start, end) + 1;

    // Calculate previous period range
    LocalDate previousStart = start.minusDays(days);
    LocalDate previousEnd = end.minusDays(days);

    return getSalesTotalByDateRange(previousStart.toString(), previousEnd.toString());
}

public static List<ProductSale> getTop10ProductSalesByDateRange(String fromDate, String toDate) {
    String query = "SELECT li.ProductCode, p.ProductName, " +
            "SUM(li.Quantity) AS TotalQuantity, SUM(li.TotalPrice) AS TotalRevenue " +
            "FROM LineItems li " +
            "JOIN Invoices i ON li.InvoiceNumber = i.InvoiceNumber " +
            "JOIN Products p ON li.ProductCode = p.ProductCode " +
            "WHERE DATE(i.InvoiceDateTime) BETWEEN ? AND ? " +
            "GROUP BY li.ProductCode " +
            "ORDER BY TotalRevenue DESC " +
            "LIMIT 10";

    List<ProductSale> sales = new ArrayList<>();
    try (var conn = Database.createConnection();
         var stmt = conn.prepareStatement(query)) {

        stmt.setString(1, fromDate);
        stmt.setString(2, toDate);
        var rs = stmt.executeQuery();

        while (rs.next()) {
            String name = rs.getString("ProductName");
            int quantity = rs.getInt("TotalQuantity");
            double revenue = rs.getDouble("TotalRevenue");
            double profit = revenue * 0.25; // estimated
            sales.add(new ProductSale(name, quantity, revenue, profit));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return sales;
}

public static List<CategoryRevenue> getSalesByCategoryByDateRange(String fromDate, String toDate) {
    String query = "SELECT " +
            "CASE " +
            "WHEN p.Description LIKE '%food%' THEN 'Food & Beverages' " +
            "WHEN p.Description LIKE '%soap%' OR p.Description LIKE '%clean%' THEN 'Household Items' " +
            "WHEN p.Description LIKE '%shampoo%' OR p.Description LIKE '%cream%' THEN 'Personal Care' " +
            "ELSE 'Others' END AS Category, " +
            "SUM(li.TotalPrice) AS Revenue " +
            "FROM LineItems li " +
            "JOIN Invoices i ON li.InvoiceNumber = i.InvoiceNumber " +
            "JOIN Products p ON li.ProductCode = p.ProductCode " +
            "WHERE DATE(i.InvoiceDateTime) BETWEEN ? AND ? " +
            "GROUP BY Category";

    List<CategoryRevenue> categoryRevenueList = new ArrayList<>();
    try (var conn = Database.createConnection();
         var stmt = conn.prepareStatement(query)) {

        stmt.setString(1, fromDate);
        stmt.setString(2, toDate);
        var rs = stmt.executeQuery();

        while (rs.next()) {
            String category = rs.getString("Category");
            double revenue = rs.getDouble("Revenue");
            categoryRevenueList.add(new CategoryRevenue(category, revenue));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return categoryRevenueList;
}
public static ObservableList<TaxSummary> fetchTaxBreakdownByDateRange(String fromDate, String toDate) {
    String query = "SELECT tr.Name AS TaxRate, " +
                   "SUM(li.TotalPrice) AS TotalSales, SUM(li.VATAmount) AS TaxAmount " +
                   "FROM LineItems li " +
                   "JOIN Invoices i ON li.InvoiceNumber = i.InvoiceNumber " +
                   "JOIN TaxRates tr ON li.TaxRateID = tr.Id " +
                   "WHERE i.InvoiceDateTime BETWEEN ? AND ? " +
                   "GROUP BY tr.Name";

    ObservableList<TaxSummary> taxData = FXCollections.observableArrayList();

    try (Connection conn = Database.createConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, fromDate);
        stmt.setString(2, toDate);

        try (var rs = stmt.executeQuery()) {
            while (rs.next()) {
                String taxRate = rs.getString("TaxRate");
                double totalSales = rs.getDouble("TotalSales");
                double taxAmount = rs.getDouble("TaxAmount");
                taxData.add(new TaxSummary(taxRate, totalSales, taxAmount));
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return taxData;
}
public static List<SaleSummary> getSalesSummaryByDateRange(String fromDate, String toDate) {
    String query = "SELECT " +
            "DATE(InvoiceDateTime) as SaleDate, " +
            "COUNT(*) as Transactions, " +
            "SUM(InvoiceTotal) as TotalRevenue, " +
            "SUM(TotalVAT) as TotalTax " +
            "FROM Invoices " +
            "WHERE DATE(InvoiceDateTime) BETWEEN ? AND ? " +
            "GROUP BY DATE(InvoiceDateTime) " +
            "ORDER BY DATE(InvoiceDateTime) ASC";

    List<SaleSummary> summaries = new ArrayList<>();
    try (var conn = Database.createConnection();
         var stmt = conn.prepareStatement(query)) {

        stmt.setString(1, fromDate);
        stmt.setString(2, toDate);

        try (var rs = stmt.executeQuery()) {
            while (rs.next()) {
                String date = rs.getString("SaleDate");
                int transactions = rs.getInt("Transactions");
                double revenue = rs.getDouble("TotalRevenue");
                double tax = rs.getDouble("TotalTax");

                summaries.add(new SaleSummary(date, transactions, revenue, tax));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return summaries;
}

public static List<String> getAllProductBarcodes() {
    List<String> barcodes = new ArrayList<>();

    String query = "SELECT ProductCode FROM Products"; // or SELECT Barcode FROM Products if Barcode column exists

    try (Connection conn = Database.createConnection();
         PreparedStatement stmt = conn.prepareStatement(query);
         var rs = stmt.executeQuery()) {

        while (rs.next()) {
            barcodes.add(rs.getString("ProductCode")); // or "Barcode"
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return barcodes;
}



}
