package com.pointofsale;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import com.pointofsale.helper.Helper;
import com.pointofsale.model.Product;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.stage.StageStyle;
import javafx.util.Duration;


public class ProductLookupDialog {

    private Stage stage;
    private VBox detailPanel;
    private TableView<Product> productTable;
    private ObservableList<Product> productData;
    private TextField searchField;
    private ComboBox<String> categoryComboBox;
    private Product selectedProduct;
    private VBox rootLayout;
    private HBox contentArea;

    /**
     * Constructor for the Product Lookup Dialog
     * 
     * @param parentStage The parent stage
     */
    public ProductLookupDialog(Stage parentStage) {
        this.stage = new Stage();
        this.stage.initOwner(parentStage);
        this.stage.initModality(Modality.APPLICATION_MODAL);
        this.stage.initStyle(StageStyle.DECORATED);
        this.stage.setTitle("Product Lookup");
        
        // Initialize dummy product data
        initializeProductData();
        
        // Create the UI
        createDialogContent();
    }
    
   private void initializeProductData() {
    productData = FXCollections.observableArrayList(Helper.fetchAllProductsFromDB());
}

    
    /**
     * Creates the content of the dialog
     */
    private void createDialogContent() {
        // Root layout
        rootLayout = new VBox();
        rootLayout.setPrefSize(800, 600);
        rootLayout.setStyle("-fx-background-color: white;");
        
        // Create the header
        HBox header = createHeader();
        
        // Create the search area
        VBox searchArea = createSearchArea();
        
        // Create the content area (table + detail panel)
        contentArea = new HBox();
        contentArea.setSpacing(0);
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        
        // Create the product table
        VBox tableArea = createTableArea();
        HBox.setHgrow(tableArea, Priority.ALWAYS);
        
        // Create the detail panel (initially hidden)
        detailPanel = createDetailPanel();
        detailPanel.setPrefWidth(0);
        detailPanel.setVisible(false);
        
        // Add table and detail panel to content area
        contentArea.getChildren().addAll(tableArea, detailPanel);
        
        // Add all components to the root layout
        rootLayout.getChildren().addAll(header, searchArea, contentArea);
        
        // Create the scene
        Scene scene = new Scene(rootLayout);
        stage.setScene(scene);
    }
    
