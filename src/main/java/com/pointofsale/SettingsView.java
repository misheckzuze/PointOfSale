package com.pointofsale;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import java.util.Optional;

public class SettingsView {
    
    private VBox mainContainer;
    private TabPane settingsTabPane;
    
    // Business Settings Fields
    private TextField businessNameField;
    private TextField tradingNameField;
    private TextField addressField;
    private TextField phoneField;
    private TextField emailField;
    private TextField tinField;
    private TextArea businessDescriptionArea;
    
    // System Settings Fields
    private ComboBox<String> currencyComboBox;
    private ComboBox<String> taxRateComboBox;
    private CheckBox enableDiscountsCheckBox;
    private CheckBox enableCustomerAccountsCheckBox;
    private CheckBox enableInventoryTrackingCheckBox;
    private ComboBox<String> receiptFormatComboBox;
    private CheckBox autoPrintReceiptsCheckBox;
    
    // User Management Fields
    private TableView<User> usersTable;
    private Button addUserButton;
    private Button editUserButton;
    private Button deleteUserButton;
    
    // Security Settings Fields
    private CheckBox requireLoginCheckBox;
    private ComboBox<String> sessionTimeoutComboBox;
    private CheckBox enableAuditLogCheckBox;
    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    
    public Node getView() {
        createSettingsInterface();
        return mainContainer;
    }
    
    private void createSettingsInterface() {
        mainContainer = new VBox();
        mainContainer.setPadding(new Insets(20));
        mainContainer.setSpacing(20);
        mainContainer.setStyle("-fx-background-color: #f5f5f7;");
        
        // Create header
        VBox header = createHeader();
        
        // Create main content with tabs
        settingsTabPane = createSettingsTabs();
        VBox.setVgrow(settingsTabPane, Priority.ALWAYS);
        
        // Create action buttons
        HBox actionButtons = createActionButtons();
        
        mainContainer.getChildren().addAll(header, settingsTabPane, actionButtons);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(5);
        
        Label titleLabel = new Label("System Settings");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        
        Label subtitleLabel = new Label("Configure your POS system settings and preferences");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
        
        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }
    
    private TabPane createSettingsTabs() {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8px;");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Create tabs
        Tab businessTab = new Tab("Business Info", createBusinessSettingsTab());
        Tab systemTab = new Tab("System", createSystemSettingsTab());
        Tab usersTab = new Tab("Users", createUserManagementTab());
        Tab securityTab = new Tab("Security", createSecuritySettingsTab());
        Tab backupTab = new Tab("Backup & Restore", createBackupTab());
        
        // Style tabs
        String tabStyle = "-fx-padding: 10px 20px; -fx-font-size: 14px; -fx-font-weight: bold;";
        businessTab.setStyle(tabStyle);
        systemTab.setStyle(tabStyle);
        usersTab.setStyle(tabStyle);
        securityTab.setStyle(tabStyle);
        backupTab.setStyle(tabStyle);
        
        tabPane.getTabs().addAll(businessTab, systemTab, usersTab, securityTab, backupTab);
        
        return tabPane;
    }
    
    private ScrollPane createBusinessSettingsTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        // Business Information Section
        VBox businessSection = createSection("Business Information", 
            "Configure your business details that appear on receipts and reports");
        
        GridPane businessGrid = new GridPane();
        businessGrid.setHgap(15);
        businessGrid.setVgap(15);
        businessGrid.setPadding(new Insets(20));
        
        // Business Name
        addFieldToGrid(businessGrid, 0, "Business Name:", 
            businessNameField = createStyledTextField("Enter your business name"));
        
        // Trading Name
        addFieldToGrid(businessGrid, 1, "Trading Name:", 
            tradingNameField = createStyledTextField("Enter trading name"));
        
        // Address
        addFieldToGrid(businessGrid, 2, "Business Address:", 
            addressField = createStyledTextField("Enter complete business address"));
        
        // Phone
        addFieldToGrid(businessGrid, 3, "Phone Number:", 
            phoneField = createStyledTextField("Enter phone number"));
        
        // Email
        addFieldToGrid(businessGrid, 4, "Email Address:", 
            emailField = createStyledTextField("Enter email address"));
        
        // TIN
        addFieldToGrid(businessGrid, 5, "TIN/Tax ID:", 
            tinField = createStyledTextField("Enter tax identification number"));
        
