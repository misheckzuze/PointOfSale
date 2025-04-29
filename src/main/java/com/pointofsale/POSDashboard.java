package com.pointofsale;

import com.pointofsale.helper.ApiClient;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import java.time.LocalDate;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.pointofsale.model.Session;
import com.pointofsale.model.InvoiceSummary;
import com.pointofsale.model.InvoiceDetails;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import com.pointofsale.model.Product;
import com.pointofsale.model.LineItemDto;
import com.pointofsale.model.InvoiceHeader;
import com.pointofsale.model.InvoicePayload;
import com.pointofsale.model.TaxBreakDown;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import com.pointofsale.helper.Helper;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.Node;
import java.util.ArrayList;
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
    private Label cashierNameLabel;
    private Label changeValueLabel;
    private TextField cashAmountField;
    private Button processPaymentButton;
    private Label taxLabel;
    private Node currentContent;
    private ComboBox<String> paymentMethodComboBox;
    private double cartDiscountAmount = 0.0;
    private double cartDiscountPercent = 0.0;
    private Label discountValueLabel;
    
    // Sample data
    private ObservableList<Product> cartItems;
    private double totalAmount = 0.0;
    private String currentCashier = Session.firstName + " " + Session.lastName;
    private String role = Session.role;
    private String receiptNumber =  generateNewReceiptNumber();


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
        // Initialize cart items list if needed
        cartItems = FXCollections.observableArrayList();
        
        // Create main layout
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f7;");
        
        // Set up the different regions of the dashboard
        root.setTop(createTopBar());
        root.setLeft(createSidebar());
        
        // Load the default content (Sales Dashboard)
        loadSalesDashboard();
        
        // Create scene and set to stage
        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("POS System");
        stage.setMaximized(true);
    }
    
     // Methods to load different content areas
    private void loadSalesDashboard() {
        // Clear current right panel (checkout) if any
        root.setRight(createCheckoutPanel());
        
        // Load sales dashboard content
        Node salesContent = createMainContent(); // Your existing method for sales dashboard
        setContent(salesContent);
        stage.setTitle("POS System - Sales Dashboard");
    }
    