    /**
     * Creates the header of the dialog
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPrefHeight(70);
        
        // Create gradient background for header
        Stop[] stops = new Stop[] {
            new Stop(0, Color.valueOf("#1a237e")),
            new Stop(1, Color.valueOf("#3949ab"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, null, stops);
        
        // Set background as rectangle with gradient
        Rectangle background = new Rectangle();
        background.widthProperty().bind(header.widthProperty());
        background.heightProperty().bind(header.heightProperty());
        background.setFill(gradient);
        
        // Header title
        Label titleLabel = new Label("Product Lookup");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        titleLabel.setPadding(new Insets(0, 0, 0, 20));
        
        // Close button
        Button closeButton = new Button("×");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                           "-fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand;");
        closeButton.setOnAction(e -> stage.close());
        
        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        
        HBox closeBox = new HBox(closeButton);
        closeBox.setAlignment(Pos.CENTER_RIGHT);
        closeBox.setPadding(new Insets(0, 15, 0, 0));
        
        // Add decorative dots to header (top right)
        GridPane dotsPattern = createDecorativePattern();
        dotsPattern.setOpacity(0.15);
        
        // Stack the background, title, and close button
        StackPane headerContent = new StackPane();
        headerContent.getChildren().addAll(background, dotsPattern);
        
        HBox controlsBox = new HBox();
        controlsBox.getChildren().addAll(titleBox, closeBox);
        controlsBox.setPrefWidth(800);
        headerContent.getChildren().add(controlsBox);
        
        header.getChildren().add(headerContent);
        HBox.setHgrow(headerContent, Priority.ALWAYS);
        
        return header;
    }
    
    /**
     * Creates a decorative dots pattern as a background element
     */
    private GridPane createDecorativePattern() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 15, 10, 400));
        grid.setAlignment(Pos.CENTER_RIGHT);
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {
                Circle dot = new Circle(2);
                dot.setFill(Color.WHITE);
                dot.setOpacity((i + j) % 2 == 0 ? 0.8 : 0.4);
                grid.add(dot, j, i);
            }
        }
        
        return grid;
    }
    
    /**
     * Creates the search area of the dialog
     */
    private VBox createSearchArea() {
        VBox searchArea = new VBox();
        searchArea.setPadding(new Insets(20));
        searchArea.setSpacing(15);
        searchArea.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        
        // Search controls container
        HBox searchControls = new HBox();
        searchControls.setSpacing(10);
        searchControls.setAlignment(Pos.CENTER_LEFT);
        
        // Search field with icon
        HBox searchFieldBox = new HBox();
        searchFieldBox.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; " +
                              "-fx-border-radius: 5; -fx-background-radius: 5;");
        searchFieldBox.setPadding(new Insets(0, 10, 0, 10));
        searchFieldBox.setAlignment(Pos.CENTER_LEFT);
        
        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
        
        searchField = new TextField();
        searchField.setPromptText("Search Products...");
        searchField.setPrefHeight(35);
        searchField.setPrefWidth(350);
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                           "-fx-font-size: 14px;");
        
        searchFieldBox.getChildren().addAll(searchIcon, searchField);
        
        // Category dropdown
        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("All Categories", "Furniture", "Lighting", "Decor", "Flooring");
        categoryComboBox.setValue("All Categories");
        categoryComboBox.setPrefHeight(35);
        categoryComboBox.setPrefWidth(150);
        categoryComboBox.setStyle("-fx-font-size: 14px; -fx-background-color: #f8f8f8; " +
                                "-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Search button
        Button searchButton = new Button("Search");
        searchButton.setPrefHeight(35);
        searchButton.setPrefWidth(100);
        searchButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; " +
                            "-fx-background-radius: 5;");
        
        // Add hover effect
        searchButton.setOnMouseEntered(e -> 
            searchButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; " +
                                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; " +
                                "-fx-background-radius: 5;")
        );
        
        searchButton.setOnMouseExited(e -> 
            searchButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; " +
                                "-fx-background-radius: 5;")
        );
        
        // Drop shadow effect for button
        DropShadow shadow = new DropShadow();
        shadow.setRadius(3.0);
        shadow.setOffsetY(2.0);
        shadow.setColor(Color.color(0.0, 0.0, 0.0, 0.2));
        searchButton.setEffect(shadow);
        
        // Filter button
        Button filterButton = new Button("Filter");
        filterButton.setPrefHeight(35);
        filterButton.setPrefWidth(80);
        filterButton.setStyle("-fx-background-color: #f8f8f8; -fx-text-fill: #555555; " +
                            "-fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 5; " +
                            "-fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        
        // Add action to search button
        searchButton.setOnAction(e -> performSearch());
        
        // Add action to search field (Enter key)
        searchField.setOnAction(e -> performSearch());
        
        // Add all controls to search area
        searchControls.getChildren().addAll(searchFieldBox, categoryComboBox, searchButton, filterButton);
        searchArea.getChildren().add(searchControls);
        
        return searchArea;
    }
    
    /**
 * Creates the table area of the dialog
 */
