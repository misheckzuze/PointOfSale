package com.pointofsale.data;

import java.sql.*;
import java.io.File;
import com.pointofsale.helper.Helper;

public class Database {

    private static final String DB_NAME = "EISTerminalDb.db";
    private static final String DB_PATH;

    static {
        boolean isDevelopment = java.lang.management.ManagementFactory.getRuntimeMXBean()
                .getInputArguments().toString().contains("jdwp");

        if (isDevelopment) {
            DB_PATH = System.getProperty("user.dir") + File.separator + "EISPointOfSaleDesktop" + File.separator + DB_NAME;
        } else {
            DB_PATH = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming"
                    + File.separator + "POSSetup" + File.separator + DB_NAME;
        }
        createDirectoryIfNotExists(DB_PATH);
    }

    private static void createDirectoryIfNotExists(String path) {
        File dbFile = new File(path);
        File parent = dbFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
    }

    public static Connection connOpen() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
    }

    public static void initializeDatabase() {
        try (Connection conn = connOpen(); Statement stmt = conn.createStatement()) {

            // Creating existing tables
            String createProductsTable = "CREATE TABLE IF NOT EXISTS Products (" +
                    "ProductCode TEXT NOT NULL PRIMARY KEY, " +
                    "ProductName TEXT NOT NULL, " +
                    "Description TEXT NOT NULL, " +
                    "Quantity REAL NOT NULL, " +
                    "UnitOfMeasure TEXT, " +
                    "Price REAL NOT NULL, " +
                    "SiteId TEXT, " +
                    "ProductExpiryDate TEXT, " +
                    "MinimumStockLevel REAL, " +
                    "TaxRateId TEXT, " +
                    "IsProduct INTEGER, " +
                    "Discount REAL DEFAULT 0.0)";

                                
            String createUsersTable = "CREATE TABLE IF NOT EXISTS Users (" +
                    "UserID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "FirstName TEXT NOT NULL," +
                    "LastName TEXT NOT NULL," +
                    "UserName TEXT NOT NULL UNIQUE," +
                    "Gender TEXT CHECK (Gender IN ('MALE', 'FEMALE'))," +
                    "PhoneNumber TEXT," +
                    "EmailAddress TEXT UNIQUE," +
                    "Address TEXT," +
                    "Role TEXT NOT NULL CHECK (Role IN ('ADMIN', 'CASHIER'))," +
                    "Password TEXT NOT NULL)";

            String createInvoicesTable = "CREATE TABLE IF NOT EXISTS Invoices (" +
                    "InvoiceNumber TEXT PRIMARY KEY," +
                    "InvoiceDateTime TEXT," +
                    "InvoiceTotal REAL," +
                    "SellerTin TEXT," +
                    "BuyerTin TEXT," +
                    "TotalVAT REAL," +
                    "OfflineTransactionSignature TEXT," +
                    "SiteId TEXT, " +
                    "ValidationUrl TEXT, " +
                    "IsReliefSupply INTEGER, " +
                    "State INTEGER, " +
                    "PaymentId TEXT, " +
                    "AmountPaid REAL)";
            
            String createActivatedTerminalTable = "CREATE TABLE IF NOT EXISTS ActivatedTerminal (" +
                    "TerminalId TEXT PRIMARY KEY, " +   
                    "TerminalPosition INTEGER, " +  
                    "TaxpayerId INTEGER, " +    
                    "ActivationDate TEXT, " +           
                    "IsActive INTEGER, " +             
                    "JwtToken TEXT, " +            
                    "SecretKey TEXT)";
            
            String createActivationCodeTable = "CREATE TABLE IF NOT EXISTS ActivationCode (" +
                    "ActivationCode TEXT PRIMARY KEY)";

            
            String createTerminalConfigurationTable = "CREATE TABLE IF NOT EXISTS TerminalConfiguration (" +
                    "TerminalId TEXT PRIMARY KEY, " +
                    "Label TEXT, " +
                    "IsActive INTEGER, " +
                    "Email TEXT, " +
                    "Phone TEXT, " +
                    "VersionNo INTEGER NOT NULL, " +
                    "TradingName TEXT, " +
                    "AddressLine TEXT)";
            
            String createInvoiceTaxBreakdownTable = "CREATE TABLE IF NOT EXISTS InvoiceTaxBreakDown (" + 
                    "InvoiceNumber TEXT, " +
                    "RateID TEXT, " +
                    "TaxableAmount REAL, " + 
                    "TaxAmount REAL)";
            
            String createPaymentTypeTable = "CREATE TABLE IF NOT EXISTS PaymentType (" + 
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "PaymentId TEXT, " +
                    "Name TEXT, " +
                    "AmountPaid REAL)";
                    
            
            String createGloblaConfigurationsTable = "CREATE TABLE IF NOT EXISTS GlobalConfiguration (" +
                     "Id INTEGER NOT NULL, " +
                     "VersionNo INTEGER NOT NULL )";
            
            String createOfflineLimitTable = "CREATE TABLE IF NOT EXISTS OfflineLimit (" +
                    "TerminalId TEXT PRIMARY KEY, " +
                    "MaxTransactionAgeInHours INTEGER, " +
                    "MaxCummulativeAmount REAL)";

            
            String createTaxpayerConfigurationTable = "CREATE TABLE IF NOT EXISTS TaxpayerConfiguration (" +
                   "TaxpayerId INTEGER PRIMARY KEY, " +
                   "TIN TEXT, " +
                   "IsVATRegistered INTEGER, " +
                   "VersionNo INTEGER NOT NULL, " +
                   "TaxOfficeCode TEXT)";
            
            String createTaxOfficesTable = "CREATE TABLE IF NOT EXISTS TaxOffices (" +
                   "Code TEXT PRIMARY KEY, " +
                   "Name TEXT)";


            // Existing tables for Invoice LineItems, Discounts, TerminalSites, etc.
            String createLineItemsTable = "CREATE TABLE IF NOT EXISTS LineItems (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "InvoiceNumber TEXT," +
                    "ProductCode TEXT," +
                    "Description TEXT," +
                    "Quantity REAL," +
                    "TaxRateID TEXT, " +
                    "Discount REAL, " +
                    "UnitPrice REAL," +
                    "TotalPrice REAL," +
                    "DiscountAmount REAL," +
                    "VATRate REAL," +
                    "IsProduct INTEGER," +
                    "VATAmount REAL," +
                    "FOREIGN KEY(InvoiceNumber) REFERENCES Invoices(InvoiceNumber))";
            
            String createDiscountsTable = "CREATE TABLE IF NOT EXISTS Discounts (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "DiscountType TEXT," +
                    "DiscountValue REAL," +
                    "DiscountReason TEXT," +
                    "InvoiceNumber TEXT," +
                    "FOREIGN KEY(InvoiceNumber) REFERENCES Invoices(InvoiceNumber))";
            
            String createTerminalSitesTable = "CREATE TABLE IF NOT EXISTS TerminalSites (" +
                    "SiteId TEXT PRIMARY KEY," +
                    "Name TEXT," +
                    "Location TEXT)";

            // TaxRates table
            String createTaxRatesTable = "CREATE TABLE IF NOT EXISTS TaxRates (" +
                    "Id TEXT PRIMARY KEY, " +
                    "Name TEXT, " +
                    "ChargeMode TEXT, " +
                    "Ordinal INTEGER, " +
                    "Rate REAL)";

            String createVoidReceiptRequestsTable = "CREATE TABLE IF NOT EXISTS VoidReceiptRequests (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "InvoiceNumber TEXT," +
                    "RequestReason TEXT," +
                    "RequestDate TEXT," +
                    "FOREIGN KEY(InvoiceNumber) REFERENCES Invoices(InvoiceNumber))";
            
            String createHeldSalesTable = "CREATE TABLE IF NOT EXISTS HeldSales (" +
                    "HoldId TEXT PRIMARY KEY, " +
                    "CustomerName TEXT, " +
                    "CustomerTIN TEXT, " +
                    "CartDiscountAmount REAL, " +
                    "CartDiscountPercent REAL, " +
                    "HoldTime TEXT" +
                     ")";

            String createHeldSaleItemsTable = "CREATE TABLE IF NOT EXISTS HeldSaleItems (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "HoldId TEXT NOT NULL, " +
                    "Barcode TEXT NOT NULL, " +               // was ProductCode
                    "ProductName TEXT NOT NULL, " +
                    "UnitPrice REAL NOT NULL, " +
                    "Quantity REAL NOT NULL, " +              // changed to REAL for decimal quantities
                    "Discount REAL, " +
                    "Total REAL, " +
                    "TotalVAT REAL, " +
                    "TaxRate TEXT, " +
                    "UnitOfMeasure TEXT, " +
                    "FOREIGN KEY(HoldId) REFERENCES HeldSales(HoldId)" +
                    ")";
            String createCustomersTable = "CREATE TABLE IF NOT EXISTS Customers (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Name TEXT NOT NULL, " +
                    "Phone TEXT, " +
                    "Email TEXT, " +
                    "Type TEXT, " +
                    "TIN TEXT UNIQUE, " +
                    "Address TEXT, " +
                    "RegisteredDate TEXT DEFAULT (datetime('now')), " +
                    "Notes TEXT" +
                    ")";



            // Execute all statements
            stmt.execute(createProductsTable);
            stmt.execute(createUsersTable);
            stmt.execute(createInvoicesTable);
            stmt.execute(createLineItemsTable);
            stmt.execute(createDiscountsTable);
            stmt.execute(createTerminalSitesTable);
            stmt.execute(createTaxRatesTable);
            stmt.execute(createVoidReceiptRequestsTable);
            stmt.execute(createActivatedTerminalTable);
            stmt.execute(createTerminalConfigurationTable);
            stmt.execute(createOfflineLimitTable);
            stmt.execute(createTaxpayerConfigurationTable);
            stmt.execute(createTaxOfficesTable);
            stmt.execute(createActivationCodeTable);
            stmt.execute(createGloblaConfigurationsTable);
            stmt.execute(createInvoiceTaxBreakdownTable);
            stmt.execute(createPaymentTypeTable);
            stmt.execute(createHeldSalesTable);
            stmt.execute(createCustomersTable);
            stmt.execute(createHeldSaleItemsTable);

            System.out.println("✅ All SQLite tables created and initialized at: " + DB_PATH);
            Helper.insertDefaultAdminIfNotExists(); // ← Insert admin after table creation


        } catch (SQLException e) {
            System.out.println("❌ Error initializing database: " + e.getMessage());
        }
    }

    public static Connection createConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
    }
}
