package com.pointofsale;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.util.Pair;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.event.Event;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import com.pointofsale.model.InvoiceHeader;
import com.pointofsale.model.LineItemDto;
import com.pointofsale.model.TerminalContactInfo;
import com.pointofsale.model.TaxBreakDown;
import com.pointofsale.helper.Helper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import com.pointofsale.model.InvoiceDetails;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import com.google.zxing.*;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.HashMap;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Callback;
import java.text.NumberFormat;
import com.google.zxing.common.BitMatrix;
import com.pointofsale.model.LevyBreakDownDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.List;
import java.util.function.Predicate;
import javafx.scene.text.TextAlignment;

public class TransactionsView {

    private TableView<InvoiceDetails> transactionsTable;
    private final ObservableList<InvoiceDetails> allTransactions = FXCollections.observableArrayList();
    private final FilteredList<InvoiceDetails> filteredTransactions = new FilteredList<>(allTransactions);
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private TextField searchField;
    private ComboBox<String> statusFilterComboBox;
    private Label totalTransactionsLabel;
    private Label totalValueLabel;
    private Label avgValueLabel;
    private Label totalTaxLabel;

    public Node getView() {
       // Initialize components
       initializeComponents();
    
       // Build UI (this sets up the labels)
       Node content = createMainContent();
    
       loadTransactions();
    
       return content;
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
        Pair<VBox, Label> transactionsPair = createStatCard("Total Transactions", "0", "receipt", "#3949ab");
        VBox transactionsCard = transactionsPair.getKey();
        totalTransactionsLabel = transactionsPair.getValue();

        // Total value card
        Pair<VBox, Label> valuePair = createStatCard("Total Value", formatCurrency(0), "money", "#00796b");
        VBox valueCard = valuePair.getKey();
        totalValueLabel = valuePair.getValue();

        // Total tax card
        Pair<VBox, Label> taxPair = createStatCard("Total Tax", formatCurrency(0), "tax", "#c62828");
        VBox taxCard = taxPair.getKey();
        totalTaxLabel = taxPair.getValue();

        // Average transaction card (not storing label)
        Pair<VBox, Label> avgPair = createStatCard("Average Transaction", formatCurrency(0), "chart", "#ff8f00");
        VBox avgCard = avgPair.getKey();
        avgValueLabel = avgPair.getValue();

        statsSection.getChildren().addAll(transactionsCard, valueCard, taxCard, avgCard);
        HBox.setHgrow(transactionsCard, Priority.ALWAYS);
        HBox.setHgrow(valueCard, Priority.ALWAYS);
        HBox.setHgrow(taxCard, Priority.ALWAYS);
        HBox.setHgrow(avgCard, Priority.ALWAYS);

        return statsSection;
    }
    
