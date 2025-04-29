package com.pointofsale;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

public class TransactionsView {

    // Mock InvoiceDetails class for demo purposes
    public static class InvoiceDetails {
        private final SimpleStringProperty invoiceNumber;
        private final SimpleStringProperty invoiceDateTime;
        private final SimpleStringProperty buyerTin;
        private final SimpleIntegerProperty itemCount;
        private final SimpleDoubleProperty invoiceTotal;
        private final SimpleDoubleProperty totalVat;
        private final BooleanProperty transmitted;
        private SimpleStringProperty validationUrl;

        public InvoiceDetails(String invoiceNumber, LocalDateTime dateTime, String buyerTin, 
                             int itemCount, double total, double vat, boolean transmitted) {
            this.invoiceNumber = new SimpleStringProperty(invoiceNumber);
            this.invoiceDateTime = new SimpleStringProperty(dateTime.toString());
            this.buyerTin = new SimpleStringProperty(buyerTin);
            this.itemCount = new SimpleIntegerProperty(itemCount);
            this.invoiceTotal = new SimpleDoubleProperty(total);
            this.totalVat = new SimpleDoubleProperty(vat);
            this.transmitted = new SimpleBooleanProperty(transmitted);
            this.validationUrl = new SimpleStringProperty(transmitted ? 
                    "https://efiscal.mra.mw/invoice/" + invoiceNumber : "");
        }

        public String getInvoiceNumber() { return invoiceNumber.get(); }
        public String getInvoiceDateTime() { return invoiceDateTime.get(); }
        public String getBuyerTin() { return buyerTin.get(); }
        public int getItemCount() { return itemCount.get(); }
        public double getInvoiceTotal() { return invoiceTotal.get(); }
        public double getTotalVat() { return totalVat.get(); }
        public boolean isTransmitted() { return transmitted.get(); }
        public void setTransmitted(boolean value) { transmitted.set(value); }
        public String getValidationUrl() { return validationUrl.get(); }
        public void setValidationUrl(String url) { validationUrl.set(url); }
    }

    private TableView<InvoiceDetails> transactionsTable;
    private final ObservableList<InvoiceDetails> allTransactions = FXCollections.observableArrayList();
    private final FilteredList<InvoiceDetails> filteredTransactions = new FilteredList<>(allTransactions);
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private TextField searchField;
    private ComboBox<String> statusFilterComboBox;
    private Label totalTransactionsLabel;
    private Label totalValueLabel;
    private Label totalTaxLabel;

    public Node getView() {
        // Initialize components
        initializeComponents();
        
        // Load initial transaction data
        loadTransactions();
        
        // Return the main content
        return createMainContent();
    }
    
    private void initializeComponents() {
        // Initialize DatePickers
        startDatePicker = new DatePicker(LocalDate.now().minusWeeks(1));
        endDatePicker = new DatePicker(LocalDate.now());
        
        // Initialize search field
        searchField = new TextField();
        searchField.setPromptText("Search transactions...");
        
        // Initialize status filter
        statusFilterComboBox = new ComboBox<>();
        statusFilterComboBox.getItems().addAll("All", "Transmitted", "Pending", "Failed");
        statusFilterComboBox.setValue("All");
    }