private VBox createTableArea() {
    VBox tableArea = new VBox();
    tableArea.setPadding(new Insets(0, 0, 20, 20));
    tableArea.setSpacing(10);
    VBox.setVgrow(tableArea, Priority.ALWAYS);
    
    // Create table
    productTable = new TableView<>();
    productTable.setItems(productData);
    productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    VBox.setVgrow(productTable, Priority.ALWAYS);
    
    // Add event handler for row selection
    productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal != null) {
            selectedProduct = newVal;
            showDetailPanel();
            updateDetailPanel();
        }
    });
    
    // Barcode column
    TableColumn<Product, String> barcodeCol = new TableColumn<>("Barcode");
    barcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));
    barcodeCol.setPrefWidth(120);
    barcodeCol.setStyle("-fx-alignment: CENTER;");
    
    // Image column (placeholder)
    TableColumn<Product, String> imgCol = new TableColumn<>("Image");
    imgCol.setCellValueFactory(new PropertyValueFactory<>("name")); // We'll use name for demo
    imgCol.setPrefWidth(60);
    imgCol.setCellFactory(column -> {
        return new TableCell<Product, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    Rectangle imagePlaceholder = new Rectangle(40, 40);
                    imagePlaceholder.setFill(Color.LIGHTGRAY);
                    imagePlaceholder.setArcWidth(5);
                    imagePlaceholder.setArcHeight(5);
                    setGraphic(imagePlaceholder);
                }
            }
        };
    });
    
    // Name column
    TableColumn<Product, String> nameCol = new TableColumn<>("Product Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameCol.setPrefWidth(200);
    
    // Description column
    TableColumn<Product, String> descriptionCol = new TableColumn<>("Description");
    descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
    descriptionCol.setPrefWidth(200);
    
    // Price column
    TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
    priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
    priceCol.setPrefWidth(80);
    priceCol.setStyle("-fx-alignment: CENTER-RIGHT;");
    priceCol.setCellFactory(col -> new TableCell<Product, Double>() {
        @Override
        protected void updateItem(Double price, boolean empty) {
            super.updateItem(price, empty);
            if (empty || price == null) {
                setText(null);
            } else {
                setText(formatCurrency(price));
            }
        }
    });
    
    // Tax Rate column
    TableColumn<Product, String> taxRateCol = new TableColumn<>("Tax Rate");
    taxRateCol.setCellValueFactory(new PropertyValueFactory<>("taxRate"));
    taxRateCol.setPrefWidth(80);
    taxRateCol.setStyle("-fx-alignment: CENTER;");
    
    // Quantity column with visual indicator
    TableColumn<Product, Double> quantityCol = new TableColumn<>("Quantity");
    quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    quantityCol.setPrefWidth(120);
    quantityCol.setCellFactory(column -> {
        return new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (quantity == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create stock indicator
                    HBox stockIndicator = new HBox(3);
                    stockIndicator.setAlignment(Pos.CENTER_LEFT);
                    
                    // Add dots based on quantity level (max 5 dots)
                    int filledDots = Math.min(5, (int) Math.ceil(quantity / 10.0));
                    for (int i = 0; i < 5; i++) {
                        Circle dot = new Circle(4);
                        if (i < filledDots) {
                            if (quantity < 5) {
                                dot.setFill(Color.valueOf("#f44336")); // Red for low stock
                            } else if (quantity < 10) {
                                dot.setFill(Color.valueOf("#ff9800")); // Orange for medium stock
                            } else {
                                dot.setFill(Color.valueOf("#4caf50")); // Green for good stock
                            }
                        } else {
                            dot.setFill(Color.valueOf("#e0e0e0")); // Gray for empty dots
                        }
                        stockIndicator.getChildren().add(dot);
                    }
                    
                    // Add quantity number
                    Label quantityLabel = new Label(" (" + quantity + ")");
                    quantityLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
                    stockIndicator.getChildren().add(quantityLabel);
                    
                    setGraphic(stockIndicator);
                }
            }
        };
    });
    
    // Unit of Measure column
    TableColumn<Product, String> uomCol = new TableColumn<>("Unit");
    uomCol.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasure"));
    uomCol.setPrefWidth(80);
    uomCol.setStyle("-fx-alignment: CENTER;");
    
    // Add columns to table
    productTable.getColumns().addAll(barcodeCol, imgCol, nameCol, descriptionCol, priceCol, taxRateCol, quantityCol, uomCol);
    
    // Add table to table area
    tableArea.getChildren().add(productTable);
    
    // Add pagination controls
    HBox paginationControls = new HBox(15);
    paginationControls.setPadding(new Insets(10, 0, 0, 0));
    paginationControls.setAlignment(Pos.CENTER);
    
    Button prevButton = new Button("◄ Previous");
    prevButton.setStyle("-fx-background-color: #f8f8f8; -fx-text-fill: #555555; " +
                      "-fx-font-size: 12px; -fx-cursor: hand; -fx-background-radius: 5; " +
                      "-fx-border-color: #e0e0e0; -fx-border-radius: 5;");
    
    Label pageLabel = new Label("Page 1 of 1");
    pageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
    
    Button nextButton = new Button("Next ►");
    nextButton.setStyle("-fx-background-color: #f8f8f8; -fx-text-fill: #555555; " +
                      "-fx-font-size: 12px; -fx-cursor: hand; -fx-background-radius: 5; " +
                      "-fx-border-color: #e0e0e0; -fx-border-radius: 5;");
    
    paginationControls.getChildren().addAll(prevButton, pageLabel, nextButton);
    tableArea.getChildren().add(paginationControls);
    
    return tableArea;
}
    /**
     * Creates the detail panel of the dialog
     */
    private VBox createDetailPanel() {
        VBox detailPanel = new VBox();
        detailPanel.setPadding(new Insets(20));
        detailPanel.setSpacing(15);
        detailPanel.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 0 1;");
        detailPanel.setPrefWidth(300);
        
        // Create product details header
        Label detailsTitle = new Label("Product Details");
        detailsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        
        // Create product image placeholder
        Rectangle productImage = new Rectangle(200, 150);
        productImage.setFill(Color.LIGHTGRAY);
        productImage.setArcWidth(5);
        productImage.setArcHeight(5);
        
        // Product details
        VBox detailsBox = new VBox(8);
        detailsBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 15 0;");
        
        Label idLabel = new Label("Barcode: ");
        idLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        
        Label nameLabel = new Label("Product Name: ");
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        
        Label descriptionTitle = new Label("Description:");
        descriptionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555555;");
        
        Label descriptionLabel = new Label();
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575; -fx-wrap-text: true;");
        
        Label priceLabel = new Label("Price: ");
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        
        Label taxRateLabel = new Label("Tax Rate: ");
        taxRateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        
        Label quantityLabel = new Label("Quantity: ");
        quantityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        
        Label unitOfMeasureLabel = new Label("Unit of Measure: ");
        unitOfMeasureLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        
        detailsBox.getChildren().addAll(
            idLabel, nameLabel, new Separator(), 
            descriptionTitle, descriptionLabel, new Separator(),
            priceLabel, taxRateLabel, quantityLabel, unitOfMeasureLabel
        );
        
        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setPadding(new Insets(15, 0, 0, 0));
        
        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setPrefHeight(35);
        addToCartButton.setPrefWidth(130);
        addToCartButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                               "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; " +
                               "-fx-background-radius: 5;");
        
        // Add hover effect
        addToCartButton.setOnMouseEntered(e -> 
            addToCartButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; " +
                                   "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; " +
                                   "-fx-background-radius: 5;")
        );
        
        addToCartButton.setOnMouseExited(e -> 
            addToCartButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                                   "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; " +
                                   "-fx-background-radius: 5;")
        );
        
        addToCartButton.setOnAction(e -> {
           if (selectedProduct != null) {
           stage.close(); // showAndSelect() will return selectedProduct
           } else {
           Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a product before adding to cart.");
            alert.showAndWait();
           }
        });

        Button viewDetailsButton = new Button("Full Details");
        viewDetailsButton.setPrefHeight(35);
        viewDetailsButton.setPrefWidth(120);
        viewDetailsButton.setStyle("-fx-background-color: #f8f8f8; -fx-text-fill: #555555; " +
                                 "-fx-font-size: 13px; -fx-cursor: hand; -fx-background-radius: 5; " +
                                 "-fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        
        // Drop shadow effect for button
        DropShadow shadow = new DropShadow();
        shadow.setRadius(3.0);
        shadow.setOffsetY(2.0);
        shadow.setColor(Color.color(0.0, 0.0, 0.0, 0.2));
        addToCartButton.setEffect(shadow);
        
        actionButtons.getChildren().addAll(addToCartButton, viewDetailsButton);
        
        // Add all components to detail panel
        detailPanel.getChildren().addAll(detailsTitle, productImage, detailsBox, actionButtons);
        
        return detailPanel;
    }
    
    /**
     * Performs a search based on the search field and category filter
     */
    private void performSearch() {
        String searchText = searchField.getText().toLowerCase();
        String category = categoryComboBox.getValue();
        
        // Filter the products based on search criteria
        ObservableList<Product> filteredData = FXCollections.observableArrayList();
        for (Product product : productData) {
            boolean categoryMatch = category.equals("All Categories") || product.getDescription().equals(category);
            boolean searchMatch = searchText.isEmpty() || 
                                product.getName().toLowerCase().contains(searchText) || 
                                String.valueOf(product.getBarcode()).contains(searchText);
            
            if (categoryMatch && searchMatch) {
                filteredData.add(product);
            }
        }
        
        // Update the table with filtered data
        productTable.setItems(filteredData);
        
        // Select first item if available
        if (!filteredData.isEmpty()) {
            productTable.getSelectionModel().select(0);
        } else {
            // Hide details panel if no results
            hideDetailPanel();
        }
    }
    
    /**
     * Shows the detail panel with animation
     */
    private void showDetailPanel() {
        // If detail panel is already visible, just update it
        if (detailPanel.isVisible() && detailPanel.getPrefWidth() > 0) {
            return;
        }
        
        // Make detail panel visible
        detailPanel.setVisible(true);
        
        // Animate the panel width
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(250), detailPanel);
        translateTransition.setFromX(300);
        translateTransition.setToX(0);
        
        // Animate the opacity
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(250), detailPanel);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        
        // Set final width
        detailPanel.setPrefWidth(300);
        
        // Play animations
        translateTransition.play();
        fadeTransition.play();
    }
    
    /**
     * Hides the detail panel with animation
     */
    private void hideDetailPanel() {
        // If detail panel is already hidden, do nothing
        if (!detailPanel.isVisible() || detailPanel.getPrefWidth() == 0) {
            return;
        }
        
        // Animate the panel width
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(250), detailPanel);
        translateTransition.setFromX(0);
        translateTransition.setToX(300);
        
        // Animate the opacity
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(250), detailPanel);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        
        // When animation finishes, hide the panel and set width to 0
        fadeTransition.setOnFinished(e -> {
            detailPanel.setVisible(false);
            detailPanel.setPrefWidth(0);
        });
        
        // Play animations
        translateTransition.play();
        fadeTransition.play();
    }
    
    /**
     * Updates the detail panel with the selected product information
     */
    private void updateDetailPanel() {
        if (selectedProduct == null || !detailPanel.isVisible()) {
            return;
        }
        
        // Get all labels from the detailsBox (index 2 in the detailPanel)
        VBox detailsBox = (VBox) detailPanel.getChildren().get(2);
        
        // Update each label with product information
        ((Label) detailsBox.getChildren().get(0)).setText("Barcode: " + selectedProduct.getBarcode());
        ((Label) detailsBox.getChildren().get(1)).setText("Name: " + selectedProduct.getName());
        // Index 2 is a separator
        // Index 3 is the description title
        ((Label) detailsBox.getChildren().get(4)).setText(selectedProduct.getDescription());
        // Index 5 is a separator
        ((Label) detailsBox.getChildren().get(6)).setText("Price: " + formatCurrency(selectedProduct.getPrice()));
        ((Label) detailsBox.getChildren().get(7)).setText("Tax Rate: " + selectedProduct.getTaxRate());
        ((Label) detailsBox.getChildren().get(8)).setText("Quantity: " + selectedProduct.getQuantity() + " units");
        ((Label) detailsBox.getChildren().get(9)).setText("Unit of Measure: " + selectedProduct.getUnitOfMeasure());
    }
    
    /**
     * Shows the product lookup dialog
     */
    public void show() {
        stage.showAndWait();
    }
    
    /**
     * Shows the product lookup dialog and returns the selected product
     * 
     * @return The selected product, or null if no product was selected
     */
    public Product showAndSelect() {
        // Make sure the detail panel is hidden initially
        hideDetailPanel();
        
        // Show the dialog
        stage.showAndWait();
        
        // Return the selected product
        return selectedProduct;
    }
/**
       * Formats a number as Malawi Kwacha currency
    */
    private String formatCurrency(double amount) {
      Locale malawiLocale = new Locale.Builder().setLanguage("en").setRegion("MW").build();
      NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(malawiLocale);
      return currencyFormatter.format(amount);
    }

}