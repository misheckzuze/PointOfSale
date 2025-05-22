package com.pointofsale;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import com.pointofsale.model.Customer;
import javafx.scene.Node;
import java.text.NumberFormat;
import javafx.scene.control.*;
import java.util.Locale;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import com.pointofsale.helper.Helper;

public class CustomersView {
    
    // Main components
    private TableView<Customer> customersTable;
    private TextField searchField;
    private ObservableList<Customer> customersList;
    private FilteredList<Customer> filteredCustomers;
    private Label statusLabel;
    private Label totalCustomersLabel;
    private Customer selectedCustomer;
    
    /**
     * Constructor - initializes the customer view
     */
    public CustomersView() {
        // Initialize with hardcoded data instead of loading from database
        customersList = FXCollections.observableArrayList(createSampleCustomers());
    }

    /**
     * Creates customers
     * @return list of customers
     */
    private ObservableList<Customer> createSampleCustomers() {
    // Now this method returns actual customers loaded from the database
    return Helper.loadCustomers();
}


    /**
     * Creates and returns the complete customers view
     * @return Node containing the customers view
     */
    public Node getView() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f5f5f7;");
        
        // Set up the top section with header and search
        mainLayout.setTop(createHeaderSection());
        
        // Set up the center with the customers table
        mainLayout.setCenter(createTableSection());
        
        // Set up the bottom with status bar
        mainLayout.setBottom(createStatusBar());
        
        // Initial table load and sorting
        Platform.runLater(() -> {
            refreshCustomersTable();
            searchField.requestFocus();
        });
        
