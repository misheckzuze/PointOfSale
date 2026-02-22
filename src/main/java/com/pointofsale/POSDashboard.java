package com.pointofsale;

import com.pointofsale.helper.ApiClient;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import com.google.gson.Gson;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.util.Optional;
import javafx.stage.Popup;
import javafx.event.EventHandler;
import javafx.animation.KeyValue;
import javafx.util.Pair;
import javafx.scene.image.Image;
import javafx.scene.shape.Polygon;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import java.awt.image.BufferedImage;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.github.sarxos.webcam.Webcam;
import javafx.scene.image.ImageView;
import com.google.zxing.DecodeHintType;
import java.util.HashMap;
import java.util.Map;
import com.google.zxing.ResultPoint;
import com.google.zxing.NotFoundException;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;
import com.github.sarxos.webcam.WebcamResolution;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.zxing.BarcodeFormat;
import javafx.stage.Modality;
import java.awt.Dimension;
import javafx.scene.Group;
import javafx.event.ActionEvent;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.text.FontWeight;
import com.pointofsale.model.Session;
import com.pointofsale.model.InvoiceSummary;
import com.pointofsale.model.LevyBreakDownDto;
import com.pointofsale.model.InvoiceDetails;
import com.pointofsale.model.InvoiceGenerationRequest;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.sound.sampled.AudioInputStream;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import com.pointofsale.model.Product;
import com.pointofsale.model.LevyDto;
import com.pointofsale.model.Vat5CertificateDetails;
import com.pointofsale.model.LineItemDto;
import com.pointofsale.model.InvoiceHeader;
import com.pointofsale.model.HeldSale;
import com.pointofsale.model.Vat5Data;
import com.pointofsale.model.InvoicePayload;
import com.pointofsale.model.TaxBreakDown;
import java.text.NumberFormat;
import javafx.scene.shape.Line;
import java.time.LocalDateTime;
import com.pointofsale.helper.Helper;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import java.util.Random;
import javafx.scene.Node;
import java.util.ArrayList;
import com.google.zxing.MultiFormatReader;
import java.util.Locale;

public class POSDashboard extends Application {
   
    // Main application components
    private Stage stage;
    private BorderPane root;
    private TableView<Product> cartTable;
    private Label totalAmountLabel;
    private Label taxValueLabel;
    private Label subtotalValueLabel;
    private TextField barcodeField;
    private Label dateTimeLabel;
    private HBox activeMenuItem;
    private Label cashierNameLabel;
    private Label receiptNumberLabel;
    private Label changeValueLabel;
    private TextField cashAmountField;
    private Button processPaymentButton;
    private Button doneButton;
    private Label taxLabel;
    private TextField heldCustomerNameField;
    private Node currentContent;
    private TextField buyerAuthField;
    private ComboBox<String> paymentMethodComboBox;
    private double cartDiscountAmount = 0.0;
    private double cartDiscountPercent = 0.0;
    private Label discountValueLabel;
    private Label leviesValueLabel;
    private TextField customerNamesField;
    private TextField  tinField; 
    private ObservableList<Product> cartItems;
    private double totalAmount = 0.0;
    private String currentCashier = Session.firstName + " " + Session.lastName;
    private String role = Session.role;
    private String receiptNumber =  generateNewReceiptNumber();
    private String transactionNote = "";
    private Label noteIndicatorLabel;
    private Stage numberPadDialog;
    private TextField activeField;
    private boolean isVat5Exempt = false;
    private Vat5Data currentVat5Data = null;
    private TextField vat5ProjectField;
    private TextField vat5CertificateField;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        createDashboard();
        barcodeField.setOnAction(e -> addProductToCart());
        Platform.runLater(() -> barcodeField.requestFocus());
        startTimeUpdater();
        stage.show();
    }

   private void createDashboard() {
    // Initialize cart items list if needed
    cartItems = FXCollections.observableArrayList();
    
    // Get screen size for responsive design
    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
    double screenWidth = screenBounds.getWidth();
    double screenHeight = screenBounds.getHeight();
    
    // Create main layout
    root = new BorderPane();
    root.setStyle("-fx-background-color: #f5f5f7;");
    
    // Set up the different regions of the dashboard
    root.setTop(createTopBar());
    root.setLeft(createSidebar());
    
    // Calculate responsive window size (use 90% of screen, with minimums)
    double windowWidth = Math.max(screenWidth * 0.9, 1024);  // Min 1024px
    double windowHeight = Math.max(screenHeight * 0.9, 768); // Min 768px
    
    // Create scene with calculated dimensions
    Scene scene = new Scene(root, windowWidth, windowHeight);
    stage.setScene(scene);
    stage.setTitle("MQ POS System");
    
    // Always maximize for now during development
    stage.setMaximized(true);
    
    
    // Load the default content (Sales Dashboard)
    loadSalesDashboard();
}
     // Methods to load different content areas
    private void loadSalesDashboard() {
        // Clear current right panel (checkout) if any
        root.setRight(createCheckoutPanel());
        
        // Load sales dashboard content
        Node salesContent = createMainContent();
        setContent(salesContent);
        stage.setTitle("MQ POS System - Sales Dashboard");
    }
    
private void loadProductsManagement() {
    // Clear checkout panel when switching to non-sales views
    root.setRight(null);
    
    // Create product management content
    ProductManagement productManagement = new ProductManagement();
    Node productsContent = productManagement.createContent();
    
    // Set the content
    setContent(productsContent);
    stage.setTitle("MQ POS System - Products Management");
}

private void loadCustomersManagement() {
    // Clear checkout panel when switching to non-sales views
    root.setRight(null);
    
    // Create customers management content
    CustomersView customersManagement = new CustomersView();
    Node customersContent = customersManagement.getView();
    
    // Set the content
    setContent(customersContent);
    stage.setTitle("MQ POS System - Customers Management");
}

private void loadAuditTrail() {
    // Clear checkout panel when switching to non-sales views
    root.setRight(null);
    
    // Create audit trail content
    AuditTrailView auditTrail = new AuditTrailView();
    Node auditContent = auditTrail.getView();
    
    // Set the content
    setContent(auditContent);
    stage.setTitle("MQ POS System - Audit Trail");
}

private void loadSettings() {
    // Clear checkout panel when switching to non-sales views
    root.setRight(null);
    
    // Create settimgs content
    SettingsView settings = new SettingsView();
    Node settengsContent = settings.getView();
    
    // Set the content
    setContent(settengsContent );
    stage.setTitle("MQ POS System - Settings");
}


private void loadReports() {
    // Clear checkout panel when switching to non-sales views
    root.setRight(null);
    
    // Create point of sale content
    ReportsView reports = new  ReportsView();
    Node posReports = reports.getView();
    
    // Set the content
    setContent(posReports);
    stage.setTitle("MQ POS System - Reports");
}

private void loadTransactions() {
    // Clear checkout panel when switching to non-sales views
    root.setRight(null);
    
    // Create product management content
    TransactionsView transactions = new TransactionsView();
    Node productsContent = transactions.getView();
    
    // Set the content
    setContent(productsContent);
    stage.setTitle("MQ POS System - Transactions");
}
    
    // Helper method to set content
    private void setContent(Node content) {
        this.currentContent = content;
        root.setCenter(content);
    }  
    
    /**
     * Creates the top bar with system info and cashier details
     */
    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setSpacing(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        String tradingName = Helper.getTrading();
        // Logo/System name
        Label systemLabel = new Label(tradingName);
        systemLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        
        // Separator
        Separator separator1 = new Separator();
        separator1.setOrientation(javafx.geometry.Orientation.VERTICAL);
        separator1.setPrefHeight(30);
        
        // Transaction info
        VBox transactionInfo = new VBox(2);
        Label receiptLabel = new Label("Receipt #:");
        receiptLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        receiptNumberLabel = new Label(receiptNumber);
        receiptNumberLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        transactionInfo.getChildren().addAll(receiptLabel, receiptNumberLabel);
        
        // Date and time
        VBox dateTimeBox = new VBox(2);
        Label dateTimeTextLabel = new Label("Date & Time:");
        dateTimeTextLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        dateTimeLabel = new Label(getCurrentDateTime());
        dateTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        dateTimeBox.getChildren().addAll(dateTimeTextLabel, dateTimeLabel);
        
        // Till info
        String label = Helper.getTerminalLabel();
        VBox tillInfo = new VBox(2);
        Label tillTextLabel = new Label("Till:");
        tillTextLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        Label tillLabel = new Label(label);
        tillLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        tillInfo.getChildren().addAll(tillTextLabel, tillLabel);

        
        // Spacer to push cashier info to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Cashier info with avatar
        HBox cashierInfo = new HBox(10);
        cashierInfo.setAlignment(Pos.CENTER);
        
        // Avatar circle
        Circle avatar = new Circle(18);
        avatar.setFill(Color.valueOf("#3949ab"));
        Text initials = new Text(getInitials(currentCashier));
        initials.setFill(Color.WHITE);
        initials.setFont(Font.font("System", FontWeight.BOLD, 14));
        StackPane avatarPane = new StackPane(avatar, initials);
        
        // Cashier details
        VBox cashierDetails = new VBox(2);
        Label cashierLabel = new Label(role);
        cashierLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        cashierNameLabel = new Label(currentCashier);
        cashierNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        cashierDetails.getChildren().addAll(cashierLabel, cashierNameLabel);
        
        cashierInfo.getChildren().addAll(avatarPane, cashierDetails);
        
        // Create a refresh button to get latest configuration
        Button refreshConfigBtn = new Button("⟳ Refresh Config");
        refreshConfigBtn.setOnAction(e -> {
        String bearerToken = Helper.getToken();
        ApiClient apiClient = new ApiClient();
        boolean success = apiClient.fetchLatestConfig(bearerToken);

        Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(success ? "Success" : "Error");
        alert.setHeaderText(null);
        alert.setContentText(success ? 
        "Configuration refreshed successfully!" : 
        "Failed to refresh configuration. Check your connection or token.");
        alert.showAndWait();
        });
        
        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3949ab; -fx-font-weight: bold; -fx-cursor: hand;");
        logoutButton.setOnAction(e -> handleLogout());
        
        // Add all components to the top bar
        topBar.getChildren().addAll(systemLabel, separator1, transactionInfo, dateTimeBox, tillInfo, spacer, cashierInfo, refreshConfigBtn, logoutButton);
        
        return topBar;
    }
    
   /**
 * Creates the sidebar with main navigation options
 */
private VBox createSidebar() {
    VBox sidebar = new VBox();
    sidebar.setPrefWidth(220);
    sidebar.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a237e, #3949ab); -fx-padding: 0;");

    // Create the menu items
    sidebar.getChildren().add(createSidebarHeader());
    HBox salesMenuItem = createSidebarMenuItem("💳 New Sale", true);
    activeMenuItem = salesMenuItem;
    salesMenuItem.setOnMouseClicked(e -> {
        setActiveMenuItem(salesMenuItem, "New Sale");
        loadSalesDashboard();
    });
    sidebar.getChildren().add(salesMenuItem);

    HBox productsMenuItem = createSidebarMenuItem("📦 Products", false);
    productsMenuItem.setOnMouseClicked(e -> {
        setActiveMenuItem(productsMenuItem, "Products");
        loadProductsManagement();
    });
    sidebar.getChildren().add(productsMenuItem);

    HBox transactionsMenuItem = createSidebarMenuItem("📊 Transactions", false);
    transactionsMenuItem.setOnMouseClicked(e -> {
        setActiveMenuItem(transactionsMenuItem, "Transactions");
        loadTransactions();
    });
    sidebar.getChildren().add(transactionsMenuItem);

    HBox customersMenuItem = createSidebarMenuItem("👥 Customers", false);
    customersMenuItem.setOnMouseClicked(e -> {
        setActiveMenuItem(customersMenuItem, "Customers");
        loadCustomersManagement();
    });
    sidebar.getChildren().add(customersMenuItem);

    HBox reportsMenuItem = createSidebarMenuItem("📈 Reports", false);
    reportsMenuItem.setOnMouseClicked(e -> {
        setActiveMenuItem(reportsMenuItem, "Reports");
        loadReports();
    });
    sidebar.getChildren().add(reportsMenuItem);
    
    HBox auditTrailMenuItem = createSidebarMenuItem("🔍 Audit Trail", false);
    auditTrailMenuItem.setOnMouseClicked(e -> {
        setActiveMenuItem(auditTrailMenuItem, "Audit Trail");
        AuditLogger.log("View Audit Trail", "User accessed the Audit Trail page.");
        loadAuditTrail();
    });
    sidebar.getChildren().add(auditTrailMenuItem);

    HBox settingsMenuItem = createSidebarMenuItem("⚙️ Settings", false);
    settingsMenuItem.setOnMouseClicked(e -> {
        setActiveMenuItem(settingsMenuItem, "️Settings");
        loadSettings();
    });
    sidebar.getChildren().add(settingsMenuItem);

    // Add spacer to push help to bottom
    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);
    sidebar.getChildren().add(spacer);

    HBox helpMenuItem = createSidebarMenuItem("❓ Help & Support", false);
    helpMenuItem.setOnMouseClicked(e -> {
        setActiveMenuItem(helpMenuItem, "Help & Support");
        // loadHelp(); // Add this method if needed
    });
    sidebar.getChildren().add(helpMenuItem);

    return sidebar;
}

private VBox createSidebarHeader() {
    VBox header = new VBox();
    header.setAlignment(Pos.CENTER);
    header.setPadding(new Insets(25, 0, 25, 0));

    Rectangle placeholderLogo = new Rectangle(100, 50);
    placeholderLogo.setFill(Color.WHITE);
    placeholderLogo.setOpacity(0.9);
    placeholderLogo.setArcWidth(10);
    placeholderLogo.setArcHeight(10);

    Label logoText = new Label("MQ POS");
    logoText.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3949ab;");

    StackPane logoStack = new StackPane(placeholderLogo, logoText);
    header.getChildren().add(logoStack);

    return header;
}

private HBox createSidebarMenuItem(String text, boolean isSelected) {
    HBox menuItem = new HBox();
    menuItem.setAlignment(Pos.CENTER_LEFT);
    menuItem.setPadding(new Insets(15, 20, 15, 20));
    menuItem.setCursor(javafx.scene.Cursor.HAND);
    
    // Selection indicator (added at the beginning of the menu item)
    Rectangle indicator = new Rectangle(5, 25);
    indicator.setFill(Color.WHITE);
    indicator.setArcWidth(2);
    indicator.setArcHeight(2);
    indicator.setVisible(isSelected);
    
    // Menu text (now includes the emoji icon)
    Label menuText = new Label(text);
    menuText.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: " + (isSelected ? "bold" : "normal") + ";");
    
    // Adjust margins - removed icon margin, reduced text margin
    HBox.setMargin(menuText, new Insets(0, 0, 0, 10)); // Reduced from 15 to 10
    HBox.setMargin(indicator, new Insets(0, 10, 0, 0));
    
    // Add only indicator and text (removed the rectangle icon)
    menuItem.getChildren().addAll(indicator, menuText);
    
    // Background styling
    if (isSelected) {
        menuItem.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2);");
    } else {
        menuItem.setStyle("-fx-background-color: transparent;");
        menuItem.setOnMouseEntered(e -> menuItem.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1);"));
        menuItem.setOnMouseExited(e -> menuItem.setStyle("-fx-background-color: transparent;"));
    }
    
    return menuItem;
}


private void setActiveMenuItem(HBox newActiveItem, String labelText) {
    if (activeMenuItem != null) {
        activeMenuItem.setStyle("-fx-background-color: transparent;");

        Label oldLabel = (Label) activeMenuItem.getChildren().filtered(node -> node instanceof Label).get(0);
        oldLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: normal;");

        // Hide previous indicator
        Rectangle oldIndicator = (Rectangle) activeMenuItem.getChildren().filtered(node -> node instanceof Rectangle).get(0);
        oldIndicator.setVisible(false);
    }

    newActiveItem.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2);");

    Label newLabel = (Label) newActiveItem.getChildren().filtered(node -> node instanceof Label).get(0);
    newLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");

    Rectangle newIndicator = (Rectangle) newActiveItem.getChildren().filtered(node -> node instanceof Rectangle).get(0);
    newIndicator.setVisible(true);

    activeMenuItem = newActiveItem;
}

