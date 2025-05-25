package com.pointofsale;

import com.pointofsale.model.AuditLog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;

public class AuditTrailView {
    private VBox mainContainer;
    private TableView<AuditLog> auditTable;
    private ObservableList<AuditLog> auditLogs;
    private FilteredList<AuditLog> filteredLogs;
    private TextField searchField;
    private ComboBox<String> actionFilter;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private Label totalRecordsLabel;
    private Label filteredRecordsLabel;

    public AuditTrailView() {
        initializeComponents();
        loadAuditLogs();
        setupEventHandlers();
    }

    public Node getView() {
        return mainContainer;
    }

    private void initializeComponents() {
        mainContainer = new VBox();
        mainContainer.setSpacing(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f5f5f7;");

        // Create header
        VBox header = createHeader();
        
        // Create filters section
        VBox filtersSection = createFiltersSection();
        
        // Create table section
        VBox tableSection = createTableSection();
        
        // Create footer with statistics
        HBox footer = createFooter();

        mainContainer.getChildren().addAll(header, filtersSection, tableSection, footer);
        VBox.setVgrow(tableSection, Priority.ALWAYS);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(0, 0, 20, 0));

        // Title
        Label titleLabel = new Label("Audit Trail");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");

        // Subtitle
        Label subtitleLabel = new Label("System activity and user actions log");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");

        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        Button refreshButton = createStyledButton("Refresh", "#4caf50", "#388e3c");
        refreshButton.setOnAction(e -> refreshAuditLogs());

        Button exportButton = createStyledButton("Export to CSV", "#2196f3", "#1976d2");
        exportButton.setOnAction(e -> exportToCSV());

        Button clearOldLogsButton = createStyledButton("Clear Old Logs", "#ff9800", "#f57c00");
        clearOldLogsButton.setOnAction(e -> showClearOldLogsDialog());

        actionButtons.getChildren().addAll(refreshButton, exportButton, clearOldLogsButton);

        header.getChildren().addAll(titleLabel, subtitleLabel, actionButtons);
        return header;
    }

