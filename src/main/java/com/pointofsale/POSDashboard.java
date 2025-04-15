package com.pointofsale;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class POSDashboard extends Application {

    // Main application components
    private Stage stage;
    private BorderPane root;
    private TableView<CartItem> cartTable;
    private Label totalAmountLabel;
    private TextField barcodeField;
    private Label dateTimeLabel;
    private Label cashierNameLabel;
    private ComboBox<String> paymentMethodComboBox;
    
    // Sample data
    private ObservableList<CartItem> cartItems;
    private double totalAmount = 0.0;
    private String currentCashier = "John Doe";
    private String receiptNumber = "R-29871";

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        createDashboard();
        startTimeUpdater();
        stage.show();
    }

    /**
     * Creates the entire dashboard with all components
     */
    private void createDashboard() {
        // Initialize cart items list
        cartItems = FXCollections.observableArrayList();
        
        // Create main layout
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f7;");
        
        // Set up the different regions of the dashboard
        root.setTop(createTopBar());
        root.setLeft(createSidebar());
        root.setCenter(createMainContent());
        root.setRight(createCheckoutPanel());
        
        // Create scene and set to stage
        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("POS System - Sales Dashboard");
        stage.setMaximized(true);
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
        
        // Logo/System name
        Label systemLabel = new Label("POS SYSTEM");
        systemLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        
        // Separator
        Separator separator1 = new Separator();
        separator1.setOrientation(javafx.geometry.Orientation.VERTICAL);
        separator1.setPrefHeight(30);
        
        // Transaction info
        VBox transactionInfo = new VBox(2);
        Label receiptLabel = new Label("Receipt #:");
        receiptLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        Label receiptNumberLabel = new Label(receiptNumber);
        receiptNumberLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        transactionInfo.getChildren().addAll(receiptLabel, receiptNumberLabel);
        
        // Date and time
        VBox dateTimeBox = new VBox(2);
        Label dateTimeTextLabel = new Label("Date & Time:");
        dateTimeTextLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        dateTimeLabel = new Label(getCurrentDateTime());
        dateTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        dateTimeBox.getChildren().addAll(dateTimeTextLabel, dateTimeLabel);
        
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
        Label cashierLabel = new Label("Cashier:");
        cashierLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        cashierNameLabel = new Label(currentCashier);
        cashierNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        cashierDetails.getChildren().addAll(cashierLabel, cashierNameLabel);
        
        cashierInfo.getChildren().addAll(avatarPane, cashierDetails);
        
        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3949ab; -fx-font-weight: bold; -fx-cursor: hand;");
        logoutButton.setOnAction(e -> handleLogout());
        
        // Add all components to the top bar
        topBar.getChildren().addAll(systemLabel, separator1, transactionInfo, dateTimeBox, spacer, cashierInfo, logoutButton);
        
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
        sidebar.getChildren().add(createSidebarMenuItem("New Sale", true));
        sidebar.getChildren().add(createSidebarMenuItem("Products", false));
        sidebar.getChildren().add(createSidebarMenuItem("Transactions", false));
        sidebar.getChildren().add(createSidebarMenuItem("Customers", false));
        sidebar.getChildren().add(createSidebarMenuItem("Reports", false));
        sidebar.getChildren().add(createSidebarMenuItem("Settings", false));
        
        // Add spacer to push help to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);
        
        // Help and support
        sidebar.getChildren().add(createSidebarMenuItem("Help & Support", false));
        
        return sidebar;
    }
    
    /**
     * Creates the header for the sidebar
     */
    private VBox createSidebarHeader() {
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(25, 0, 25, 0));
        
        // Create a placeholder logo
        Rectangle placeholderLogo = new Rectangle(50, 50);
        placeholderLogo.setFill(Color.WHITE);
        placeholderLogo.setOpacity(0.9);
        placeholderLogo.setArcWidth(10);
        placeholderLogo.setArcHeight(10);
        
        Label logoText = new Label("POS");
        logoText.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3949ab;");
        
        StackPane logoStack = new StackPane(placeholderLogo, logoText);
        header.getChildren().add(logoStack);
        
        return header;
    }
    
    /**
     * Creates individual menu items for the sidebar
     */
    private HBox createSidebarMenuItem(String text, boolean isSelected) {
        HBox menuItem = new HBox();
        menuItem.setAlignment(Pos.CENTER_LEFT);
        menuItem.setPadding(new Insets(15, 20, 15, 20));
        menuItem.setCursor(javafx.scene.Cursor.HAND);
        
        if (isSelected) {
            menuItem.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2);");
        } else {
            menuItem.setStyle("-fx-background-color: transparent;");
            // Add hover effect
            menuItem.setOnMouseEntered(e -> 
                menuItem.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1);")
            );
            menuItem.setOnMouseExited(e -> 
                menuItem.setStyle("-fx-background-color: transparent;")
            );
        }
        
        // Menu icon (simplified with a rectangle for now)
        Rectangle icon = new Rectangle(18, 18);
        icon.setFill(Color.WHITE);
        icon.setOpacity(0.8);
        icon.setArcWidth(4);
        icon.setArcHeight(4);
        
        // Menu text
        Label menuText = new Label(text);
        menuText.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: " + (isSelected ? "bold" : "normal") + ";");
        HBox.setMargin(menuText, new Insets(0, 0, 0, 15));
        
        menuItem.getChildren().addAll(icon, menuText);
        
        // Selection indicator for active menu
        if (isSelected) {
            Rectangle indicator = new Rectangle(5, 25);
            indicator.setFill(Color.WHITE);
            indicator.setArcWidth(2);
            indicator.setArcHeight(2);
            
            // Position indicator at the left edge
            StackPane indicatorContainer = new StackPane(indicator);
            indicatorContainer.setAlignment(Pos.CENTER_LEFT);
            indicatorContainer.setPadding(new Insets(0, 0, 0, -20));
            
            menuItem.getChildren().add(indicatorContainer);
        }
        
        return menuItem;
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
        
        // Set action to open the Product Lookup dialog
        lookupButton.setOnAction(e -> {
          ProductLookupDialog lookupDialog = new ProductLookupDialog(stage);
          ProductLookupDialog.Product selectedProduct = lookupDialog.showAndSelect();

          if (selectedProduct != null) {
          // Handle the selected product (add to cart, show details, etc.)
          System.out.println("Selected product: " + selectedProduct.getName());
          // TODO: Add logic to handle the selected product
          }
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
        TableColumn<CartItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        
        TableColumn<CartItem, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);
        
        TableColumn<CartItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);
        priceCol.setCellFactory(col -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(formatCurrency(price));
                }
            }
        });
        
        TableColumn<CartItem, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(70);
        
        TableColumn<CartItem, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setPrefWidth(120);
        totalCol.setCellFactory(col -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(formatCurrency(total));
                }
            }
        });
        
        TableColumn<CartItem, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(col -> new TableCell<CartItem, Void>() {
            private final Button deleteButton = new Button("Remove");
            
            {
                deleteButton.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3px;");
                deleteButton.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
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
        
        cartTable.getColumns().addAll(idCol, nameCol, priceCol, qtyCol, totalCol, actionCol);
        
        cartSection.getChildren().addAll(cartHeader, cartTable);
        
        // Add all sections to main content
        mainContent.getChildren().addAll(searchSection, cartSection);
        VBox.setVgrow(cartSection, Priority.ALWAYS);
        
        return mainContent;
    }
    
    /**
     * Creates the checkout panel on the right side
     */
    private VBox createCheckoutPanel() {
        VBox checkoutPanel = new VBox(15);
        checkoutPanel.setPrefWidth(300);
        checkoutPanel.setPadding(new Insets(20, 20, 20, 0));
        
        // Create card panel with shadow effect
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        
        // Payment summary title
        Label summaryTitle = new Label("Payment Summary");
        summaryTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #424242;");
        
        // Separator
        Separator separator = new Separator();
        
        // Subtotal section
        GridPane summaryGrid = new GridPane();
        summaryGrid.setVgap(12);
        summaryGrid.setHgap(10);
        
        int row = 0;
        
        // Add subtotal row
        Label subtotalLabel = new Label("Subtotal:");
        subtotalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
        Label subtotalValueLabel = new Label(formatCurrency(0.0));
        subtotalValueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #424242; -fx-font-weight: bold;");
        summaryGrid.add(subtotalLabel, 0, row);
        summaryGrid.add(subtotalValueLabel, 1, row);
        row++;
        
        // Add discount row
        Label discountLabel = new Label("Discount:");
        discountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
        Label discountValueLabel = new Label(formatCurrency(0.0));
        discountValueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #c62828; -fx-font-weight: bold;");
        summaryGrid.add(discountLabel, 0, row);
        summaryGrid.add(discountValueLabel, 1, row);
        row++;
        
        // Add tax row
        Label taxLabel = new Label("Tax (10%):");
        taxLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
        Label taxValueLabel = new Label(formatCurrency(0.0));
        taxValueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #424242; -fx-font-weight: bold;");
        summaryGrid.add(taxLabel, 0, row);
        summaryGrid.add(taxValueLabel, 1, row);
        row++;
        
        // Add separator for total
        Separator totalSeparator = new Separator();
        GridPane.setColumnSpan(totalSeparator, 2);
        summaryGrid.add(totalSeparator, 0, row);
        row++;
        
        // Add total row
        Label totalLabel = new Label("Total:");
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #424242;");
        totalAmountLabel = new Label(formatCurrency(0.0));
        totalAmountLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        summaryGrid.add(totalLabel, 0, row);
        summaryGrid.add(totalAmountLabel, 1, row);
        
        // Column constraints to push values to the right
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHalignment(javafx.geometry.HPos.RIGHT);
        
        summaryGrid.getColumnConstraints().addAll(col1, col2);
        
        // Payment method selector
        VBox paymentMethodBox = new VBox(8);
        Label paymentMethodLabel = new Label("Payment Method");
        paymentMethodLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #424242;");
        
        paymentMethodComboBox = new ComboBox<>();
        paymentMethodComboBox.getItems().addAll("Cash", "Credit Card", "Debit Card", "Mobile Payment");
        paymentMethodComboBox.setValue("Cash");
        paymentMethodComboBox.setPrefWidth(Double.MAX_VALUE);
        paymentMethodComboBox.setPrefHeight(40);
        paymentMethodComboBox.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px;");
        
        paymentMethodBox.getChildren().addAll(paymentMethodLabel, paymentMethodComboBox);
        
        // Cash amount (visible only for cash payment)
        VBox cashAmountBox = new VBox(8);
        Label cashAmountLabel = new Label("Cash Amount");
        cashAmountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #424242;");
        
        TextField cashAmountField = new TextField();
        cashAmountField.setPromptText("Enter amount");
        cashAmountField.setPrefHeight(40);
        cashAmountField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                              "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: white;");
        
        cashAmountBox.getChildren().addAll(cashAmountLabel, cashAmountField);
        
        // Process payment button
        Button processPaymentButton = new Button("PROCESS PAYMENT");
        processPaymentButton.setPrefHeight(50);
        processPaymentButton.setPrefWidth(Double.MAX_VALUE);
        processPaymentButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                                   "-fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; " +
                                   "-fx-background-radius: 5px;");
        
        // Add hover effect
        processPaymentButton.setOnMouseEntered(e -> 
            processPaymentButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; " +
                                       "-fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; " +
                                       "-fx-background-radius: 5px;")
        );
        
        processPaymentButton.setOnMouseExited(e -> 
            processPaymentButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                                       "-fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; " +
                                       "-fx-background-radius: 5px;")
        );
        
        processPaymentButton.setOnAction(e -> processPayment());
        
        // Additional customer actions panel
        VBox customerActionsPanel = new VBox(10);
        customerActionsPanel.setPadding(new Insets(20, 0, 0, 0));
        
        Label customerActionsTitle = new Label("Customer Actions");
        customerActionsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #424242;");
        
        Button addCustomerButton = createSecondaryActionButton("Add Customer");
        Button addNoteButton = createSecondaryActionButton("Add Note");
        Button holdSaleButton = createSecondaryActionButton("Hold Sale");
        Button printReceiptButton = createSecondaryActionButton("Print Preview");
        
        customerActionsPanel.getChildren().addAll(customerActionsTitle, addCustomerButton, addNoteButton, holdSaleButton, printReceiptButton);
        
        // Add all elements to the card
        card.getChildren().addAll(summaryTitle, separator, summaryGrid, paymentMethodBox, cashAmountBox, processPaymentButton);
        
        // Add card and customer actions to checkout panel
        checkoutPanel.getChildren().addAll(card, customerActionsPanel);
        
        // Make sure the sidebar and panel stay at the top
        VBox.setVgrow(card, Priority.NEVER);
        VBox.setVgrow(customerActionsPanel, Priority.NEVER);
        
        return checkoutPanel;
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
     * Creates a styled secondary action button for customer actions
     */
    private Button createSecondaryActionButton(String text) {
        Button button = new Button(text);
        button.setPrefHeight(35);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-background-color: white; -fx-text-fill: #3949ab; " +
                      "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; " +
                      "-fx-background-radius: 5px; -fx-border-color: #3949ab; -fx-border-radius: 5px;");
        
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
        
        // For demonstration, add sample products
        CartItem newItem;
        
        // Simulate different products based on input
        if (barcode.equalsIgnoreCase("apple") || barcode.equals("1001")) {
            newItem = new CartItem(1001, "Fresh Apple", 1.25, 1);
        } else if (barcode.equalsIgnoreCase("water") || barcode.equals("1002")) {
            newItem = new CartItem(1002, "Mineral Water 500ml", 1.50, 1);
        } else if (barcode.equalsIgnoreCase("bread") || barcode.equals("1003")) {
            newItem = new CartItem(1003, "Whole Grain Bread", 3.75, 1);
        } else if (barcode.equalsIgnoreCase("milk") || barcode.equals("1004")) {
            newItem = new CartItem(1004, "Organic Milk 1L", 2.99, 1);
        } else {
            // Generic product for any other input
            newItem = new CartItem(1000 + cartItems.size(), "Product " + barcode, 10.00, 1);
        }
        
        // Check if item already exists in cart
        boolean found = false;
        for (CartItem item : cartItems) {
            if (item.getId() == newItem.getId()) {
                // Increase quantity
                item.setQuantity(item.getQuantity() + 1);
                item.updateTotal();
                found = true;
                break;
            }
        }
        
        // Add new item if not found
       if (!found) {
       cartItems.add(newItem);
       }

      // Update cart and totals
      cartTable.refresh();
      updateTotals();
      barcodeField.clear();
      barcodeField.requestFocus();
        
    }
    
    /**
     * Removes an item from the cart
     */
    private void removeItemFromCart(CartItem item) {
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
     * Updates the total amounts in the checkout panel
     */
    private void updateTotals() {
        // Calculate subtotal
        double subtotal = 0.0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotal();
        }
        
        // Calculate tax and total
        double tax = subtotal * 0.10; // 10% tax
        double total = subtotal + tax;
        
        // Update labels in the checkout panel
        totalAmount = total;
        totalAmountLabel.setText(formatCurrency(total));
        
        // Update item count in cart header
        int itemCount = cartItems.size();
        ((Label) ((HBox) ((VBox) cartTable.getParent()).getChildren().get(0)).getChildren().get(1))
            .setText("(" + itemCount + " item" + (itemCount == 1 ? "" : "s") + ")");
    }
    
    /**
     * Processes the payment
     */
    private void processPayment() {
        if (cartItems.isEmpty()) {
            showAlert("Error", "Cart is empty. Please add items before processing payment.");
            return;
        }
        
        // Show payment processing feedback
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment Processing");
        alert.setHeaderText("Processing Payment");
        alert.setContentText("Processing " + paymentMethodComboBox.getValue() + " payment for " + 
                          formatCurrency(totalAmount) + "...");
        alert.showAndWait();
        
        // Show payment success and generate new receipt
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment Complete");
        alert.setHeaderText("Payment Successful!");
        alert.setContentText("Payment of " + formatCurrency(totalAmount) + " was processed successfully.\n\n" +
                          "Receipt #" + receiptNumber + " has been printed.");
        alert.showAndWait();
        
        // Reset cart and generate new receipt number
        cartItems.clear();
        updateTotals();
        generateNewReceiptNumber();
    }
    
    /**
     * Generates a new receipt number
     */
    private void generateNewReceiptNumber() {
        // Generate a semi-random receipt number for demonstration
        int randomDigits = (int) (Math.random() * 10000);
        receiptNumber = "R-" + String.format("%05d", randomDigits);
        
        // Update the receipt number in the top bar
        ((Label) ((VBox) ((HBox) root.getTop()).getChildren().get(2)).getChildren().get(1))
            .setText(receiptNumber);
    }
    
    /**
     * Formats a number as currency
     */
    private String formatCurrency(double amount) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
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
            // In a real application, this would redirect to the login screen
            showAlert("Logout", "You have been logged out successfully.");
            stage.close();
        }
    }
    
    /**
     * Shows an alert dialog with the given title and message
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Inner class for cart item data
     */
    public static class CartItem {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleDoubleProperty price;
        private final SimpleIntegerProperty quantity;
        private final SimpleDoubleProperty total;
        
        public CartItem(int id, String name, double price, int quantity) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.total = new SimpleDoubleProperty(price * quantity);
        }
        
        public void updateTotal() {
            this.total.set(this.price.get() * this.quantity.get());
        }
        
        // Getters and setters
        public int getId() { return id.get(); }
        public void setId(int id) { this.id.set(id); }
        
        public String getName() { return name.get(); }
        public void setName(String name) { this.name.set(name); }
        
        public double getPrice() { return price.get(); }
        public void setPrice(double price) { 
            this.price.set(price);
            updateTotal();
        }
        
        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int quantity) { 
            this.quantity.set(quantity);
            updateTotal();
        }
        
        public double getTotal() { return total.get(); }
    }
    
    // Main method for testing the dashboard
    public static void main(String[] args) {
        launch(args);
    }
}