private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}    
    /**
     * Creates the main content area with product search and cart
     */
    private VBox createMainContent() {
        VBox mainContent = new VBox();
        mainContent.setPadding(new Insets(20));
        mainContent.setSpacing(20);
        
        // Product search section
        VBox searchSection = new VBox(10);
        
        // Title for the section
        Label searchTitle = new Label("Product Search");
        searchTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #424242;");
        
        // Barcode/product search with button
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        barcodeField = new TextField();
        barcodeField.setPromptText("Scan barcode or search product");
        barcodeField.setPrefHeight(40);
        barcodeField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                          "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: white;");
        HBox.setHgrow(barcodeField, Priority.ALWAYS);
        
        // Add 'Enter' key handler
        barcodeField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                addProductToCart();
            }
        });
        
        Button searchButton = new Button("Add Product");
        searchButton.setPrefHeight(40);
        searchButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; " +
                           "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; " +
                           "-fx-background-radius: 5px;");
        searchButton.setOnAction(e -> addProductToCart());
        
        searchBox.getChildren().addAll(barcodeField, searchButton);
        
        // Quick action buttons
        HBox quickActions = new HBox(10);
        
        Button scanButton = createActionButton("Scan", "#00796b");
        Button lookupButton = createActionButton("Product Lookup", "#0277bd");
        Button discountButton = createActionButton("Apply Discount", "#c62828");
        Button quantityButton = createActionButton("Change Quantity", "#ff8f00");
        
        discountButton.setOnAction(e -> applyDiscount());
        scanButton.setOnAction(e -> {
        showBarcodeScanner();
        });
        
        // Add this event handler to the quantityButton after it's created
     quantityButton.setOnAction(e -> {
    Product selectedProduct = cartTable.getSelectionModel().getSelectedItem();

    if (selectedProduct == null) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("No Item Selected");
        alert.setHeaderText(null);
        alert.setContentText("Please select a product from the cart to change its quantity.");
        alert.showAndWait();
        return;
    }

    TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedProduct.getQuantity()));
    dialog.setTitle("Change Quantity");
    dialog.setHeaderText("Product: " + selectedProduct.getName());
    dialog.setContentText("Enter new quantity:");

    TextField quantityField = dialog.getEditor();
    quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue.matches("\\d*")) {
            quantityField.setText(newValue.replaceAll("[^\\d]", ""));
        }
    });

    dialog.showAndWait().ifPresent(result -> {
        try {
            int newQuantity = Integer.parseInt(result);

            if (newQuantity < 0) {
                showAlert("Invalid Quantity", "Quantity must be zero or more.");
                return;
            }

          // 🔍 Validate stock ONLY for products (not services)
if (selectedProduct.isProduct()) {
    Product stockProduct = Helper.fetchProductByBarcode(selectedProduct.getBarcode());
    double stockQty = stockProduct != null ? stockProduct.getQuantity() : 0;

    if (newQuantity > stockQty) {
        showAlert("Stock Error", "Only " + stockQty + " in stock.");
        return;
    }
}

            if (newQuantity == 0) {
                removeItemFromCart(selectedProduct);
            } else {
                selectedProduct.setQuantity(newQuantity);
            }

            cartTable.refresh();
            updateTotals();

        } catch (NumberFormatException ex) {
            showAlert("Invalid Input", "Please enter a valid number.");
        }
    });
});       
 // Set action to open the Product Lookup dialog
 lookupButton.setOnAction(e -> {
    ProductLookupDialog lookupDialog = new ProductLookupDialog(stage);
    Product selectedProduct = lookupDialog.showAndSelect();

    if (selectedProduct == null) return;

    String barcode = selectedProduct.getBarcode();

    // 🔒 Normal product flow (with stock validation)
    Product stockProduct = Helper.fetchProductByBarcode(barcode);
    if (stockProduct == null) {
        showAlert("Error", "Product not found in database.");
        return;
    }

    double stockQty = stockProduct.getQuantity();
    if (stockQty <= 0) {
        showAlert("Out of Stock", "Item is out of stock.");
        return;
    }

    Product existingItem = null;
    double cartQty = 0;

    for (Product item : cartItems) {
        if (barcode.equals(item.getBarcode())) {
            existingItem = item;
            cartQty = item.getQuantity();
            break;
        }
    }

    if (cartQty + 1 > stockQty) {
        showAlert("Stock Limit", "Only " + stockQty + " in stock.");
        return;
    }

    if (existingItem != null) {
        existingItem.setQuantity(existingItem.getQuantity() + 1);
        existingItem.updateTotal();
    } else {
        selectedProduct.setQuantity(1);
        cartItems.add(selectedProduct);
    }

    cartTable.refresh();
    updateTotals();
    barcodeField.clear();
    barcodeField.requestFocus();
});

       
        quickActions.getChildren().addAll(scanButton, lookupButton, discountButton, quantityButton);
        
        searchSection.getChildren().addAll(searchTitle, searchBox, quickActions);
        
        // Cart section
        VBox cartSection = new VBox(10);
        VBox.setVgrow(cartSection, Priority.ALWAYS);
        
        // Title with counter
        HBox cartHeader = new HBox(10);
        cartHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label cartTitle = new Label("Shopping Cart");
        cartTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #424242;");
        
        Label itemCountLabel = new Label("(0 items)");
        itemCountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        Button clearCartButton = new Button("Clear Cart");
        clearCartButton.setStyle("-fx-background-color: transparent; -fx-border-color: #c62828; " +
                               "-fx-text-fill: #c62828; -fx-cursor: hand; -fx-background-radius: 5px; -fx-border-radius: 5px;");
        clearCartButton.setOnAction(e -> clearCart());
        
        cartHeader.getChildren().addAll(cartTitle, itemCountLabel, headerSpacer, clearCartButton);
        
        // Cart table
        cartTable = new TableView<>();
        cartTable.setItems(cartItems);
        cartTable.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");
        VBox.setVgrow(cartTable, Priority.ALWAYS);
        
        // Define table columns
        TableColumn<Product, Integer> idCol = new TableColumn<>("Barcode");
        idCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        idCol.setPrefWidth(95);
        
        TableColumn<Product, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
       TableColumn<Product, String> rateCol = new TableColumn<>("Tax Rate");
       rateCol.setCellValueFactory(new PropertyValueFactory<>("taxRate"));
       rateCol.setPrefWidth(60);

       TableColumn<Product, String> unitCol = new TableColumn<>("Unit of Measure");
       unitCol.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasure"));
       unitCol.setPrefWidth(250);

        
TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
priceCol.setCellValueFactory(cellData -> 
    cellData.getValue().priceProperty().asObject()
);
priceCol.setPrefWidth(120);

priceCol.setCellFactory(col -> new TableCell<Product, Double>() {
  @Override
protected void updateItem(Double price, boolean empty) {
    super.updateItem(price, empty);
    if (empty || price == null) {
        setText(null);
        setOnMouseClicked(null);
        setStyle("");
        return;
    }
    
    Product product = getTableRow().getItem(); // safer way to get product
    if (product == null) return;
    
    setText(formatCurrency(product.getPrice())); // read directly from product, not the 'price' parameter
    
    if (!product.isProduct()) {
        setStyle("-fx-text-fill: #1565c0; -fx-font-weight: bold;");
        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Double newPrice = promptForServicePrice(product);
                if (newPrice != null) {
                    product.setPrice(newPrice);
                    cartTable.refresh();
                    updateTotals();
                }
            }
        });
    } else {
        setStyle("");
        setOnMouseClicked(null);
    }
}
});

        
        TableColumn<Product, Double> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(70);
        
       TableColumn<Product, Double> totalCol = new TableColumn<>("Total");
totalCol.setCellValueFactory(cellData ->
    cellData.getValue().totalProperty().asObject()
);
totalCol.setPrefWidth(140);

totalCol.setCellFactory(col -> new TableCell<Product, Double>() {
    @Override
    protected void updateItem(Double total, boolean empty) {
        super.updateItem(total, empty);
        if (empty || total == null) {
            setText(null);
        } else {
            setText(formatCurrency(total));
        }
    }
});
        
        TableColumn<Product, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(col -> new TableCell<Product, Void>() {
            private final Button deleteButton = new Button("Remove");
            
            {
                deleteButton.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3px;");
                deleteButton.setOnAction(event -> {
                    Product item = getTableView().getItems().get(getIndex());
                    removeItemFromCart(item);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
        
        cartTable.getColumns().addAll(idCol, nameCol,rateCol, unitCol, priceCol, qtyCol, totalCol, actionCol);
        
        cartSection.getChildren().addAll(cartHeader, cartTable);
        
        // Add all sections to main content
        mainContent.getChildren().addAll(searchSection, cartSection);
        VBox.setVgrow(cartSection, Priority.ALWAYS);
        
        return mainContent;
    }
    
private VBox createCheckoutPanel() {
    // Get screen size for responsive calculations
    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
    double screenWidth = screenBounds.getWidth();

    // Calculate responsive panel width
    double panelWidth;
    if (screenWidth <= 1024) {
        panelWidth = Math.max(screenWidth * 0.25, 250);
    } else if (screenWidth <= 1440) {
        panelWidth = Math.max(screenWidth * 0.22, 300);
    } else {
        panelWidth = Math.max(screenWidth * 0.20, 320);
    }

    VBox checkoutPanel = new VBox(15);
    checkoutPanel.setPrefWidth(panelWidth);
    checkoutPanel.setMinWidth(panelWidth);
    checkoutPanel.setMaxWidth(panelWidth);

    double padding = Math.max(panelWidth * 0.06, 15);
    checkoutPanel.setPadding(new Insets(padding, padding, padding, 0));

    // ===== CARD =====
    VBox card = new VBox(15);
    card.setPadding(new Insets(padding));
    card.setStyle("-fx-background-color: white; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");

    double titleFontSize = Math.max(panelWidth * 0.06, 16);
    double labelFontSize = Math.max(panelWidth * 0.047, 13);
    double inputHeight = Math.max(panelWidth * 0.12, 35);

    // Payment summary title
    Label summaryTitle = new Label("Payment Summary");
    summaryTitle.setStyle(String.format("-fx-font-size: %.0fpx; -fx-font-weight: bold; -fx-text-fill: #424242;", titleFontSize));

    Separator separator = new Separator();

    // Customer Info Section
    VBox customerInfoBox = new VBox(8);
    Label customerInfoLabel = new Label("Customer Information");
    customerInfoLabel.setStyle(String.format("-fx-font-size: %.0fpx; -fx-font-weight: bold; -fx-text-fill: #424242;", labelFontSize));

    customerNamesField = new TextField();
    customerNamesField.setPromptText("Enter customer name");
    customerNamesField.setPrefHeight(inputHeight);
    customerNamesField.setStyle(String.format("-fx-font-size: %.0fpx; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: white;", labelFontSize));

    tinField = new TextField();
    tinField.setPromptText("Enter Tax Identification Number");
    tinField.setPrefHeight(inputHeight);
    tinField.setStyle(String.format("-fx-font-size: %.0fpx; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: white;", labelFontSize));

    buyerAuthField = new TextField();
    buyerAuthField.setPromptText("Enter Buyer Authorization Code");
    buyerAuthField.setPrefHeight(inputHeight);
    buyerAuthField.setStyle(String.format("-fx-font-size: %.0fpx; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
            "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: white;", labelFontSize));
    buyerAuthField.setVisible(false);
    buyerAuthField.setManaged(false);

    tinField.textProperty().addListener((obs, oldVal, newVal) -> {
        boolean isBusiness = newVal != null && !newVal.trim().isEmpty();
        buyerAuthField.setVisible(isBusiness);
        buyerAuthField.setManaged(isBusiness);
        if (!isBusiness) buyerAuthField.clear();
    });

    customerInfoBox.getChildren().addAll(customerInfoLabel, customerNamesField, tinField, buyerAuthField);

    // ===== SUMMARY GRID =====
    GridPane summaryGrid = new GridPane();
    summaryGrid.setVgap(12);
    summaryGrid.setHgap(10);
    int row = 0;

    Label subtotalLabel = new Label("Subtotal:");
    subtotalLabel.setStyle(String.format("-fx-font-size: %.0fpx; -fx-text-fill: #757575;", labelFontSize));
    subtotalValueLabel = new Label(formatCurrency(0.0));
    subtotalValueLabel.setStyle(String.format("-fx-font-size: %.0fpx; -fx-text-fill: #424242; -fx-font-weight: bold;", labelFontSize));
    summaryGrid.add(subtotalLabel, 0, row);
    summaryGrid.add(subtotalValueLabel, 1, row++);
    
    Label discountLabel = new Label("Discount:");
    discountLabel.setStyle(String.format("-fx-font-size: %.0fpx; -fx-text-fill: #757575;", labelFontSize));
    discountValueLabel = new Label(formatCurrency(0.0));
    discountValueLabel.setStyle(String.format("-fx-font-size: %.0fpx; -fx-text-fill: #c62828; -fx-font-weight: bold;", labelFontSize));
    summaryGrid.add(discountLabel, 0, row);
    summaryGrid.add(discountValueLabel, 1, row++);

    taxLabel = new Label("VAT Amount:");
    taxLabel.setStyle(String.format("-fx-font-size: %.0fpx; -fx-text-fill: #757575;", labelFontSize));
    taxValueLabel = new Label(formatCurrency(0.0));
    taxValueLabel.setStyle(String.format("-fx-font-size: %.0fpx; -fx-text-fill: #424242; -fx-font-weight: bold;", labelFontSize));
    summaryGrid.add(taxLabel, 0, row);
    summaryGrid.add(taxValueLabel, 1, row++);
    Label levyLabel = new Label("Levies:");
    levyLabel.setStyle(String.format("-fx-font-size: %.0fpx; -fx-text-fill: #757575;", labelFontSize));

    leviesValueLabel = new Label(formatCurrency(0.0));
    leviesValueLabel.setStyle(String.format("-fx-font-size: %.0fpx; -fx-text-fill: #424242; -fx-font-weight: bold;", labelFontSize));

    summaryGrid.add(levyLabel, 0, row);
    summaryGrid.add(leviesValueLabel, 1, row++);


    Separator totalSeparator = new Separator();
    GridPane.setColumnSpan(totalSeparator, 2);
    summaryGrid.add(totalSeparator, 0, row++);

    Label totalLabel = new Label("Total:");
    totalLabel.setStyle(String.format("-fx-font-size: %.0fpx; -fx-font-weight: bold; -fx-text-fill: #424242;", titleFontSize));
    totalAmountLabel = new Label(formatCurrency(0.0));
    totalAmountLabel.setStyle(String.format("-fx-font-size: %.0fpx; -fx-font-weight: bold; -fx-text-fill: #1a237e;", titleFontSize * 1.1));
    summaryGrid.add(totalLabel, 0, row);
    summaryGrid.add(totalAmountLabel, 1, row);

    ColumnConstraints col1 = new ColumnConstraints();
    col1.setHgrow(Priority.ALWAYS);
    ColumnConstraints col2 = new ColumnConstraints();
    col2.setHalignment(HPos.RIGHT);
    summaryGrid.getColumnConstraints().addAll(col1, col2);

    // ===== PAYMENT METHOD =====
    VBox paymentMethodBox = new VBox(8);
    Label paymentMethodLabel = new Label("Payment Method");
    paymentMethodLabel.setStyle(String.format("-fx-font-size: %.0fpx; -fx-font-weight: bold; -fx-text-fill: #424242;", labelFontSize));

    paymentMethodComboBox = new ComboBox<>();
    paymentMethodComboBox.getItems().addAll("Cash", "Credit Card", "Debit Card", "Invoice", "Cheque", "Mobile Payment", "Mixed Payment");
    paymentMethodComboBox.setValue("Cash");
    paymentMethodComboBox.setPrefWidth(Double.MAX_VALUE);
    paymentMethodComboBox.setPrefHeight(inputHeight);
    paymentMethodComboBox.setStyle(String.format("-fx-font-size: %.0fpx; -fx-background-radius: 5px;", labelFontSize));

    paymentMethodBox.getChildren().addAll(paymentMethodLabel, paymentMethodComboBox);

    VBox cashAmountBox = createCashAmountSection();

    card.getChildren().addAll(summaryTitle, separator, customerInfoBox, summaryGrid, paymentMethodBox, cashAmountBox);

    // ===== SECONDARY ACTION BUTTONS =====
    VBox customerActionsPanel = new VBox(10);
    customerActionsPanel.setPadding(new Insets(padding, 0, 0, 0));

    Label customerActionsTitle = new Label("Customer Actions");
    customerActionsTitle.setStyle(String.format("-fx-font-size: %.0fpx; -fx-font-weight: bold; -fx-text-fill: #424242;", titleFontSize));
    
    Button validateVat5Button = createResponsiveSecondaryActionButton("Validate VAT5", labelFontSize, inputHeight * 0.8);
    Button addCustomerButton = createResponsiveSecondaryActionButton("Add Customer", labelFontSize, inputHeight * 0.8);
    Button addNoteButton = createResponsiveSecondaryActionButton("Add Note", labelFontSize, inputHeight * 0.8);
    Button holdSaleButton = createResponsiveSecondaryActionButton("Hold Sale", labelFontSize, inputHeight * 0.8);
    Button viewHeldSalesButton = createResponsiveSecondaryActionButton("View Held Sales", labelFontSize, inputHeight * 0.8);
    Button printReceiptButton = createResponsiveSecondaryActionButton("Print Preview", labelFontSize, inputHeight * 0.8);

    holdSaleButton.setOnAction(e -> holdSale());
    validateVat5Button.setOnAction(e -> showVat5ValidationDialog());
    viewHeldSalesButton.setOnAction(e -> showHeldSales());
    addCustomerButton.setOnAction(e -> addCustomer());
    printReceiptButton.setOnAction(e -> showPrintPreview());
    addNoteButton.setOnAction(e -> addNote());

    // --- Grid layout for small screens (2 buttons per row) ---
    if (screenWidth <= 1024) {
        GridPane actionGrid = new GridPane();
        actionGrid.setHgap(10);
        actionGrid.setVgap(10);

        actionGrid.add(addCustomerButton, 0, 0);
        actionGrid.add(addNoteButton, 1, 0);
        actionGrid.add(holdSaleButton, 0, 1);
        actionGrid.add(viewHeldSalesButton, 1, 1);
         actionGrid.add(validateVat5Button, 0, 2);
        actionGrid.add(printReceiptButton, 0, 2, 2, 1); //full width

        customerActionsPanel.getChildren().addAll(customerActionsTitle, actionGrid);
    } else {
        customerActionsPanel.getChildren().addAll(customerActionsTitle, addCustomerButton, addNoteButton, holdSaleButton, viewHeldSalesButton, validateVat5Button, printReceiptButton);
    }

    // ===== SCROLLABLE CONTENT =====
    VBox scrollContent = new VBox(20, card, customerActionsPanel);
    ScrollPane scrollPane = new ScrollPane(scrollContent);
    scrollPane.setFitToWidth(true);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setStyle("-fx-background-color: transparent;");

    // ===== PROCESS PAYMENT BUTTON =====
    double buttonHeight = Math.max(panelWidth * 0.17, 45);
    processPaymentButton = new Button("PROCESS PAYMENT");
    processPaymentButton.setPrefHeight(buttonHeight);
    processPaymentButton.setPrefWidth(Double.MAX_VALUE);
    processPaymentButton.setDisable(true);
    processPaymentButton.setStyle(String.format(
    "-fx-background-color: #1a237e; -fx-text-fill: white; -fx-font-size: %.0fpx; -fx-font-weight: bold; -fx-background-radius: 5px;",
    titleFontSize));

    processPaymentButton.setCursor(Cursor.HAND);

    processPaymentButton.setOnMouseEntered(e -> processPaymentButton.setStyle(String.format(
    "-fx-background-color: #3949ab; -fx-text-fill: white; -fx-font-size: %.0fpx; -fx-font-weight: bold; -fx-background-radius: 5px;",
    titleFontSize)));

    processPaymentButton.setOnMouseExited(e -> processPaymentButton.setStyle(String.format(
    "-fx-background-color: #1a237e; -fx-text-fill: white; -fx-font-size: %.0fpx; -fx-font-weight: bold; -fx-background-radius: 5px;",
    titleFontSize)));

    processPaymentButton.setOnAction(e -> processPayment());


    // ===== FINAL LAYOUT =====
    VBox finalLayout = new VBox();
    VBox.setVgrow(scrollPane, Priority.ALWAYS);
    finalLayout.getChildren().addAll(scrollPane, processPaymentButton);

    checkoutPanel.getChildren().add(finalLayout);

    return checkoutPanel;
}

private Double promptForServicePrice(Product service) {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Enter Price");
    dialog.setHeaderText("Service: " + service.getName());
    dialog.setContentText("Enter price:");

    TextField priceField = dialog.getEditor();
    priceField.textProperty().addListener((obs, oldV, newV) -> {
        if (!newV.matches("\\d*(\\.\\d{0,2})?")) {
            priceField.setText(oldV);
        }
    });

    return dialog.showAndWait()
            .map(val -> {
                try {
                    double p = Double.parseDouble(val);
                    return p > 0 ? p : null;
                } catch (Exception e) {
                    return null;
                }
            })
            .orElse(null);
}

private void showVat5ValidationDialog() {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("VAT5 Certificate Validation");
    dialog.setHeaderText("Enter VAT5 Certificate Details");

    // Create form
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    vat5ProjectField = new TextField();
    vat5ProjectField.setPromptText("Project Number");
    vat5ProjectField.setPrefWidth(250);

    vat5CertificateField = new TextField();
    vat5CertificateField.setPromptText("Certificate Number");
    vat5CertificateField.setPrefWidth(250);

    // Calculate total quantity from cart
    double totalQuantity = cartItems.stream()
        .mapToDouble(Product::getQuantity)
        .sum();
    
    TextField quantityField = new TextField(String.valueOf(totalQuantity));
    quantityField.setPromptText("Quantity");
    quantityField.setDisable(true); // Auto-calculated
    quantityField.setPrefWidth(250);

    Label statusLabel = new Label("");
    statusLabel.setStyle("-fx-font-weight: bold;");

    grid.add(new Label("Project Number:"), 0, 0);
    grid.add(vat5ProjectField, 1, 0);
    grid.add(new Label("Certificate Number:"), 0, 1);
    grid.add(vat5CertificateField, 1, 1);
    grid.add(new Label("Quantity:"), 0, 2);
    grid.add(quantityField, 1, 2);
    grid.add(statusLabel, 0, 3, 2, 1);

    dialog.getDialogPane().setContent(grid);

    ButtonType validateButtonType = new ButtonType("Validate", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(validateButtonType, cancelButtonType);

    Button validateButton = (Button) dialog.getDialogPane().lookupButton(validateButtonType);
    validateButton.setDisable(true);

    // Enable validate button only when both fields have text
    vat5ProjectField.textProperty().addListener((obs, oldVal, newVal) -> {
        validateButton.setDisable(newVal.trim().isEmpty() || vat5CertificateField.getText().trim().isEmpty());
    });
    vat5CertificateField.textProperty().addListener((obs, oldVal, newVal) -> {
        validateButton.setDisable(newVal.trim().isEmpty() || vat5ProjectField.getText().trim().isEmpty());
    });

    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == validateButtonType) {
            return validateButtonType;
        }
        return null;
    });

    Optional<ButtonType> result = dialog.showAndWait();
    
    if (result.isPresent() && result.get() == validateButtonType) {
        String projectNum = vat5ProjectField.getText().trim();
        String certNum = vat5CertificateField.getText().trim();
        
        // Show loading indicator
        statusLabel.setText("Validating...");
        statusLabel.setStyle("-fx-text-fill: #1a237e; -fx-font-weight: bold;");
        String bearerToken = Helper.getToken(); 
        
         ApiClient apiClient = new ApiClient();
        // Call API
        apiClient.validateVat5Certificate(projectNum, certNum, totalQuantity, 
            bearerToken, 
            vat5Data -> handleVat5ValidationResult(vat5Data, dialog, statusLabel));
    }
}

private void handleVat5ValidationResult(Vat5Data vat5Data, Dialog<ButtonType> dialog, Label statusLabel) {
    if (vat5Data != null && vat5Data.isValid) {
        // Certificate is valid - apply VAT exemptions
        isVat5Exempt = true;
        currentVat5Data = vat5Data;
        
        statusLabel.setText("✓ Valid Certificate - VAT Exempted");
        statusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        
        // Update totals to remove VAT
        updateTotals();
        
        // Show success message
        Platform.runLater(() -> {
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("VAT5 Validation Success");
            successAlert.setHeaderText("Certificate Validated Successfully");
            successAlert.setContentText(
                "Project: " + vat5Data.projectNumber + "\n" +
                "Certificate: " + vat5Data.vat5CertificateNumber + "\n" +
                "VAT has been exempted for this transaction."
            );
            successAlert.showAndWait();
            dialog.close();
        });
    } else {
        // Certificate is invalid
        isVat5Exempt = false;
        currentVat5Data = null;
        
        statusLabel.setText("✗ Invalid Certificate");
        statusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        
        Platform.runLater(() -> {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("VAT5 Validation Failed");
            errorAlert.setHeaderText("Certificate Validation Failed");
            errorAlert.setContentText("The VAT5 certificate is invalid or has expired.");
            errorAlert.showAndWait();
        });
    }
}

/**
 * Helper method to create responsive secondary action buttons - maintains original style
 */
private Button createResponsiveSecondaryActionButton(String text, double fontSize, double height) {
    Button button = new Button(text);
    button.setPrefHeight(height);
    button.setMaxWidth(Double.MAX_VALUE);
    button.setStyle(String.format("-fx-background-color: white; -fx-text-fill: #3949ab; " +
                   "-fx-font-size: %.0fpx; -fx-font-weight: bold; -fx-cursor: hand; " +
                   "-fx-background-radius: 5px; -fx-border-color: #3949ab; -fx-border-radius: 5px;", fontSize));
    
    return button;
}
    /**
     * Creates a styled action button for quick actions
     */
    private Button createActionButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefHeight(35);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                      "-fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; " +
                      "-fx-background-radius: 5px;");
        HBox.setHgrow(button, Priority.ALWAYS);
        
        return button;
    }
    
    /**
     * Adds a product to the cart based on barcode input
    */
   private void addProductToCart() {
    String barcode = barcodeField.getText().trim();

    if (barcode.isEmpty()) {
        showAlert("Error", "Please enter a product barcode or search term.");
        return;
    }

    // Fetch product from DB
    Product newItem = Helper.fetchProductByBarcode(barcode);

    if (newItem == null) {
        showAlert("Error", "Product not found for barcode: " + barcode);
        return;
    }

    // Check if item already exists in cart
    boolean found = false;
    for (Product item : cartItems) {
        if (item.getBarcode().equals(newItem.getBarcode())) {
            // Check available stock
            if (item.getQuantity() + 1 > newItem.getQuantity()) {
                showAlert("Out of Stock", "Cannot add more. Only " + newItem.getQuantity() + " available in stock.");
                return;
            }

            item.setQuantity(item.getQuantity() + 1);
            item.updateTotal();
            found = true;
            break;
        }
    }
    if (!found) {
        if (newItem.getQuantity() < 1) {
            showAlert("Out of Stock", "This product is currently out of stock.");
            return;
        }
        newItem.setQuantity(1);
        newItem.updateTotal();
        cartItems.add(newItem);
    }

    cartTable.refresh();
    updateTotals();
    barcodeField.clear();
    barcodeField.requestFocus();
}

    /**
     * Removes an item from the cart
     */
    private void removeItemFromCart(Product item) {
        cartItems.remove(item);
        updateTotals();
    }
    
    /**
     * Clears all items from the cart
     */
    private void clearCart() {
        // Confirm before clearing
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Cart");
        alert.setHeaderText("Clear Shopping Cart");
        alert.setContentText("Are you sure you want to clear all items from the cart?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            cartItems.clear();
            updateTotals();
        }
    }
    