    private VBox createFiltersSection() {
        VBox filtersSection = new VBox(15);
        filtersSection.setStyle("-fx-background-color: white; -fx-background-radius: 10px; " +
                              "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        filtersSection.setPadding(new Insets(20));

        Label filtersTitle = new Label("Filters");
        filtersTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // First row of filters
        HBox firstRow = new HBox(15);
        firstRow.setAlignment(Pos.CENTER_LEFT);

        // Search field
        VBox searchBox = new VBox(5);
        Label searchLabel = new Label("Search:");
        searchLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        searchField = new TextField();
        searchField.setPromptText("Search by username, action, or details...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 8px; -fx-background-radius: 5px; " +
                           "-fx-border-radius: 5px; -fx-border-color: #ddd;");
        searchBox.getChildren().addAll(searchLabel, searchField);

        // Action filter
        VBox actionBox = new VBox(5);
        Label actionLabel = new Label("Action Type:");
        actionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        actionFilter = new ComboBox<>();
        actionFilter.setPromptText("All Actions");
        actionFilter.setPrefWidth(200);
        actionFilter.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");
        actionBox.getChildren().addAll(actionLabel, actionFilter);

        firstRow.getChildren().addAll(searchBox, actionBox);

        // Second row of filters
        HBox secondRow = new HBox(15);
        secondRow.setAlignment(Pos.CENTER_LEFT);

        // Date range filters
        VBox fromDateBox = new VBox(5);
        Label fromDateLabel = new Label("From Date:");
        fromDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        fromDatePicker = new DatePicker();
        fromDatePicker.setPrefWidth(150);
        fromDatePicker.setStyle("-fx-font-size: 14px;");
        fromDateBox.getChildren().addAll(fromDateLabel, fromDatePicker);

        VBox toDateBox = new VBox(5);
        Label toDateLabel = new Label("To Date:");
        toDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        toDatePicker = new DatePicker();
        toDatePicker.setPrefWidth(150);
        toDatePicker.setStyle("-fx-font-size: 14px;");
        toDateBox.getChildren().addAll(toDateLabel, toDatePicker);

        // Clear filters button
        Button clearFiltersButton = createStyledButton("Clear Filters", "#9e9e9e", "#757575");
        clearFiltersButton.setOnAction(e -> clearFilters());
        VBox.setMargin(clearFiltersButton, new Insets(20, 0, 0, 0));

        secondRow.getChildren().addAll(fromDateBox, toDateBox, clearFiltersButton);

        filtersSection.getChildren().addAll(filtersTitle, firstRow, secondRow);
        return filtersSection;
    }

    private VBox createTableSection() {
        VBox tableSection = new VBox(10);
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        // Table header
        HBox tableHeader = new HBox();
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label tableTitle = new Label("Audit Records");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        filteredRecordsLabel = new Label("Showing 0 of 0 records");
        filteredRecordsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        tableHeader.getChildren().addAll(tableTitle, spacer, filteredRecordsLabel);

        // Create and configure table
        auditTable = new TableView<>();
        auditTable.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                           "-fx-border-width: 1px; -fx-background-radius: 8px; -fx-border-radius: 8px;");
        auditTable.setRowFactory(tv -> {
            TableRow<AuditLog> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    row.setStyle(getRowStyle(newItem.getAction()));
                }
            });
            return row;
        });

        VBox.setVgrow(auditTable, Priority.ALWAYS);

        // Configure table columns
        setupTableColumns();

        // Table container with styling
        VBox tableContainer = new VBox(auditTable);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10px; " +
                               "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        tableContainer.setPadding(new Insets(0));
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        tableSection.getChildren().addAll(tableHeader, tableContainer);
        return tableSection;
    }

    private void setupTableColumns() {
        // ID Column
        TableColumn<AuditLog, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setStyle("-fx-alignment: CENTER;");

        // Timestamp Column
        TableColumn<AuditLog, String> timestampCol = new TableColumn<>("Date & Time");
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timestampCol.setPrefWidth(180);
        timestampCol.setCellFactory(col -> new TableCell<AuditLog, String>() {
            @Override
            protected void updateItem(String timestamp, boolean empty) {
                super.updateItem(timestamp, empty);
                if (empty || timestamp == null) {
                    setText(null);
                } else {
                    try {
                        LocalDateTime dateTime = LocalDateTime.parse(timestamp, 
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        setText(dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")));
                    } catch (Exception e) {
                        setText(timestamp);
                    }
                }
            }
        });

        // Username Column
        TableColumn<AuditLog, String> usernameCol = new TableColumn<>("User");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(120);

        // Action Column
        TableColumn<AuditLog, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<AuditLog, String>() {
            @Override
            protected void updateItem(String action, boolean empty) {
                super.updateItem(action, empty);
                if (empty || action == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(action);
                    setStyle(getActionCellStyle(action));
                }
            }
        });

        // Details Column
        TableColumn<AuditLog, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsCol.setPrefWidth(400);
        detailsCol.setCellFactory(col -> new TableCell<AuditLog, String>() {
            @Override
            protected void updateItem(String details, boolean empty) {
                super.updateItem(details, empty);
                if (empty || details == null) {
                    setText(null);
                } else {
                    setText(details);
                    setWrapText(true);
                }
            }
        });

        auditTable.getColumns().addAll(idCol, timestampCol, usernameCol, actionCol, detailsCol);
    }

    private HBox createFooter() {
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(10, 0, 0, 0));

        totalRecordsLabel = new Label("Total Records: 0");
        totalRecordsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label lastUpdatedLabel = new Label("Last Updated: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")));
        lastUpdatedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        footer.getChildren().addAll(totalRecordsLabel, lastUpdatedLabel);
        return footer;
    }

    private void loadAuditLogs() {
        try {
            List<AuditLog> logs = AuditLogger.getAllLogs();
            auditLogs = FXCollections.observableArrayList(logs);
            filteredLogs = new FilteredList<>(auditLogs);
            
            SortedList<AuditLog> sortedLogs = new SortedList<>(filteredLogs);
            sortedLogs.comparatorProperty().bind(auditTable.comparatorProperty());
            
            auditTable.setItems(sortedLogs);
            
            // Populate action filter dropdown
            populateActionFilter();
            
            // Update statistics
            updateStatistics();
            
        } catch (Exception e) {
            showErrorAlert("Error Loading Audit Logs", "Failed to load audit logs: " + e.getMessage());
        }
    }

    private void populateActionFilter() {
        ObservableList<String> actions = FXCollections.observableArrayList();
        actions.add("All Actions");
        
        auditLogs.stream()
                .map(AuditLog::getAction)
                .distinct()
                .sorted()
                .forEach(actions::add);
        
        actionFilter.setItems(actions);
        actionFilter.setValue("All Actions");
    }

    private void setupEventHandlers() {
        // Search field listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        // Action filter listener
        actionFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        // Date picker listeners
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        Predicate<AuditLog> searchPredicate = log -> {
            String searchText = searchField.getText();
            if (searchText == null || searchText.trim().isEmpty()) {
                return true;
            }
            
            String lowerCaseSearch = searchText.toLowerCase();
            return log.getUsername().toLowerCase().contains(lowerCaseSearch) ||
                   log.getAction().toLowerCase().contains(lowerCaseSearch) ||
                   log.getDetails().toLowerCase().contains(lowerCaseSearch);
        };

        Predicate<AuditLog> actionPredicate = log -> {
            String selectedAction = actionFilter.getValue();
            return selectedAction == null || selectedAction.equals("All Actions") || 
                   log.getAction().equals(selectedAction);
        };

        Predicate<AuditLog> datePredicate = log -> {
            try {
                LocalDateTime logDateTime = LocalDateTime.parse(log.getTimestamp(), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                LocalDate logDate = logDateTime.toLocalDate();
                
                LocalDate fromDate = fromDatePicker.getValue();
                LocalDate toDate = toDatePicker.getValue();
                
                if (fromDate != null && logDate.isBefore(fromDate)) {
                    return false;
                }
                
                if (toDate != null && logDate.isAfter(toDate)) {
                    return false;
                }
                
                return true;
            } catch (Exception e) {
                return true; // Include if date parsing fails
            }
        };

        filteredLogs.setPredicate(searchPredicate.and(actionPredicate).and(datePredicate));
        updateStatistics();
    }

    private void clearFilters() {
        searchField.clear();
        actionFilter.setValue("All Actions");
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
    }

    private void refreshAuditLogs() {
        loadAuditLogs();
        showInfoAlert("Refresh Complete", "Audit logs have been refreshed.");
    }

    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Audit Logs");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("audit_logs_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        // Get the current stage (you might need to pass this from the main application)
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write CSV header
                writer.write("ID,Timestamp,Username,Action,Details\n");
                
                // Write data
                for (AuditLog log : filteredLogs) {
                    writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        log.getId(),
                        log.getTimestamp(),
                        log.getUsername(),
                        log.getAction(),
                        log.getDetails().replace("\"", "\"\"")));
                }
                
                showInfoAlert("Export Successful", 
                    "Audit logs exported successfully to: " + file.getAbsolutePath());
                
            } catch (IOException e) {
                showErrorAlert("Export Failed", "Failed to export audit logs: " + e.getMessage());
            }
        }
    }

    private void showClearOldLogsDialog() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Clear Old Logs");
        confirmDialog.setHeaderText("Delete Old Audit Logs");
        confirmDialog.setContentText("This action will permanently delete audit logs older than 30 days. Continue?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // You would implement the actual deletion logic here
                // For now, just show a placeholder message
                showInfoAlert("Feature Not Implemented", 
                    "Log cleanup functionality will be implemented in a future version.");
            }
        });
    }

    private void updateStatistics() {
        int totalRecords = auditLogs.size();
        int filteredRecords = filteredLogs.size();
        
        totalRecordsLabel.setText("Total Records: " + totalRecords);
        filteredRecordsLabel.setText("Showing " + filteredRecords + " of " + totalRecords + " records");
    }

    private String getRowStyle(String action) {
        switch (action.toLowerCase()) {
            case "login":
            case "logout":
                return "-fx-background-color: #e8f5e8;";
            case "sale":
            case "transaction":
                return "-fx-background-color: #e3f2fd;";
            case "delete":
            case "void":
                return "-fx-background-color: #ffebee;";
            case "error":
                return "-fx-background-color: #fff3e0;";
            default:
                return "-fx-background-color: white;";
        }
    }

    private String getActionCellStyle(String action) {
        switch (action.toLowerCase()) {
            case "login":
                return "-fx-text-fill: #2e7d32; -fx-font-weight: bold;";
            case "logout":
                return "-fx-text-fill: #1976d2; -fx-font-weight: bold;";
            case "sale":
            case "transaction":
                return "-fx-text-fill: #0288d1; -fx-font-weight: bold;";
            case "delete":
            case "void":
                return "-fx-text-fill: #d32f2f; -fx-font-weight: bold;";
            case "error":
                return "-fx-text-fill: #f57c00; -fx-font-weight: bold;";
            default:
                return "-fx-text-fill: #424242;";
        }
    }

    private Button createStyledButton(String text, String bgColor, String hoverColor) {
        Button button = new Button(text);
        button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-padding: 8px 16px; -fx-background-radius: 5px; -fx-cursor: hand;", bgColor));
        
        button.setOnMouseEntered(e -> button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-padding: 8px 16px; -fx-background-radius: 5px; -fx-cursor: hand;", hoverColor)));
        
        button.setOnMouseExited(e -> button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-padding: 8px 16px; -fx-background-radius: 5px; -fx-cursor: hand;", bgColor)));
        
        return button;
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