        return mainLayout;
    }
    
    /**
     * Creates the header section with title and search functionality
     * @return VBox containing header components
     */
    private VBox createHeaderSection() {
        VBox headerSection = new VBox(15);
        headerSection.setPadding(new Insets(0, 0, 15, 0));
        
        // Create title section
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Customer Management");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Statistics box
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(10, 15, 10, 15));
        statsBox.setStyle("-fx-background-color: white; -fx-background-radius: 5px; " +
                         "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // Total customers stat
        VBox totalCustomersBox = new VBox(2);
        totalCustomersBox.setAlignment(Pos.CENTER);
        
        Label totalLabel = new Label("Total Customers");
        totalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        
        totalCustomersLabel = new Label(String.valueOf(customersList.size()));
        totalCustomersLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        
        totalCustomersBox.getChildren().addAll(totalLabel, totalCustomersLabel);
        
        // Active customers stat
        VBox activeCustomersBox = new VBox(2);
        activeCustomersBox.setAlignment(Pos.CENTER);
        
        Label activeLabel = new Label("Active Customers");
        activeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        
        long activeCount = customersList.stream()
                .filter(c -> "Active".equals(c.getStatus()))
                .count();
        
        Label activeCustomersLabel = new Label(String.valueOf(activeCount));
        activeCustomersLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00796b;");
        
        activeCustomersBox.getChildren().addAll(activeLabel, activeCustomersLabel);
        
        // Inactive customers stat
        VBox inactiveCustomersBox = new VBox(2);
        inactiveCustomersBox.setAlignment(Pos.CENTER);
        
        Label inactiveLabel = new Label("Inactive Customers");
        inactiveLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        
        long inactiveCount = customersList.stream()
                .filter(c -> "Inactive".equals(c.getStatus()))
                .count();
        
        Label inactiveCustomersLabel = new Label(String.valueOf(inactiveCount));
        inactiveCustomersLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #c62828;");
        
        inactiveCustomersBox.getChildren().addAll(inactiveLabel, inactiveCustomersLabel);
        
        statsBox.getChildren().addAll(totalCustomersBox, activeCustomersBox, inactiveCustomersBox);
        
        titleBar.getChildren().addAll(titleLabel, spacer, statsBox);
        
        // Create search section
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Search customers by name, email, phone, or TIN...");
        searchField.setPrefHeight(40);
        searchField.setPrefWidth(400);
        searchField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                           "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: white;");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCustomers(newValue);
        });
        
        ComboBox<String> filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("All Customers", "Active Customers", "Inactive Customers", "Business Customers", "Individual Customers");
        filterComboBox.setValue("All Customers");
        filterComboBox.setPrefHeight(40);
        filterComboBox.setStyle("-fx-font-size: 14px;");
        
        ComboBox<String> sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll("Sort by Name", "Sort by Date Added", "Sort by Last Purchase", "Sort by Sales Value");
        sortComboBox.setValue("Sort by Name");
        sortComboBox.setPrefHeight(40);
        sortComboBox.setStyle("-fx-font-size: 14px;");
        
        filterComboBox.setOnAction(e -> applyFilters(filterComboBox.getValue(), searchField.getText()));
        
        sortComboBox.setOnAction(e -> {
            applySorting(sortComboBox.getValue());
        });
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setPrefHeight(40);
        refreshButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; " +
                            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; " +
                            "-fx-background-radius: 5px;");
        refreshButton.setOnAction(e -> refreshCustomersTable());
        
        searchBar.getChildren().addAll(searchField, filterComboBox, sortComboBox, refreshButton);
        
        // Add components to the header section
        headerSection.getChildren().addAll(titleBar, searchBar);
        
        return headerSection;
    }
    
    /**
     * Creates the table section with the customers table
     * @return VBox containing customers table and controls
     */
    private VBox createTableSection() {
        VBox tableSection = new VBox(15);
        
        // Create the customers table
        customersTable = new TableView<>();
        customersTable.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");
        customersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(customersTable, Priority.ALWAYS);
        
        // Define table columns
        TableColumn<Customer, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);
        
        TableColumn<Customer, String> nameCol = new TableColumn<>("Customer Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(180);
        
        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(120);
        
        TableColumn<Customer, String> tinCol = new TableColumn<>("TIN");
        tinCol.setCellValueFactory(new PropertyValueFactory<>("tin"));
        tinCol.setPrefWidth(120);
        
        TableColumn<Customer, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> {
            boolean isBusiness = cellData.getValue().getTin() != null && !cellData.getValue().getTin().isEmpty();
            return new SimpleStringProperty(isBusiness ? "Business" : "Individual");
        });
        typeCol.setPrefWidth(100);
        
        TableColumn<Customer, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(column -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(status);
                    
                    // Style based on status
                    if (status.equals("Active")) {
                        setStyle("-fx-text-fill: #00796b; -fx-font-weight: bold;");
                    } else if (status.equals("Inactive")) {
                        setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #424242;");
                    }
                }
            }
        });
        
        TableColumn<Customer, String> lastPurchaseCol = new TableColumn<>("Last Purchase");
        lastPurchaseCol.setCellValueFactory(new PropertyValueFactory<>("lastPurchaseDate"));
        lastPurchaseCol.setPrefWidth(140);
        
        TableColumn<Customer, String> registeredDateCol = new TableColumn<>("Registered On");
        registeredDateCol.setCellValueFactory(new PropertyValueFactory<>("registeredDate"));
        registeredDateCol.setPrefWidth(140);
        
        // Action column for view/edit/delete
        TableColumn<Customer, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(createActionButtonCellFactory());
        
        customersTable.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, tinCol, typeCol, 
                                        statusCol, lastPurchaseCol, registeredDateCol, actionCol);
        
        // Add double-click handler to view details
        customersTable.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    viewCustomerDetails(row.getItem());
                }
            });
            return row;
        });
        
        // Add select listener
        customersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedCustomer = newSelection;
        });
        
        // Initialize filtered list
        filteredCustomers = new FilteredList<>(customersList, p -> true);
        SortedList<Customer> sortedCustomers = new SortedList<>(filteredCustomers);
        sortedCustomers.comparatorProperty().bind(customersTable.comparatorProperty());
        customersTable.setItems(sortedCustomers);
        
        tableSection.getChildren().add(customersTable);
        
        return tableSection;
    }
    
    /**
     * Creates action button cell factory for the customers table
     */
    private Callback<TableColumn<Customer, Void>, TableCell<Customer, Void>> createActionButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Customer, Void> call(final TableColumn<Customer, Void> param) {
                return new TableCell<>() {
                    private final HBox buttonsBox = new HBox(5);
                    private final Button viewBtn = new Button("View");
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    
                    {
                        // Configure buttons
                        viewBtn.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; -fx-cursor: hand; " +
                                      "-fx-background-radius: 3px; -fx-font-size: 11px; -fx-padding: 5 8;");
                        editBtn.setStyle("-fx-background-color: #ff8f00; -fx-text-fill: white; -fx-cursor: hand; " +
                                      "-fx-background-radius: 3px; -fx-font-size: 11px; -fx-padding: 5 8;");
                        deleteBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-cursor: hand; " +
                                        "-fx-background-radius: 3px; -fx-font-size: 11px; -fx-padding: 5 8;");
                        
                        viewBtn.setOnAction(event -> {
                            Customer customer = getTableView().getItems().get(getIndex());
                            viewCustomerDetails(customer);
                        });
                        
                        editBtn.setOnAction(event -> {
                            Customer customer = getTableView().getItems().get(getIndex());
                            editCustomer(customer);
                        });
                        
                        deleteBtn.setOnAction(event -> {
                            Customer customer = getTableView().getItems().get(getIndex());
                            deleteCustomer(customer);
                        });
                        
                        buttonsBox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                        buttonsBox.setAlignment(Pos.CENTER);
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(buttonsBox);
                        }
                    }
                };
            }
        };
    }
    
    /**
     * Creates the status bar at the bottom of the view
     * @return HBox containing status information
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(10, 0, 0, 0));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #757575;");
        
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        
        statusBar.getChildren().add(statusLabel);
        
        return statusBar;
    }
    
    /**
     * Filter customers based on search text
     * @param searchText The text to search for
     */
    private void filterCustomers(String searchText) {
        filteredCustomers.setPredicate(customer -> {
            // If search text is empty, show all customers
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            
            String lowerCaseFilter = searchText.toLowerCase();
            
            // Match against customer fields
            if (customer.getName().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (customer.getPhone() != null && customer.getPhone().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (customer.getTin() != null && customer.getTin().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (customer.getId().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            
            return false;
        });
        
        updateStatusWithFilterResults();
    }
    
    /**
     * Apply additional filters for customers table
     * @param filterOption The selected filter option
     * @param searchText Current search text
     */
    private void applyFilters(String filterOption, String searchText) {
        filteredCustomers.setPredicate(customer -> {
            // First apply text search
            boolean matchesSearch = true;
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                matchesSearch = customer.getName().toLowerCase().contains(lowerCaseFilter) ||
                               (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(lowerCaseFilter)) ||
                               (customer.getPhone() != null && customer.getPhone().toLowerCase().contains(lowerCaseFilter)) ||
                               (customer.getTin() != null && customer.getTin().toLowerCase().contains(lowerCaseFilter)) ||
                               customer.getId().toLowerCase().contains(lowerCaseFilter);
            }
            
            // Then apply category filter
            boolean matchesCategory = switch (filterOption) {
                case "Active Customers" -> "Active".equals(customer.getStatus());
                case "Inactive Customers" -> "Inactive".equals(customer.getStatus());
                case "Business Customers" -> customer.getTin() != null && !customer.getTin().isEmpty();
                case "Individual Customers" -> customer.getTin() == null || customer.getTin().isEmpty();
                default -> true; // "All Customers"
            };
            
            return matchesSearch && matchesCategory;
        });
        
        updateStatusWithFilterResults();
    }
    
    /**
     * Apply sorting to the customers table
     * @param sortOption The selected sort option
     */
    private void applySorting(String sortOption) {
        switch (sortOption) {
            case "Sort by Name" -> {
                customersTable.getSortOrder().clear();
                customersTable.getColumns().get(1).setSortType(TableColumn.SortType.ASCENDING);
                customersTable.getSortOrder().add(customersTable.getColumns().get(1));
            }
            case "Sort by Date Added" -> {
                customersTable.getSortOrder().clear();
                customersTable.getColumns().get(8).setSortType(TableColumn.SortType.DESCENDING);
                customersTable.getSortOrder().add(customersTable.getColumns().get(8));
            }
            case "Sort by Last Purchase" -> {
                customersTable.getSortOrder().clear();
                customersTable.getColumns().get(7).setSortType(TableColumn.SortType.DESCENDING);
                customersTable.getSortOrder().add(customersTable.getColumns().get(7));
            }
            case "Sort by Sales Value" -> {
                // This would require additional data in the Customer model
                // For now, we'll sort by ID as a placeholder
                customersTable.getSortOrder().clear();
                customersTable.getColumns().get(0).setSortType(TableColumn.SortType.ASCENDING);
                customersTable.getSortOrder().add(customersTable.getColumns().get(0));
            }
        }
        customersTable.sort();
    }
    
    /**
 * View customer details in a professionally styled dialog
 * @param customer The customer to view
 */
private void viewCustomerDetails(Customer customer) {
    if (customer == null) return;
    
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Customer Details");
    
    // Apply custom styling to dialog
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #3949ab; -fx-border-width: 0 0 2 0;");
    dialogPane.setPrefWidth(650);
    dialogPane.setPrefHeight(650);
    
    // Create a custom header with the primary color
    VBox headerBox = new VBox();
    headerBox.setStyle("-fx-background-color: #3949ab; -fx-padding: 15px;");
    Label headerLabel = new Label("Customer Information");
    headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
    headerBox.getChildren().add(headerLabel);
    
    Label subHeaderLabel = new Label("ID: " + customer.getId() + " - " + customer.getName());
    subHeaderLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
    headerBox.getChildren().add(subHeaderLabel);
    
    // Set the button types with custom styling
    ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
    ButtonType editButtonType = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
    dialogPane.getButtonTypes().addAll(closeButtonType, editButtonType);
    
    // Create the customer details grid with improved styling
    GridPane grid = new GridPane();
    grid.setHgap(15);
    grid.setVgap(15);
    grid.setPadding(new Insets(20, 25, 20, 25));
    grid.setStyle("-fx-background-color: white;");
    
    // Style for all section headers
    String sectionHeaderStyle = "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #3949ab; " +
                               "-fx-padding: 5px 0 0 0; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;";
    
    // Style for all labels
    String labelStyle = "-fx-font-weight: bold; -fx-text-fill: #555555;";
    
    // Style for all value fields
    String valueStyle = "-fx-background-color: #f8f8f8; -fx-border-color: #dddddd; " +
                        "-fx-border-radius: 3px; -fx-padding: 5px; -fx-font-size: 13px;";
    
    // Section 1: Customer Information
    Label customerInfoHeader = new Label("Customer Information");
    customerInfoHeader.setStyle(sectionHeaderStyle);
    grid.add(customerInfoHeader, 0, 0, 2, 1);
    
    // Customer ID
    Label idLabel = new Label("Customer ID:");
    idLabel.setStyle(labelStyle);
    grid.add(idLabel, 0, 1);
    
    Label idValue = new Label(customer.getId());
    idValue.setStyle(valueStyle);
    grid.add(idValue, 1, 1);
    
    // Customer name
    Label nameLabel = new Label("Customer Name:");
    nameLabel.setStyle(labelStyle);
    grid.add(nameLabel, 0, 2);
    
    Label nameValue = new Label(customer.getName());
    nameValue.setStyle(valueStyle);
    grid.add(nameValue, 1, 2);
    
    // Email
    Label emailLabel = new Label("Email:");
    emailLabel.setStyle(labelStyle);
    grid.add(emailLabel, 0, 3);
    
    Label emailValue = new Label(customer.getEmail());
    emailValue.setStyle(valueStyle);
    grid.add(emailValue, 1, 3);
    
    // Phone
    Label phoneLabel = new Label("Phone:");
    phoneLabel.setStyle(labelStyle);
    grid.add(phoneLabel, 0, 4);
    
    Label phoneValue = new Label(customer.getPhone());
    phoneValue.setStyle(valueStyle);
    grid.add(phoneValue, 1, 4);
    
    // Address
    Label addressLabel = new Label("Address:");
    addressLabel.setStyle(labelStyle);
    grid.add(addressLabel, 0, 5);
    
    Label addressValue = new Label(customer.getAddress() != null ? customer.getAddress() : "");
    addressValue.setStyle(valueStyle);
    grid.add(addressValue, 1, 5);
    
    // TIN
    Label tinLabel = new Label("TIN:");
    tinLabel.setStyle(labelStyle);
    grid.add(tinLabel, 0, 6);
    
    Label tinValue = new Label(customer.getTin() != null ? customer.getTin() : "");
    tinValue.setStyle(valueStyle);
    grid.add(tinValue, 1, 6);
    
    // Section 2: Account Information
    Label accountInfoHeader = new Label("Account Information");
    accountInfoHeader.setStyle(sectionHeaderStyle);
    grid.add(accountInfoHeader, 0, 7, 2, 1);
    
    // Status
    Label statusLabel = new Label("Status:");
    statusLabel.setStyle(labelStyle);
    grid.add(statusLabel, 0, 8);
    
    Label statusValue = new Label(customer.getStatus());
    // Style status with color based on Active/Inactive
    String statusColor = customer.getStatus().equals("Active") ? "#4CAF50" : "#F44336";
    statusValue.setStyle(valueStyle + "; -fx-text-fill: " + statusColor + "; -fx-font-weight: bold;");
    grid.add(statusValue, 1, 8);
    
    // Registration Date
    Label regDateLabel = new Label("Registered Date:");
    regDateLabel.setStyle(labelStyle);
    grid.add(regDateLabel, 0, 9);
    
    Label regDateValue = new Label(customer.getRegisteredDate());
    regDateValue.setStyle(valueStyle);
    grid.add(regDateValue, 1, 9);
    
    // Last Purchase Date
    Label lastPurchaseLabel = new Label("Last Purchase:");
    lastPurchaseLabel.setStyle(labelStyle);
    grid.add(lastPurchaseLabel, 0, 10);
    
    Label lastPurchaseValue = new Label(customer.getLastPurchaseDate() != null ? 
                                       customer.getLastPurchaseDate() : "No purchases");
    lastPurchaseValue.setStyle(valueStyle);
    grid.add(lastPurchaseValue, 1, 10);
    
    // Notes
    Label notesLabel = new Label("Notes:");
    notesLabel.setStyle(labelStyle);
    grid.add(notesLabel, 0, 11);
    
    TextArea notesArea = new TextArea(customer.getNotes() != null ? customer.getNotes() : "");
    notesArea.setWrapText(true);
    notesArea.setPrefRowCount(3);
    notesArea.setEditable(false);
    notesArea.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #dddddd; " +
                      "-fx-border-radius: 3px; -fx-padding: 5px; -fx-font-size: 13px;");
    grid.add(notesArea, 1, 11);
    
    // Section 3: Purchase History
    Label historyHeader = new Label("Purchase History");
    historyHeader.setStyle(sectionHeaderStyle);
    grid.add(historyHeader, 0, 12, 2, 1);
    
    // Create TableView with styling
    TableView<PurchaseRecord> purchaseTable = new TableView<>();
    purchaseTable.setPrefHeight(150);
    purchaseTable.setStyle("-fx-background-color: white; -fx-border-color: #dddddd;");
    
    // Style the table header
    purchaseTable.getStylesheets().add("data:text/css," +
        ".table-view .column-header { -fx-background-color: #f0f0f0; -fx-border-color: #dddddd; }" +
        ".table-view .column-header .label { -fx-text-fill: #3949ab; -fx-font-weight: bold; }");
    
    // Define columns
    TableColumn<PurchaseRecord, String> dateCol = new TableColumn<>("Date");
    dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
    dateCol.setPrefWidth(100);
    
    TableColumn<PurchaseRecord, String> invoiceCol = new TableColumn<>("Invoice #");
    invoiceCol.setCellValueFactory(new PropertyValueFactory<>("invoice"));
    invoiceCol.setPrefWidth(120);
    
    TableColumn<PurchaseRecord, Double> amountCol = new TableColumn<>("Amount");
    amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
    amountCol.setPrefWidth(100);
    // Format the amount column with currency
    amountCol.setCellFactory(tc -> new TableCell<PurchaseRecord, Double>() {
        @Override
        protected void updateItem(Double amount, boolean empty) {
            super.updateItem(amount, empty);
            if (empty || amount == null) {
                setText(null);
            } else {
                setText(String.format("$%.2f", amount));
            }
        }
    });
    
    TableColumn<PurchaseRecord, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    statusCol.setPrefWidth(100);
    // Add color to status cells
    statusCol.setCellFactory(tc -> new TableCell<PurchaseRecord, String>() {
        @Override
        protected void updateItem(String status, boolean empty) {
            super.updateItem(status, empty);
            if (empty || status == null) {
                setText(null);
                setStyle("");
            } else {
                setText(status);
                if (status.equals("Paid")) {
                    setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                } else if (status.equals("Pending")) {
                    setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                } else if (status.equals("Overdue")) {
                    setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                }
            }
        }
    });
    
    purchaseTable.getColumns().addAll(dateCol, invoiceCol, amountCol, statusCol);
    
    // Add sample purchase history data
    ObservableList<PurchaseRecord> purchaseHistory = createSamplePurchaseHistory(customer);
    purchaseTable.setItems(purchaseHistory);
    
    // Add the table to the grid
    grid.add(purchaseTable, 0, 13, 2, 1);
    
    // Add total purchases summary
    double totalAmount = 0.0;
    for (PurchaseRecord record : purchaseHistory) {
        totalAmount += record.getAmount();
    }
    
    Label totalPurchasesLabel = new Label("Total Purchases:");
    totalPurchasesLabel.setStyle(labelStyle + "-fx-font-size: 14px;");
    grid.add(totalPurchasesLabel, 0, 14);
    
    Label totalAmountValue = new Label(formatCurrency(totalAmount));
    totalAmountValue.setStyle(valueStyle + "-fx-font-weight: bold; -fx-font-size: 14px;");
    grid.add(totalAmountValue, 1, 14);
    
    // Set column constraints for responsive layout
    ColumnConstraints column1 = new ColumnConstraints();
    column1.setMinWidth(150);
    column1.setPrefWidth(150);
    
    ColumnConstraints column2 = new ColumnConstraints();
    column2.setHgrow(Priority.ALWAYS);
    column2.setFillWidth(true);
    
    grid.getColumnConstraints().addAll(column1, column2);
    
    // Combine header and grid in a VBox
    VBox contentBox = new VBox();
    contentBox.getChildren().addAll(headerBox, grid);
    contentBox.setSpacing(0);
    
    dialog.getDialogPane().setContent(contentBox);
    
    // Style the buttons
    Button editButton = (Button) dialog.getDialogPane().lookupButton(editButtonType);
    editButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; -fx-font-weight: bold; " +
                       "-fx-padding: 8px 20px; -fx-cursor: hand;");
    
    Button closeButton = (Button) dialog.getDialogPane().lookupButton(closeButtonType);
    closeButton.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #555555; " +
                        "-fx-padding: 8px 20px; -fx-cursor: hand;");
    
    // Request focus on the close button
    Platform.runLater(() -> closeButton.requestFocus());
    
    // Show the dialog and handle the response
    Optional<ButtonType> result = dialog.showAndWait();
    if (result.isPresent() && result.get() == editButtonType) {
        editCustomer(customer);
    }
}
/**
     * Creates sample purchase history for a customer
     * @param customer The customer to create history for
     * @return List of sample purchase records
     */
    private ObservableList<PurchaseRecord> createSamplePurchaseHistory(Customer customer) {
        ObservableList<PurchaseRecord> history = FXCollections.observableArrayList();
        
        // Only show purchase history for active customers
        if ("Active".equals(customer.getStatus())) {
            String customerId = customer.getId();
            
            switch (customerId) {
                case "C001" -> {
                    history.add(new PurchaseRecord("2025-05-01", "INV-10025", 325.50, "Completed"));
                    history.add(new PurchaseRecord("2025-04-15", "INV-9876", 120.75, "Completed"));
                    history.add(new PurchaseRecord("2025-03-22", "INV-9254", 450.00, "Completed"));
                }
                case "C002" -> {
                    history.add(new PurchaseRecord("2025-04-20", "INV-10010", 1250.00, "Completed"));
                    history.add(new PurchaseRecord("2025-03-15", "INV-9720", 875.25, "Completed"));
                    history.add(new PurchaseRecord("2025-02-10", "INV-9315", 2100.00, "Completed"));
                    history.add(new PurchaseRecord("2025-01-05", "INV-8950", 950.50, "Completed"));
                }
                case "C004" -> {
                    history.add(new PurchaseRecord("2025-05-05", "INV-10032", 750.00, "Completed"));
                    history.add(new PurchaseRecord("2025-04-05", "INV-9825", 750.00, "Completed"));
                    history.add(new PurchaseRecord("2025-03-05", "INV-9600", 750.00, "Completed"));
                }
                case "C005" -> {
                    history.add(new PurchaseRecord("2025-04-25", "INV-10015", 85.99, "Completed"));
                    history.add(new PurchaseRecord("2025-03-10", "INV-9650", 129.50, "Completed"));
                }
               case "C006" -> {
                    history.add(new PurchaseRecord("2025-05-10", "INV-10045", 1875.25, "Completed"));
                    history.add(new PurchaseRecord("2025-04-22", "INV-9988", 2250.00, "Completed"));
                    history.add(new PurchaseRecord("2025-04-08", "INV-9850", 1375.50, "Completed"));
                }
                case "C008" -> {
                    history.add(new PurchaseRecord("2025-05-02", "INV-10030", 950.25, "Completed"));
                    history.add(new PurchaseRecord("2025-04-12", "INV-9860", 1200.00, "Completed"));
                }
                // Default case - all other customers get empty history
                default -> { }
            }
        }
        
        return history;
    }
    
    private String formatCurrency(double value) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormat.format(value).replace("$", "MK ");
    }    
   /**
 * Edit customer information in a professionally styled dialog
 * @param customer The customer to edit
 */