/**
 * Updates the total amounts in the checkout panel and refreshes the change calculation
*/
private void updateTotals() {
    double subtotal = 0.0;        // VAT-inclusive subtotal (before cart discount)
    double totalTax = 0.0;
    double totalLevies = 0.0;
    int totalQuantity = 0;
    double itemLevelDiscountTotal = 0.0;

    // Step 1: Item-level discounts (kept as requested)
    for (Product item : cartItems) {
        double discount = item.getDiscount();

        if (discount > 0) {
            applyDiscountToItem(item, discount, false);
        }

        double itemPrice = item.getPrice();       // VAT-inclusive price
        double itemQuantity = item.getQuantity();
        double itemSubtotal = itemPrice * itemQuantity;

        // Track item-level discount total
        double discountPerItem = 0.0;
        if (discount > 0) {
            double originalPrice = item.getOriginalPrice();
            discountPerItem = (originalPrice - itemPrice) * itemQuantity;
        }
        itemLevelDiscountTotal += discountPerItem;

        subtotal += itemSubtotal;
        totalQuantity += (int) itemQuantity;
    }

    // Step 2: Flat cart discount (always flat as you said)
    double cartLevelDiscount = 0.0;
    if (cartDiscountAmount > 0) {
        cartLevelDiscount = Math.min(cartDiscountAmount, subtotal);
    }

    double discountedSubtotal = subtotal - cartLevelDiscount;

    // Step 3: VAT after discount (VAT extracted from discounted amounts)
    boolean isVATRegistered = Helper.isVATRegistered();
    for (Product item : cartItems) {
        double itemGross = item.getPrice() * item.getQuantity();
        double taxRate = Helper.getTaxRateById(item.getTaxRate());

        if (isVATRegistered && !isVat5Exempt) {
            double proportion = subtotal == 0 ? 0 : itemGross / subtotal;
            double itemDiscountShare = cartLevelDiscount * proportion;
            double itemDiscountedGross = itemGross - itemDiscountShare;

            double itemVAT = (itemDiscountedGross * taxRate) / (100 + taxRate);
            totalTax += itemVAT;
        }
    }

    // Step 4: Levies (VAT-excluded, item-based)
    List<LevyDto> levies = Helper.getActiveLevies();
    if (!cartItems.isEmpty()) {
        for (LevyDto levy : levies) {

            double levyAmount = 0.0;

            for (Product item : cartItems) {
                double itemGross = item.getPrice() * item.getQuantity();
                double taxRate = Helper.getTaxRateById(item.getTaxRate());

                double itemVAT = 0.0;
                if (isVATRegistered && !isVat5Exempt) {
                    itemVAT = (itemGross * taxRate) / (100 + taxRate);
                }

                double preVatAmount = itemGross - itemVAT; // VAT-excluded base

                if ("PERCENTAGE".equalsIgnoreCase(levy.getChargeMode())) {
                    levyAmount += (preVatAmount * levy.getRate()) / 100.0;
                } else if ("Item".equalsIgnoreCase(levy.getChargeMode())) {
                    levyAmount += levy.getRate() * item.getQuantity();
                }
            }

            totalLevies += levyAmount;
        }
    }

    // Step 5: Totals
    double totalDiscount = itemLevelDiscountTotal + cartLevelDiscount;
    double total = discountedSubtotal + totalLevies;

    // Update UI
    subtotalValueLabel.setText(formatCurrency(discountedSubtotal - totalTax));
    taxValueLabel.setText(formatCurrency(totalTax));
    discountValueLabel.setText(formatCurrency(totalDiscount));
    leviesValueLabel.setText(formatCurrency(totalLevies));
    totalAmountLabel.setText(formatCurrency(total));

    totalAmount = total;

    ((Label) ((HBox) ((VBox) cartTable.getParent()).getChildren().get(0)).getChildren().get(1))
        .setText("(" + totalQuantity + " item" + (totalQuantity == 1 ? "" : "s") + ")");

    updateChangeCalculation();
}
/**
 * Updates the change calculation and payment button state
 */
