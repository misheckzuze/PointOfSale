package com.pointofsale;

import com.pointofsale.helper.Helper;
import com.pointofsale.helper.ApiClient;
import com.pointofsale.model.Product;
import com.pointofsale.model.TaxRates;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Callback;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ProductManagement {

    private TableView<Product> productTable;
    private ObservableList<Product> productData;
    private TextField searchField;
    private ComboBox<String> categoryFilter;
    private ComboBox<String> taxRateFilter;
    private List<TaxRates> taxRates;

    // Stats counters
    private Label totalProductsLabel;
    private Label activeProductsLabel;
    private Label outOfStockLabel;
    private Label lowStockLabel;

    /**
     * Creates and returns the product management content to be displayed
     * @return The root node containing all product management UI
     */
    public Node createContent() {
        // Load products from the database
        productData = FXCollections.observableArrayList(Helper.fetchAllProductsFromDB());
        
        // Create main content container
        VBox mainContent = createMainContent();
        
        return mainContent;
    }

    private VBox createMainContent() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(25));

        // Product stats cards
        HBox statsBox = createStatsCards();

        // Search and filter section
        VBox searchAndFilterSection = createSearchAndFilterSection();

        // Product table
        VBox tableSection = createProductTableSection();

        mainContent.getChildren().addAll(statsBox, searchAndFilterSection, tableSection);
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        return mainContent;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        // Total Products Card
        VBox totalProducts = createStatsCard("Total Products", "0", "#3949ab");
        totalProductsLabel = (Label) totalProducts.getChildren().get(1);
        
        // Active Products Card
        VBox activeProducts = createStatsCard("Active Products", "0", "#00897b");
        activeProductsLabel = (Label) activeProducts.getChildren().get(1);
        
        // Out of Stock Card
        VBox outOfStock = createStatsCard("Out of Stock", "0", "#e53935");
        outOfStockLabel = (Label) outOfStock.getChildren().get(1);
        
        // Low Stock Card
        VBox lowStock = createStatsCard("Low Stock", "0", "#ff8f00");
        lowStockLabel = (Label) lowStock.getChildren().get(1);
        
        // Update stats
        updateProductStats();

        statsBox.getChildren().addAll(totalProducts, activeProducts, outOfStock, lowStock);
        for (int i = 0; i < statsBox.getChildren().size(); i++) {
            HBox.setHgrow(statsBox.getChildren().get(i), Priority.ALWAYS);
        }

        return statsBox;
    }

    private VBox createStatsCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private VBox createSearchAndFilterSection() {
        VBox searchAndFilter = new VBox(15);
        searchAndFilter.setPadding(new Insets(10, 0, 10, 0));

        // Search bar and filters in one row
        HBox filtersRow = new HBox(15);
        filtersRow.setAlignment(Pos.CENTER_LEFT);

        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search products by name, barcode or description...");
        searchField.setPrefHeight(40);
        searchField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; " +
                "-fx-border-radius: 5px; -fx-border-color: #e0e0e0; " +
                "-fx-border-width: 1px; -fx-background-color: white; -fx-padding: 5px 10px;");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        // Add listener to filter products as user types
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts();
        });

        // Category filter
        categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("Category");
        categoryFilter.setPrefHeight(40);
        categoryFilter.setPrefWidth(150);
        categoryFilter.getItems().add("All Categories");
        // Add categories from database or hardcoded list
        categoryFilter.getItems().addAll("Electronics", "Food", "Clothing", "Beverages", "Stationery");
        categoryFilter.setValue("All Categories");
        categoryFilter.setOnAction(e -> filterProducts());

        // Tax rate filter
        taxRateFilter = new ComboBox<>();
        taxRateFilter.setPromptText("Tax Rate");
        taxRateFilter.setPrefHeight(40);
        taxRateFilter.setPrefWidth(150);
        taxRateFilter.getItems().add("All Tax Rates");
        taxRates = Helper.getTaxRates();

        // Add only the tax rate IDs to the combo box
        for (TaxRates taxRate : taxRates) {
            taxRateFilter.getItems().add(taxRate.getTaxRateId());
        }

        taxRateFilter.setValue("All Tax Rates");
        taxRateFilter.setOnAction(e -> filterProducts());

        // Reset filters button
        Button resetButton = new Button("Reset Filters");
        resetButton.setPrefHeight(40);
        resetButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #424242; " +
                "-fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 5px;");
        resetButton.setOnAction(e -> resetFilters());

        filtersRow.getChildren().addAll(searchField, categoryFilter, taxRateFilter, resetButton);

        searchAndFilter.getChildren().addAll(filtersRow);
        return searchAndFilter;
    }

    private VBox createProductTableSection() {
        VBox tableSection = new VBox(10);
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        // Title with action buttons
        HBox tableHeader = new HBox(10);
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        Label tableTitle = new Label("Product Inventory");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #424242;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        // Export button
        Button exportButton = new Button("Export");
        exportButton.setStyle("-fx-background-color: transparent; -fx-border-color: #3949ab; " +
                "-fx-text-fill: #3949ab; -fx-cursor: hand; -fx-background-radius: 5px; -fx-border-radius: 5px;");
        
        // Add Product button
        Button addProductButton = new Button("+ Fetch Products");
        addProductButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-background-radius: 5px; -fx-padding: 8px 15px;");
        
        String token = Helper.getToken();
        String tin = Helper.getTin();
        String siteId = Helper.getTerminalSiteId();

        addProductButton.setOnAction(e -> {
            ApiClient apiClient = new ApiClient();
            
            apiClient.getTerminalSiteProducts(tin, siteId, token, productsFetched -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Product Fetch Status");

                if (productsFetched) {
                    alert.setAlertType(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("✅ Success");
                    alert.setContentText("Products fetched and saved successfully.");
                    
                    // Refresh the product data after fetching
                    productData.setAll(Helper.fetchAllProductsFromDB());
                    updateProductStats();
                } else {
                    alert.setAlertType(Alert.AlertType.ERROR);
                    alert.setHeaderText("⚠ Failed");
                    alert.setContentText("Failed to fetch products.");
                }

                alert.showAndWait();
            });
        });
        
        // Import button
        Button importButton = new Button("Import");
        importButton.setStyle("-fx-background-color: transparent; -fx-border-color: #3949ab; " +
                "-fx-text-fill: #3949ab; -fx-cursor: hand; -fx-background-radius: 5px; -fx-border-radius: 5px;");

        tableHeader.getChildren().addAll(tableTitle, headerSpacer, importButton, exportButton, addProductButton);

        // Create product table
        productTable = new TableView<>();
        productTable.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");
        VBox.setVgrow(productTable, Priority.ALWAYS);

        // Define table columns
        TableColumn<Product, String> barcodeCol = new TableColumn<>("Barcode");
        barcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        barcodeCol.setPrefWidth(120);

        TableColumn<Product, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);

        TableColumn<Product, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasure"));
        unitCol.setPrefWidth(80);

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);
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

        TableColumn<Product, Double> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discount"));
        discountCol.setPrefWidth(80);
        discountCol.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double discount, boolean empty) {
                super.updateItem(discount, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(discount > 0 ? formatCurrency(discount) : "-");
                }
            }
        });

        TableColumn<Product, String> taxRateCol = new TableColumn<>("Tax Rate");
        taxRateCol.setCellValueFactory(new PropertyValueFactory<>("taxRate"));
        taxRateCol.setPrefWidth(80);

        TableColumn<Product, Double> totalVATCol = new TableColumn<>("VAT");
        totalVATCol.setCellValueFactory(new PropertyValueFactory<>("totalVAT"));
        totalVATCol.setPrefWidth(80);
        totalVATCol.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double vat, boolean empty) {
                super.updateItem(vat, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(formatCurrency(vat));
                }
            }
        });

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setPrefWidth(80);

        TableColumn<Product, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            double stock = cellData.getValue().getQuantity();
            if (stock <= 0) {
                return new SimpleStringProperty("Out of Stock");
            } else if (stock < 10) {
                return new SimpleStringProperty("Low Stock");
            } else {
                return new SimpleStringProperty("In Stock");
            }
        });
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "Out of Stock":
                            setStyle("-fx-text-fill: #e53935; -fx-font-weight: bold;");
                            break;
                        case "Low Stock":
                            setStyle("-fx-text-fill: #ff8f00; -fx-font-weight: bold;");
                            break;
                        case "In Stock":
                            setStyle("-fx-text-fill: #00897b; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });

        TableColumn<Product, String> productTypeCol = new TableColumn<>("Type");
        productTypeCol.setCellValueFactory(cellData ->
            new ReadOnlyStringWrapper(cellData.getValue().isProduct() ? "Product" : "Service"));
        productTypeCol.setPrefWidth(80);

        TableColumn<Product, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(180);
        actionsCol.setCellFactory(createActionButtonCellFactory());

        productTable.getColumns().addAll(barcodeCol, nameCol, categoryCol, unitCol, priceCol, 
                discountCol, taxRateCol, totalVATCol, stockCol, statusCol, productTypeCol, actionsCol);

        // Set items to table (filtered list)
        FilteredList<Product> filteredData = new FilteredList<>(productData, p -> true);
        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(productTable.comparatorProperty());
        productTable.setItems(sortedData);

        // Add search field key handler
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                filterProducts();
            }
        });

        tableSection.getChildren().addAll(tableHeader, productTable);
        return tableSection;
    }

    private Callback<TableColumn<Product, Void>, TableCell<Product, Void>> createActionButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Product, Void> call(final TableColumn<Product, Void> param) {
                return new TableCell<>() {
                    private final Button discountBtn = new Button("Discount");
                    private final HBox pane = new HBox(5);

                    {
                        discountBtn.setStyle("-fx-background-color: #ff8f00; -fx-text-fill: white; " +
                                "-fx-cursor: hand; -fx-background-radius: 3px; -fx-font-size: 12px;");

                        discountBtn.setOnAction(event -> {
                            Product product = getTableView().getItems().get(getIndex());
                            showDiscountDialog(product);
                        });

                        pane.getChildren().addAll(discountBtn);
                        pane.setAlignment(Pos.CENTER);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
    }

    private void filterProducts() {
        String searchText = searchField.getText().toLowerCase();
        String categoryText = categoryFilter.getValue();
        String taxRateText = taxRateFilter.getValue();

        FilteredList<Product> filteredData = new FilteredList<>(productData);
        
        filteredData.setPredicate(product -> {
            // If search field is empty and all filters are set to "All", show all products
            if ((searchText == null || searchText.isEmpty()) &&
                    (categoryText.equals("All Categories")) &&
                    (taxRateText.equals("All Tax Rates"))) {
                return true;
            }

            boolean matchesSearch = true;
            boolean matchesCategory = true;
            boolean matchesTaxRate = true;

            // Check if product matches search text
            if (searchText != null && !searchText.isEmpty()) {
                matchesSearch = product.getName().toLowerCase().contains(searchText) ||
                        product.getBarcode().toLowerCase().contains(searchText) ||
                        product.getDescription().toLowerCase().contains(searchText);
            }

            // Check if product matches category filter
            if (!categoryText.equals("All Categories")) {
                matchesCategory = product.getDescription().equals(categoryText);
            }

            if (!taxRateText.equals("All Tax Rates")) {
                String productTaxRateId = product.getTaxRate();
                matchesTaxRate = productTaxRateId.equals(taxRateText);
            }
            return matchesSearch && matchesCategory && matchesTaxRate;
        });

        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(productTable.comparatorProperty());
        productTable.setItems(sortedData);
    }

    private void resetFilters() {
        searchField.clear();
        categoryFilter.setValue("All Categories");
        taxRateFilter.setValue("All Tax Rates");
        filterProducts();
    }

    private void updateProductStats() {
        int total = productData.size();
        int active = 0;
        int outOfStockCount = 0;
        int lowStock = 0;

        for (Product product : productData) {
            double quantity = product.getQuantity();

            if (quantity >= 10) {
                active++;
            } else if (quantity > 0 && quantity < 10) {
                lowStock++;
            } else if (quantity == 0) {
                outOfStockCount++;
            }
        }

        totalProductsLabel.setText(String.valueOf(total));
        activeProductsLabel.setText(String.valueOf(active + lowStock));
        outOfStockLabel.setText(String.valueOf(outOfStockCount));
        lowStockLabel.setText(String.valueOf(lowStock));
    }
        
    private void showDiscountDialog(Product product) {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Apply Discount");
    dialog.setHeaderText("Apply Discount to " + product.getName());

    ButtonType applyButtonType = new ButtonType("Apply Discount", ButtonBar.ButtonData.OK_DONE);
    ButtonType removeButtonType = new ButtonType("Remove Discount", ButtonBar.ButtonData.LEFT);
    dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, removeButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    Label currentPriceLabel = new Label(formatCurrency(product.getPrice()));
    currentPriceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

    ToggleGroup discountType = new ToggleGroup();
    RadioButton percentageOption = new RadioButton("Percentage (%)");
    percentageOption.setToggleGroup(discountType);
    percentageOption.setSelected(true);

    RadioButton amountOption = new RadioButton("Fixed Amount");
    amountOption.setToggleGroup(discountType);

    TextField discountValue = new TextField();
    if (product.getDiscount() > 0) {
        discountValue.setText(String.valueOf(product.getDiscount()));
    }

    Label finalPriceLabel = new Label("--");
    finalPriceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1a237e;");

    discountValue.textProperty().addListener((observable, oldValue, newValue) -> {
        try {
            double discount = Double.parseDouble(newValue);
            double finalPrice;

            if (percentageOption.isSelected()) {
                finalPrice = product.getPrice() * (1 - discount / 100);
            } else {
                finalPrice = product.getPrice() - discount;
            }

            if (finalPrice < 0) finalPrice = 0;
            finalPriceLabel.setText(formatCurrency(finalPrice));
        } catch (NumberFormatException e) {
            finalPriceLabel.setText("--");
        }
    });

    grid.add(new Label("Current Price:"), 0, 0);
    grid.add(currentPriceLabel, 1, 0);
    grid.add(new Label("Discount Type:"), 0, 1);
    grid.add(new HBox(10, percentageOption, amountOption), 1, 1);
    grid.add(new Label("Discount Value:"), 0, 2);
    grid.add(discountValue, 1, 2);
    grid.add(new Label("Final Price:"), 0, 3);
    grid.add(finalPriceLabel, 1, 3);

    dialog.getDialogPane().setContent(grid);
    discountValue.requestFocus();

    Optional<ButtonType> result = dialog.showAndWait();

    if (result.isPresent()) {
        if (result.get() == applyButtonType) {
            try {
                double discount = Double.parseDouble(discountValue.getText());

                if (percentageOption.isSelected()) {
                    double discountAmount = product.getPrice() * (discount / 100);
                    product.setDiscount(discountAmount);
                } else {
                    product.setDiscount(discount);
                }

                boolean success = Helper.updateProductDiscount(product.getBarcode(), product.getDiscount());
                if (success) {
                    productTable.refresh();
                    showNotification("Discount Applied", "Discount has been applied to " + product.getName());
                } else {
                    showAlert("Failed to Save Discount", "There was an issue updating the discount in the database.");
                }

            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid discount value.");
            }

        } else if (result.get() == removeButtonType) {
            product.setDiscount(0);
            boolean success = Helper.updateProductDiscount(product.getBarcode(), 0);
            if (success) {
                productTable.refresh();
                showNotification("Discount Removed", "Discount has been removed from " + product.getName());
            } else {
                showAlert("Failed to Remove Discount", "Could not update the database.");
            }
        }
    }
}
    
    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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