    private Pair<VBox, Label> createStatCard(String title, String value, String icon, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5px; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Title label
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #757575;");

        // Value label
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        // Icon section
        Circle iconCircle = new Circle(15);
        iconCircle.setFill(Color.web(color, 0.2));

        Text iconText = new Text(icon.substring(0, 1).toUpperCase());
        iconText.setFill(Color.web(color));
        iconText.setFont(Font.font("System", FontWeight.BOLD, 12));

        StackPane iconPane = new StackPane(iconCircle, iconText);

        // Value section
        HBox valueSection = new HBox(10);
        valueSection.setAlignment(Pos.CENTER_LEFT);
        valueSection.getChildren().add(valueLabel);

        card.getChildren().addAll(titleLabel, valueSection, iconPane);

        return new Pair<>(card, valueLabel);
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
        LocalDateTime dateTime = cellData.getValue().getInvoiceDateTime();
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
            setStyle("-fx-text-fill: " + ("Transmitted".equals(status) ? "#00796b;" : "#ff8f00;"));
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
    
    // Updated createActionCellFactory method
private Callback<TableColumn<InvoiceDetails, Void>, TableCell<InvoiceDetails, Void>> createActionCellFactory() {
    return new Callback<>() {
        @Override
        public TableCell<InvoiceDetails, Void> call(final TableColumn<InvoiceDetails, Void> param) {
            return new TableCell<>() {
                private final Button viewButton = new Button("View");
                private final Button previewButton = new Button("Preview");
                private final Button syncButton = new Button("Sync");
                private final HBox buttonBox = new HBox(5);
                
                {
                    viewButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; -fx-cursor: hand; " +
                                  "-fx-background-radius: 3px; -fx-font-size: 12px;");
                    viewButton.setOnAction(event -> {
                        InvoiceDetails invoice = getTableView().getItems().get(getIndex());
                        viewTransaction(invoice);
                    });
                    
                    previewButton.setStyle("-fx-background-color: #00796b; -fx-text-fill: white; -fx-cursor: hand; " +
                                     "-fx-background-radius: 3px; -fx-font-size: 12px;");
                    previewButton.setOnAction(event -> {
                        InvoiceDetails invoice = getTableView().getItems().get(getIndex());
                        previewReceipt(invoice);
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
                        buttonBox.getChildren().addAll(viewButton, previewButton);
                        
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
    allTransactions.clear();
    allTransactions.addAll(Helper.getAllTransactions());
    applyFilters();
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
            LocalDateTime invoiceDate = invoice.getInvoiceDateTime();
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
            totalTax += invoice.getTotalVAT();
        }
        
   
        totalTransactionsLabel.setText(String.valueOf(transactionCount));
   
        totalValueLabel.setText(formatCurrency(totalValue));
    
        totalTaxLabel.setText(formatCurrency(totalTax));
        
         // Calculate and update average transaction value
        double avgValue = transactionCount > 0 ? totalValue / transactionCount : 0;
        avgValueLabel.setText(formatCurrency(avgValue));
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
        LocalDateTime dateTime = invoice.getInvoiceDateTime();
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
        grid.add(createDetailValue(formatCurrency(invoice.getTotalVAT())), 1, row++);
        
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
    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmAlert.setTitle("Sync Transaction");
    confirmAlert.setHeaderText("Sync Invoice " + invoice.getInvoiceNumber() + " with Tax Authority?");
    confirmAlert.setContentText("This will attempt to transmit the invoice data to the tax authority system.");

    Optional<ButtonType> result = confirmAlert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
        // Progress UI
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
        progressAlert.getDialogPane().getButtonTypes().setAll(ButtonType.CLOSE);
        progressAlert.setOnCloseRequest(Event::consume); // Prevent manual close while syncing

        // Show progress
        Platform.runLater(progressAlert::show);

        // Background thread for sync
        new Thread(() -> {
            boolean success = Helper.transmitInvoice(invoice.getInvoiceNumber());

            Platform.runLater(() -> {
                progressAlert.setOnCloseRequest(null); // Allow close now
                progressAlert.close();
                
                if (success) {
                    showAlert("Sync Complete",
                            "Invoice " + invoice.getInvoiceNumber() + " has been successfully transmitted.");
                     // Reload data (this is missing!)
                     allTransactions.clear();
                     allTransactions.addAll(Helper.getAllTransactions());
    
                       // Apply filters (this is missing!)
                     applyFilters();
    
                    transactionsTable.refresh();
                    updateStatistics();
                } else {
                    showAlert("Sync Failed",
                            "Invoice " + invoice.getInvoiceNumber() + " could not be transmitted.\nPlease check your network and try again.");
                }
            });
        }).start();
    }
}

   private void syncAllPending() {
    if (!Platform.isFxApplicationThread()) {
        Platform.runLater(this::syncAllPending);
        return;
    }

    long pendingCount = filteredTransactions.stream()
            .filter(invoice -> !invoice.isTransmitted())
            .count();

    if (pendingCount == 0) {
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("No Pending Transactions");
        infoAlert.setHeaderText(null);
        infoAlert.setContentText("There are no pending transactions to synchronize.");
        infoAlert.show();
        return;
    }

    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmAlert.setTitle("Sync All Pending");
    confirmAlert.setHeaderText("Sync " + pendingCount + " Pending Transactions?");
    confirmAlert.setContentText("This will attempt to transmit all pending invoices to the tax authority system.");

    // show confirmation dialog and handle response in separate thread
    confirmAlert.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
            ProgressBar progress = new ProgressBar(0);
            progress.setPrefWidth(300);
            Label statusLabel = new Label("Preparing to sync...");
            VBox progressBox = new VBox(10, new Label("Syncing Transactions with Tax Authority"), progress, statusLabel);
            progressBox.setPadding(new Insets(20));
            progressBox.setAlignment(Pos.CENTER);

            Alert progressAlert = new Alert(Alert.AlertType.NONE);
            progressAlert.setTitle("Syncing");
            progressAlert.getDialogPane().setContent(progressBox);
            progressAlert.getDialogPane().getButtonTypes().clear();
            progressAlert.setOnCloseRequest(Event::consume);

            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            progressAlert.getDialogPane().getButtonTypes().add(closeButton);
            progressAlert.getDialogPane().lookupButton(closeButton).setVisible(false);

            progressAlert.show();

            Thread syncThread = new Thread(() -> {
                AtomicInteger syncedCount = new AtomicInteger(0);
                int totalToSync = (int) pendingCount;

                try {
                    Helper.retryPendingTransactions(
                            (progressPair) -> {
                                int current = syncedCount.incrementAndGet();
                                String invoiceNumber = progressPair.getValue();
                                Platform.runLater(() -> {
                                    statusLabel.setText("Processing: " + invoiceNumber);
                                    progress.setProgress((double) current / totalToSync);
                                });
                            },
                            (failedInvoices) -> {
                                Platform.runLater(() -> {
                                    progress.setProgress(1.0);
                                    PauseTransition pauseBeforeClose = new PauseTransition(Duration.seconds(1));
                                    pauseBeforeClose.setOnFinished(e -> {
                                        progressAlert.setOnCloseRequest(null);
                                        progressAlert.close();

                                        PauseTransition pauseBeforeAlert = new PauseTransition(Duration.millis(300));
                                        pauseBeforeAlert.setOnFinished(event -> {
                                            Platform.runLater(() -> {
                                                if (failedInvoices.isEmpty()) {
                                                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                                                    success.setTitle("Sync Complete");
                                                    success.setHeaderText(null);
                                                    success.setContentText("All pending invoices have been successfully transmitted.");
                                                    success.show();
                                                } else {
                                                    String failedList = String.join(", ", failedInvoices);
                                                    Alert partial = new Alert(Alert.AlertType.WARNING);
                                                    partial.setTitle("Partial Sync");
                                                    partial.setHeaderText(null);
                                                    partial.setContentText(
                                                            (failedInvoices.size() == totalToSync)
                                                                    ? "All invoices failed to sync:\n" + failedList
                                                                    : "Some invoices failed to sync:\n" + failedList
                                                    );
                                                    partial.show();
                                                }
                                                // Reload all data
                                                allTransactions.clear();
                                                allTransactions.addAll(Helper.getAllTransactions());
                                            
                                                // Reapply filters to update the filtered list
                                                applyFilters();
                                                transactionsTable.refresh();
                                                updateStatistics();
                                            });
                                        });
                                        pauseBeforeAlert.play();
                                    });
                                    pauseBeforeClose.play();
                                });
                            }
                    );
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        progress.setProgress(1.0);
                        progressAlert.setOnCloseRequest(null);
                        progressAlert.close();

                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Sync Error");
                        error.setHeaderText(null);
                        error.setContentText("An error occurred: " + ex.getMessage());
                        error.show();
                        updateStatistics();
                    });
                }
            });

            syncThread.setDaemon(true);
            syncThread.start();
                        transactionsTable.refresh();
        }
    });
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
    
//method to preview receipt
private void previewReceipt(InvoiceDetails invoice) {
    Alert previewAlert = new Alert(Alert.AlertType.NONE);
    previewAlert.setTitle("Receipt Preview");
    previewAlert.setHeaderText("Receipt for Invoice: " + invoice.getInvoiceNumber());
    
    // Create receipt preview content
    VBox receiptContent = createReceiptPreview(invoice);
    
    // Create scroll pane for long receipts
    ScrollPane scrollPane = new ScrollPane(receiptContent);
    scrollPane.setFitToWidth(true);
    scrollPane.setPrefSize(400, 600);
    scrollPane.setStyle("-fx-background-color: white;");
    
    // Create button bar
    HBox buttonBar = new HBox(10);
    buttonBar.setAlignment(Pos.CENTER_RIGHT);
    buttonBar.setPadding(new Insets(10, 0, 0, 0));
    
    Button printButton = new Button("Print Receipt");
    printButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; -fx-font-weight: bold;");
    printButton.setOnAction(e -> {
        previewAlert.close();
        printReceipt(invoice);
    });
    
    Button closeButton = new Button("Close");
    closeButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white;");
    closeButton.setOnAction(e -> previewAlert.close());
    
    buttonBar.getChildren().addAll(printButton, closeButton);
    
    VBox mainContent = new VBox(10);
    mainContent.getChildren().addAll(scrollPane, buttonBar);
    mainContent.setPadding(new Insets(10));
    
    previewAlert.getDialogPane().setContent(mainContent);
    
    // Add a hidden ButtonType to enable the X button
    ButtonType hiddenClose = new ButtonType("", ButtonBar.ButtonData.CANCEL_CLOSE);
    previewAlert.getDialogPane().getButtonTypes().add(hiddenClose);
    
    // Hide the button but keep the functionality
    previewAlert.getDialogPane().lookupButton(hiddenClose).setVisible(false);
    previewAlert.getDialogPane().lookupButton(hiddenClose).setManaged(false);
    
     previewAlert.showAndWait();
}
    
// Method to create receipt preview content - matches EscPosReceiptPrinter exactly
private VBox createReceiptPreview(InvoiceDetails invoice) {
    VBox receiptBox = new VBox();
    receiptBox.setStyle("-fx-background-color: white; -fx-padding: 20px; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");
    receiptBox.setAlignment(Pos.TOP_CENTER);
    receiptBox.setSpacing(0); // No spacing to match thermal printer
    
    // Get terminal contact info and company details (same as EscPosReceiptPrinter)
    TerminalContactInfo contact = Helper.getTerminalContactInfo();
    String tin = Helper.getTin();
    String companyName = Helper.getTrading();
    String storeEmail = contact.email;
    String storeAddress = contact.addressLine;
    String storePhone = contact.phone;
    String vatStatus = Helper.isVATRegistered() ? "*VAT REGISTERED*" : "*NOT VAT REGISTERED*";
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String currentDateTime = LocalDateTime.now().format(formatter);
    
    // Get line items and tax breakdowns
    List<LineItemDto> lineItems = Helper.getLineItems(invoice.getInvoiceNumber());
    List<TaxBreakDown> taxBreakdowns = Helper.generateTaxBreakdown(lineItems);
    List<LevyBreakDownDto> levies = Helper.getLevyBreakdown(invoice.getInvoiceNumber());
    String buyersName = invoice.getBuyerTin() != null && !invoice.getBuyerTin().isEmpty() ? 
                      invoice.getBuyerTin() : "";
    String buyersTIN = invoice.getBuyerTin() != null ? invoice.getBuyerTin() : "";
    String validationUrl = invoice.getValidationUrl() != null ? invoice.getValidationUrl() : "";
    
    // Match the exact structure from EscPosReceiptPrinter
    
    // Empty line (from ESC_INIT + LF)
    receiptBox.getChildren().add(createReceiptLabel(""));
    
    // Legal Receipt Start Marker (CENTER + EMPHASIZED)
    receiptBox.getChildren().add(createBoldReceiptLabel("*** START OF LEGAL RECEIPT ***"));
    receiptBox.getChildren().add(createReceiptLabel(""));
    receiptBox.getChildren().add(createReceiptLabel(""));
    
    // Company Header Block (EMPHASIZED + DOUBLE_SIZE equivalent)
    Label companyLabel = new Label(companyName);
    companyLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 16px; -fx-alignment: center;");
    receiptBox.getChildren().add(companyLabel);
    
    // Store information (all centered)
    receiptBox.getChildren().add(createReceiptLabel(storeAddress));
    receiptBox.getChildren().add(createReceiptLabel("Tel: " + storePhone));
    receiptBox.getChildren().add(createReceiptLabel("E-MAIL: " + storeEmail));
    receiptBox.getChildren().add(createReceiptLabel("TIN: " + tin));
    receiptBox.getChildren().add(createReceiptLabel(vatStatus));
    receiptBox.getChildren().add(createReceiptLabel(""));
    receiptBox.getChildren().add(createReceiptLabel(""));
    
    // Receipt Title (EMPHASIZED)
    receiptBox.getChildren().add(createBoldReceiptLabel("*** TAX INVOICE ***"));
    
    // Pretty border
    receiptBox.getChildren().add(createDividerLabel());
    
    // Transaction Details - left-right aligned (matching printFormattedLine)
    receiptBox.getChildren().add(createReceiptLine("Receipt#", invoice.getInvoiceNumber()));
    receiptBox.getChildren().add(createReceiptLine("Date", currentDateTime));
    receiptBox.getChildren().add(createReceiptLine("Customer", buyersName));
    receiptBox.getChildren().add(createReceiptLine("Buyer TIN", buyersTIN));
    
    // Divider before items
    receiptBox.getChildren().add(createDividerLabel());
    
    // Line items with exact formatting from EscPosReceiptPrinter
    for (LineItemDto item : lineItems) {
        // Item description (full name, left aligned)
        receiptBox.getChildren().add(createLeftAlignedLabel(item.getDescription()));
        
        // Quantity * Unit Price and Total Amount with tax rate
        String quantityPrice = String.format("%s X %s", 
            item.getQuantity(),
            formatCurrency(item.getUnitPrice())
        );
        String totalAmount = formatCurrency(item.getQuantity() * item.getUnitPrice());
        String taxRateDisplay = Helper.isVATRegistered() ? totalAmount + " " + item.getTaxRateId() : totalAmount;
        
        receiptBox.getChildren().add(createReceiptLine(quantityPrice, taxRateDisplay));
    }
    
    // Divider before totals
    receiptBox.getChildren().add(createDividerLabel());
    
    // Calculate totals (exact same logic as EscPosReceiptPrinter)
    double totalVAT = 0;
    double subtotal = 0;
    
    if (Helper.isVATRegistered()) {
        totalVAT = taxBreakdowns.stream().mapToDouble(TaxBreakDown::getTaxAmount).sum();
        subtotal = taxBreakdowns.stream().mapToDouble(TaxBreakDown::getTaxableAmount).sum();
    } else {
        subtotal = taxBreakdowns.stream().mapToDouble(TaxBreakDown::getTaxableAmount).sum();
        totalVAT = 0;
    }
    
    double totalLevies = 0.0;
      if (levies != null && !levies.isEmpty()) {
      totalLevies = levies.stream()
                        .mapToDouble(LevyBreakDownDto::getLevyAmount)
                        .sum();
}

double invoiceTotal = subtotal + totalVAT + totalLevies;

    double amountPaid = invoice.getAmountPaid();
    String transactionType = invoice.getTransactionType();
    
    // Print totals with left-right alignment
    receiptBox.getChildren().add(createReceiptLine("Subtotal", formatCurrency(subtotal)));
    
    if (Helper.isVATRegistered()) {
        // Tax breakdowns - print only if VAT registered
        for (TaxBreakDown tax : taxBreakdowns) {
            String taxRateStr = String.valueOf(Helper.getTaxRateById(tax.getRateId()));
            String label = String.format("VAT %s - %s%%", tax.getRateId(), taxRateStr);
            receiptBox.getChildren().add(createReceiptLine(label, formatCurrency(tax.getTaxAmount())));
        }
    }
    
    /* ===========================
   LEVIES
   =========================== */
   if (levies != null && !levies.isEmpty()) {
      for (LevyBreakDownDto levy : levies) {
          String levyName = Helper.getLevyNameById(levy.getLevyTypeId());
          receiptBox.getChildren().add(
            createReceiptLine(levyName, formatCurrency(levy.getLevyAmount()))
          );
        }
    }
    
    // Total, emphasized
    receiptBox.getChildren().add(createBoldReceiptLine("TOTAL", formatCurrency(invoiceTotal)));
    
    //Transaction type:
    receiptBox.getChildren().add(createReceiptLine("Transaction Type", transactionType));
    
    // Payment information (simplified - using invoice total as amount tendered)
    receiptBox.getChildren().add(createReceiptLine("Amount Paid", formatCurrency(amountPaid)));
    
    // Change only if > 0 (assuming no change for existing invoices)
    double change = amountPaid-invoiceTotal;
    if (change > 0) {
        receiptBox.getChildren().add(createReceiptLine("Change", formatCurrency(change)));
    }
    
    receiptBox.getChildren().add(createReceiptLabel(""));
    
    // Validation section
    receiptBox.getChildren().add(createDividerLabel());
    receiptBox.getChildren().add(createBoldReceiptLabel("*** VALIDATE YOUR RECEIPT ***"));
    
    // QR Code
    printQRCode(receiptBox, validationUrl);
    
    // Footer
    receiptBox.getChildren().add(createReceiptLabel(""));
    receiptBox.getChildren().add(createDividerLabel());
    receiptBox.getChildren().add(createBoldReceiptLabel("*** THANK YOU FOR YOUR BUSINESS ***"));
    receiptBox.getChildren().add(createReceiptLabel("Keep this receipt for your records"));
    receiptBox.getChildren().add(createReceiptLabel(""));
    receiptBox.getChildren().add(createReceiptLabel(""));
    
    // Legal Receipt End Marker
    receiptBox.getChildren().add(createBoldReceiptLabel("*** END OF LEGAL RECEIPT ***"));
    receiptBox.getChildren().add(createReceiptLabel(""));
    receiptBox.getChildren().add(createReceiptLabel(""));
    receiptBox.getChildren().add(createReceiptLabel(""));
    
    return receiptBox;
}

/**
 * Prints QR code for JavaFX receipt preview - matches thermal printer structure exactly
 */
private void printQRCode(VBox receiptBox, String data) {  
    try {
        // Make sure data isn't too long for QR code
        String qrData = data.length() > 300 ? data.substring(0, 300) : data;
        
        // Center the QR code (equivalent to output.write(ESC_ALIGN_CENTER))
        
        // Clear QR code buffer (equivalent to ESC/POS command)
        
        // Set QR code model (equivalent to ESC/POS command)
        
        // Set QR code size (1-16), bigger numbers = larger QR code
        byte qrSize = 4; // Larger size for better scanning
        
        // Set error correction level (48 = L, 49 = M, 50 = Q, 51 = H)
        byte errorCorrectionLevel = 51; // H level = highest error correction
        
        // Store QR code data (equivalent to ESC/POS data storage)
        
        // Generate and display QR code (equivalent to Print QR code command)
        BufferedImage qrImage = generateQRCodeImage(qrData, qrSize, errorCorrectionLevel);
        
        if (qrImage != null) {
            // Convert BufferedImage to JavaFX Image
            WritableImage fxImage = SwingFXUtils.toFXImage(qrImage, null);
            
            // Create ImageView to display the QR code
            ImageView qrImageView = new ImageView(fxImage);
            qrImageView.setFitWidth(120);
            qrImageView.setFitHeight(120);
            qrImageView.setPreserveRatio(true);
            
            receiptBox.getChildren().add(qrImageView);
        } else {
            throw new Exception("QR code generation failed");
        }
        
    } catch (Exception e) {
        // If QR code fails, write a message (matches thermal printer exactly)
        receiptBox.getChildren().add(createReceiptLabel(""));
        receiptBox.getChildren().add(createReceiptLabel("(QR Code Unavailable)"));
        receiptBox.getChildren().add(createReceiptLabel(""));
    }
}

/**
 * Generate QR code image using ZXing - uses same parameters as thermal printer
 */
private BufferedImage generateQRCodeImage(String data, byte qrSize, byte errorCorrectionLevel) {
    try {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        
        // Map error correction level from thermal printer values
        com.google.zxing.qrcode.decoder.ErrorCorrectionLevel ecLevel;
        switch (errorCorrectionLevel) {
            case 48: ecLevel = com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L; break; // L
            case 49: ecLevel = com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M; break; // M
            case 50: ecLevel = com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.Q; break; // Q
            case 51: default: ecLevel = com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H; break; // H
        }
        
        // Calculate size based on qrSize parameter (similar to thermal printer scaling)
        int imageSize = Math.max(100, qrSize * 25);
        
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ecLevel);
        hints.put(EncodeHintType.MARGIN, 1);
        
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, imageSize, imageSize, hints);
        