private void updateChangeCalculation() {
    // Get the cash amount from the text field
    String cashText = cashAmountField.getText();
    double cashAmount = 0.0;
    
    try {
        if (!cashText.isEmpty()) {
            cashAmount = Double.parseDouble(cashText);
        }
    } catch (NumberFormatException e) {
        // Invalid input, treat as zero
    }
    
    // Calculate change
    double change = cashAmount - totalAmount;
    
    // Update change label
    changeValueLabel.setText(formatCurrency(Math.max(0, change)));
    
    // Enable/disable process payment button based on whether customer has provided enough cash
    boolean isSufficientPayment = cashAmount >= totalAmount && totalAmount > 0;
    processPaymentButton.setDisable(!isSufficientPayment);
    doneButton.setDisable(!isSufficientPayment);
    
    // Optionally change the button style when disabled
    if (isSufficientPayment) {
        processPaymentButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                                  "-fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; " +
                                  "-fx-background-radius: 5px;");
    } else {
        processPaymentButton.setStyle("-fx-background-color: #9e9e9e; -fx-text-fill: white; " +
                                  "-fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: default; " +
                                  "-fx-background-radius: 5px;");
    }
}    
 private void processPayment() {
    boolean isBusiness = tinField.getText() != null && !tinField.getText().trim().isEmpty();
    String authCode = buyerAuthField.getText();

    if (isBusiness) {
        if (authCode == null || authCode.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Authorization Required");
            alert.setHeaderText(null);
            alert.setContentText("Business purchase requires buyer authorization code. Please enter it to proceed.");
            alert.showAndWait();
            buyerAuthField.requestFocus();
            return;
        }
        
        String bearerToken = Helper.getToken();     

        // Validate authorization code before proceeding
        ApiClient apiClient = new ApiClient();
        apiClient.validateAuthorizationCode(authCode, bearerToken, isValid -> {
            if (!isValid) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Authorization Code");
                alert.setHeaderText(null);
                alert.setContentText("The authorization code provided is invalid. Please check and try again.");
                alert.showAndWait();
                buyerAuthField.requestFocus();
                return;
            }

            // Continue if valid
            continueAfterValidation();
        });

        return; // prevent fallthrough while async runs
    }

    // If not business, continue immediately
    continueAfterValidation();
}

 private void continueAfterValidation() {
    double offlineThreshold = Helper.getOfflineTransactionLimit();
    LocalDateTime firstUnSyncTime = Helper.getLastSuccessfulSyncTimeFromInvoices();
    double currentOfflineAmount = firstUnSyncTime != null
        ? java.time.Duration.between(firstUnSyncTime, LocalDateTime.now()).toHours()
        : 0;

    if (currentOfflineAmount >= offlineThreshold) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Offline Limit Reached");
            alert.setHeaderText(null);
            alert.setContentText("⚠️ Offline transaction time limit reached. Please sync before continuing.");
            alert.showAndWait();
        });
        return;
    }

    Helper.checkAndHandleTerminalBlocking(isAllowed -> {
        if (isAllowed) {
            Platform.runLater(this::proceedWithPayment);
        }
    });
}

    private void proceedWithPayment() {
    
    String selectedPaymentMethod = paymentMethodComboBox.getValue();
    String buyerTIN = tinField.getText().trim();
    String buyersName = customerNamesField.getText().trim();
    String amountTenderedText = cashAmountField.getText().trim();
    double amountTendered = Double.parseDouble(amountTenderedText);
    String changeValueText = changeValueLabel.getText();
    changeValueText = changeValueText.replace("MK", "").replace(",", "").trim();
    double changeValue = Double.parseDouble(changeValueText);
    String buyersTIN = !buyerTIN.isEmpty() ? buyerTIN : "";

    String buyerName = !buyersName.isEmpty() ? buyersName : "";
    
    if (cartItems.isEmpty()) {
        showAlert("Error", "Cart is empty. Please add items before processing payment.");
        return;
    }        
    
    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmAlert.setTitle("Confirm Payment");
    confirmAlert.setHeaderText("Proceed with Payment?");
    
    // Add VAT5 exemption notice to confirmation
    String confirmMessage = "Do you want to continue with " 
        + selectedPaymentMethod + " payment for " + formatCurrency(totalAmount) + "?";
    
    if (isVat5Exempt && currentVat5Data != null) {
        confirmMessage += "\n\n✓ VAT5 Certificate Applied"
            + "\nProject: " + currentVat5Data.projectNumber
            + "\nCertificate: " + currentVat5Data.vat5CertificateNumber
            + "\n\nVAT has been exempted for this transaction.";
    }
    
    confirmAlert.setContentText(confirmMessage);

    ButtonType okButton = new ButtonType("Proceed", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

    confirmAlert.getButtonTypes().setAll(okButton, cancelButton);

    Optional<ButtonType> result = confirmAlert.showAndWait();
    if (result.isEmpty() || result.get() == cancelButton) {
        System.out.println("❌ Payment cancelled by user.");
        return;
    }

    String invoiceNumber = generateNewReceiptNumber();
    String buyerAuthorizationCode = buyerAuthField.getText().trim();
    
    // 1. Build invoice header
    InvoiceHeader invoiceHeader = Helper.getInvoiceHeader(invoiceNumber, buyerTIN, buyerAuthorizationCode, selectedPaymentMethod);
    
    // ADD VAT5 CERTIFICATE INFO TO INVOICE HEADER IF EXEMPT
    if (isVat5Exempt && currentVat5Data != null) {
    Vat5CertificateDetails vat5Details = new Vat5CertificateDetails();
    vat5Details.projectNumber = currentVat5Data.projectNumber;
    vat5Details.certificateNumber = currentVat5Data.vat5CertificateNumber;
    vat5Details.quantity = currentVat5Data.quantity;
    
    invoiceHeader.setVat5CertificateDetails(vat5Details);
}

    // 2. Convert products to line items WITH VAT5 EXEMPTION APPLIED
    List<LineItemDto> lineItems = createLineItems(); // Use new method

    // 3. Build invoice summary WITH CORRECT VAT CALCULATION
    InvoiceSummary invoiceSummary = new InvoiceSummary();
    List<TaxBreakDown> taxBreakdowns = Helper.generateTaxBreakdown(lineItems);
    
    // Calculate totals from line items (which already have VAT exemption applied)
    double totalVATAmount = lineItems.stream().mapToDouble(LineItemDto::getTotalVAT).sum();
    double invoiceTotal = lineItems.stream().mapToDouble(LineItemDto::getTotal).sum();
    
    //Including Levies
    //Including Levies (VAT-excluded)
List<LevyDto> activeLevies = Helper.getActiveLevies();
List<LevyBreakDownDto> levyBreakDowns = new ArrayList<>();
double totalLevies = 0.0;

for (LevyDto levy : activeLevies) {
    LevyBreakDownDto levyDto = new LevyBreakDownDto();
    levyDto.setLevyTypeId(levy.getId());
    levyDto.setLevyRate(levy.getRate());

    double levyAmount = 0.0;

    for (LineItemDto item : lineItems) {
        double lineTotal = item.getTotal();        // price including VAT
        double vatRate = item.getTotalVAT();        // e.g., 16%
        
        // Extract pre-VAT base
        double preVatAmount = lineTotal - vatRate; // formula: preVAT = Total * 100 / (100 + Rate)
        
        // Apply levy on pre-VAT base
        double itemLevy = "PERCENTAGE".equalsIgnoreCase(levy.getChargeMode()) ?
                          (preVatAmount * levy.getRate()) / 100.0 :
                          levy.getRate();
        
        levyAmount += itemLevy;
    }

    levyDto.setLevyAmount(levyAmount);
    levyBreakDowns.add(levyDto);
    totalLevies += levyAmount;
}

invoiceSummary.setLevyBreakDown(levyBreakDowns);
    
    invoiceSummary.setTaxBreakDown(taxBreakdowns);
    invoiceSummary.setTotalVAT(totalVATAmount);
    invoiceSummary.setInvoiceTotal(invoiceTotal);
    // Update total to include levies
    invoiceSummary.setInvoiceTotal(invoiceTotal + totalLevies);
    invoiceSummary.setOfflineSignature("");
    invoiceSummary.setAmountTendered(amountTendered); 

    // 4. Put everything into the payload
    InvoicePayload payload = new InvoicePayload();
    payload.setInvoiceHeader(invoiceHeader);
    payload.setInvoiceLineItems(lineItems);
    payload.setInvoiceSummary(invoiceSummary);
    
    double totalVAT = invoiceSummary.getTotalVAT();
    String offlineSignature = invoiceSummary.getOfflineSignature();
    boolean isTransmitted = false; 
    String paymentId = paymentMethodComboBox.getValue();
    double amountPaid = totalAmount;

    // Save locally before transmitting
    boolean saveSuccess = Helper.saveTransaction(
        invoiceHeader,
        lineItems,
        taxBreakdowns,
        levyBreakDowns,
        totalAmount,
        totalVAT,
        offlineSignature,
        "",
        isTransmitted,
        paymentId,
        amountTendered
    );

    if (saveSuccess) {
        System.out.println("Transaction saved locally.");
        if (isVat5Exempt) {
            System.out.println("VAT5 Certificate Applied - VAT Amount: " + formatCurrency(totalVAT));
        }
    } else {
        System.err.println("Failed to save transaction locally.");
    }
    
    // Step 5: Convert to JSON
    Gson gson = new Gson();
    String jsonPayload = gson.toJson(payload);
    String bearerToken = Helper.getToken();
    System.out.println("Payload: " + jsonPayload);

    // Step 6: Submit the request
    ApiClient apiClient = new ApiClient();
    
    apiClient.submitTransactions(jsonPayload, bearerToken, (success, returnedValidationUrl) -> {
        Platform.runLater(() -> {
            if (success) {
                Helper.updateValidationUrl(invoiceHeader.getInvoiceNumber(), returnedValidationUrl);
                Helper.markAsTransmitted(invoiceHeader.getInvoiceNumber());
                
                try {
                    EscPosReceiptPrinter.printReceipt(
                        invoiceHeader,
                        buyerName,
                        buyersTIN,
                        lineItems,
                        returnedValidationUrl, 
                        amountTendered, 
                        changeValue,
                        taxBreakdowns,
                        levyBreakDowns
                    );
                    System.out.println("Premium receipt printed successfully.");
                } catch (Exception e) {
                    System.err.println("Failed to print receipt: " + e.getMessage());
                    e.printStackTrace();
                }
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Transaction Status");
                successAlert.setHeaderText("🎉 Transaction Processed!");
                
                String successMessage = "The transaction was processed successfully.";
                if (isVat5Exempt) {
                    successMessage += "\n\n✓ VAT5 Certificate Applied"
                        + "\nVAT Amount: " + formatCurrency(totalVAT);
                }
                
                successAlert.setContentText(successMessage);
                successAlert.showAndWait();
            } else {
                Alert failureAlert = new Alert(Alert.AlertType.ERROR);
                failureAlert.setTitle("Transaction Status");
                failureAlert.setHeaderText("🚨 Sync Error!");
                failureAlert.setContentText("Transaction failed. Saved locally for later sync.");
                failureAlert.showAndWait();
                
                long transactionCount = 0;
                InvoiceDetails lastDetails = Helper.getLastInvoiceDetails();

                if (lastDetails != null) {
                    transactionCount = Helper.convertSequentialToBase10(lastDetails.getInvoiceNumber()) + 1;      
                }
                
                try {
                    InvoiceGenerationRequest generationRequest = new InvoiceGenerationRequest();
                    generationRequest.numItems = lineItems.size();
                    generationRequest.transactiondate = LocalDateTime.now();
                    generationRequest.transactionCount = transactionCount + 1;
                    generationRequest.invoiceTotal = amountPaid;
                    generationRequest.vatAmount = totalVAT;
                    generationRequest.invoiceNumber = invoiceNumber;

                    String secretKey = Helper.getSecretKey();
                    String validationUrl = Helper.generateOfflineReceiptSignature(generationRequest, secretKey);

                    invoiceSummary.setOfflineSignature(validationUrl.split("S=")[1]);
                    
                    Helper.updateOfflineTransactionDetails(invoiceHeader.getInvoiceNumber(), validationUrl, invoiceSummary.getOfflineSignature());

                    EscPosReceiptPrinter.printReceipt(
                        invoiceHeader,
                        buyerName,
                        buyersTIN,
                        lineItems,
                        validationUrl,
                        amountTendered, 
                        changeValue,
                        taxBreakdowns,
                        levyBreakDowns
                    );
                    System.out.println("Offline receipt printed.");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Failed to print offline receipt.");
                }
            }
        });
    });
    
    // Reset cart and generate new receipt number
    cartItems.clear();
    cashAmountField.clear();
    tinField.clear();
    isVat5Exempt = false;  // Reset VAT5 exemption
    currentVat5Data = null;  // Clear VAT5 data
    buyerAuthField.clear();
    customerNamesField.clear();
    receiptNumberLabel.setText(generateNewReceiptNumber());
    updateTotals();
}
    
    /**
      * Generates a new receipt number
    */
    /**
 * Generates a new receipt number
 */
public String generateNewReceiptNumber() {
    long taxpayerId = Helper.getTaxpayerId();
    int terminalPosition = Helper.getTerminalPosition();
    LocalDate transactionDate = LocalDate.now();
    long transactionCount = 1;

    InvoiceDetails lastDetails = Helper.getLastInvoiceDetails();

    if (lastDetails != null) {
        LocalDate lastDate = lastDetails.getInvoiceDateTime().toLocalDate();

        // If last invoice is from today, continue counting
        if (lastDate.equals(transactionDate)) {
            transactionCount = Helper.convertSequentialToBase10(lastDetails.getInvoiceNumber()) + 1;
        } else {
            // new day → reset to 1
            transactionCount = 1;
        }
    }

    return Helper.generateReceiptNumber(taxpayerId, terminalPosition, transactionDate, transactionCount);
}


private void applyDiscount() {
    // Check if we have items in the cart
    if (cartItems.isEmpty()) {
        showAlert("No Items", "Please add items to the cart before applying a discount.");
        return;
    }

    // Get the selected product (for item-specific discount)
    Product selectedProduct = cartTable.getSelectionModel().getSelectedItem();
    
    // Create the discount dialog
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Apply Discount");
    dialog.setHeaderText(selectedProduct != null ? 
        "Apply discount to: " + selectedProduct.getName() : 
        "Apply discount to entire cart");
    
    // Set up the dialog pane with the app's style
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setStyle("-fx-background-color: white; -fx-padding: 20px;");
    dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    
    // Create discount type options
    VBox content = new VBox(15);
    content.setPadding(new Insets(10));
    
    // Radio buttons for discount type
    ToggleGroup discountTypeGroup = new ToggleGroup();
    RadioButton percentageOption = new RadioButton("Percentage Discount");
    percentageOption.setToggleGroup(discountTypeGroup);
    percentageOption.setSelected(true);
    
    RadioButton amountOption = new RadioButton("Fixed Amount Discount");
    amountOption.setToggleGroup(discountTypeGroup);
    
    // Input field for discount value
    TextField discountValueField = new TextField();
    discountValueField.setPromptText("Enter discount value");
    discountValueField.setPrefHeight(35);
    discountValueField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; " +
                            "-fx-border-radius: 5px; -fx-border-color: #e0e0e0; " +
                            "-fx-border-width: 1px; -fx-background-color: white;");

    // Only allow numbers and decimal points in the text field
    discountValueField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue.matches("\\d*\\.?\\d*")) {
            discountValueField.setText(oldValue);
        }
    });
    
    // Scope options (only show if an item is selected)
    VBox scopeBox = new VBox(10);
    ToggleGroup scopeGroup = new ToggleGroup();
    RadioButton itemOption = new RadioButton("Apply to selected item only");
    RadioButton cartOption = new RadioButton("Apply to entire cart");
    
    itemOption.setToggleGroup(scopeGroup);
    cartOption.setToggleGroup(scopeGroup);
    
    if (selectedProduct != null) {
        itemOption.setSelected(true);
        scopeBox.getChildren().addAll(new Label("Discount Scope:"), itemOption, cartOption);
    } else {
        cartOption.setSelected(true);
    }
    
    content.getChildren().addAll(
        new Label("Discount Type:"),
        percentageOption, 
        amountOption,
        new Label("Discount Value:"),
        discountValueField
    );
    
    if (selectedProduct != null) {
        content.getChildren().add(scopeBox);
    }
    
    dialogPane.setContent(content);
    
    // Show dialog and process the result
    dialog.showAndWait().ifPresent(result -> {
        if (result == ButtonType.OK) {
            try {
                double discountValue = Double.parseDouble(discountValueField.getText());
                boolean isPercentage = percentageOption.isSelected();
                
                if (discountValue <= 0) {
                    showAlert("Invalid Discount", "Please enter a discount value greater than zero.");
                    return;
                }
                
                // Cap percentage discount at 100%
                if (isPercentage && discountValue > 100) {
                    discountValue = 100;
                }
                
                if (selectedProduct != null && itemOption.isSelected()) {
                    // Apply discount to specific item
                    applyDiscountToItem(selectedProduct, discountValue, isPercentage);
                } else {
                    // Apply discount to entire cart
                    applyDiscountToCart(discountValue, isPercentage);
                }
                
                // Update totals to reflect discount
                updateTotals();
                
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter a valid number for the discount value.");
            }
        }
    });
}

// Method to apply discount to a specific item
private void applyDiscountToItem(Product product, double discountValue, boolean isPercentage) {
    double originalPrice = product.getOriginalPrice();
    if (originalPrice == 0) {
        // If original price isn't set, store it now
        originalPrice = product.getPrice();
        product.setOriginalPrice(originalPrice);
    }
    
    if (isPercentage) {
        double discountAmount = originalPrice * (discountValue / 100.0);
        product.setPrice(originalPrice - discountAmount);
        product.setDiscountPercent(discountValue);
        product.setDiscountAmount(0);
    } else {
        // Ensure discount doesn't exceed item price
        double safeDicountAmount = Math.min(discountValue, originalPrice);
        product.setPrice(originalPrice - safeDicountAmount);
        product.setDiscountAmount(safeDicountAmount);
        product.setDiscountPercent(0);
    }
    
    product.updateTotal();
    cartTable.refresh();
}

// Method to apply discount to the entire cart  
private void applyDiscountToCart(double discountValue, boolean isPercentage) {
    if (isPercentage) {
        cartDiscountPercent = discountValue;
        cartDiscountAmount = 0;
    } else {
        cartDiscountAmount = discountValue;
        cartDiscountPercent = 0;
    }
}

/**
 * Shows an advanced professional barcode scanner interface
 */