        // Business Description
        Label descLabel = new Label("Business Description:");
        descLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");
        businessGrid.add(descLabel, 0, 6);
        
        businessDescriptionArea = new TextArea();
        businessDescriptionArea.setPromptText("Brief description of your business");
        businessDescriptionArea.setPrefRowCount(3);
        businessDescriptionArea.setStyle(getTextFieldStyle());
        businessGrid.add(businessDescriptionArea, 1, 6);
        
        businessSection.getChildren().add(businessGrid);
        content.getChildren().add(businessSection);
        
        // Load existing data
        loadBusinessSettings();
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }
    
    private ScrollPane createSystemSettingsTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        // General System Settings
        VBox systemSection = createSection("General Settings", 
            "Configure general system behavior and preferences");
        
        GridPane systemGrid = new GridPane();
        systemGrid.setHgap(15);
        systemGrid.setVgap(15);
        systemGrid.setPadding(new Insets(20));
        
        // Currency Settings
        Label currencyLabel = new Label("Default Currency:");
        currencyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");
        systemGrid.add(currencyLabel, 0, 0);
        
        currencyComboBox = new ComboBox<>(FXCollections.observableArrayList(
            "PHP - Philippine Peso", "USD - US Dollar", "EUR - Euro", "GBP - British Pound"));
        currencyComboBox.setValue("PHP - Philippine Peso");
        currencyComboBox.setStyle(getComboBoxStyle());
        currencyComboBox.setMaxWidth(Double.MAX_VALUE);
        systemGrid.add(currencyComboBox, 1, 0);
        
        // Tax Rate Settings
        Label taxLabel = new Label("Default Tax Rate:");
        taxLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");
        systemGrid.add(taxLabel, 0, 1);
        
        taxRateComboBox = new ComboBox<>(FXCollections.observableArrayList(
            "0% - No Tax", "5% - Reduced Rate", "12% - Standard Rate", "15% - Higher Rate"));
        taxRateComboBox.setValue("12% - Standard Rate");
        taxRateComboBox.setStyle(getComboBoxStyle());
        taxRateComboBox.setMaxWidth(Double.MAX_VALUE);
        systemGrid.add(taxRateComboBox, 1, 1);
        
        systemSection.getChildren().add(systemGrid);
        
        // Feature Settings
        VBox featuresSection = createSection("Features", 
            "Enable or disable system features");
        
        VBox featuresBox = new VBox(15);
        featuresBox.setPadding(new Insets(20));
        
        enableDiscountsCheckBox = createStyledCheckBox("Enable Discounts", 
            "Allow discounts to be applied to products and transactions");
        enableCustomerAccountsCheckBox = createStyledCheckBox("Enable Customer Accounts", 
            "Allow customers to have accounts with stored information");
        enableInventoryTrackingCheckBox = createStyledCheckBox("Enable Inventory Tracking", 
            "Track product quantities and stock levels");
        
        featuresBox.getChildren().addAll(enableDiscountsCheckBox, enableCustomerAccountsCheckBox, 
                                        enableInventoryTrackingCheckBox);
        featuresSection.getChildren().add(featuresBox);
        
        // Receipt Settings
        VBox receiptSection = createSection("Receipt Settings", 
            "Configure receipt printing and formatting");
        
        GridPane receiptGrid = new GridPane();
        receiptGrid.setHgap(15);
        receiptGrid.setVgap(15);
        receiptGrid.setPadding(new Insets(20));
        
        Label formatLabel = new Label("Receipt Format:");
        formatLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");
        receiptGrid.add(formatLabel, 0, 0);
        
        receiptFormatComboBox = new ComboBox<>(FXCollections.observableArrayList(
            "Standard Format", "Compact Format", "Detailed Format"));
        receiptFormatComboBox.setValue("Standard Format");
        receiptFormatComboBox.setStyle(getComboBoxStyle());
        receiptFormatComboBox.setMaxWidth(Double.MAX_VALUE);
        receiptGrid.add(receiptFormatComboBox, 1, 0);
        
        autoPrintReceiptsCheckBox = createStyledCheckBox("Auto-print Receipts", 
            "Automatically print receipts after each transaction");
        receiptGrid.add(autoPrintReceiptsCheckBox, 0, 1, 2, 1);
        
        receiptSection.getChildren().add(receiptGrid);
        
        content.getChildren().addAll(systemSection, featuresSection, receiptSection);
        
        // Load existing settings
        loadSystemSettings();
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }
    
    private ScrollPane createUserManagementTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        // Users Section
        VBox usersSection = createSection("User Management", 
            "Manage system users and their permissions");
        
        // User management buttons
        HBox userButtons = new HBox(10);
        userButtons.setPadding(new Insets(20, 20, 10, 20));
        
        addUserButton = createActionButton("Add User", "#3949ab");
        editUserButton = createActionButton("Edit User", "#ff8f00");
        deleteUserButton = createActionButton("Delete User", "#c62828");
        
        addUserButton.setOnAction(e -> addUser());
        editUserButton.setOnAction(e -> editUser());
        deleteUserButton.setOnAction(e -> deleteUser());
        
        userButtons.getChildren().addAll(addUserButton, editUserButton, deleteUserButton);
        
        // Users table
        usersTable = new TableView<>();
        usersTable.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0;");
        
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));
        usernameCol.setPrefWidth(150);
        
        TableColumn<User, String> fullNameCol = new TableColumn<>("Full Name");
        fullNameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));
        fullNameCol.setPrefWidth(200);
        
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRole()));
        roleCol.setPrefWidth(120);
        
        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);
        
        TableColumn<User, String> lastLoginCol = new TableColumn<>("Last Login");
        lastLoginCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLastLogin()));
        lastLoginCol.setPrefWidth(150);
        
        usersTable.getColumns().addAll(usernameCol, fullNameCol, roleCol, statusCol, lastLoginCol);
        usersTable.setPrefHeight(300);
        
        usersSection.getChildren().addAll(userButtons, usersTable);
        content.getChildren().add(usersSection);
        
        // Load users data
        loadUsersData();
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }
    
    private ScrollPane createSecuritySettingsTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        // Security Settings
        VBox securitySection = createSection("Security Settings", 
            "Configure security and access control settings");
        
        GridPane securityGrid = new GridPane();
        securityGrid.setHgap(15);
        securityGrid.setVgap(15);
        securityGrid.setPadding(new Insets(20));
        
        requireLoginCheckBox = createStyledCheckBox("Require Login", 
            "Users must log in to access the system");
        securityGrid.add(requireLoginCheckBox, 0, 0, 2, 1);
        
        Label timeoutLabel = new Label("Session Timeout:");
        timeoutLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");
        securityGrid.add(timeoutLabel, 0, 1);
        
        sessionTimeoutComboBox = new ComboBox<>(FXCollections.observableArrayList(
            "15 minutes", "30 minutes", "1 hour", "2 hours", "Never"));
        sessionTimeoutComboBox.setValue("30 minutes");
        sessionTimeoutComboBox.setStyle(getComboBoxStyle());
        sessionTimeoutComboBox.setMaxWidth(Double.MAX_VALUE);
        securityGrid.add(sessionTimeoutComboBox, 1, 1);
        
        enableAuditLogCheckBox = createStyledCheckBox("Enable Audit Log", 
            "Log all user actions for security monitoring");
        securityGrid.add(enableAuditLogCheckBox, 0, 2, 2, 1);
        
        securitySection.getChildren().add(securityGrid);
        
        // Password Change Section
        VBox passwordSection = createSection("Change Password", 
            "Change your account password");
        
        GridPane passwordGrid = new GridPane();
        passwordGrid.setHgap(15);
        passwordGrid.setVgap(15);
        passwordGrid.setPadding(new Insets(20));
        
        addPasswordFieldToGrid(passwordGrid, 0, "Current Password:", 
            currentPasswordField = new PasswordField());
        addPasswordFieldToGrid(passwordGrid, 1, "New Password:", 
            newPasswordField = new PasswordField());
        addPasswordFieldToGrid(passwordGrid, 2, "Confirm Password:", 
            confirmPasswordField = new PasswordField());
        
        Button changePasswordButton = new Button("Change Password");
        changePasswordButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; " +
                                     "-fx-font-weight: bold; -fx-padding: 10px 20px; -fx-cursor: hand;");
        changePasswordButton.setOnAction(e -> changePassword());
        passwordGrid.add(changePasswordButton, 1, 3);
        
        passwordSection.getChildren().add(passwordGrid);
        
        content.getChildren().addAll(securitySection, passwordSection);
        
        // Load security settings
        loadSecuritySettings();
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }
    
    private ScrollPane createBackupTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        
        // Backup Section
        VBox backupSection = createSection("Database Backup", 
            "Create and manage database backups");
        
        VBox backupBox = new VBox(15);
        backupBox.setPadding(new Insets(20));
        
        HBox backupButtons = new HBox(10);
        Button createBackupButton = createActionButton("Create Backup", "#4CAF50");
        Button scheduleBackupButton = createActionButton("Schedule Backup", "#ff8f00");
        
        createBackupButton.setOnAction(e -> createBackup());
        scheduleBackupButton.setOnAction(e -> scheduleBackup());
        
        backupButtons.getChildren().addAll(createBackupButton, scheduleBackupButton);
        
        Label backupInfo = new Label("Last backup: Never");
        backupInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
        
        backupBox.getChildren().addAll(backupButtons, backupInfo);
        backupSection.getChildren().add(backupBox);
        
        // Restore Section
        VBox restoreSection = createSection("Database Restore", 
            "Restore database from a backup file");
        
        VBox restoreBox = new VBox(15);
        restoreBox.setPadding(new Insets(20));
        
        Button selectBackupButton = createActionButton("Select Backup File", "#0277bd");
        Button restoreButton = createActionButton("Restore Database", "#c62828");
        
        selectBackupButton.setOnAction(e -> selectBackupFile());
        restoreButton.setOnAction(e -> restoreDatabase());
        restoreButton.setDisable(true);
        
        Label warningLabel = new Label("⚠️ Warning: Restoring will overwrite all current data!");
        warningLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #c62828; -fx-font-weight: bold;");
        
        restoreBox.getChildren().addAll(selectBackupButton, restoreButton, warningLabel);
        restoreSection.getChildren().add(restoreBox);
        
        content.getChildren().addAll(backupSection, restoreSection);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }
    
    private VBox createSection(String title, String description) {
        VBox section = new VBox(10);
        section.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                        "-fx-border-radius: 8px; -fx-padding: 0;");
        
        // Section header
        VBox header = new VBox(5);
        header.setPadding(new Insets(20, 20, 10, 20));
        header.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8px 8px 0 0;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3949ab;");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #757575;");
        descLabel.setWrapText(true);
        
        header.getChildren().addAll(titleLabel, descLabel);
        section.getChildren().add(header);
        
        return section;
    }
    
    private TextField createStyledTextField(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setStyle(getTextFieldStyle());
        return field;
    }
    
    private CheckBox createStyledCheckBox(String text, String description) {
        VBox container = new VBox(5);
        
        CheckBox checkBox = new CheckBox(text);
        checkBox.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        descLabel.setWrapText(true);
        
        container.getChildren().addAll(checkBox, descLabel);
        return checkBox;
    }
    
    private Button createActionButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                       "-fx-font-weight: bold; -fx-padding: 8px 16px; -fx-cursor: hand; " +
                       "-fx-background-radius: 5px;");
        return button;
    }
    
    private void addFieldToGrid(GridPane grid, int row, String labelText, TextField field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        
        // Set column constraints
        if (grid.getColumnConstraints().isEmpty()) {
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setMinWidth(150);
            col1.setPrefWidth(150);
            
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setHgrow(Priority.ALWAYS);
            
            grid.getColumnConstraints().addAll(col1, col2);
        }
    }
    
    private void addPasswordFieldToGrid(GridPane grid, int row, String labelText, PasswordField field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");
        grid.add(label, 0, row);
        
        field.setStyle(getTextFieldStyle());
        grid.add(field, 1, row);
        
        // Set column constraints if not already set
        if (grid.getColumnConstraints().isEmpty()) {
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setMinWidth(150);
            col1.setPrefWidth(150);
            
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setHgrow(Priority.ALWAYS);
            
            grid.getColumnConstraints().addAll(col1, col2);
        }
    }
    
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button saveButton = new Button("Save Settings");
        saveButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; " +
                           "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12px 30px; " +
                           "-fx-cursor: hand; -fx-background-radius: 5px;");
        saveButton.setOnAction(e -> saveAllSettings());
        
        Button resetButton = new Button("Reset to Defaults");
        resetButton.setStyle("-fx-background-color: transparent; -fx-border-color: #757575; " +
                            "-fx-text-fill: #757575; -fx-font-size: 14px; -fx-padding: 12px 30px; " +
                            "-fx-cursor: hand; -fx-background-radius: 5px; -fx-border-radius: 5px;");
        resetButton.setOnAction(e -> resetToDefaults());
        
        buttonBox.getChildren().addAll(resetButton, saveButton);
        return buttonBox;
    }
    
    // Styling methods
    private String getTextFieldStyle() {
        return "-fx-background-color: white; -fx-border-color: #e0e0e0; " +
               "-fx-border-radius: 5px; -fx-padding: 8px; -fx-font-size: 13px;";
    }
    
    private String getComboBoxStyle() {
        return "-fx-background-color: white; -fx-border-color: #e0e0e0; " +
               "-fx-border-radius: 5px; -fx-padding: 5px;";
    }
    
    // Data loading methods
    private void loadBusinessSettings() {
        // Load from database or configuration
        businessNameField.setText("Sample Business Name");
        tradingNameField.setText("Sample Trading");
        // ... load other fields
    }
    
    private void loadSystemSettings() {
        enableDiscountsCheckBox.setSelected(true);
        enableCustomerAccountsCheckBox.setSelected(true);
        enableInventoryTrackingCheckBox.setSelected(false);
        autoPrintReceiptsCheckBox.setSelected(true);
    }
    
    private void loadSecuritySettings() {
        requireLoginCheckBox.setSelected(true);
        enableAuditLogCheckBox.setSelected(false);
    }
    
    private void loadUsersData() {
        // This would typically load from database
        // For now, we'll leave the table empty
    }
    
    // Action methods
    private void addUser() {
        showUserDialog(null, "Add New User");
    }
    
    private void editUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            showUserDialog(selectedUser, "Edit User");
        } else {
            showAlert("No Selection", "Please select a user to edit.");
        }
    }
    
    private void deleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete User");
            confirmAlert.setHeaderText("Are you sure you want to delete this user?");
            confirmAlert.setContentText("This action cannot be undone.");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Delete user logic here
                usersTable.getItems().remove(selectedUser);
                showAlert("Success", "User deleted successfully.");
            }
        } else {
            showAlert("No Selection", "Please select a user to delete.");
        }
    }
    
    private void showUserDialog(User user, String title) {
        // Implementation for user add/edit dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        // Add user form fields here
        dialog.showAndWait();
    }
    
    private void changePassword() {
        if (currentPasswordField.getText().isEmpty() || 
            newPasswordField.getText().isEmpty() || 
            confirmPasswordField.getText().isEmpty()) {
            showAlert("Validation Error", "Please fill in all password fields.");
            return;
        }
        
        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            showAlert("Password Mismatch", "New password and confirmation do not match.");
            return;
        }
        
        // Implement password change logic here
        showAlert("Success", "Password changed successfully.");
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
    
    private void createBackup() {
        // Implement backup creation
        showAlert("Backup Created", "Database backup created successfully.");
    }
    
    private void scheduleBackup() {
        // Implement backup scheduling
        showAlert("Backup Scheduled", "Automatic backup has been scheduled.");
    }
    
    private void selectBackupFile() {
        // Implement file selection for restore
        showAlert("File Selected", "Backup file selected for restore.");
    }
    
    private void restoreDatabase() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Restore Database");
        confirmAlert.setHeaderText("This will overwrite all current data!");
        confirmAlert.setContentText("Are you sure you want to proceed?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Implement restore logic
            showAlert("Restore Complete", "Database restored successfully.");
        }
    }
    
    private void saveAllSettings() {
        // Implement save logic for all settings
        showAlert("Settings Saved", "All settings have been saved successfully.");
    }
    
    private void resetToDefaults() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Reset Settings");
        confirmAlert.setHeaderText("Reset all settings to default values?");
        confirmAlert.setContentText("This action cannot be undone.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Reset all fields to defaults
            loadBusinessSettings();
            loadSystemSettings();
            loadSecuritySettings();
            showAlert("Settings Reset", "All settings have been reset to defaults.");
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Inner class for User data
    public static class User {
        private String username;
        private String fullName;
        private String role;
        private String status;
        private String lastLogin;
        
        public User(String username, String fullName, String role, String status, String lastLogin) {
            this.username = username;
            this.fullName = fullName;
            this.role = role;
            this.status = status;
            this.lastLogin = lastLogin;
        }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getLastLogin() { return lastLogin; }
        public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
    }
}