        BufferedImage qrImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < imageSize; x++) {
            for (int y = 0; y < imageSize; y++) {
                qrImage.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }
        
        return qrImage;
        
    } catch (Exception e) {
        System.err.println("Failed to generate QR code: " + e.getMessage());
        return null;
    }
}

private Label createBoldReceiptLine(String left, String right) {
    // Calculate spacing for 48 character width
    int spacing = Math.max(1, 48 - left.length() - right.length());
    StringBuilder line = new StringBuilder(left);
    for (int i = 0; i < spacing; i++) {
        line.append(" ");
    }
    line.append(right);
    
    Label label = new Label(line.toString());
    label.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 12px; -fx-alignment: center;");
    return label;
}

// Helper methods for receipt preview
private Label createReceiptLabel(String text) {
    Label label = new Label(text);
    label.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
    return label;
}

private Label createBoldReceiptLabel(String text) {
    Label label = new Label(text);
    label.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 12px;");
    return label;
}

private Label createReceiptLine(String left, String right) {
    int spacing = Math.max(1, 48 - left.length() - right.length());
    StringBuilder line = new StringBuilder(left);
    for (int i = 0; i < spacing; i++) {
        line.append(" ");
    }
    line.append(right);

    Label label = new Label(line.toString());
    label.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
    label.setWrapText(false);
    label.setTextOverrun(OverrunStyle.CLIP); 
    label.setMaxWidth(Double.MAX_VALUE);
    label.setMinWidth(Region.USE_PREF_SIZE);

    return label;
}