private String showBarcodeScanner() {
    // Create an atomic reference to hold the camera reference that can be accessed from lambdas
    AtomicReference<Webcam> cameraRef = new AtomicReference<>();
    
    Stage scannerStage = new Stage();
    scannerStage.initModality(Modality.APPLICATION_MODAL);
    scannerStage.initOwner(stage);
    scannerStage.setTitle("Barcode Scanner");
    scannerStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/scanner_icon.png")));
    scannerStage.setResizable(true);

    BorderPane scannerRoot = new BorderPane();
    scannerRoot.setStyle("-fx-background-color: #f5f5f7;");

    // Header
    HBox header = new HBox();
    header.setPadding(new Insets(15, 20, 15, 20));
    header.setStyle("-fx-background-color: #1a237e;");
    header.setAlignment(Pos.CENTER_LEFT);

    Label titleLabel = new Label("Advanced Barcode Scanner");
    titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Button closeButton = new Button("×");
    closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px; -fx-cursor: hand;");
    closeButton.setOnAction(e -> {
        Webcam camera = cameraRef.get();
        if (camera != null) {
            camera.close();
        }
        scannerStage.close();
    });

    header.getChildren().addAll(titleLabel, spacer, closeButton);
    scannerRoot.setTop(header);

    // Scanner view area
    StackPane scannerViewPane = new StackPane();
    scannerViewPane.setPadding(new Insets(20));
    scannerViewPane.setStyle("-fx-background-color: #424242;");

    // Camera view - using JavaFX ImageView to display frames
    ImageView cameraView = new ImageView();
    cameraView.setFitWidth(580);
    cameraView.setFitHeight(320);
    cameraView.setPreserveRatio(true);

    Rectangle viewport = new Rectangle(580, 320);
    viewport.setArcWidth(10);
    viewport.setArcHeight(10);

    // Set the clip on the camera view
    cameraView.setClip(viewport);

    Rectangle scanArea = new Rectangle(400, 200);
    scanArea.setFill(Color.rgb(0, 0, 0, 0.1));
    scanArea.setArcWidth(10);
    scanArea.setArcHeight(10);
    scanArea.setStroke(Color.rgb(255, 255, 255, 0.2));
    scanArea.setStrokeWidth(1);
    scanArea.setTranslateX(0);
    scanArea.setTranslateY(0);

    // Corner markers
    int markerSize = 20;
    Line[] cornerMarkers = {
        // Top-left
        new Line(0, 0, markerSize, 0), 
        new Line(0, 0, 0, markerSize),
        // Top-right
        new Line(400 - markerSize, 0, 400, 0), 
        new Line(400, 0, 400, markerSize),
        // Bottom-left
        new Line(0, 200, markerSize, 200), 
        new Line(0, 200 - markerSize, 0, 200),
        // Bottom-right
        new Line(400 - markerSize, 200, 400, 200), 
        new Line(400, 200 - markerSize, 400, 200)
    };
    
    for (Line marker : cornerMarkers) {
        marker.setStroke(Color.rgb(0, 230, 118, 0.8));
        marker.setStrokeWidth(3);
    }

    Group scanAreaGroup = new Group();
    scanAreaGroup.getChildren().add(scanArea);
    scanAreaGroup.getChildren().addAll(cornerMarkers);
    
    // Center the scan area in the camera view
    scanAreaGroup.setTranslateX((580 - 400) / 2);
    scanAreaGroup.setTranslateY((320 - 200) / 2);

    // Scanning line animation
    Rectangle scanLine = new Rectangle(400, 3);
    scanLine.setFill(Color.rgb(0, 230, 118, 0.8));
    scanLine.setTranslateY(10);
    scanLine.setTranslateX((580 - 400) / 2);

    DropShadow glow = new DropShadow();
    glow.setColor(Color.rgb(0, 230, 118, 0.6));
    glow.setRadius(15);
    scanLine.setEffect(glow);

    TranslateTransition scanAnimation = new TranslateTransition(Duration.seconds(2), scanLine);
    scanAnimation.setFromY((320 - 200) / 2 + 10);
    scanAnimation.setToY((320 - 200) / 2 + 180);
    scanAnimation.setCycleCount(Timeline.INDEFINITE);
    scanAnimation.setAutoReverse(true);
    scanAnimation.play();

    Timeline pulseEffect = new Timeline(
        new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 10)),
        new KeyFrame(Duration.millis(500), new KeyValue(glow.radiusProperty(), 20)),
        new KeyFrame(Duration.millis(1000), new KeyValue(glow.radiusProperty(), 10))
    );
    pulseEffect.setCycleCount(Timeline.INDEFINITE);
    pulseEffect.play();

    // Info text
    Label cameraInfo = new Label("HD Camera Active | 1280×720 | 30fps");
    cameraInfo.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");
    StackPane.setAlignment(cameraInfo, Pos.TOP_RIGHT);
    StackPane.setMargin(cameraInfo, new Insets(10, 15, 0, 0));

    // Status indicators
    Circle dot1 = new Circle(4, Color.rgb(0, 230, 118, 0.8));
    Circle dot2 = new Circle(4, Color.rgb(0, 230, 118, 0.8));
    Circle dot3 = new Circle(4, Color.rgb(0, 230, 118, 0.8));

    Label scanningLabel = new Label("Scanning for barcode");
    scanningLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

    HBox statusBox = new HBox(5, scanningLabel, dot1, dot2, dot3);
    statusBox.setAlignment(Pos.CENTER);
    statusBox.setPadding(new Insets(8, 15, 8, 15));
    statusBox.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 15;");
    StackPane.setAlignment(statusBox, Pos.BOTTOM_CENTER);
    StackPane.setMargin(statusBox, new Insets(0, 0, 15, 0));

    Timeline dotAnimation = new Timeline(
        new KeyFrame(Duration.ZERO,
            new KeyValue(dot1.opacityProperty(), 1.0),
            new KeyValue(dot2.opacityProperty(), 0.3),
            new KeyValue(dot3.opacityProperty(), 0.3)),
        new KeyFrame(Duration.millis(300),
            new KeyValue(dot1.opacityProperty(), 0.3),
            new KeyValue(dot2.opacityProperty(), 1.0),
            new KeyValue(dot3.opacityProperty(), 0.3)),
        new KeyFrame(Duration.millis(600),
            new KeyValue(dot1.opacityProperty(), 0.3),
            new KeyValue(dot2.opacityProperty(), 0.3),
            new KeyValue(dot3.opacityProperty(), 1.0)),
        new KeyFrame(Duration.millis(900),
            new KeyValue(dot1.opacityProperty(), 1.0),
            new KeyValue(dot2.opacityProperty(), 0.3),
            new KeyValue(dot3.opacityProperty(), 0.3))
    );
    dotAnimation.setCycleCount(Timeline.INDEFINITE);
    dotAnimation.play();

    // Add camera view and overlays to the scanner pane
    scannerViewPane.getChildren().addAll(cameraView, scanAreaGroup, scanLine, cameraInfo, statusBox);
    scannerRoot.setCenter(scannerViewPane);

    // Controls section
    VBox controlsPane = new VBox(15);
    controlsPane.setPadding(new Insets(20));
    controlsPane.setStyle("-fx-background-color: white;");

    Label controlsTitle = new Label("Scanning Controls");
    controlsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

    TextField barcodeInput = new TextField();
    barcodeInput.setPromptText("Enter barcode manually");
    barcodeInput.setPrefHeight(35);
    barcodeInput.setStyle("-fx-font-size: 14px;");
    HBox.setHgrow(barcodeInput, Priority.ALWAYS);

    Button submitButton = new Button("Submit");
    submitButton.setPrefHeight(35);
    submitButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-font-weight: bold;");

    HBox manualEntryBox = new HBox(10, barcodeInput, submitButton);
    manualEntryBox.setAlignment(Pos.CENTER_LEFT);

    ComboBox<String> scanModeCombo = new ComboBox<>();
    scanModeCombo.getItems().addAll("EAN-13", "UPC-A", "CODE 128", "QR Code", "All Formats");
    scanModeCombo.setValue("All Formats");
    scanModeCombo.setPrefWidth(120);

    CheckBox beepOnScanCheck = new CheckBox("Sound on scan");
    beepOnScanCheck.setSelected(true);

    CheckBox autoSubmitCheck = new CheckBox("Auto-submit");
    autoSubmitCheck.setSelected(true);

    FlowPane optionsPane = new FlowPane(15, 15, scanModeCombo, beepOnScanCheck, autoSubmitCheck);
    optionsPane.setAlignment(Pos.CENTER_LEFT);

    Button cancelButton = new Button("Cancel");
    cancelButton.setPrefSize(120, 40);
    cancelButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #424242;");
    cancelButton.setOnAction(e -> {
        Webcam camera = cameraRef.get();
        if (camera != null) {
            camera.close();
        }
        scannerStage.close();
    });

    Button switchCameraButton = new Button("Switch Camera");
    switchCameraButton.setPrefSize(120, 40);
    switchCameraButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; -fx-font-weight: bold;");

    HBox actionButtonsBox = new HBox(10, cancelButton, switchCameraButton);
    actionButtonsBox.setAlignment(Pos.CENTER);

    controlsPane.getChildren().addAll(controlsTitle, manualEntryBox, optionsPane, new Separator(), actionButtonsBox);
    scannerRoot.setBottom(controlsPane);
    
    // Reference to hold the barcode result
    AtomicReference<String> resultBarcode = new AtomicReference<>("");
    
    // Create an atomic reference to store the processing thread
    AtomicReference<Thread> processingThreadRef = new AtomicReference<>();
    
    // Create an atomic boolean to track barcode detection status
    AtomicBoolean barcodeFound = new AtomicBoolean(false);
    
    try {
        // Initialize webcam
        Webcam camera = Webcam.getDefault();
        if (camera == null) {
            throw new Exception("No webcam detected");
        }
        
        // Store camera in atomic reference for access from lambdas
        cameraRef.set(camera);
        
        // Set camera resolution
        Dimension[] dimensions = camera.getViewSizes();
        Dimension bestSize = null;
        
        // Choose the highest resolution that fits our needs
        for (Dimension dim : dimensions) {
            if (bestSize == null || 
                (dim.width >= 640 && dim.height >= 480 && 
                 (bestSize.width < dim.width || bestSize.height < dim.height))) {
                bestSize = dim;
            }
        }
        
        if (bestSize != null) {
            camera.setViewSize(bestSize);
        } else {
            camera.setViewSize(WebcamResolution.HD.getSize());
        }
        
        // Open camera
        camera.open();
        
        // Update camera info
        cameraInfo.setText("Camera Active | " + camera.getViewSize().width + "×" + 
                          camera.getViewSize().height + " | " + camera.getFPS() + "fps");
        
        // Create barcode processor
        BarcodeFormat[] formats = getFormatsForMode(scanModeCombo.getValue());
        
        // Create multi-format reader
        final MultiFormatReader barcodeReader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(formats));
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        barcodeReader.setHints(hints);
        
        // Update formats when combo box changes
        scanModeCombo.setOnAction(e -> {
            BarcodeFormat[] newFormats = getFormatsForMode(scanModeCombo.getValue());
            
            Map<DecodeHintType, Object> newHints = new HashMap<>();
            newHints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(newFormats));
            newHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            barcodeReader.setHints(newHints);
        });
        
        // Camera frame processing thread
        Thread processingThread = new Thread(() -> {
            BufferedImage lastFrame = null;
            AtomicReference<Result> lastResult = new AtomicReference<>(); // Fixed: r to Result
            long lastDetectionTime = 0;
            
            while (!Thread.interrupted() && cameraRef.get().isOpen()) {
                try {
                    // Capture frame from camera
                    final BufferedImage image = cameraRef.get().getImage();
                    if (image == null) continue;
                    
                    // Create JavaFX image from camera frame
                    final WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
                    
                    // Update the UI with the camera frame
                    Platform.runLater(() -> cameraView.setImage(fxImage));
                    
                    // Don't process every frame for efficiency
                    if (System.currentTimeMillis() - lastDetectionTime < 200) {
                        Thread.sleep(20);
                        continue;
                    }
                    
                    // If a barcode was already found, don't keep processing
                    if (barcodeFound.get()) {
                        Thread.sleep(50);
                        continue;
                    }
                    
                    // Store the current frame for processing
                    lastFrame = image;
                    
                    // Calculate scan area in image coordinates
                    double scaleX = (double) image.getWidth() / cameraView.getFitWidth();
                    double scaleY = (double) image.getHeight() / cameraView.getFitHeight();
                    
                    int scanAreaX = (int) ((cameraView.getFitWidth() - 400) / 2 * scaleX);
                    int scanAreaY = (int) ((cameraView.getFitHeight() - 200) / 2 * scaleY);
                    int scanAreaWidth = (int) (400 * scaleX);
                    int scanAreaHeight = (int) (200 * scaleY);
                    
                    // Ensure scan area is within the image bounds
                    scanAreaX = Math.max(0, scanAreaX);
                    scanAreaY = Math.max(0, scanAreaY);
                    scanAreaWidth = Math.min(image.getWidth() - scanAreaX, scanAreaWidth);
                    scanAreaHeight = Math.min(image.getHeight() - scanAreaY, scanAreaHeight);
                    
                    // Create subimage for the scan area
                    BufferedImage subImage = image.getSubimage(
                        scanAreaX, scanAreaY, scanAreaWidth, scanAreaHeight
                    );
                    
                    // Process the image to detect barcodes
                    try {
                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
                            new BufferedImageLuminanceSource(subImage)
                        ));
                        
                        Result result = barcodeReader.decode(bitmap);
                        
                        if (result != null) {
                            lastResult.set(result);
                            barcodeFound.set(true);
                            lastDetectionTime = System.currentTimeMillis();
                            
                            // Play beep sound if enabled
                            if (beepOnScanCheck.isSelected()) {
                                playBeepSound();
                            }
                            
                            // Update UI to show detected barcode
                            Platform.runLater(() -> {
                                Result detectedResult = lastResult.get();
                                scanningLabel.setText("Barcode detected: " + detectedResult.getText());
                                
                                // Stop animations
                                dotAnimation.stop();
                                dot1.setOpacity(1.0);
                                dot2.setOpacity(1.0);
                                dot3.setOpacity(1.0);
                                
                                // Highlight the detected barcode region
                                ResultPoint[] points = detectedResult.getResultPoints();
                                if (points != null && points.length > 0) {
                                    Polygon highlight = new Polygon();
                                    
                                    // Convert result points to scene coordinates
                                    for (ResultPoint point : points) {
                                        double x = point.getX() / scaleX + scanAreaGroup.getTranslateX();
                                        double y = point.getY() / scaleY + scanAreaGroup.getTranslateY();
                                        highlight.getPoints().addAll(x, y);
                                    }
                                    
                                    highlight.setFill(Color.TRANSPARENT);
                                    highlight.setStroke(Color.rgb(0, 230, 118));
                                    highlight.setStrokeWidth(3);
                                    
                                    // Create barcode label
                                    Label barcodeLabel = new Label(detectedResult.getText());
                                    barcodeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; " +
                                                         "-fx-text-fill: #00e676; -fx-background-color: rgba(0,0,0,0.5); " +
                                                         "-fx-padding: 5px;");
                                    
                                    // Calculate position for the label (below the barcode)
                                    double minY = Double.MAX_VALUE;
                                    double maxY = Double.MIN_VALUE;
                                    double avgX = 0;
                                    
                                    for (int i = 0; i < points.length; i++) {
                                        double y = points[i].getY() / scaleY + scanAreaGroup.getTranslateY();
                                        double x = points[i].getX() / scaleX + scanAreaGroup.getTranslateX();
                                        minY = Math.min(minY, y);
                                        maxY = Math.max(maxY, y);
                                        avgX += x;
                                    }
                                    avgX /= points.length;
                                    
                                    StackPane.setAlignment(barcodeLabel, Pos.TOP_LEFT);
                                    barcodeLabel.setTranslateX(avgX - 50);  // Estimate width
                                    barcodeLabel.setTranslateY(maxY + 10);
                                    
                                    scannerViewPane.getChildren().addAll(highlight, barcodeLabel);
                                }
                                
                                // Auto-submit if enabled
                                if (autoSubmitCheck.isSelected()) {
                                    PauseTransition pause = new PauseTransition(Duration.seconds(1.0));
                                    pause.setOnFinished(event -> {
                                        resultBarcode.set(detectedResult.getText());
                                        
                                        Webcam cam = cameraRef.get();
                                        if (cam != null) {
                                            cam.close();
                                        }
                                        
                                        scannerStage.close();
                                        
                                        // Process the barcode
                                        barcodeField.setText(detectedResult.getText());
                                        addProductToCart();
                                    });
                                    pause.play();
                                }
                            });
                        }
                    } catch (NotFoundException e) {
                        // No barcode found in this frame, continue processing
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    // Sleep to control processing rate
                    Thread.sleep(30);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        processingThread.setDaemon(true);
        processingThread.start();
        processingThreadRef.set(processingThread);
        
        // Handler for switching cameras
        switchCameraButton.setOnAction(e -> {
            Webcam currentCamera = cameraRef.get();
            if (currentCamera != null) {
                // Request thread to stop and close current camera
                Thread currentThread = processingThreadRef.get();
                if (currentThread != null) {
                    currentThread.interrupt();
                }
                currentCamera.close();
                
                try {
                    // Get all available webcams
                    List<Webcam> cameras = Webcam.getWebcams();
                    if (cameras.size() <= 1) {
                        // No other cameras available
                        showAlert(Alert.AlertType.INFORMATION, "Camera Switch", 
                                 "No additional cameras available.");
                        return;
                    }
                    
                    // Find the next camera (circular)
                    int currentIndex = cameras.indexOf(currentCamera);
                    int nextIndex = (currentIndex + 1) % cameras.size();
                    
                    // Initialize the new camera
                    // For simplicity, we'll just close this dialog and reopen
                    scannerStage.close();
                    Platform.runLater(this::showBarcodeScanner);
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Camera Switch Error", 
                             "Failed to switch camera: " + ex.getMessage());
                }
            }
        });
        
        // Handle manual entry with submit handler
        EventHandler<ActionEvent> submitHandler = event -> {
            String barcode = barcodeInput.getText().trim();
            if (!barcode.isEmpty()) {
                resultBarcode.set(barcode);
                
                Webcam cam = cameraRef.get();
                if (cam != null) {
                    cam.close();
                }
                
                scannerStage.close();
                
                // Process the barcode
                Platform.runLater(() -> {
                    barcodeField.setText(barcode);
                    addProductToCart();
                });
            }
        };
        
        // Connect submit handler to button and text field enter key
        submitButton.setOnAction(submitHandler);
        barcodeInput.setOnAction(submitHandler);
        
    } catch (Exception e) {
        e.printStackTrace();
        // Show error to user
        showAlert(Alert.AlertType.ERROR, "Camera Error", 
                 "Could not initialize camera: " + e.getMessage() + 
                 "\nPlease ensure your camera is connected and not in use by another application.");
        
        // Disable camera-dependent UI elements
        scanningLabel.setText("Camera unavailable");
        scanModeCombo.setDisable(true);
        switchCameraButton.setDisable(true);
    }

    Scene scene = new Scene(scannerRoot, 700, 600);
    scannerStage.setScene(scene);
    
    // Clean up resources when closing
    scannerStage.setOnCloseRequest(event -> {
        scanAnimation.stop();
        pulseEffect.stop();
        dotAnimation.stop();
        
        Webcam camera = cameraRef.get();
        if (camera != null) {
            camera.close();
        }
    });
    
    scannerStage.showAndWait();
    
    // Return the scanned barcode (or empty string if none was scanned)
    return resultBarcode.get();
}

// Helper method to get barcode formats based on selected mode
private BarcodeFormat[] getFormatsForMode(String mode) {
    switch (mode) {
        case "EAN-13":
            return new BarcodeFormat[]{BarcodeFormat.EAN_13};
        case "UPC-A":
            return new BarcodeFormat[]{BarcodeFormat.UPC_A};
        case "CODE 128":
            return new BarcodeFormat[]{BarcodeFormat.CODE_128};
        case "QR Code":
            return new BarcodeFormat[]{BarcodeFormat.QR_CODE};
        default:
            return new BarcodeFormat[]{
                BarcodeFormat.UPC_A, BarcodeFormat.UPC_E, 
                BarcodeFormat.EAN_13, BarcodeFormat.EAN_8,
                BarcodeFormat.CODE_39, BarcodeFormat.CODE_93, 
                BarcodeFormat.CODE_128, BarcodeFormat.QR_CODE,
                BarcodeFormat.DATA_MATRIX, BarcodeFormat.ITF
            };
    }
}
/**
 * Helper method to play a beep sound when a barcode is detected
 */