private void loadProductsManagement() {
    // Clear checkout panel when switching to non-sales views
    root.setRight(null);
    
    // Create product management content
    ProductManagement productManagement = new ProductManagement();
    Node productsContent = productManagement.createContent();
    
    // Set the content
    setContent(productsContent);
    stage.setTitle("POS System - Products Management");
}
private void loadTransactions() {
    // Clear checkout panel when switching to non-sales views
    root.setRight(null);
    
    // Create product management content
    TransactionsView transactions = new TransactionsView();
    Node productsContent = transactions.getView();
    
    // Set the content
    setContent(productsContent);
    stage.setTitle("POS System - Transactions");
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
        Label cashierLabel = new Label(role);
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
   // Sales dashboard menu item
    HBox salesMenuItem = createSidebarMenuItem("New Sale", true);
    salesMenuItem.setOnMouseClicked(event -> loadSalesDashboard());
    sidebar.getChildren().add(salesMenuItem);
    
    // Products menu item
    HBox productsMenuItem = createSidebarMenuItem("Products", false);
    productsMenuItem.setOnMouseClicked(event -> loadProductsManagement());
    sidebar.getChildren().add(productsMenuItem);
    
    HBox transactionsMenuItem = createSidebarMenuItem("Transactions", false);
    transactionsMenuItem.setOnMouseClicked(event -> loadTransactions());
    sidebar.getChildren().add(transactionsMenuItem);
      
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

// You may need to add this helper method if it doesn't exist already
private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
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
        
        discountButton.setOnAction(e -> applyDiscount());
        
        // Add this event handler to the quantityButton after it's created
       quantityButton.setOnAction(e -> {
          // Check if an item is selected in the cart
          Product selectedProduct = cartTable.getSelectionModel().getSelectedItem();
    
          if (selectedProduct == null) {
             // Show alert if no item is selected
             Alert alert = new Alert(Alert.AlertType.WARNING);
             alert.setTitle("No Item Selected");
             alert.setHeaderText(null);
             alert.setContentText("Please select a product from the cart to change its quantity.");
             alert.showAndWait();
            return;
           }
    
           // Create a dialog to get the new quantity
           TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedProduct.getQuantity()));
           dialog.setTitle("Change Quantity");
           dialog.setHeaderText("Product: " + selectedProduct.getName());
           dialog.setContentText("Enter new quantity:");
    
           // Set the dialog to only accept numbers
           TextField quantityField = dialog.getEditor();
           quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
           if (!newValue.matches("\\d*")) {
             quantityField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    
            // Handle the result
           dialog.showAndWait().ifPresent(result -> {
           try {
            int newQuantity = Integer.parseInt(result);
            
            if (newQuantity > 0) {
                // Update the product quantity
                selectedProduct.setQuantity(newQuantity);
                
                // Refresh the table to show updated quantities and totals
                cartTable.refresh();
                updateTotals();
            } else if (newQuantity == 0) {
                // Remove the item if quantity is set to zero
                removeItemFromCart(selectedProduct);
            } else {
                // Show error for negative quantity
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Quantity");
                alert.setHeaderText(null);
                alert.setContentText("Quantity must be greater than or equal to 0.");
                alert.showAndWait();
            }
            } catch (NumberFormatException ex) {
            // Handle invalid input
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a valid number.");
            alert.showAndWait();
           }
        });
    });
        
        // Set action to open the Product Lookup dialog
        lookupButton.setOnAction(e -> {
          ProductLookupDialog lookupDialog = new ProductLookupDialog(stage);
          Product selectedProduct = lookupDialog.showAndSelect();

          if (selectedProduct != null) {
          // Handle the selected product (add to cart, show details, etc.)
          System.out.println("Selected product: " + selectedProduct.getName());
          // Check if the product already exists in the cart
          boolean found = false;
          for (Product item : cartItems) {
            if (item.getBarcode().equals(selectedProduct.getBarcode())) {
                // If product exists, increase the quantity
                item.increaseQuantity();
                found = true;
                break;
            }
           }

            // If the product doesn't exist in the cart, add the new product
           if (!found) {
            cartItems.add(selectedProduct);
          }

           // Update the cart table and totals
           cartTable.refresh();
           updateTotals(); // Assuming this method updates the cart totals

           // Optionally, clear the barcode field and refocus
           barcodeField.clear();
           barcodeField.requestFocus();
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
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(120);
        priceCol.setCellFactory(col -> new TableCell<Product, Double>() {
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
        
        TableColumn<Product, Double> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(70);
        
        TableColumn<Product, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setPrefWidth(140);
        totalCol.setCellFactory(col -> new TableCell<Product, Double>() {
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
    
    // Customer Information section
    VBox customerInfoBox = new VBox(8);
    Label customerInfoLabel = new Label("Customer Information");
    customerInfoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #424242;");
    
    // Customer name field
    TextField customerNameField = new TextField();
    customerNameField.setPromptText("Enter customer name");
    customerNameField.setPrefHeight(35);
    customerNameField.setStyle("-fx-font-size: 13px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                          "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: white;");
    
    // TIN field
    TextField tinField = new TextField();
    tinField.setPromptText("Enter Tax Identification Number");
    tinField.setPrefHeight(35);
    tinField.setStyle("-fx-font-size: 13px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                   "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: white;");
    
    customerInfoBox.getChildren().addAll(customerInfoLabel, customerNameField, tinField);
    
    // Subtotal section
    GridPane summaryGrid = new GridPane();
    summaryGrid.setVgap(12);
    summaryGrid.setHgap(10);
    
    int row = 0;
    
    // Add subtotal row
    Label subtotalLabel = new Label("Subtotal:");
    subtotalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
    subtotalValueLabel = new Label(formatCurrency(0.0));
    subtotalValueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #424242; -fx-font-weight: bold;");
    summaryGrid.add(subtotalLabel, 0, row);
    summaryGrid.add(subtotalValueLabel, 1, row);
    row++;
    
    // Add discount row
    Label discountLabel = new Label("Discount:");
    discountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
    discountValueLabel = new Label(formatCurrency(0.0));
    discountValueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #c62828; -fx-font-weight: bold;");
    summaryGrid.add(discountLabel, 0, row);
    summaryGrid.add(discountValueLabel, 1, row);
    row++;
    
    // Add tax row
    taxLabel = new Label("Tax (16.5%):");
    taxLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
    taxValueLabel = new Label(formatCurrency(0.0));
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
    
    cashAmountField = new TextField();
    cashAmountField.setPromptText("Enter amount");
    cashAmountField.setPrefHeight(40);
    cashAmountField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                          "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: white;");
    
    // Add change display
    Label changeLabel = new Label("Change:");
    changeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #424242;");
    
    changeValueLabel = new Label(formatCurrency(0.0));
    changeValueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #009688;");
    
    HBox changeBox = new HBox(10);
    changeBox.setAlignment(Pos.CENTER_LEFT);
    changeBox.getChildren().addAll(changeLabel, changeValueLabel);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    changeBox.getChildren().add(0, spacer);
    
    // Add real-time change calculation when cash amount changes
     cashAmountField.textProperty().addListener((observable, oldValue, newValue) -> {
    // Validate input to allow only numbers and decimal points
       if (!newValue.matches("\\d*\\.?\\d*")) {
          cashAmountField.setText(oldValue);
        } else {
        updateChangeCalculation();
        }
    });    
    cashAmountBox.getChildren().addAll(cashAmountLabel, cashAmountField, changeBox);
    
    // Process payment button
    processPaymentButton = new Button("PROCESS PAYMENT");
    processPaymentButton.setPrefHeight(50);
    processPaymentButton.setPrefWidth(Double.MAX_VALUE);
    processPaymentButton.setDisable(true); // Initially disabled
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
    card.getChildren().addAll(summaryTitle, separator, customerInfoBox, summaryGrid, paymentMethodBox, cashAmountBox, processPaymentButton);
    
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
            item.setQuantity(item.getQuantity() + 1);
            item.updateTotal();
            found = true;
            break;
        }
    }

    if (!found) {
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
    double subtotal = 0.0;
    double totalTax = 0.0;
    int totalQuantity = 0;
    double itemLevelDiscountTotal = 0.0;

    for (Product item : cartItems) {
        double itemPrice = item.getPrice();
        double itemQuantity = item.getQuantity();
        double itemSubtotal = itemPrice * itemQuantity;

        // Calculate item-level discount from originalPrice
        double originalPrice = item.getOriginalPrice();
        double discountPerItem = (originalPrice - itemPrice) * itemQuantity;
        itemLevelDiscountTotal += discountPerItem;

        // Get tax rate from DB using TaxRate ID
        double taxRate = Helper.getTaxRateById(item.getTaxRate());
        boolean isVATRegistered = Helper.isVATRegistered();
        double itemVAT = isVATRegistered ? (itemSubtotal * taxRate) / (100 + taxRate) : 0;

        subtotal += itemSubtotal;
        totalTax += itemVAT;
        totalQuantity += itemQuantity;
    }

    // Cart-level discount
    double cartLevelDiscount = 0.0;
    if (cartDiscountPercent > 0) {
        cartLevelDiscount = subtotal * (cartDiscountPercent / 100.0);
    } else if (cartDiscountAmount > 0) {
        cartLevelDiscount = Math.min(cartDiscountAmount, subtotal);
    }

    double totalDiscount = itemLevelDiscountTotal + cartLevelDiscount;
    double total = subtotal - cartLevelDiscount;

    // Update UI
    subtotalValueLabel.setText(formatCurrency(subtotal - totalTax)); 
    taxLabel.setText("VAT Amount:");
    taxValueLabel.setText(formatCurrency(totalTax));
    discountValueLabel.setText(formatCurrency(totalDiscount));
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
    
    /**
     * Processes the payment
     */
private void processPayment() {
    Helper.checkAndHandleTerminalBlocking(isAllowed -> {
        if (isAllowed) {
            Platform.runLater(this::proceedWithPayment);
        }
    });
}

    private void proceedWithPayment() {
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
        
       // 1. Create and populate the invoice header
       InvoiceHeader invoiceHeader = new InvoiceHeader();
       invoiceHeader.setInvoiceNumber(generateNewReceiptNumber());
       invoiceHeader.setInvoiceDateTime(LocalDateTime.now().toString());
       invoiceHeader.setSellerTIN(Helper.getTin());
       invoiceHeader.setBuyerTIN("");
       invoiceHeader.setBuyerAuthorizationCode("");
       invoiceHeader.setSiteId(Helper.getTerminalSiteId());
       invoiceHeader.setGlobalConfigVersion(Helper.getGlobalVersion());
       invoiceHeader.setTaxpayerConfigVersion(Helper.getTaxpayerVersion());
       invoiceHeader.setTerminalConfigVersion(Helper.getTerminalVersion());
       invoiceHeader.setReliefSupply(false);
       invoiceHeader.setVat5CertificateDetails(null);
       invoiceHeader.setPaymentMethod("Cash");

    // 2. Convert products to line items
    List<LineItemDto> lineItems = new ArrayList<>();
    for (Product product : cartItems) {
       LineItemDto lineItemDto = Helper.convertProductToLineItemDto(product);
       lineItems.add(lineItemDto);
    }

        // 3. Build invoice summary
       InvoiceSummary invoiceSummary = new InvoiceSummary();
       List<TaxBreakDown> taxBreakdowns = Helper.generateTaxBreakdown(lineItems);
       invoiceSummary.setTaxBreakDown(taxBreakdowns);
       invoiceSummary.setTotalVAT(lineItems.stream().mapToDouble(LineItemDto::getTotalVAT).sum());
       invoiceSummary.setInvoiceTotal(lineItems.stream().mapToDouble(LineItemDto::getTotal).sum());
       invoiceSummary.setOfflineSignature("");

       // 4. Put everything into the payload
       InvoicePayload payload = new InvoicePayload();
       payload.setInvoiceHeader(invoiceHeader);
       payload.setInvoiceLineItems(lineItems);
       payload.setInvoiceSummary(invoiceSummary);
        
      double totalVAT = invoiceSummary.getTotalVAT();
      String offlineSignature = invoiceSummary.getOfflineSignature();
      String validationUrl = ""; 
      boolean isTransmitted = false; 
      String paymentId = paymentMethodComboBox.getValue();
      double amountPaid = totalAmount;

    // Save locally before transmitting
     boolean saveSuccess = Helper.saveTransaction(
       invoiceHeader,
       lineItems,
       taxBreakdowns,
       totalAmount,
       totalVAT,
       offlineSignature,
       validationUrl,
       isTransmitted,
       paymentId,
       amountPaid
    );

   if (saveSuccess) {
      System.out.println("✅ Transaction saved locally.");
   } else {
    System.err.println("⚠️ Failed to save transaction locally.");
   }


       // Step 5: Convert to JSON
       Gson gson = new Gson();
       String jsonPayload = gson.toJson(payload);
       String bearerToken = Helper.getToken();

       // Step 6: Submit the request
       ApiClient apiClient = new ApiClient();
       
       apiClient.submitTransactions(jsonPayload, bearerToken, (success, returnedValidationUrl) -> {
       Platform.runLater(() -> {
       if (success) {
       
        Helper.updateValidationUrl(invoiceHeader.getInvoiceNumber(), returnedValidationUrl);
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Transaction Status");
        successAlert.setHeaderText("🎉 Transaction Processed!");
        successAlert.setContentText("The transaction was processed successfully.");
        successAlert.showAndWait();

        Helper.markAsTransmitted(invoiceHeader.getInvoiceNumber());
        } else {
        Alert failureAlert = new Alert(Alert.AlertType.ERROR);
        failureAlert.setTitle("Transaction Status");
        failureAlert.setHeaderText("🚨 Processing Failed!");
        failureAlert.setContentText("Transaction failed. Saved locally for later sync.");
        failureAlert.showAndWait();
        }

        });
      });
        // Reset cart and generate new receipt number
        cartItems.clear();
        cashAmountField.clear();
        updateTotals();
        generateNewReceiptNumber();
    }
    
    /**
      * Generates a new receipt number
    */
    public String generateNewReceiptNumber() {
    long taxpayerId = Helper.getTaxpayerId();
    int terminalPosition = Helper.getTerminalPosition();
    LocalDate transactionDate = LocalDate.now();
    long transactionCount = 3;

    InvoiceDetails lastDetails = Helper.getLastInvoiceDetails();

    if (lastDetails != null) {
        String[] parts = lastDetails.getInvoiceNumber().split("-");
        if (parts.length == 4) {
            transactionCount = Helper.base64ToBase10(parts[3]) + 1;
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