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
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Base64;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.json.JsonObject;
import com.pointofsale.model.Product;
import com.pointofsale.model.TaxRates;
import com.pointofsale.model.InvoiceDetails;
import com.pointofsale.model.Session;
import com.pointofsale.model.InvoiceHeader;
import com.pointofsale.model.TaxBreakDown;
import com.pointofsale.model.LineItemDto;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
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
    byte[] decoded = Base64.getUrlDecoder().decode(base64);
    long result = 0;
    for (byte b : decoded) {
        result = (result << 8) + (b & 0xFF);
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

    String insertInvoiceQuery = "INSERT INTO Invoices (InvoiceNumber, InvoiceDateTime, InvoiceTotal, SellerTin, BuyerTin, TotalVAT, OfflineSignature, ValidationUrl, IsReliefSupply, State, PaymentId, AmountPaid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    String insertLineItemQuery = "INSERT INTO LineItems (ProductCode, Description, UnitPrice, Quantity, InvoiceNumber, TaxRateID, Discount, IsProduct) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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

            // Insert LineItems + Update Product Quantity
            for (LineItemDto item : lineItems) {
                if (item.isProduct()) {
                    double currentQty = getProductQuantity(item.getProductCode());
                    double newQty = currentQty - item.getQuantity();
                    updateProductStmt.setDouble(1, newQty);
                    updateProductStmt.setString(2, item.getProductCode());
                    updateProductStmt.executeUpdate();
                }

                lineItemStmt.setString(1, item.getProductCode());
                lineItemStmt.setString(2, item.getDescription());
                lineItemStmt.setDouble(3, item.getUnitPrice());
                lineItemStmt.setDouble(4, item.getQuantity());
                lineItemStmt.setString(5, invoice.getInvoiceNumber());
                lineItemStmt.setString(6, item.getTaxRateId());
                lineItemStmt.setDouble(7, item.getDiscount());
                lineItemStmt.setBoolean(8, item.isProduct());
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

public static InvoiceHeader getInvoiceHeader(String invoiceNumber) {
    InvoiceHeader invoiceHeader = new InvoiceHeader();
    invoiceHeader.setInvoiceNumber(invoiceNumber);
    invoiceHeader.setInvoiceDateTime(LocalDateTime.now().toString());
    invoiceHeader.setSellerTIN(getTin());
    invoiceHeader.setBuyerTIN("");
    invoiceHeader.setBuyerAuthorizationCode("");
    invoiceHeader.setSiteId(getTerminalSiteId());
    invoiceHeader.setGlobalConfigVersion(getGlobalVersion());
    invoiceHeader.setTaxpayerConfigVersion(getTaxpayerVersion());
    invoiceHeader.setTerminalConfigVersion(getTerminalVersion());
    invoiceHeader.setReliefSupply(false);
    invoiceHeader.setVat5CertificateDetails(null);
    invoiceHeader.setPaymentMethod("Cash"); 
    return invoiceHeader;
}

public static List<LineItemDto> getLineItems(String invoiceNumber) {
    List<LineItemDto> items = new ArrayList<>();

    String query = "SELECT ProductCode, Description, UnitPrice, Quantity, TaxRateID, Discount, IsProduct " +
                   "FROM LineItems WHERE InvoiceNumber = ?";

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
            item.setIsProduct(rs.getBoolean("IsProduct"));
            items.add(item);
        }

    } catch (SQLException ex) {
        System.err.println("❌ Error fetching line items: " + ex.getMessage());
    }

    return items;
}

}