private Label createDividerLabel() {
    StringBuilder divider = new StringBuilder();
    for (int i = 0; i < 48; i++) {
        divider.append("-");
    }
    Label label = new Label(divider.toString());
    label.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
    return label;
}

private void printReceipt(InvoiceDetails invoice) {
    try {
        // Get all necessary data for printing
        List<LineItemDto> lineItems = Helper.getLineItems(invoice.getInvoiceNumber());
        List<TaxBreakDown> taxBreakdowns = Helper.generateTaxBreakdown(lineItems);
        List<LevyBreakDownDto> levyBreakdowns = Helper.getLevyBreakdown(invoice.getInvoiceNumber()); // <-- added

        String validationUrl = invoice.getValidationUrl() != null ? invoice.getValidationUrl() : "";
        
        // Create invoice header object
        InvoiceHeader invoiceHeader = new InvoiceHeader();
        invoiceHeader.setPaymentMethod(invoice.getTransactionType());
        invoiceHeader.setInvoiceNumber(invoice.getInvoiceNumber());
        
        // For simplicity, assuming payment details - you may need to get these from your data
        double invoiceTotal = invoice.getInvoiceTotal();
        double amountTendered = invoice.getAmountPaid();

        double change = amountTendered - invoiceTotal;
        
        String buyersName = invoice.getBuyerTin() != null && !invoice.getBuyerTin().isEmpty() ? 
                          invoice.getBuyerTin() : "Walk-in Customer";
        String buyersTIN = invoice.getBuyerTin() != null ? invoice.getBuyerTin() : "";
        
        // Call the actual print method
        EscPosReceiptPrinter.printReceipt(
            invoiceHeader,
            buyersName,
            buyersTIN,
            lineItems,
            validationUrl,
            amountTendered,
            change,
            taxBreakdowns,
            levyBreakdowns
        );
        
        showAlert("Print Success", "Receipt printed successfully for " + invoice.getInvoiceNumber());
        
    } catch (Exception e) {
        showAlert("Print Error", "Failed to print receipt: " + e.getMessage());
    }
}

private Label createLeftAlignedLabel(String text) {
    Label label = new Label(text);
    label.setFont(Font.font("Courier New", 12));

    label.setAlignment(Pos.CENTER_LEFT); 
    label.setTextAlignment(TextAlignment.LEFT); 
    label.setMaxWidth(Double.MAX_VALUE); 
    label.setTextOverrun(OverrunStyle.CLIP);
    label.setWrapText(true);

    return label;
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