private void playBeepSound() {
    try {
        // Load the beep sound file
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
            getClass().getResourceAsStream("/sounds/beep.wav")
        );
        
        // Get a sound clip resource
        Clip clip = AudioSystem.getClip();
        
        // Open audio clip and load samples from the audio input stream
        clip.open(audioInputStream);
        
        // Start playing the sound
        clip.start();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

/**
 * Helper method to show alerts
 */
private void showAlert(Alert.AlertType type, String title, String content) {
    Platform.runLater(() -> {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    });
}  
    /**
       * Formats a number as Malawi Kwacha currency
    */
    private String formatCurrency(double amount) {
      Locale malawiLocale = new Locale.Builder().setLanguage("en").setRegion("MW").build();
      NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(malawiLocale);
      return currencyFormatter.format(amount);
    }
    
    /**
     * Gets the current date and time as formatted string
     */
    private String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
        return now.format(formatter);
    }
    
    /**
     * Starts the timer to update the date/time display
     */
    private void startTimeUpdater() {
        Thread timeThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                Platform.runLater(() -> {
                    dateTimeLabel.setText(getCurrentDateTime());
                });
            }
        });
        
        timeThread.setDaemon(true);
        timeThread.start();
    }
    
    /**
     * Gets the initials from a full name
     */
    private String getInitials(String name) {
        StringBuilder initials = new StringBuilder();
        for (String part : name.split(" ")) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }
    
 private void holdSale() {
    // Validate there are items in the cart
    if (cartItems.isEmpty()) {
        showAlert("Error", "Cart is empty. Add items before holding a sale.");
        return;
    }

    // Create a dialog to get customer info and hold reason
    Dialog<Pair<String, String>> dialog = new Dialog<>();
    dialog.setTitle("Hold Sale");
    dialog.setHeaderText(null); // Remove default header text for custom styling
    
    // Set the button types
    ButtonType saveButtonType = new ButtonType("Hold Sale", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    // Style the dialog pane
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setStyle("-fx-background-color: white;");
    dialogPane.setPrefWidth(500);
    
    // Create main container
    VBox container = new VBox(20);
    container.setPadding(new Insets(25));
    
    // Header with title
    Label headerLabel = new Label("Hold Sale");
    headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
    
    // Instruction text
    Label instructionLabel = new Label("Enter customer information to hold this sale");
    instructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
    
    // Create the customer name and reason form
    VBox formContent = new VBox(18);
    formContent.setPadding(new Insets(15, 0, 10, 0));
    
    // Customer name field with label
    VBox customerNameBox = new VBox(8);
    Label customerNameLabel = new Label("Customer Name");
    customerNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555555;");
    
    heldCustomerNameField = new TextField();
    heldCustomerNameField.setPromptText("Enter customer name");
    heldCustomerNameField.setPrefHeight(40);
    heldCustomerNameField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                              "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: #f8f8f8;");
    
    customerNameBox.getChildren().addAll(customerNameLabel, heldCustomerNameField);
    
    // Reason field with label
    VBox reasonBox = new VBox(8);
    Label reasonLabel = new Label("Reason for Holding Sale");
    reasonLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555555;");
    
    TextArea reasonField = new TextArea();
    reasonField.setPromptText("Enter the reason for holding this sale");
    reasonField.setPrefRowCount(3);
    reasonField.setWrapText(true);
    reasonField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                        "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: #f8f8f8;");
    
    reasonBox.getChildren().addAll(reasonLabel, reasonField);
    
    // Add fields to form
    formContent.getChildren().addAll(customerNameBox, reasonBox);
    
    // Add all components to container
    container.getChildren().addAll(headerLabel, instructionLabel, formContent);
    dialogPane.setContent(container);
    
    // Style the buttons
    Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
    saveButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                      "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; " +
                      "-fx-background-radius: 5px;");
    
    // Add hover effect to hold sale button
    saveButton.setOnMouseEntered(e -> 
        saveButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; " +
                          "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; " +
                          "-fx-background-radius: 5px;")
    );
    
    saveButton.setOnMouseExited(e -> 
        saveButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                          "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; " +
                          "-fx-background-radius: 5px;")
    );
    
    // Style cancel button
    Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
    cancelButton.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #555555; " +
                        "-fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 5px; " +
                        "-fx-border-color: #e0e0e0; -fx-border-width: 1px;");
    
    // Add hover effect to cancel button
    cancelButton.setOnMouseEntered(e -> 
        cancelButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #555555; " +
                            "-fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 5px; " +
                            "-fx-border-color: #e0e0e0; -fx-border-width: 1px;")
    );
    
    cancelButton.setOnMouseExited(e -> 
        cancelButton.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #555555; " +
                            "-fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 5px; " +
                            "-fx-border-color: #e0e0e0; -fx-border-width: 1px;")
    );
    
    // Add drop shadow to dialog
    DropShadow dialogShadow = new DropShadow();
    dialogShadow.setRadius(10.0);
    dialogShadow.setOffsetY(5.0);
    dialogShadow.setColor(Color.color(0.0, 0.0, 0.0, 0.2));
    dialogPane.setEffect(dialogShadow);

    // Request focus on the customer name field by default
    Platform.runLater(() -> heldCustomerNameField.requestFocus());

    // Convert the result to a customer name-reason pair when the save button is clicked
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == saveButtonType) {
            return new Pair<>(heldCustomerNameField.getText(), reasonField.getText());
        }
        return null;
    });

    Optional<Pair<String, String>> result = dialog.showAndWait();

    result.ifPresent(customerReasonPair -> {
        String customerName = customerReasonPair.getKey();
        String reason = customerReasonPair.getValue();

        // Generate a unique hold ID
        String holdId = generateHoldId();

        // Get customer TIN from UI if possible
        String customerTIN = "";
        try {
            if (root.getRight() instanceof VBox) {
                VBox rightPane = (VBox) root.getRight();
                Node firstChild = rightPane.getChildren().get(0);
                if (firstChild instanceof VBox) {
                    VBox topSection = (VBox) firstChild;
                    Node possibleTinField = topSection.getChildren().get(2);
                    if (possibleTinField instanceof TextField) {
                        TextField tinField = (TextField) possibleTinField;
                        customerTIN = tinField.getText();
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Could not extract TIN field: " + ex.getMessage());
        }

        // Copy cart items
        List<Product> itemsCopy = new ArrayList<>();
        for (Product item : cartItems) {
            Product copy = item.clone(); // Clone without casting
            itemsCopy.add(copy);
        }

        // Create a new held sale
        HeldSale heldSale = new HeldSale(
            holdId,
            customerName,
            customerTIN,
            itemsCopy,
            cartDiscountAmount,
            cartDiscountPercent
        );

        // Save the held sale to the database (this replaces storing in memory)
        Helper.saveHeldSale(heldSale);
        

        // Style the confirmation alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sale Held Successfully");
        alert.setHeaderText(null);
        alert.setContentText("Sale has been held with ID: " + holdId + "\nCustomer: " + customerName);
        
        DialogPane alertPane = alert.getDialogPane();
        alertPane.setStyle("-fx-background-color: white;");
        alertPane.getStyleClass().add("modern-alert");
        
        // Style the OK button on the alert
        Button okButton = (Button) alertPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5px;");
        
        // Clear the current cart
        cartItems.clear();
        updateTotals();
        
        // Show the confirmation alert
        alert.showAndWait();
    });
}


// Add this method to POSDashboard class
private String generateHoldId() {
    // Create a unique ID with date and random number
    LocalDateTime now = LocalDateTime.now();
    String dateStr = now.format(DateTimeFormatter.ofPattern("yyMMdd"));
    String timeStr = now.format(DateTimeFormatter.ofPattern("HHmmss"));
    return "H" + dateStr + timeStr;
}
private void showHeldSales() {
    List<HeldSale> heldSales = Helper.getAllHeldSales();

    if (heldSales.isEmpty()) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Held Sales");
        alert.setHeaderText("No Sales on Hold");
        alert.setContentText("There are currently no held sales available.");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 5px; -fx-padding: 15px;");

        Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
        if (headerLabel != null) {
            headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        }

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4px;");
        }

        alert.showAndWait();
        return;
    }

    Dialog<HeldSale> dialog = new Dialog<>();
    dialog.setTitle("Held Sales");
    dialog.setHeaderText("Select a held sale to restore");

    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8px; -fx-padding: 20px; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
    dialogPane.setPrefWidth(600);
    dialogPane.setPrefHeight(500);

    Label headerLabel = new Label("◷ Held Sales");
    headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");

    Label subHeaderLabel = new Label("Select a held sale to restore");
    subHeaderLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-padding: 0 0 10px 0;");

    ButtonType restoreButtonType = new ButtonType("Restore Sale", ButtonBar.ButtonData.OK_DONE);
    ButtonType deleteButtonType = new ButtonType("Delete Sale", ButtonBar.ButtonData.LEFT);
    ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialogPane.getButtonTypes().addAll(restoreButtonType, deleteButtonType, cancelButtonType);
    
    ListView<HeldSale> listView = new ListView<>();
    listView.setPrefHeight(350);
    listView.setStyle("-fx-background-color: white; -fx-background-radius: 5px; -fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 5, 0, 0, 2);");

    listView.setCellFactory(lv -> new ListCell<HeldSale>() {
        @Override
        protected void updateItem(HeldSale item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle(null);
            } else {
                VBox card = new VBox(8);
                card.setStyle("-fx-background-color: white; -fx-padding: 12px; -fx-border-color: #f0f0f0; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-spacing: 8px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.05), 3, 0, 0, 1);");

                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);

                Label holdIdLabel = new Label("🏷️ Hold #" + item.getHoldId());
                holdIdLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");

                Label timeLabel = new Label("🕒 " + item.getFormattedHoldTime());
                timeLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                header.getChildren().addAll(holdIdLabel, spacer, timeLabel);

                Label customerLabel = new Label("👤 " + item.getCustomerName());
                customerLabel.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13px;");

                Label itemsLabel = new Label("🛒 " + item.getItems().size() + " items");
                itemsLabel.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13px;");

                card.getChildren().addAll(header, customerLabel, itemsLabel);
                setGraphic(card);

                selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected) {
                        card.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 12px; -fx-border-color: #3949ab; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-spacing: 8px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 3, 0, 0, 1);");
                    } else {
                        card.setStyle("-fx-background-color: white; -fx-padding: 12px; -fx-border-color: #f0f0f0; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-spacing: 8px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.05), 3, 0, 0, 1);");
                    }
                });

                setOnMouseEntered(e -> {
                    if (!isSelected()) {
                        card.setStyle("-fx-background-color: #f5f9ff; -fx-padding: 12px; -fx-border-color: #e0e0e0; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-spacing: 8px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 3, 0, 0, 1);");
                    }
                });

                setOnMouseExited(e -> {
                    if (!isSelected()) {
                        card.setStyle("-fx-background-color: white; -fx-padding: 12px; -fx-border-color: #f0f0f0; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-spacing: 8px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.05), 3, 0, 0, 1);");
                    }
                });
            }
        }
    });

    listView.getItems().addAll(heldSales);
    
    // Initialize buttons as disabled
    Platform.runLater(() -> {
        Button restoreButton = (Button) dialog.getDialogPane().lookupButton(restoreButtonType);
        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
        
        // Initially disable buttons since no item is selected
        if (restoreButton != null) restoreButton.setDisable(true);
        if (deleteButton != null) deleteButton.setDisable(true);
        
        if (restoreButton != null) {
            restoreButton.setStyle(
                "-fx-background-color: #1a237e; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5px; -fx-min-width: 120px;"
            );

            restoreButton.setOnMouseEntered(e -> {
                if (!restoreButton.isDisabled()) {
                    restoreButton.setStyle(
                        "-fx-background-color: #3949ab; -fx-text-fill: white; -fx-font-size: 14px; " +
                        "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5px; -fx-min-width: 120px;"
                    );
                }
            });

            restoreButton.setOnMouseExited(e -> {
                if (!restoreButton.isDisabled()) {
                    restoreButton.setStyle(
                        "-fx-background-color: #1a237e; -fx-text-fill: white; -fx-font-size: 14px; " +
                        "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5px; -fx-min-width: 120px;"
                    );
                }
            });
        }
        
        if (deleteButton != null) {
            deleteButton.setStyle(
                "-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5px; -fx-min-width: 120px;"
            );

            deleteButton.setOnMouseEntered(e -> {
                if (!deleteButton.isDisabled()) {
                    deleteButton.setStyle(
                        "-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; " +
                        "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5px; -fx-min-width: 120px;"
                    );
                }
            });

            deleteButton.setOnMouseExited(e -> {
                if (!deleteButton.isDisabled()) {
                    deleteButton.setStyle(
                        "-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-size: 14px; " +
                        "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5px; -fx-min-width: 120px;"
                    );
                }
            });
        }
        
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        if (cancelButton != null) {
            cancelButton.setStyle(
                "-fx-background-color: #e0e0e0; -fx-text-fill: #424242; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5px; -fx-min-width: 100px;"
            );
            
            cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(
                "-fx-background-color: #eeeeee; -fx-text-fill: #424242; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5px; -fx-min-width: 100px;"
            ));

            cancelButton.setOnMouseExited(e -> cancelButton.setStyle(
                "-fx-background-color: #e0e0e0; -fx-text-fill: #424242; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5px; -fx-min-width: 100px;"
            ));
        }
    });
    
    // Listen for selection changes to enable/disable buttons
    listView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
        Platform.runLater(() -> {
            Button restoreButton = (Button) dialog.getDialogPane().lookupButton(restoreButtonType);
            Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
            
            boolean hasSelection = (newSelection != null);
            
            if (restoreButton != null) restoreButton.setDisable(!hasSelection);
            if (deleteButton != null) deleteButton.setDisable(!hasSelection);
        });
    });

    VBox content = new VBox(10);
    content.getChildren().addAll(headerLabel, subHeaderLabel, listView);
    dialog.getDialogPane().setContent(content);

    Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
    deleteButton.addEventFilter(ActionEvent.ACTION, event -> {
        // Get the selected item
        HeldSale selectedSale = listView.getSelectionModel().getSelectedItem();
        if (selectedSale != null) {
            // Delete without confirmation
            boolean deleted = Helper.deleteHeldSaleById(selectedSale.getHoldId());
            if (deleted) {
                // Show success notification
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sale Deleted");
                alert.setHeaderText(null);
                alert.setContentText("Held sale has been deleted successfully.");
                alert.showAndWait();
                
                // Refresh the dialog (close and reopen)
                dialog.close();
                showHeldSales();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Failed to delete held sale");
                errorAlert.setContentText("The held sale could not be deleted from the database.");
                errorAlert.showAndWait();
            }
            
            event.consume();
        }
    });

    //handle the restore operation in the result converter
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == restoreButtonType) {
            return listView.getSelectionModel().getSelectedItem();
        }
        return null;
    });

    Optional<HeldSale> result = dialog.showAndWait();

    result.ifPresent(selectedSale -> {
        // This will only execute for restore operations
        restoreHeldSale(selectedSale);
    });
}

/**
 * Handles the Add Customer action with professional UI design
 */
private void addCustomer() {
    // Create a dialog
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Add New Customer");
    dialog.setHeaderText(null);
    
    // Set the dialog style to match application with enhanced styling
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setPrefWidth(500);
    dialogPane.setPrefHeight(500);
    dialogPane.setStyle("-fx-background-color: white; -fx-padding: 0;");
    
    // Create main container with header
    VBox mainContainer = new VBox();
    mainContainer.setSpacing(0);
    
    // Create professional header with icon
    HBox header = new HBox();
    header.setPadding(new Insets(20, 25, 20, 25));
    header.setStyle("-fx-background-color: linear-gradient(to right, #1a237e, #3949ab); -fx-background-radius: 5px 5px 0 0;");
    
    // Create a circle avatar for the person icon
    Circle personIcon = new Circle(25);
    personIcon.setFill(Color.WHITE);
    personIcon.setOpacity(0.9);
    
    // Use a text as the icon
    Text userIcon = new Text("");
    userIcon.setFont(Font.font("FontAwesome", 25));
    userIcon.setFill(Color.web("#1a237e"));
    
    StackPane iconContainer = new StackPane(personIcon, userIcon);
    
    // Header text
    VBox headerTextContainer = new VBox(5);
    Label headerTitle = new Label("Add New Customer");
    headerTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
    
    Label headerSubtitle = new Label("Enter customer details to add to the system");
    headerSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.8);");
    
    headerTextContainer.getChildren().addAll(headerTitle, headerSubtitle);
    HBox.setMargin(headerTextContainer, new Insets(0, 0, 0, 15));
    
    header.getChildren().addAll(iconContainer, headerTextContainer);
    header.setAlignment(Pos.CENTER_LEFT);
    
    // Add the header to the main container
    mainContainer.getChildren().add(header);
    
    // Create the form with better styling
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setFitToWidth(true);
    scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");
    
    VBox formContainer = new VBox(15);
    formContainer.setPadding(new Insets(25));
    formContainer.setStyle("-fx-background-color: white;");
    
    // Define enhanced text field style
    String textFieldStyle = "-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                          "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: white; " +
                          "-fx-padding: 10px 15px;";
    
    String labelStyle = "-fx-font-size: 13px; -fx-text-fill: #616161; -fx-font-weight: bold;";
    
    // Customer Type section
    VBox customerTypeSection = new VBox(8);
    Label customerTypeLabel = new Label("CUSTOMER TYPE");
    customerTypeLabel.setStyle(labelStyle);
    
    HBox typeButtons = new HBox(15);
    typeButtons.setPrefHeight(45);
    
    ToggleGroup typeGroup = new ToggleGroup();
    
    // Individual button
    ToggleButton individualButton = new ToggleButton("Individual");
    individualButton.setToggleGroup(typeGroup);
    individualButton.setSelected(true);
    individualButton.setPrefWidth(200);
    individualButton.setPrefHeight(40);
    individualButton.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5px; " +
                           "-fx-font-size: 14px; -fx-text-fill: #616161; -fx-cursor: hand;");
    
    // Business button
    ToggleButton businessButton = new ToggleButton("Business");
    businessButton.setToggleGroup(typeGroup);
    businessButton.setPrefWidth(200);
    businessButton.setPrefHeight(40);
    businessButton.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5px; " +
                         "-fx-font-size: 14px; -fx-text-fill: #616161; -fx-cursor: hand;");
    
    // Add selection effect
    individualButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal) {
            individualButton.setStyle("-fx-background-color: #EBF5FB; -fx-border-color: #3949ab; -fx-border-radius: 5px; " +
                                   "-fx-font-size: 14px; -fx-text-fill: #1a237e; -fx-font-weight: bold; -fx-cursor: hand;");
        } else {
            individualButton.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5px; " +
                                   "-fx-font-size: 14px; -fx-text-fill: #616161; -fx-cursor: hand;");
        }
    });
    
    businessButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal) {
            businessButton.setStyle("-fx-background-color: #EBF5FB; -fx-border-color: #3949ab; -fx-border-radius: 5px; " +
                                 "-fx-font-size: 14px; -fx-text-fill: #1a237e; -fx-font-weight: bold; -fx-cursor: hand;");
        } else {
            businessButton.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5px; " +
                                 "-fx-font-size: 14px; -fx-text-fill: #616161; -fx-cursor: hand;");
        }
    });
    
    typeButtons.getChildren().addAll(individualButton, businessButton);
    customerTypeSection.getChildren().addAll(customerTypeLabel, typeButtons);
    
    // Customer Basic Info
    VBox nameBox = createFormFieldWithIcon("", "Full Name", "Enter customer full name", textFieldStyle, labelStyle);
    TextField nameField = (TextField) nameBox.getChildren().get(1);
    
    VBox phoneBox = createFormFieldWithIcon("", "Phone Number", "Enter phone number", textFieldStyle, labelStyle);
    TextField phoneField = (TextField) phoneBox.getChildren().get(1);
    
    VBox emailBox = createFormFieldWithIcon("", "Email Address", "Enter email address", textFieldStyle, labelStyle);
    TextField emailField = (TextField) emailBox.getChildren().get(1);
    
    // TIN field - initially not visible
    VBox tinBox = createFormFieldWithIcon("", "Tax Identification Number", "Enter TIN number", textFieldStyle, labelStyle);
    TextField tpinField = (TextField) tinBox.getChildren().get(1);
    tinBox.setManaged(false);
    tinBox.setVisible(false);
    
    // Address Section
    VBox addressBox = new VBox(8);
    Label addressLabel = new Label("ADDRESS");
    addressLabel.setStyle(labelStyle);
    
    TextArea addressField = new TextArea();
    addressField.setPromptText("Enter full address");
    addressField.setPrefRowCount(3);
    addressField.setWrapText(true);
    addressField.setStyle(textFieldStyle);
    
    addressBox.getChildren().addAll(addressLabel, addressField);
    
    // Show/hide TIN field based on customer type
    businessButton.setOnAction(e -> {
        tinBox.setManaged(true);
        tinBox.setVisible(true);
    });
    
    individualButton.setOnAction(e -> {
        tinBox.setManaged(false);
        tinBox.setVisible(false);
        tinField.clear();
    });
    
    // Add all form sections
    formContainer.getChildren().addAll(
        customerTypeSection,
        createSectionDivider(),
        nameBox,
        phoneBox,
        emailBox,
        tinBox,
        createSectionDivider(),
        addressBox
    );
    
    scrollPane.setContent(formContainer);
    
    // Create the footer with action buttons
    HBox footer = new HBox();
    footer.setPadding(new Insets(15, 25, 15, 25));
    footer.setSpacing(10);
    footer.setAlignment(Pos.CENTER_RIGHT);
    footer.setStyle("-fx-background-color: #f5f5f7; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
    
    Button cancelButton = new Button("Cancel");
    cancelButton.setPrefHeight(40);
    cancelButton.setPrefWidth(100);
    cancelButton.setStyle("-fx-background-color: white; -fx-text-fill: #616161; -fx-border-color: #e0e0e0; " +
                        "-fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-size: 14px; -fx-cursor: hand;");
    
    Button saveButton = new Button("Save Customer");
    saveButton.setPrefHeight(40);
    saveButton.setPrefWidth(150);
    saveButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-font-weight: bold; " +
                      "-fx-background-radius: 5px; -fx-font-size: 14px; -fx-cursor: hand;");
    
    // Hover effects
    saveButton.setOnMouseEntered(e -> 
        saveButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-background-radius: 5px; -fx-font-size: 14px; -fx-cursor: hand;")
    );
    
    saveButton.setOnMouseExited(e -> 
        saveButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-background-radius: 5px; -fx-font-size: 14px; -fx-cursor: hand;")
    );
    
    cancelButton.setOnMouseEntered(e -> 
        cancelButton.setStyle("-fx-background-color: #f5f5f7; -fx-text-fill: #616161; -fx-border-color: #e0e0e0; " +
                           "-fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-size: 14px; -fx-cursor: hand;")
    );
    
    cancelButton.setOnMouseExited(e -> 
        cancelButton.setStyle("-fx-background-color: white; -fx-text-fill: #616161; -fx-border-color: #e0e0e0; " +
                           "-fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-size: 14px; -fx-cursor: hand;")
    );
    
    footer.getChildren().addAll(cancelButton, saveButton);
    
    // Add form and footer to main container
    mainContainer.getChildren().addAll(scrollPane, footer);
    VBox.setVgrow(scrollPane, Priority.ALWAYS);
    
    // Add the main container to the dialog
    dialogPane.setContent(mainContainer);
    
    // Handle dialog buttons
    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
    
    // Hide the default buttons as we're using custom ones
    Button defaultSaveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
    Button defaultCancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
    defaultSaveButton.setVisible(false);
    defaultCancelButton.setVisible(false);
    
    // Add action handlers to our custom buttons
    saveButton.setOnAction(e -> {
        if (validateCustomerForm(nameField, phoneField, emailField, tpinField, businessButton.isSelected())) {
            dialog.setResult(saveButtonType);
            dialog.close();
        }
    });
    
    cancelButton.setOnAction(e -> {
        dialog.setResult(cancelButtonType);
        dialog.close();
    });
    
    // Add focus indicator on active field
    nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal) {
            nameBox.setStyle("-fx-border-color: #3949ab; -fx-border-width: 0 0 0 3; -fx-padding: 0 0 0 10;");
        } else {
            nameBox.setStyle("-fx-border-color: transparent; -fx-border-width: 0 0 0 3; -fx-padding: 0 0 0 10;");
        }
    });
    
    phoneField.focusedProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal) {
            phoneBox.setStyle("-fx-border-color: #3949ab; -fx-border-width: 0 0 0 3; -fx-padding: 0 0 0 10;");
        } else {
            phoneBox.setStyle("-fx-border-color: transparent; -fx-border-width: 0 0 0 3; -fx-padding: 0 0 0 10;");
        }
    });
    
    emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal) {
            emailBox.setStyle("-fx-border-color: #3949ab; -fx-border-width: 0 0 0 3; -fx-padding: 0 0 0 10;");
        } else {
            emailBox.setStyle("-fx-border-color: transparent; -fx-border-width: 0 0 0 3; -fx-padding: 0 0 0 10;");
        }
    });
    
    // Show dialog and process result
    dialog.showAndWait().ifPresent(result -> {
        if (result == saveButtonType) {
            // Save customer details
            String name = nameField.getText();
            String phone = phoneField.getText();
            String email = emailField.getText();
            String type = businessButton.isSelected() ? "Business" : "Individual";
            String tin = tpinField.getText();
            String address = addressField.getText();
            
            Helper.saveCustomerToDatabase(name, phone, email, type, tin, address, "");
            
            // Update the customer name field in checkout panel
            customerNamesField.setText(name);
            
            // If business customer, also update TIN field in checkout panel
            if ("Business".equals(type) && !tin.isEmpty()) {
                tinField.setText(tin);
            }
            
            // Show success notification
            showSuccessNotification("Customer Added", "Customer has been successfully added to the system.");
        }
    });
}