private void editCustomer(Customer customer) {
    if (customer == null) return;
    
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Edit Customer");
    
    // Apply custom styling to dialog
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #3949ab; -fx-border-width: 0 0 2 0;");
    dialogPane.setPrefWidth(550);
    dialogPane.setPrefHeight(600);
    
    // Create a custom header with the primary color
    VBox headerBox = new VBox();
    headerBox.setStyle("-fx-background-color: #3949ab; -fx-padding: 15px;");
    Label headerLabel = new Label("Edit Customer Information");
    headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
    headerBox.getChildren().add(headerLabel);
    
    Label subHeaderLabel = new Label("Update customer details below");
    subHeaderLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
    headerBox.getChildren().add(subHeaderLabel);
    
    // Set the button types with custom styling
    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialogPane.getButtonTypes().addAll(cancelButtonType, saveButtonType);
    
    // Create the customer form grid with improved styling
    GridPane grid = new GridPane();
    grid.setHgap(15);
    grid.setVgap(15);
    grid.setPadding(new Insets(20, 25, 20, 25));
    grid.setStyle("-fx-background-color: white;");
    
    // Style for all labels
    String labelStyle = "-fx-font-weight: bold; -fx-text-fill: #555555;";
    // Style for all text fields
    String textFieldStyle = "-fx-background-color: #f8f8f8; -fx-border-color: #dddddd; " +
                           "-fx-border-radius: 3px; -fx-padding: 5px; -fx-font-size: 13px;";
    // Style for read-only fields
    String readOnlyStyle = "-fx-background-color: #f0f0f0; -fx-border-color: #dddddd; " +
                          "-fx-text-fill: #777777; -fx-border-radius: 3px; -fx-padding: 5px;";
    
    // Customer ID - read-only
    Label idLabel = new Label("Customer ID:");
    idLabel.setStyle(labelStyle);
    grid.add(idLabel, 0, 0);
    
    TextField idField = new TextField(customer.getId());
    idField.setEditable(false);
    idField.setStyle(readOnlyStyle);
    grid.add(idField, 1, 0);
    
    // Customer name
    Label nameLabel = new Label("Customer Name:*");
    nameLabel.setStyle(labelStyle);
    grid.add(nameLabel, 0, 1);
    
    TextField nameField = new TextField(customer.getName());
    nameField.setStyle(textFieldStyle);
    nameField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        if (isNowFocused) {
            nameField.setStyle(textFieldStyle + "-fx-border-color: #3949ab; -fx-border-width: 1px;");
        } else {
            nameField.setStyle(textFieldStyle);
        }
    });
    grid.add(nameField, 1, 1);
    
    // Email
    Label emailLabel = new Label("Email:*");
    emailLabel.setStyle(labelStyle);
    grid.add(emailLabel, 0, 2);
    
    TextField emailField = new TextField(customer.getEmail());
    emailField.setStyle(textFieldStyle);
    emailField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        if (isNowFocused) {
            emailField.setStyle(textFieldStyle + "-fx-border-color: #3949ab; -fx-border-width: 1px;");
        } else {
            emailField.setStyle(textFieldStyle);
        }
    });
    grid.add(emailField, 1, 2);
    
    // Phone
    Label phoneLabel = new Label("Phone:*");
    phoneLabel.setStyle(labelStyle);
    grid.add(phoneLabel, 0, 3);
    
    TextField phoneField = new TextField(customer.getPhone());
    phoneField.setStyle(textFieldStyle);
    phoneField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        if (isNowFocused) {
            phoneField.setStyle(textFieldStyle + "-fx-border-color: #3949ab; -fx-border-width: 1px;");
        } else {
            phoneField.setStyle(textFieldStyle);
        }
    });
    grid.add(phoneField, 1, 3);
    
    // Address
    Label addressLabel = new Label("Address:");
    addressLabel.setStyle(labelStyle);
    grid.add(addressLabel, 0, 4);
    
    TextField addressField = new TextField(customer.getAddress());
    addressField.setStyle(textFieldStyle);
    addressField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        if (isNowFocused) {
            addressField.setStyle(textFieldStyle + "-fx-border-color: #3949ab; -fx-border-width: 1px;");
        } else {
            addressField.setStyle(textFieldStyle);
        }
    });
    grid.add(addressField, 1, 4);
    
    // TIN
    Label tinLabel = new Label("TIN:");
    tinLabel.setStyle(labelStyle);
    grid.add(tinLabel, 0, 5);
    
    TextField tinField = new TextField(customer.getTin());
    tinField.setStyle(textFieldStyle);
    tinField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        if (isNowFocused) {
            tinField.setStyle(textFieldStyle + "-fx-border-color: #3949ab; -fx-border-width: 1px;");
        } else {
            tinField.setStyle(textFieldStyle);
        }
    });
    grid.add(tinField, 1, 5);
    
    // Status
    Label statusLabel = new Label("Status:*");
    statusLabel.setStyle(labelStyle);
    grid.add(statusLabel, 0, 6);
    
    ComboBox<String> statusComboBox = new ComboBox<>();
    statusComboBox.getItems().addAll("Active", "Inactive");
    statusComboBox.setValue(customer.getStatus());
    statusComboBox.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; " +
                           "-fx-border-radius: 3px; -fx-padding: 3px; -fx-font-size: 13px;");
    statusComboBox.setMaxWidth(Double.MAX_VALUE);
    grid.add(statusComboBox, 1, 6);
    
    // Registered date - read-only
    Label regDateLabel = new Label("Registered Date:");
    regDateLabel.setStyle(labelStyle);
    grid.add(regDateLabel, 0, 7);
    
    TextField registeredDateField = new TextField(customer.getRegisteredDate());
    registeredDateField.setEditable(false);
    registeredDateField.setStyle(readOnlyStyle);
    grid.add(registeredDateField, 1, 7);
    
    // Last purchase date
    Label lastPurchaseLabel = new Label("Last Purchase:");
    lastPurchaseLabel.setStyle(labelStyle);
    grid.add(lastPurchaseLabel, 0, 8);
    
    DatePicker lastPurchaseDatePicker = new DatePicker();
    lastPurchaseDatePicker.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 3px;");
    
    if (customer.getLastPurchaseDate() != null && !customer.getLastPurchaseDate().isEmpty()) {
        try {
            lastPurchaseDatePicker.setValue(LocalDate.parse(customer.getLastPurchaseDate()));
        } catch (Exception e) {
            // Use current date if parsing fails
            lastPurchaseDatePicker.setValue(LocalDate.now());
        }
    }
    
    grid.add(lastPurchaseDatePicker, 1, 8);
    
    // Notes
    Label notesLabel = new Label("Notes:");
    notesLabel.setStyle(labelStyle);
    grid.add(notesLabel, 0, 9);
    
    TextArea notesArea = new TextArea(customer.getNotes());
    notesArea.setWrapText(true);
    notesArea.setPrefRowCount(3);
    notesArea.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #dddddd; " +
                      "-fx-border-radius: 3px; -fx-padding: 5px; -fx-font-size: 13px;");
    notesArea.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
        if (isNowFocused) {
            notesArea.setStyle(notesArea.getStyle() + "; -fx-border-color: #3949ab; -fx-border-width: 1px;");
        } else {
            notesArea.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #dddddd; " +
                              "-fx-border-radius: 3px; -fx-padding: 5px; -fx-font-size: 13px;");
        }
    });
    grid.add(notesArea, 1, 9);
    
    // Validation message
    Label validationLabel = new Label("* Required fields");
    validationLabel.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px; -fx-font-style: italic;");
    grid.add(validationLabel, 0, 10, 2, 1);
    
    // Set column constraints for responsive layout
    ColumnConstraints column1 = new ColumnConstraints();
    column1.setMinWidth(100);
    column1.setPrefWidth(120);
    
    ColumnConstraints column2 = new ColumnConstraints();
    column2.setHgrow(Priority.ALWAYS);
    column2.setFillWidth(true);
    
    grid.getColumnConstraints().addAll(column1, column2);
    
    // Combine header and grid in a VBox
    VBox contentBox = new VBox();
    contentBox.getChildren().addAll(headerBox, grid);
    contentBox.setSpacing(0);
    
    dialog.getDialogPane().setContent(contentBox);
    
    // Style the buttons
    Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
    saveButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; -fx-font-weight: bold; " +
                       "-fx-padding: 8px 20px; -fx-cursor: hand;");
    
    Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
    cancelButton.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #555555; " +
                         "-fx-padding: 8px 20px; -fx-cursor: hand;");
    
    // Request focus on the name field by default
    Platform.runLater(() -> nameField.requestFocus());
    
    // Enable/disable save button depending on whether required fields are filled
    saveButton.setDisable(false);
    
    // Add form validation with visual feedback
    nameField.textProperty().addListener((observable, oldValue, newValue) -> {
        boolean isEmpty = newValue.trim().isEmpty();
        if (isEmpty) {
            nameField.setStyle(textFieldStyle + "; -fx-border-color: #e53935;");
        } else {
            nameField.setStyle(textFieldStyle);
        }
        validateForm(saveButton, nameField, emailField, phoneField);
    });
    
    emailField.textProperty().addListener((observable, oldValue, newValue) -> {
        boolean isEmpty = newValue.trim().isEmpty();
        if (isEmpty) {
            emailField.setStyle(textFieldStyle + "; -fx-border-color: #e53935;");
        } else {
            emailField.setStyle(textFieldStyle);
        }
        validateForm(saveButton, nameField, emailField, phoneField);
    });
    
    phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
        boolean isEmpty = newValue.trim().isEmpty();
        if (isEmpty) {
            phoneField.setStyle(textFieldStyle + "; -fx-border-color: #e53935;");
        } else {
            phoneField.setStyle(textFieldStyle);
        }
        validateForm(saveButton, nameField, emailField, phoneField);
    });
    
    // Show the dialog and handle the response
    Optional<ButtonType> result = dialog.showAndWait();
    if (result.isPresent() && result.get() == saveButtonType) {
        // Validate the form
        if (nameField.getText().trim().isEmpty() || 
            emailField.getText().trim().isEmpty() || 
            phoneField.getText().trim().isEmpty()) {
            
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please fill in all required fields");
            alert.setContentText("Customer name, email and phone are required.");
            
            // Style the alert
            DialogPane alertPane = alert.getDialogPane();
            alertPane.setStyle("-fx-background-color: white; -fx-border-color: #e53935; -fx-border-width: 0 0 2 0;");
            alertPane.lookupButton(ButtonType.OK).setStyle("-fx-background-color: #e53935; -fx-text-fill: white;");
            
            alert.showAndWait();
            
            // Show the form again
            editCustomer(customer);
            return;
        }
        
        
        Helper.updateCustomer(customer.getId(), nameField.getText().trim(), phoneField.getText().trim(), emailField.getText().trim(),
                                  tinField.getText().trim(), addressField.getText().trim(), notesArea.getText().trim());
        
        // Refresh the table and show success message
        refreshCustomersTable();
        showStatusMessage("Customer " + customer.getId() + " updated successfully");
    }
}