    private Pane createMainContent() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));

        // Title section
        Label title = new Label("Transaction History");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");

        // Filters section
        HBox filtersSection = createFiltersSection();
        
        // Statistics cards section
        HBox statsSection = createStatsSection();
        
        // Transactions table
        VBox tableSection = createTransactionsTable();
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        
        // Action buttons section
        HBox actionsSection = createActionsSection();
        
        // Add components to main content
        mainContent.getChildren().addAll(title, filtersSection, statsSection, tableSection, actionsSection);
        
        return mainContent;
    }
    
    private HBox createFiltersSection() {
        HBox filtersSection = new HBox(15);
        filtersSection.setPadding(new Insets(0, 0, 10, 0));
        filtersSection.setAlignment(Pos.CENTER_LEFT);
        filtersSection.setStyle("-fx-background-color: white; -fx-background-radius: 5px; -fx-padding: 15px; " +
                          "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // Date range filters
        VBox dateRangeBox = new VBox(5);
        Label dateRangeLabel = new Label("Date Range");
        dateRangeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #424242;");
        
        HBox datePickersBox = new HBox(10);
        
        // Using already initialized date pickers
        startDatePicker.setPrefWidth(140);
        startDatePicker.setStyle("-fx-font-size: 12px;");
        startDatePicker.setPromptText("Start Date");
        
        endDatePicker.setPrefWidth(140);
        endDatePicker.setStyle("-fx-font-size: 12px;");
        endDatePicker.setPromptText("End Date");
        
        datePickersBox.getChildren().addAll(startDatePicker, endDatePicker);
        dateRangeBox.getChildren().addAll(dateRangeLabel, datePickersBox);
        
        // Status filter
        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("Status");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #424242;");
        
        // Using already initialized status filter
        statusFilterComboBox.setPrefWidth(150);
        statusFilterComboBox.setStyle("-fx-font-size: 12px;");
        
        statusBox.getChildren().addAll(statusLabel, statusFilterComboBox);
        
        // Search field
        VBox searchBox = new VBox(5);
        Label searchLabel = new Label("Search");
        searchLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #424242;");
        
        // Using already initialized search field
        searchField.setPrefWidth(220);
        searchField.setStyle("-fx-font-size: 12px;");
        
        searchBox.getChildren().addAll(searchLabel, searchField);
        
        // Apply filters button
        Button applyFiltersButton = new Button("Apply Filters");
        applyFiltersButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; -fx-font-weight: bold; " +
                                "-fx-cursor: hand; -fx-background-radius: 3px;");
        applyFiltersButton.setOnAction(e -> applyFilters());
        
        // Reset filters button
        Button resetFiltersButton = new Button("Reset");
        resetFiltersButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3949ab; -fx-font-weight: bold; " +
                                "-fx-cursor: hand; -fx-border-color: #3949ab; -fx-border-radius: 3px; -fx-background-radius: 3px;");
        resetFiltersButton.setOnAction(e -> resetFilters());
        
        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.BOTTOM_LEFT);
        buttonsBox.getChildren().addAll(applyFiltersButton, resetFiltersButton);
        
        // Add everything to the filters section
        filtersSection.getChildren().addAll(dateRangeBox, statusBox, searchBox);
        
        // Add spacer to push buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        filtersSection.getChildren().add(spacer);
        
        filtersSection.getChildren().add(buttonsBox);
        
        return filtersSection;
    }
    
    private HBox createStatsSection() {
        HBox statsSection = new HBox(15);
        statsSection.setPadding(new Insets(0, 0, 10, 0));
        
        // Total transactions card
        VBox transactionsCard = createStatCard("Total Transactions", "0", "receipt", "#3949ab");
        
        // Check if the second child is a VBox before casting
        if (transactionsCard.getChildren().size() > 1 && transactionsCard.getChildren().get(1) instanceof VBox) {
            totalTransactionsLabel = (Label) ((VBox) transactionsCard.getChildren().get(1)).getChildren().get(0);
        } else {
            System.err.println("Error: Expected VBox, but found " + transactionsCard.getChildren().get(1).getClass());
        }
        
        // Total value card
        VBox valueCard = createStatCard("Total Value", formatCurrency(0), "money", "#00796b");
        if (valueCard.getChildren().size() > 1 && valueCard.getChildren().get(1) instanceof VBox) {
            totalValueLabel = (Label) ((VBox) valueCard.getChildren().get(1)).getChildren().get(0);
        }
        
        // Total tax card
        VBox taxCard = createStatCard("Total Tax", formatCurrency(0), "tax", "#c62828");
        if (taxCard.getChildren().size() > 1 && taxCard.getChildren().get(1) instanceof VBox) {
            totalTaxLabel = (Label) ((VBox) taxCard.getChildren().get(1)).getChildren().get(0);
        }
        
        // Average transaction card
        VBox avgCard = createStatCard("Average Transaction", formatCurrency(0), "chart", "#ff8f00");
        
        statsSection.getChildren().addAll(transactionsCard, valueCard, taxCard, avgCard);
        HBox.setHgrow(transactionsCard, Priority.ALWAYS);
        HBox.setHgrow(valueCard, Priority.ALWAYS);
        HBox.setHgrow(taxCard, Priority.ALWAYS);
        HBox.setHgrow(avgCard, Priority.ALWAYS);
        
        return statsSection;
    }
    
    private VBox createStatCard(String title, String value, String icon, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5px; " +
                   "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // Card title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #757575;");
        
        // Card value
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        // Icon placeholder
        Circle iconCircle = new Circle(15);
        iconCircle.setFill(Color.web(color, 0.2));
        
        Text iconText = new Text(icon.substring(0, 1).toUpperCase());
        iconText.setFill(Color.web(color));
        iconText.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        StackPane iconPane = new StackPane(iconCircle, iconText);
        
        // Value section
        HBox valueSection = new HBox(10);
        valueSection.setAlignment(Pos.CENTER_LEFT);
        valueSection.getChildren().addAll(valueLabel);
        
        card.getChildren().addAll(titleLabel, valueSection, iconPane);
        
        return card;
    }
    
    private VBox createTransactionsTable() {
        VBox tableSection = new VBox(10);
        tableSection.setPadding(new Insets(0));
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        
        // Table header
        HBox tableHeader = new HBox();
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label tableTitle = new Label("Transactions");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #424242;");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        // Export button
        Button exportButton = new Button("Export Data");
        exportButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; -fx-font-weight: bold; " +
                          "-fx-cursor: hand; -fx-background-radius: 3px;");
        exportButton.setOnAction(e -> exportTransactions());
        
        tableHeader.getChildren().addAll(tableTitle, headerSpacer, exportButton);
        
        // Transactions table
        transactionsTable = new TableView<>();
        transactionsTable.setItems(filteredTransactions);
        transactionsTable.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");
        VBox.setVgrow(transactionsTable, Priority.ALWAYS);
        
        // Define table columns
        TableColumn<InvoiceDetails, String> receiptCol = new TableColumn<>("Receipt #");
        receiptCol.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        receiptCol.setPrefWidth(150);
        
        TableColumn<InvoiceDetails, String> dateCol = new TableColumn<>("Date & Time");
        dateCol.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = LocalDateTime.parse(cellData.getValue().getInvoiceDateTime());
            return new SimpleStringProperty(dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")));
        });
        dateCol.setPrefWidth(180);
        
        TableColumn<InvoiceDetails, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(cellData -> {
            String buyerTin = cellData.getValue().getBuyerTin();
            return new SimpleStringProperty(buyerTin != null && !buyerTin.isEmpty() ? buyerTin : "Walk-in Customer");
        });
        customerCol.setPrefWidth(150);
        
        TableColumn<InvoiceDetails, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getItemCount())));
        itemsCol.setPrefWidth(70);
        
        TableColumn<InvoiceDetails, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getInvoiceTotal()).asObject());
        amountCol.setPrefWidth(100);
        amountCol.setCellFactory(col -> new TableCell<InvoiceDetails, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(formatCurrency(amount));
                }
            }
        });
        
        TableColumn<InvoiceDetails, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isTransmitted() ? "Transmitted" : "Pending"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<InvoiceDetails, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("Transmitted".equals(status)) {
                        setStyle("-fx-text-fill: #00796b;");
                    } else {
                        setStyle("-fx-text-fill: #ff8f00;");
                    }
                }
            }
        });
        
        TableColumn<InvoiceDetails, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(createActionCellFactory());
        
        transactionsTable.getColumns().addAll(receiptCol, dateCol, customerCol, itemsCol, amountCol, statusCol, actionCol);
        
        // Add event listener for row selection
        transactionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            // Handle row selection if needed
        });
        
        tableSection.getChildren().addAll(tableHeader, transactionsTable);
        
        return tableSection;
    }
    
    private Callback<TableColumn<InvoiceDetails, Void>, TableCell<InvoiceDetails, Void>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<InvoiceDetails, Void> call(final TableColumn<InvoiceDetails, Void> param) {
                return new TableCell<>() {
                    private final Button viewButton = new Button("View");
                    private final Button syncButton = new Button("Sync");
                    private final HBox buttonBox = new HBox(5);
                    
                    {
                        viewButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; -fx-cursor: hand; " +
                                      "-fx-background-radius: 3px; -fx-font-size: 12px;");
                        viewButton.setOnAction(event -> {
                            InvoiceDetails invoice = getTableView().getItems().get(getIndex());
                            viewTransaction(invoice);
                        });
                        
                        syncButton.setStyle("-fx-background-color: #ff8f00; -fx-text-fill: white; -fx-cursor: hand; " +
                                      "-fx-background-radius: 3px; -fx-font-size: 12px;");
                        syncButton.setOnAction(event -> {
                            InvoiceDetails invoice = getTableView().getItems().get(getIndex());
                            syncTransaction(invoice);
                        });
                        
                        buttonBox.setAlignment(Pos.CENTER_LEFT);
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            buttonBox.getChildren().clear();
                            buttonBox.getChildren().add(viewButton);
                            
                            InvoiceDetails invoice = getTableView().getItems().get(getIndex());
                            if (!invoice.isTransmitted()) {
                                buttonBox.getChildren().add(syncButton);
                            }
                            
                            setGraphic(buttonBox);
                        }
                    }
                };
            }
        };
    }
    
    private HBox createActionsSection() {
        HBox actionsSection = new HBox(10);
        actionsSection.setPadding(new Insets(10, 0, 0, 0));
        actionsSection.setAlignment(Pos.CENTER_RIGHT);
        
        Button syncAllButton = new Button("Sync All Pending");
        syncAllButton.setStyle("-fx-background-color: #00796b; -fx-text-fill: white; -fx-font-weight: bold; " +
                          "-fx-cursor: hand; -fx-background-radius: 3px;");
        syncAllButton.setOnAction(e -> syncAllPending());
        
        actionsSection.getChildren().add(syncAllButton);
        
        return actionsSection;
    }
    
    private void loadTransactions() {
        // Clear existing data
        allTransactions.clear();
        
        // Add some hardcoded transactions instead of loading from DB
        LocalDateTime now = LocalDateTime.now();
        
        // Transmitted transactions
        allTransactions.add(new InvoiceDetails("INV-2025-0001", now.minusDays(1), "CUST-001", 5, 12500.00, 1875.00, true));
        allTransactions.add(new InvoiceDetails("INV-2025-0002", now.minusDays(1).minusHours(2), "CUST-002", 3, 5600.00, 840.00, true));
        allTransactions.add(new InvoiceDetails("INV-2025-0003", now.minusDays(2), "", 2, 2350.00, 352.50, true));
        allTransactions.add(new InvoiceDetails("INV-2025-0004", now.minusDays(2).minusHours(5), "CUST-003", 7, 18900.00, 2835.00, true));
        allTransactions.add(new InvoiceDetails("INV-2025-0005", now.minusDays(3), "", 1, 750.00, 112.50, true));
        
        // Pending transactions
        allTransactions.add(new InvoiceDetails("INV-2025-0006", now.minusHours(3), "CUST-004", 4, 6500.00, 975.00, false));
        allTransactions.add(new InvoiceDetails("INV-2025-0007", now.minusHours(1), "", 2, 1800.00, 270.00, false));
        allTransactions.add(new InvoiceDetails("INV-2025-0008", now, "CUST-001", 6, 15200.00, 2280.00, false));
        
        // Apply filters to update UI
        applyFilters();
        
        // Update statistics
        updateStatistics();
    }
    
    private void applyFilters() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String searchText = searchField.getText().toLowerCase();
        String statusFilter = statusFilterComboBox.getValue();
        
        // Create a composite predicate based on filters
        Predicate<InvoiceDetails> datePredicate = invoice -> {
            if (startDate == null || endDate == null) {
                return true;
            }
            LocalDateTime invoiceDate = LocalDateTime.parse(invoice.getInvoiceDateTime());
            return !invoiceDate.toLocalDate().isBefore(startDate) && !invoiceDate.toLocalDate().isAfter(endDate);
        };
        
        Predicate<InvoiceDetails> searchPredicate = invoice -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            return invoice.getInvoiceNumber().toLowerCase().contains(searchText) ||
                  (invoice.getBuyerTin() != null && invoice.getBuyerTin().toLowerCase().contains(searchText)) ||
                  String.valueOf(invoice.getInvoiceTotal()).contains(searchText);
        };
        
        Predicate<InvoiceDetails> statusPredicate = invoice -> {
            if ("All".equals(statusFilter)) {
                return true;
            } else if ("Transmitted".equals(statusFilter)) {
                return invoice.isTransmitted();
            } else if ("Pending".equals(statusFilter)) {
                return !invoice.isTransmitted();
            }
            return true;
        };
        
        // Apply combined predicates
        filteredTransactions.setPredicate(datePredicate.and(searchPredicate).and(statusPredicate));
        
        // Update statistics based on filtered data
        updateStatistics();
    }
    
    private void resetFilters() {
        startDatePicker.setValue(LocalDate.now().minusWeeks(1));
        endDatePicker.setValue(LocalDate.now());
        searchField.clear();
        statusFilterComboBox.setValue("All");
        
        applyFilters();
    }
    
    private void updateStatistics() {
        int transactionCount = filteredTransactions.size();
        double totalValue = 0;
        double totalTax = 0;
        
        for (InvoiceDetails invoice : filteredTransactions) {
            totalValue += invoice.getInvoiceTotal();
            totalTax += invoice.getTotalVat();
        }
        
        // Update statistics labels
        if (totalTransactionsLabel != null) {
            totalTransactionsLabel.setText(String.valueOf(transactionCount));
        }
        
        if (totalValueLabel != null) {
            totalValueLabel.setText(formatCurrency(totalValue));
        }
        
        if (totalTaxLabel != null) {
            totalTaxLabel.setText(formatCurrency(totalTax));
        }
    }
    
    private void viewTransaction(InvoiceDetails invoice) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Transaction Details");
        alert.setHeaderText("Invoice: " + invoice.getInvoiceNumber());
        
        // Create a formatted content for the dialog
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 10, 10, 10));
        
        // Transaction details
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 0, 0, 0));
        
        // Add info fields
        int row = 0;
        
        grid.add(createDetailLabel("Date & Time:"), 0, row);
        LocalDateTime dateTime = LocalDateTime.parse(invoice.getInvoiceDateTime());
        grid.add(createDetailValue(dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"))), 1, row++);
        
        grid.add(createDetailLabel("Customer:"), 0, row);
        String customer = invoice.getBuyerTin() != null && !invoice.getBuyerTin().isEmpty() 
                ? invoice.getBuyerTin() : "Walk-in Customer";
        grid.add(createDetailValue(customer), 1, row++);
        
        grid.add(createDetailLabel("Items:"), 0, row);
        grid.add(createDetailValue(String.valueOf(invoice.getItemCount())), 1, row++);
        
        grid.add(createDetailLabel("Amount:"), 0, row);
        grid.add(createDetailValue(formatCurrency(invoice.getInvoiceTotal())), 1, row++);
        
        grid.add(createDetailLabel("VAT:"), 0, row);
        grid.add(createDetailValue(formatCurrency(invoice.getTotalVat())), 1, row++);
        
        grid.add(createDetailLabel("Status:"), 0, row);
        Label statusValue = createDetailValue(invoice.isTransmitted() ? "Transmitted" : "Pending");
        statusValue.setStyle(invoice.isTransmitted() 
                ? "-fx-text-fill: #00796b; -fx-font-weight: bold;" 
                : "-fx-text-fill: #ff8f00; -fx-font-weight: bold;");
        grid.add(statusValue, 1, row++);
        
        if (invoice.isTransmitted() && invoice.getValidationUrl() != null && !invoice.getValidationUrl().isEmpty()) {
            grid.add(createDetailLabel("Validation URL:"), 0, row);
            Hyperlink link = new Hyperlink(invoice.getValidationUrl());
            link.setOnAction(e -> {
                // Open link in browser (in a real app)
                showAlert("Open URL", "Opening validation URL in browser: " + invoice.getValidationUrl());
            });
            grid.add(link, 1, row++);
        }
        
        content.getChildren().add(grid);
        
        // Add action buttons if needed
        if (!invoice.isTransmitted()) {
            Button syncButton = new Button("Sync with Tax Authority");
            syncButton.setStyle("-fx-background-color: #ff8f00; -fx-text-fill: white;");
            syncButton.setOnAction(e -> {
                alert.close();
                syncTransaction(invoice);
            });
            content.getChildren().add(syncButton);
        }
        
        // Add print receipt button
        Button printButton = new Button("Print Receipt");
        printButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white;");
        printButton.setOnAction(e -> {
            showAlert("Print", "Printing receipt for " + invoice.getInvoiceNumber());
        });
        content.getChildren().add(printButton);
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
    
    private Label createDetailLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #424242;");
        return label;
    }
    
    private Label createDetailValue(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #212121;");
        return label;
    }
    
    private void syncTransaction(InvoiceDetails invoice) {
        // In a real application, this would connect to the tax authority API
        // For demo purposes, we'll just simulate the process
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Sync Transaction");
        confirmAlert.setHeaderText("Sync Invoice " + invoice.getInvoiceNumber() + " with Tax Authority?");
        confirmAlert.setContentText("This will attempt to transmit the invoice data to the tax authority system.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Show progress indicator
            ProgressIndicator progress = new ProgressIndicator();
            progress.setMinSize(50, 50);
            
            VBox progressBox = new VBox(10);
            progressBox.setAlignment(Pos.CENTER);
            progressBox.getChildren().addAll(
                progress,
                new Label("Syncing with Tax Authority...")
            );
            
            Alert progressAlert = new Alert(Alert.AlertType.NONE);
            progressAlert.setTitle("Syncing");
            progressAlert.getDialogPane().setContent(progressBox);
            progressAlert.getDialogPane().getButtonTypes().clear();
            
            // Show the progress dialog
            Platform.runLater(() -> progressAlert.show());
            
            // Simulate network delay
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    
                    // Update the invoice status
                    invoice.setTransmitted(true);
                    invoice.setValidationUrl("https://efiscal.mra.mw/invoice/" + invoice.getInvoiceNumber());
                    
                    // Close progress dialog and show success
                    Platform.runLater(() -> {
                        progressAlert.close();
                        showAlert("Sync Complete", 
                                "Invoice " + invoice.getInvoiceNumber() + " has been successfully transmitted to the tax authority.");
                        
                        // Refresh the table to update the UI
                        transactionsTable.refresh();
                        updateStatistics();
                    });
                } catch (InterruptedException e) {
                    Platform.runLater(() -> {
                        progressAlert.close();
                        showAlert("Sync Failed", "Failed to sync invoice. Please try again later.");
                    });
                }
            }).start();
        }
    }
    
    private void syncAllPending() {
        // Count pending transactions
        long pendingCount = filteredTransactions.stream()
                .filter(invoice -> !invoice.isTransmitted())
                .count();
        
        if (pendingCount == 0) {
            showAlert("No Pending Transactions", "There are no pending transactions to synchronize.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Sync All Pending");
        confirmAlert.setHeaderText("Sync " + pendingCount + " Pending Transactions?");
        confirmAlert.setContentText("This will attempt to transmit all pending invoices to the tax authority system.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Show progress indicator
            ProgressBar progress = new ProgressBar(0);
            progress.setPrefWidth(300);
            
            Label statusLabel = new Label("Preparing to sync...");
            
            VBox progressBox = new VBox(10);
            progressBox.setPadding(new Insets(20));
            progressBox.setAlignment(Pos.CENTER);
            progressBox.getChildren().addAll(
                new Label("Syncing Transactions with Tax Authority"),
                progress,
                statusLabel
            );
            
            Alert progressAlert = new Alert(Alert.AlertType.NONE);
            progressAlert.setTitle("Syncing");
            progressAlert.getDialogPane().setContent(progressBox);
            progressAlert.getDialogPane().getButtonTypes().clear();
            
            // Show the progress dialog
            Platform.runLater(() -> progressAlert.show());
            
            // Simulate network process
            new Thread(() -> {
                try {
                    final double[] count = {0};
                    final double total = pendingCount;
                    
                    // Process each pending invoice
                    for (InvoiceDetails invoice : filteredTransactions) {
                        if (!invoice.isTransmitted()) {
                            // Update progress UI
                            final String currentInvoice = invoice.getInvoiceNumber();
                            Platform.runLater(() -> {
                                statusLabel.setText("Processing: " + currentInvoice);
                                progress.setProgress(count[0] / total);
                            });
                            
                            // Simulate processing time (would be API call in real app)
                            Thread.sleep(800);
                            
                            // Update the invoice status
                            invoice.setTransmitted(true);
                            invoice.setValidationUrl("https://efiscal.mra.mw/invoice/" + invoice.getInvoiceNumber());
                            
                            count[0]++;
                        }
                    }
                    
                    // Final update and close
                    Platform.runLater(() -> {
                        progress.setProgress(1.0);
                        statusLabel.setText("Completed successfully!");
                        
                        // Close after a short delay
                        try {
                            Thread.sleep(1000);
                            progressAlert.close();
                            
                            showAlert("Sync Complete", 
                                    "All pending invoices have been successfully transmitted to the tax authority.");
                            
                            // Refresh the table to update the UI
                            transactionsTable.refresh();
                            updateStatistics();
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                    });
                } catch (InterruptedException e) {
                    Platform.runLater(() -> {
                        progressAlert.close();
                        showAlert("Sync Failed", "Failed to sync invoices. Please try again later.");
                    });
                }
            }).start();
        }
    }
    
    private void exportTransactions() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Export Transactions");
        alert.setHeaderText("Export Filtered Transactions");
        
        // Create options for export format
        ComboBox<String> formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll("CSV", "Excel", "PDF");
        formatCombo.setValue("Excel");
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Select export format:"),
            formatCombo
        );
        
        alert.getDialogPane().setContent(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String format = formatCombo.getValue();
            
            // Simulate export process
            Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
            progressAlert.setTitle("Exporting Data");
            progressAlert.setHeaderText(null);
            progressAlert.setContentText("Exporting " + filteredTransactions.size() + 
                                      " transactions to " + format + " format...");
            
            // In a real app, we would actually export the data here
            progressAlert.show();
            
            new Thread(() -> {
                try {
                    // Simulate processing time
                    Thread.sleep(2000);
                    
                    Platform.runLater(() -> {
                        progressAlert.close();
                        showAlert("Export Complete", 
                                "Transaction data has been exported successfully in " + format + " format.");
                    });
                } catch (InterruptedException e) {
                    Platform.runLater(() -> {
                        progressAlert.close();
                        showAlert("Export Failed", "Failed to export data. Please try again.");
                    });
                }
            }).start();
        }
    }
    
    private String formatCurrency(double value) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormat.format(value).replace("$", "MK ");
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}