/**
 * Helper method to create a form field with icon
 */
private VBox createFormFieldWithIcon(String icon, String labelText, String promptText, String textFieldStyle, String labelStyle) {
    VBox container = new VBox(8);
    container.setStyle("-fx-border-color: transparent; -fx-border-width: 0 0 0 3; -fx-padding: 0 0 0 10;");
    
    Label label = new Label(labelText);
    label.setStyle(labelStyle);
    
    HBox fieldContainer = new HBox(10);
    fieldContainer.setAlignment(Pos.CENTER_LEFT);
    
    // Create icon (placeholder - replace with actual icon implementation)
    Label iconLabel = new Label(icon);
    iconLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #9e9e9e;");
    
    TextField textField = new TextField();
    textField.setPromptText(promptText);
    textField.setStyle(textFieldStyle);
    HBox.setHgrow(textField, Priority.ALWAYS);
    
    fieldContainer.getChildren().addAll(textField);
    container.getChildren().addAll(label, textField);
    
    return container;
}

/**
 * Creates a visual divider for form sections
 */
private Separator createSectionDivider() {
    Separator separator = new Separator();
    separator.setStyle("-fx-padding: 5 0 5 0;");
    return separator;
}

/**
 * Validate customer form inputs
 */
private boolean validateCustomerForm(TextField nameField, TextField phoneField, TextField emailField, TextField tinField, boolean isBusiness) {
    // Reset error styling
    nameField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                     "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: white; -fx-padding: 10px 15px;");
    
    // Validate name (required)
    if (nameField.getText().trim().isEmpty()) {
        nameField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                        "-fx-border-color: #c62828; -fx-border-width: 1px; -fx-background-color: white; -fx-padding: 10px 15px;");
        
        showErrorNotification("Validation Error", "Customer name is required.");
        return false;
    }
    
    // Validate phone (optional, but if provided must be valid)
    if (!phoneField.getText().trim().isEmpty() && !phoneField.getText().matches("^\\d{10,15}$")) {
        phoneField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                         "-fx-border-color: #c62828; -fx-border-width: 1px; -fx-background-color: white; -fx-padding: 10px 15px;");
        
        showErrorNotification("Validation Error", "Please enter a valid phone number.");
        return false;
    }
    
    // Validate email (optional, but if provided must be valid)
    if (!emailField.getText().trim().isEmpty() && !emailField.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
        emailField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                         "-fx-border-color: #c62828; -fx-border-width: 1px; -fx-background-color: white; -fx-padding: 10px 15px;");
        
        showErrorNotification("Validation Error", "Please enter a valid email address.");
        return false;
    }
    
    // Validate TIN for business customers
    if (isBusiness && tinField.getText().trim().isEmpty()) {
        tinField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                       "-fx-border-color: #c62828; -fx-border-width: 1px; -fx-background-color: white; -fx-padding: 10px 15px;");
        
        showErrorNotification("Validation Error", "TIN is required for business customers.");
        return false;
    }
    
    return true;
}

/**
 * Show success notification
 */
private void showSuccessNotification(String title, String message) {
    // Create a styled notification pane
    StackPane notificationPane = new StackPane();
    notificationPane.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 5px; -fx-padding: 15px; " +
                           "-fx-border-color: #81C784; -fx-border-radius: 5px; -fx-border-width: 1px; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
    
    HBox content = new HBox(15);
    content.setAlignment(Pos.CENTER_LEFT);
    
    // Success icon
    Circle successCircle = new Circle(15);
    successCircle.setFill(Color.web("#4CAF50"));
    
    Text checkIcon = new Text("✓");
    checkIcon.setFill(Color.WHITE);
    checkIcon.setFont(Font.font("System", FontWeight.BOLD, 16));
    
    StackPane iconContainer = new StackPane(successCircle, checkIcon);
    
    // Message content
    VBox messageContainer = new VBox(5);
    Label titleLabel = new Label(title);
    titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
    
    Label messageLabel = new Label(message);
    messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #37474F;");
    
    messageContainer.getChildren().addAll(titleLabel, messageLabel);
    
    content.getChildren().addAll(iconContainer, messageContainer);
    notificationPane.getChildren().add(content);
    
    // Create a popup
    Popup popup = new Popup();
    popup.getContent().add(notificationPane);
    popup.setAutoHide(true);
    
    // Show the popup
    popup.show(stage);
    
    // Position the popup in the bottom right
    popup.setX(stage.getX() + stage.getWidth() - notificationPane.getPrefWidth() - 20);
    popup.setY(stage.getY() + stage.getHeight() - notificationPane.getPrefHeight() - 20);
    
    // Close after 3 seconds
    PauseTransition delay = new PauseTransition(Duration.seconds(3));
    delay.setOnFinished(e -> popup.hide());
    delay.play();
}

/**
 * Show error notification
 */
private void showErrorNotification(String title, String message) {
    // Create a styled notification pane
    StackPane notificationPane = new StackPane();
    notificationPane.setStyle("-fx-background-color: #FFEBEE; -fx-background-radius: 5px; -fx-padding: 15px; " +
                           "-fx-border-color: #EF9A9A; -fx-border-radius: 5px; -fx-border-width: 1px; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
    
    HBox content = new HBox(15);
    content.setAlignment(Pos.CENTER_LEFT);
    
    // Error icon
    Circle errorCircle = new Circle(15);
    errorCircle.setFill(Color.web("#F44336"));
    
    Text errorIcon = new Text("!");
    errorIcon.setFill(Color.WHITE);
    errorIcon.setFont(Font.font("System", FontWeight.BOLD, 16));
    
    StackPane iconContainer = new StackPane(errorCircle, errorIcon);
    
    // Message content
    VBox messageContainer = new VBox(5);
    Label titleLabel = new Label(title);
    titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #C62828;");
    
    Label messageLabel = new Label(message);
    messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #37474F;");
    
    messageContainer.getChildren().addAll(titleLabel, messageLabel);
    
    content.getChildren().addAll(iconContainer, messageContainer);
    notificationPane.getChildren().add(content);
    
    // Create a popup
    Popup popup = new Popup();
    popup.getContent().add(notificationPane);
    popup.setAutoHide(true);
    
    // Show the popup
    popup.show(stage);
    
    // Position the popup in the bottom right
    popup.setX(stage.getX() + stage.getWidth() - notificationPane.getPrefWidth() - 20);
    popup.setY(stage.getY() + stage.getHeight() - notificationPane.getPrefHeight() - 20);
    
    // Close after 3 seconds
    PauseTransition delay = new PauseTransition(Duration.seconds(3));
    delay.setOnFinished(e -> popup.hide());
    delay.play();
}


private void restoreHeldSale(HeldSale sale) {
    // Clear current cart
    cartItems.clear();
    
    // Add items from held sale
    for (Product item : sale.getItems()) {
        cartItems.add(item);
    }

    // Restore discount
    cartDiscountAmount = sale.getCartDiscountAmount();
    cartDiscountPercent = sale.getCartDiscountPercent();
    
    // Update customer info if available
    if (sale.getCustomerName() != null && !sale.getCustomerName().isEmpty()) {
        VBox customerInfoBox = (VBox) ((VBox) root.getRight()).getChildren().get(0).lookup(".customer-info-box");
        if (customerInfoBox != null) {
            ((TextField) customerInfoBox.getChildren().get(1)).setText(sale.getCustomerName());
            ((TextField) customerInfoBox.getChildren().get(2)).setText(sale.getCustomerTIN());
        }
    }

    // Remove the held sale from the database
    boolean deleted = Helper.deleteHeldSaleById(sale.getHoldId());
    if (!deleted) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error");
        errorAlert.setHeaderText("Failed to remove held sale");
        errorAlert.setContentText("The held sale could not be removed from the database.");
        errorAlert.showAndWait();
        return;
    }

    // Update totals
    updateTotals();
    
    // Show confirmation
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Sale Restored");
    alert.setHeaderText(null);
    alert.setContentText("Held sale has been restored successfully.");
    alert.showAndWait();
}

/**
 * Shows the print preview dialog for the current cart
 */
private void showPrintPreview() {
    if (cartItems.isEmpty()) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Empty Cart");
        alert.setHeaderText(null);
        alert.setContentText("Cannot print receipt for empty cart. Please add items to cart first.");
        alert.showAndWait();
        return;
    }
    
    // Calculate totals using the existing logic
    double subtotal = calculateSubtotal();
    double tax = calculateTax();
    double total = calculateTotal();
    
    PrintPreviewDialog printPreview = new PrintPreviewDialog(
        stage, 
        cartItems, 
        subtotal, 
        tax, 
        total, 
        receiptNumber, 
        currentCashier
    );
    
    printPreview.showAndWait();
}

// Helper methods that reuse your existing updateTotals() logic
private double calculateSubtotal() {
    double subtotal = 0.0;
    
    for (Product item : cartItems) {
        double itemPrice = item.getPrice();
        double itemQuantity = item.getQuantity();
        double itemSubtotal = itemPrice * itemQuantity;
        subtotal += itemSubtotal;
    }
    
    // Apply cart-level discount
    double cartLevelDiscount = 0.0;
    if (cartDiscountPercent > 0) {
        cartLevelDiscount = subtotal * (cartDiscountPercent / 100.0);
    } else if (cartDiscountAmount > 0) {
        cartLevelDiscount = Math.min(cartDiscountAmount, subtotal);
    }
    
    // Get tax amount to subtract for net subtotal (same logic as your updateTotals)
    double totalTax = calculateTax();
    
    return subtotal - totalTax; // Net subtotal (excluding VAT)
}

private double calculateTax() {
    double totalTax = 0.0;
    
    for (Product item : cartItems) {
        double itemPrice = item.getPrice();
        double itemQuantity = item.getQuantity();
        double itemSubtotal = itemPrice * itemQuantity;
        
        // Get tax rate from DB using TaxRate ID (same as your updateTotals logic)
        double taxRate = Helper.getTaxRateById(item.getTaxRate());
        boolean isVATRegistered = Helper.isVATRegistered();
        
        // Calculate VAT (same formula as your updateTotals)
        double itemVAT = isVATRegistered ? (itemSubtotal * taxRate) / (100 + taxRate) : 0;
        totalTax += itemVAT;
    }
    
    return totalTax;
}

private double calculateTotal() {
    double subtotal = 0.0;
    
    // Calculate gross subtotal (before cart discount)
    for (Product item : cartItems) {
        double itemPrice = item.getPrice();
        double itemQuantity = item.getQuantity();
        double itemSubtotal = itemPrice * itemQuantity;
        subtotal += itemSubtotal;
    }
    
    // Apply cart-level discount (same logic as updateTotals)
    double cartLevelDiscount = 0.0;
    if (cartDiscountPercent > 0) {
        cartLevelDiscount = subtotal * (cartDiscountPercent / 100.0);
    } else if (cartDiscountAmount > 0) {
        cartLevelDiscount = Math.min(cartDiscountAmount, subtotal);
    }
    
    return subtotal - cartLevelDiscount;
}

// Method to show the Add Note dialog
private void addNote() {
    // Create custom dialog
    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("Add Transaction Note");
    dialog.setHeaderText("Add a note to this transaction");
    dialog.setResizable(true);
    
    // Set the button types
    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
    
    // Create the main content
    VBox content = new VBox(15);
    content.setPadding(new Insets(20));
    content.setPrefWidth(400);
    content.setPrefHeight(300);
    
    // Note type selection
    Label noteTypeLabel = new Label("Note Type:");
    noteTypeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
    
    ComboBox<String> noteTypeComboBox = new ComboBox<>();
    noteTypeComboBox.getItems().addAll(
        "General Note", 
        "Customer Request", 
        "Special Instructions", 
        "Discount Reason", 
        "Return/Exchange Note",
        "Internal Note"
    );
    noteTypeComboBox.setValue("General Note");
    noteTypeComboBox.setPrefHeight(35);
    noteTypeComboBox.setStyle("-fx-font-size: 13px; -fx-background-radius: 5px; -fx-border-color: #1a237e; -fx-border-width: 1px;");
    
    // Note text area
    Label noteTextLabel = new Label("Note Details:");
    noteTextLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
    
    TextArea noteTextArea = new TextArea();
    noteTextArea.setPromptText("Enter your note here...\n\nExample:\n- Customer requested gift wrapping\n- Applied senior citizen discount\n- Special delivery instructions");
    noteTextArea.setPrefRowCount(6);
    noteTextArea.setWrapText(true);
    noteTextArea.setStyle("-fx-font-size: 13px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                         "-fx-border-color: #1a237e; -fx-border-width: 1px; -fx-background-color: white;");
    
    // Set existing note if available
    if (!transactionNote.isEmpty()) {
        // Parse existing note to extract type and text
        String[] noteParts = transactionNote.split(": ", 2);
        if (noteParts.length == 2) {
            String existingType = noteParts[0].replace("[", "").replace("]", "");
            String existingText = noteParts[1];
            
            // Set the combo box value if it matches
            if (noteTypeComboBox.getItems().contains(existingType)) {
                noteTypeComboBox.setValue(existingType);
            }
            noteTextArea.setText(existingText);
        } else {
            noteTextArea.setText(transactionNote);
        }
    }
    
    // Character counter
    Label charCountLabel = new Label("0 / 500 characters");
    charCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
    
    // Add character limit and counter
    noteTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
        int length = newValue.length();
        charCountLabel.setText(length + " / 500 characters");
        
        if (length > 500) {
            noteTextArea.setText(oldValue); // Revert to previous value
            charCountLabel.setText(oldValue.length() + " / 500 characters");
            charCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #c62828;");
        } else {
            charCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        }
        
        // Update character count color as it approaches limit
        if (length > 450) {
            charCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff8f00;");
        } else if (length > 400) {
            charCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        }
    });
    
    // Current note display (if exists)
    VBox currentNoteBox = new VBox(5);
    if (!transactionNote.isEmpty()) {
        Label currentNoteLabel = new Label("Current Note:");
        currentNoteLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        
        Label currentNoteText = new Label(transactionNote);
        currentNoteText.setWrapText(true);
        currentNoteText.setStyle("-fx-font-size: 12px; -fx-text-fill: #616161; " +
                               "-fx-background-color: #e8eaf6; -fx-padding: 8px; " +
                               "-fx-background-radius: 3px; -fx-border-radius: 3px; " +
                               "-fx-border-color: #1a237e; -fx-border-width: 1px;");
        
        currentNoteBox.getChildren().addAll(currentNoteLabel, currentNoteText);
    }
    
    // Quick note buttons for common scenarios
    Label quickNotesLabel = new Label("Quick Notes:");
    quickNotesLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
    
    FlowPane quickNotesPane = new FlowPane();
    quickNotesPane.setHgap(8);
    quickNotesPane.setVgap(5);
    
    String[] quickNotes = {
        "Customer paid exact amount",
        "Gift receipt requested",
        "No bag needed",
        "Senior citizen discount applied",
        "Loyalty points used",
        "Return within 30 days"
    };
    
    for (String quickNote : quickNotes) {
        Button quickButton = new Button(quickNote);
        quickButton.setStyle("-fx-font-size: 11px; -fx-background-color: #e8eaf6; " +
                           "-fx-text-fill: #1a237e; -fx-background-radius: 15px; " +
                           "-fx-padding: 4px 8px; -fx-cursor: hand; " +
                           "-fx-border-color: #1a237e; -fx-border-width: 1px;");
        quickButton.setOnAction(e -> {
            String currentText = noteTextArea.getText();
            String newText = currentText.isEmpty() ? quickNote : currentText + "\n• " + quickNote;
            if (newText.length() <= 500) {
                noteTextArea.setText(newText);
            }
        });
        quickNotesPane.getChildren().add(quickButton);
    }
    
    // Add all components to content
    content.getChildren().addAll(
        noteTypeLabel, noteTypeComboBox,
        noteTextLabel, noteTextArea, charCountLabel,
        currentNoteBox,
        quickNotesLabel, quickNotesPane
    );
    
    // Only show current note box if there's an existing note
    if (transactionNote.isEmpty()) {
        content.getChildren().remove(currentNoteBox);
    }
    
    dialog.getDialogPane().setContent(content);
    
    // Enable/disable the save button based on input
    Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
    saveButton.setDisable(true);
    
    // Style the save button with organization color
    saveButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                       "-fx-background-radius: 5px; -fx-font-weight: bold;");
    
    noteTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
        saveButton.setDisable(newValue.trim().isEmpty());
    });
    
    // Set initial state if there's existing text
    if (!noteTextArea.getText().trim().isEmpty()) {
        saveButton.setDisable(false);
    }
    
    // Convert the result to a note when the save button is clicked
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == saveButtonType) {
            String noteType = noteTypeComboBox.getValue();
            String noteText = noteTextArea.getText().trim();
            if (!noteText.isEmpty()) {
                return "[" + noteType + "]: " + noteText;
            }
        }
        return null;
    });
    
    // Style the dialog with organization theme
    dialog.getDialogPane().setStyle("-fx-background-color: white;");
    
    // Style the header area with organization color
    dialog.getDialogPane().setHeaderText(null); // Clear default header
    Label customHeader = new Label("Add note to this transaction");
    customHeader.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                         "-fx-padding: 15px; -fx-font-weight: bold; -fx-font-size: 14px; " +
                         "-fx-pref-width: 400px; -fx-alignment: center;");
    dialog.getDialogPane().setHeader(customHeader);
    
    // Show dialog and handle result
    Optional<String> result = dialog.showAndWait();
    result.ifPresent(note -> {
        transactionNote = note;
        updateNoteIndicator();
        
        // Show confirmation
        showNoteConfirmation(note);
    });
}