/**
 * Helper method to validate form and enable/disable save button
 */
private void validateForm(Button saveButton, TextField nameField, TextField emailField, TextField phoneField) {
    boolean isValid = !nameField.getText().trim().isEmpty() && 
                     !emailField.getText().trim().isEmpty() && 
                     !phoneField.getText().trim().isEmpty();
    saveButton.setDisable(!isValid);
}
    
    /**
     * Delete a customer after confirmation
     * @param customer The customer to delete
     */
    private void deleteCustomer(Customer customer) {
        if (customer == null) return;
        
        Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Customer: " + customer.getName());
        confirmDialog.setContentText("Are you sure you want to delete this customer? This action cannot be undone.");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Remove the customer from the list
            Helper.deleteCustomer(customer.getId());
            
            // If this was the selected customer, clear the selection
            if (customer.equals(selectedCustomer)) {
                selectedCustomer = null;
            }
            
            // Refresh the table and show success message
            refreshCustomersTable();
            showStatusMessage("Customer " + customer.getId() + " deleted successfully");
        }
    }
    
    /**
     * Refreshes the customers table
     */
    private void refreshCustomersTable() {
        
        customersList = FXCollections.observableArrayList(Helper.loadCustomers());
        // Update the table
        customersTable.refresh();
        
        // Update statistics
        totalCustomersLabel.setText(String.valueOf(customersList.size()));
        
        // Update status message
        updateStatusWithFilterResults();
    }
    
    /**
     * Updates the status bar with filtering results
     */
    private void updateStatusWithFilterResults() {
        int totalShowing = customersTable.getItems().size();
        int totalCustomers = customersList.size();
        
        if (totalShowing < totalCustomers) {
            statusLabel.setText("Showing " + totalShowing + " of " + totalCustomers + " customers");
        } else {
            statusLabel.setText("Showing all " + totalCustomers + " customers");
        }
    }
    
    /**
     * Shows a status message in the status bar
     * @param message The message to show
     */
    private void showStatusMessage(String message) {
        statusLabel.setText(message);
        
        // Reset to filter status after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(this::updateStatusWithFilterResults);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Inner class to represent a purchase record for display in the customer details view
     */
    public static class PurchaseRecord {
        private final String date;
        private final String invoice;
        private final double amount;
        private final String status;
        
        public PurchaseRecord(String date, String invoice, double amount, String status) {
            this.date = date;
            this.invoice = invoice;
            this.amount = amount;
            this.status = status;
        }
        
        public String getDate() { return date; }
        public String getInvoice() { return invoice; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }
    }
}
    