// Method to update the note indicator in the UI
private void updateNoteIndicator() {
    if (noteIndicatorLabel == null) {
        // Create note indicator if it doesn't exist
        noteIndicatorLabel = new Label();
        noteIndicatorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3949ab; " +
                                  "-fx-background-color: #e8eaf6; -fx-padding: 4px 8px; " +
                                  "-fx-background-radius: 10px; -fx-border-radius: 10px;");
        
        // Add to the customer information section (you'll need to modify your createCheckoutPanel method)
        // This should be added after the customer info fields
    }
    
    if (!transactionNote.isEmpty()) {
        // Extract note type for display
        String displayText = "📝 Note Added";
        if (transactionNote.contains("]:")) {
            String noteType = transactionNote.substring(transactionNote.indexOf("[") + 1, transactionNote.indexOf("]"));
            displayText = "📝 " + noteType;
        }
        
        noteIndicatorLabel.setText(displayText);
        noteIndicatorLabel.setVisible(true);
        noteIndicatorLabel.setManaged(true);
        
        // Add tooltip with full note
        Tooltip tooltip = new Tooltip(transactionNote);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(300);
        Tooltip.install(noteIndicatorLabel, tooltip);
    } else {
        noteIndicatorLabel.setVisible(false);
        noteIndicatorLabel.setManaged(false);
    }
}

// Method to show confirmation after adding note
private void showNoteConfirmation(String note) {
    Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
    confirmation.setTitle("Note Added");
    confirmation.setHeaderText("Transaction note has been added successfully");
    
    // Create content with note preview
    VBox content = new VBox(10);
    content.setPadding(new Insets(10));
    
    Label notePreview = new Label(note);
    notePreview.setWrapText(true);
    notePreview.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10px; " +
                        "-fx-background-radius: 5px; -fx-border-radius: 5px;");
    
    Label infoLabel = new Label("This note will be included in the transaction record and receipt.");
    infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
    
    content.getChildren().addAll(notePreview, infoLabel);
    confirmation.getDialogPane().setContent(content);
    
    // Auto-close after 3 seconds
    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> confirmation.close()));
    timeline.play();
    
    confirmation.showAndWait();
}

// Method to clear the note (optional - you might want to add a clear note button)
private void clearNote() {
    Alert confirmClear = new Alert(Alert.AlertType.CONFIRMATION);
    confirmClear.setTitle("Clear Note");
    confirmClear.setHeaderText("Are you sure you want to clear the transaction note?");
    confirmClear.setContentText("This action cannot be undone.");
    
    Optional<ButtonType> result = confirmClear.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
        transactionNote = "";
        updateNoteIndicator();
    }
}

// Method to get the current note (for use in receipt generation or transaction saving)
public String getTransactionNote() {
    return transactionNote;
}

private VBox createCashAmountSection() {
    VBox cashAmountBox = new VBox(8);
    Label cashAmountLabel = new Label("Cash Amount");
    cashAmountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #424242;");
    
    // Create HBox to hold the text field and number pad button - FIXED ALIGNMENT
    HBox cashInputBox = new HBox(8); // Increased spacing for better look
    cashInputBox.setAlignment(Pos.CENTER_LEFT); // Ensure proper alignment
    
    // Cash amount field
    cashAmountField = new TextField();
    cashAmountField.setPromptText("Enter amount");
    cashAmountField.setPrefHeight(40);
    cashAmountField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                          "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: white;");
    
    // Number pad button - FIXED SIZE AND STYLE
    Button numberPadButton = new Button("🔢");
    numberPadButton.setPrefHeight(40);
    numberPadButton.setPrefWidth(45); // Slightly smaller for better proportion
    numberPadButton.setMinWidth(45);
    numberPadButton.setMaxWidth(45);
    numberPadButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; " +
                            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; " +
                            "-fx-background-radius: 5px; -fx-border-radius: 5px;");
    
    // Hover effects
    numberPadButton.setOnMouseEntered(e -> 
        numberPadButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; " +
                               "-fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; " +
                               "-fx-background-radius: 5px; -fx-border-radius: 5px;")
    );
    
    numberPadButton.setOnMouseExited(e -> 
        numberPadButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; " +
                               "-fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; " +
                               "-fx-background-radius: 5px; -fx-border-radius: 5px;")
    );
    
    numberPadButton.setOnAction(e -> {
        activeField = cashAmountField;
        showNumberPadDialog();
    });
    
    // Add field and button to HBox - FIELD TAKES REMAINING SPACE
    cashInputBox.getChildren().addAll(cashAmountField, numberPadButton);
    HBox.setHgrow(cashAmountField, Priority.ALWAYS); // Field takes available space
    HBox.setHgrow(numberPadButton, Priority.NEVER);  // Button stays fixed size
    
    // Validation listener
    cashAmountField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue.matches("\\d*\\.?\\d*")) {
            cashAmountField.setText(oldValue);
        } else {
            updateChangeCalculation();
        }
    });
    
    // Change display
    Label changeLabel = new Label("Change:");
    changeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #424242;");
    
    changeValueLabel = new Label(formatCurrency(0.0));
    changeValueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #009688;");
    
    HBox changeBox = new HBox(10);
    changeBox.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    changeBox.getChildren().addAll(spacer, changeLabel, changeValueLabel);
    
    // Add all components
    cashAmountBox.getChildren().addAll(cashAmountLabel, cashInputBox, changeBox);
    return cashAmountBox;
}

/**
 * STEP 3: Shows the number pad dialog - FIXED CENTERING
 */
private void showNumberPadDialog() {
    if (numberPadDialog != null && numberPadDialog.isShowing()) {
        numberPadDialog.toFront();
        updateDisplayField();
        return;
    }
    
    createNumberPadDialog();
    
    // FIXED CENTERING - Center the dialog on the screen
    if (activeField != null && activeField.getScene() != null) {
        Stage primaryStage = (Stage) activeField.getScene().getWindow();
        
        // Show dialog first to get its dimensions
        numberPadDialog.show();
        
        // Center on primary stage
        double centerX = primaryStage.getX() + (primaryStage.getWidth() / 2) - (numberPadDialog.getWidth() / 2);
        double centerY = primaryStage.getY() + (primaryStage.getHeight() / 2) - (numberPadDialog.getHeight() / 2);
        
        numberPadDialog.setX(centerX);
        numberPadDialog.setY(centerY);
    } else {
        numberPadDialog.show();
        // Fallback - center on screen
        numberPadDialog.centerOnScreen();
    }
}

/**
 * STEP 4: Creates the number pad dialog - FIXED WIDTH AND WHITE BUTTONS
 */
private void createNumberPadDialog() {
    numberPadDialog = new Stage();
    numberPadDialog.initModality(Modality.NONE);
    numberPadDialog.setTitle("POS Number Pad");
    numberPadDialog.setResizable(false);
    numberPadDialog.setAlwaysOnTop(true);
    
    // FIXED DIALOG CONTENT - INCREASED WIDTH
    VBox dialogContent = new VBox(20);
    dialogContent.setPadding(new Insets(25));
    dialogContent.setStyle("-fx-background-color: #f5f5f5; " +
                          "-fx-background-radius: 10px; -fx-border-color: #ddd; " +
                          "-fx-border-width: 2px; -fx-border-radius: 10px;");
    dialogContent.setPrefWidth(400); // INCREASED WIDTH
    dialogContent.setMinWidth(400);
    
    // Header
    HBox titleBar = new HBox();
    titleBar.setAlignment(Pos.CENTER_LEFT);
    titleBar.setPadding(new Insets(15, 20, 15, 20));
    titleBar.setStyle("-fx-background-color: #2196f3; -fx-background-radius: 8px;");
    
    Label titleLabel = new Label("CASH AMOUNT ENTRY");
    titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
    
    Region titleSpacer = new Region();
    HBox.setHgrow(titleSpacer, Priority.ALWAYS);
    
    Button closeButton = new Button("✕");
    closeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                        "-fx-font-size: 16px; -fx-font-weight: bold; " +
                        "-fx-pref-width: 35px; -fx-pref-height: 35px; " +
                        "-fx-background-radius: 50%; -fx-cursor: hand;");
    closeButton.setOnAction(e -> numberPadDialog.hide());
    
    titleBar.getChildren().addAll(titleLabel, titleSpacer, closeButton);
    
    // Display field
    TextField displayField = new TextField();
    displayField.setId("displayField");
    displayField.setEditable(false);
    displayField.setPrefHeight(60);
    displayField.setStyle("-fx-background-color: #000; -fx-border-color: #4caf50; " +
                         "-fx-border-width: 2px; -fx-border-radius: 8px; " +
                         "-fx-text-fill: #4caf50; -fx-font-size: 24px; -fx-font-weight: bold; " +
                         "-fx-font-family: 'Courier New', monospace; -fx-alignment: center-right;");
    
    updateDisplayField();
    
    // FIXED NUMBER PAD GRID - WIDER BUTTONS
    GridPane buttonGrid = new GridPane();
    buttonGrid.setHgap(15); // Increased spacing
    buttonGrid.setVgap(15);
    buttonGrid.setAlignment(Pos.CENTER);
    
    // Number buttons layout (7-9, 4-6, 1-3)
    int[][] layout = {{7, 8, 9}, {4, 5, 6}, {1, 2, 3}};
    
    for (int row = 0; row < 3; row++) {
        for (int col = 0; col < 3; col++) {
            Button numberButton = createWhiteNumberButton(String.valueOf(layout[row][col]));
            buttonGrid.add(numberButton, col, row);
        }
    }
    
    // Bottom row: CLEAR, 0, DECIMAL
    Button clearButton = new Button("CLEAR");
    clearButton.setPrefSize(110, 60); // WIDER BUTTONS
    clearButton.setStyle("-fx-background-color: white; -fx-text-fill: #f44336; " +
                        "-fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-border-color: #f44336; -fx-border-width: 2px; " +
                        "-fx-background-radius: 8px; -fx-border-radius: 8px; -fx-cursor: hand;");
    clearButton.setOnAction(e -> {
        clearActiveField();
        updateDisplayField();
    });
    addWhiteButtonHoverEffect(clearButton, "#f44336");
    
    Button zeroButton = createWhiteNumberButton("0");
    Button decimalButton = createWhiteNumberButton(".");
    
    buttonGrid.add(clearButton, 0, 3);
    buttonGrid.add(zeroButton, 1, 3);
    buttonGrid.add(decimalButton, 2, 3);
    
    // Action buttons
    HBox actionButtons = new HBox(15);
    actionButtons.setAlignment(Pos.CENTER);
    
    Button backspaceButton = new Button("⌫ DELETE");
    backspaceButton.setPrefSize(170, 50); // WIDER BUTTONS
    backspaceButton.setStyle("-fx-background-color: white; -fx-text-fill: #ff9800; " +
                            "-fx-font-size: 14px; -fx-font-weight: bold; " +
                            "-fx-border-color: #ff9800; -fx-border-width: 2px; " +
                            "-fx-background-radius: 8px; -fx-border-radius: 8px; -fx-cursor: hand;");
    backspaceButton.setOnAction(e -> {
        backspaceActiveField();
        updateDisplayField();
    });
    addWhiteButtonHoverEffect(backspaceButton, "#ff9800");
    
    doneButton = new Button("✓ PROCESS PAYMENT");
    doneButton.setPrefSize(200, 50); // WIDER BUTTONS
    doneButton.setStyle("-fx-background-color: white; -fx-text-fill: #4caf50; " +
                       "-fx-font-size: 14px; -fx-font-weight: bold; " +
                       "-fx-border-color: #4caf50; -fx-border-width: 2px; " +
                       "-fx-background-radius: 8px; -fx-border-radius: 8px; -fx-cursor: hand;");
    doneButton.setOnAction(e -> {
    processPayment();
    numberPadDialog.hide();
});
     doneButton.setDisable(true);

    addWhiteButtonHoverEffect(doneButton, "#4caf50");
    
    actionButtons.getChildren().addAll(backspaceButton, doneButton);
    
    // Add all components
    dialogContent.getChildren().addAll(titleBar, displayField, buttonGrid, actionButtons);
    
    Scene scene = new Scene(dialogContent);
    numberPadDialog.setScene(scene);
}

/**
 * STEP 5: Creates white number buttons
 */
private Button createWhiteNumberButton(String text) {
    Button button = new Button(text);
    button.setPrefSize(110, 60); // WIDER BUTTONS
    button.setStyle("-fx-background-color: white; -fx-text-fill: #333; " +
                   "-fx-font-size: 20px; -fx-font-weight: bold; " +
                   "-fx-border-color: #ddd; -fx-border-width: 2px; " +
                   "-fx-background-radius: 8px; -fx-border-radius: 8px; -fx-cursor: hand;");
    
    button.setOnAction(e -> {
        appendToActiveField(text);
        updateDisplayField();
    });
    
    addWhiteButtonHoverEffect(button, "#2196f3");
    return button;
}

/**
 * STEP 6: White button hover effects
 */
private void addWhiteButtonHoverEffect(Button button, String hoverColor) {
    String originalStyle = button.getStyle();
    
    button.setOnMouseEntered(e -> 
        button.setStyle("-fx-background-color: " + hoverColor + "; -fx-text-fill: white; " +
                       "-fx-font-size: " + (button.getText().length() > 3 ? "14px" : "20px") + "; " +
                       "-fx-font-weight: bold; -fx-background-radius: 8px; -fx-border-radius: 8px; " +
                       "-fx-cursor: hand; -fx-border-color: " + hoverColor + "; -fx-border-width: 2px;")
    );
    
    button.setOnMouseExited(e -> button.setStyle(originalStyle));
}

/**
 * STEP 7: Helper methods (unchanged)
 */
private void updateDisplayField() {
    if (numberPadDialog != null && numberPadDialog.getScene() != null) {
        TextField displayField = (TextField) numberPadDialog.getScene().lookup("#displayField");
        if (displayField != null && activeField != null) {
            displayField.setText(activeField.getText());
        }
    }
}

private void appendToActiveField(String text) {
    if (activeField != null) {
        String currentText = activeField.getText();
        
        // Prevent multiple decimal points
        if (text.equals(".") && currentText.contains(".")) {
            return;
        }
        
        activeField.setText(currentText + text);
        activeField.positionCaret(activeField.getText().length());
    }
}

private void clearActiveField() {
    if (activeField != null) {
        activeField.clear();
    }
}

private void backspaceActiveField() {
    if (activeField != null && !activeField.getText().isEmpty()) {
        String currentText = activeField.getText();
        activeField.setText(currentText.substring(0, currentText.length() - 1));
        activeField.positionCaret(activeField.getText().length());
    }
}

private List<LineItemDto> createLineItems() {
    List<LineItemDto> lineItems = new ArrayList<>();
    
    for (Product product : cartItems) {
        LineItemDto lineItemDto = Helper.convertProductToLineItemDto(product);
        
        // If VAT5 exempt, zero out the VAT amounts
        if (isVat5Exempt) {
            lineItemDto.setTotalVAT(0.0);
            lineItemDto.setTaxRateId(lineItemDto.getTaxRateId());
            // Recalculate total without VAT
            double itemTotal = lineItemDto.getUnitPrice() * lineItemDto.getQuantity();
            double discountAmount = itemTotal * (lineItemDto.getDiscount() / 100.0);
            lineItemDto.setTotal(itemTotal - discountAmount);
        }
        
        lineItems.add(lineItemDto);
    }
    
    return lineItems;
}

// Method to check if there's a note
public boolean hasTransactionNote() {
    return !transactionNote.isEmpty();
}
    /**
     * Handles the logout action
     */
    private void handleLogout() {
    // Confirm before logout
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Logout");
    alert.setHeaderText("Confirm Logout");
    alert.setContentText("Are you sure you want to logout?");

    if (alert.showAndWait().get() == ButtonType.OK) {
        try {
            // Close current stage
            Stage currentStage = (Stage) stage.getScene().getWindow();
            currentStage.close();

            // Restart application with LoginView
            Platform.runLater(() -> {
                try {
                    new LoginView().start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unable to return to login screen.");
        }
    }
}
    
         
    public static void main(String[] args) {
        launch(args);
    }
}

