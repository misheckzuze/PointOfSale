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
import com.pointofsale.helper.Helper;
import com.pointofsale.model.User;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;

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
        // Users table
        usersTable = new TableView<>();
        usersTable.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0;");

        // Username Column
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserName()));
        usernameCol.setPrefWidth(150);

       // Full Name Column (from getFullName helper method)
       TableColumn<User, String> fullNameCol = new TableColumn<>("Full Name");
       fullNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
       fullNameCol.setPrefWidth(200);

       // Role Column
       TableColumn<User, String> roleCol = new TableColumn<>("Role");
       roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
       roleCol.setPrefWidth(120);

       // Gender Column
       TableColumn<User, String> genderCol = new TableColumn<>("Gender");
       genderCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGender()));
       genderCol.setPrefWidth(100);

       // Phone Column
       TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhoneNumber()));
       phoneCol.setPrefWidth(130);

        // Email Column
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
         emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmailAddress()));
        emailCol.setPrefWidth(180);

        // Address Column
        TableColumn<User, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAddress()));
        addressCol.setPrefWidth(180);

        // Add all columns to the table
        usersTable.getColumns().addAll(
         usernameCol, fullNameCol, roleCol, genderCol, phoneCol, emailCol, addressCol
        );

        // Set preferred height
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
    try {
        // Clear existing data
        usersTable.getItems().clear();

        // Fetch users from database
        List<User> users = Helper.getAllUsers();

        // Add users to the table
        usersTable.getItems().addAll(users);

    } catch (Exception e) {
        showErrorAlert("Load Error", "Failed to load users: " + e.getMessage());
    }
}

    
    // Add User Dialog Implementation
private void addUser() {
    Dialog<User> dialog = new Dialog<>();
    dialog.setTitle("Add New User");
    dialog.setHeaderText("Create a new user account");
    
    // Set dialog styling
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #1a237e; -fx-border-width: 2px;");
    
    // Create form fields
    GridPane grid = new GridPane();
    grid.setHgap(15);
    grid.setVgap(15);
    grid.setPadding(new Insets(25, 25, 25, 25));
    
    // Form fields
    TextField firstNameField = new TextField();
    firstNameField.setPromptText("Enter first name");
    firstNameField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    TextField lastNameField = new TextField();
    lastNameField.setPromptText("Enter last name");
    lastNameField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    TextField usernameField = new TextField();
    usernameField.setPromptText("Enter username");
    usernameField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    ComboBox<String> genderComboBox = new ComboBox<>();
    genderComboBox.getItems().addAll("MALE", "FEMALE");
    genderComboBox.setPromptText("Select gender");
    genderComboBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    TextField phoneField = new TextField();
    phoneField.setPromptText("Enter phone number");
    phoneField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    TextField emailField = new TextField();
    emailField.setPromptText("Enter email address");
    emailField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    TextArea addressArea = new TextArea();
    addressArea.setPromptText("Enter address");
    addressArea.setPrefRowCount(3);
    addressArea.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    ComboBox<String> roleComboBox = new ComboBox<>();
    roleComboBox.getItems().addAll("ADMIN", "CASHIER");
    roleComboBox.setPromptText("Select role");
    roleComboBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    PasswordField passwordField = new PasswordField();
    passwordField.setPromptText("Enter password");
    passwordField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    PasswordField confirmPasswordField = new PasswordField();
    confirmPasswordField.setPromptText("Confirm password");
    confirmPasswordField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    // Create labels with corporate styling
    Label[] labels = {
        new Label("First Name:*"),
        new Label("Last Name:*"),
        new Label("Username:*"),
        new Label("Gender:"),
        new Label("Phone Number:"),
        new Label("Email Address:"),
        new Label("Address:"),
        new Label("Role:*"),
        new Label("Password:*"),
        new Label("Confirm Password:*")
    };
    
    for (Label label : labels) {
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a237e; -fx-font-size: 14px;");
    }
    
    // Add components to grid
    int row = 0;
    grid.add(labels[0], 0, row);
    grid.add(firstNameField, 1, row++);
    grid.add(labels[1], 0, row);
    grid.add(lastNameField, 1, row++);
    grid.add(labels[2], 0, row);
    grid.add(usernameField, 1, row++);
    grid.add(labels[3], 0, row);
    grid.add(genderComboBox, 1, row++);
    grid.add(labels[4], 0, row);
    grid.add(phoneField, 1, row++);
    grid.add(labels[5], 0, row);
    grid.add(emailField, 1, row++);
    grid.add(labels[6], 0, row);
    grid.add(addressArea, 1, row++);
    grid.add(labels[7], 0, row);
    grid.add(roleComboBox, 1, row++);
    grid.add(labels[8], 0, row);
    grid.add(passwordField, 1, row++);
    grid.add(labels[9], 0, row);
    grid.add(confirmPasswordField, 1, row++);
    
    // Set column constraints
    ColumnConstraints col1 = new ColumnConstraints();
    col1.setMinWidth(140);
    ColumnConstraints col2 = new ColumnConstraints();
    col2.setMinWidth(280);
    grid.getColumnConstraints().addAll(col1, col2);
    
    dialogPane.setContent(grid);
    
    // Add buttons
    ButtonType addButtonType = new ButtonType("Add User", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(addButtonType, cancelButtonType);
    
    // Style buttons
    Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
    Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
    
    addButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-font-weight: bold; " +
                     "-fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
    cancelButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
    
    // Add validation
    addButton.setDisable(true);
    
    // Enable/disable add button based on form validation
    Runnable validateForm = () -> {
        boolean isValid = !firstNameField.getText().trim().isEmpty() &&
                         !lastNameField.getText().trim().isEmpty() &&
                         !usernameField.getText().trim().isEmpty() &&
                         roleComboBox.getValue() != null &&
                         !passwordField.getText().isEmpty() &&
                         !confirmPasswordField.getText().isEmpty() &&
                         passwordField.getText().equals(confirmPasswordField.getText());
        addButton.setDisable(!isValid);
        
        // Update password field border color based on match
        if (!passwordField.getText().isEmpty() && !confirmPasswordField.getText().isEmpty()) {
            if (passwordField.getText().equals(confirmPasswordField.getText())) {
                confirmPasswordField.setStyle("-fx-border-color: #4caf50; -fx-border-radius: 5px; -fx-padding: 10px;");
            } else {
                confirmPasswordField.setStyle("-fx-border-color: #f44336; -fx-border-radius: 5px; -fx-padding: 10px;");
            }
        } else {
            confirmPasswordField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
        }
    };
    
    // Add listeners for validation
    firstNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
    lastNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
    usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
    roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
    passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
    confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
    
    // Set result converter
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == addButtonType) {
            User user = new User();
            user.setFirstName(firstNameField.getText().trim());
            user.setLastName(lastNameField.getText().trim());
            user.setUserName(usernameField.getText().trim());
            user.setGender(genderComboBox.getValue());
            user.setPhoneNumber(phoneField.getText().trim());
            user.setEmailAddress(emailField.getText().trim());
            user.setAddress(addressArea.getText().trim());
            user.setRole(roleComboBox.getValue());
            user.setPassword(passwordField.getText());
            return user;
        }
        return null;
    });
    
    // Show dialog and handle result
    Optional<User> result = dialog.showAndWait();
    result.ifPresent(user -> {
        try {
            // Add user to database
            if (Helper.addUser(user)) {
                // Refresh table
                loadUsersData();
                
                // Show success message
                showSuccessAlert("User Added", "User '" + user.getUserName() + "' has been successfully added.");
            } else {
                showErrorAlert("Error Adding User", "Failed to add user. Username or email may already exist.");
            }
            
        } catch (Exception e) {
            showErrorAlert("Error Adding User", "Failed to add user: " + e.getMessage());
        }
    });
}

// Edit User Dialog Implementation
private void editUser() {
    User selectedUser = usersTable.getSelectionModel().getSelectedItem();
    
    if (selectedUser == null) {
        showWarningAlert("No Selection", "Please select a user to edit.");
        return;
    }
    
    Dialog<User> dialog = new Dialog<>();
    dialog.setTitle("Edit User");
    dialog.setHeaderText("Modify user information for: " + selectedUser.getUserName());
    
    // Set dialog styling
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #1a237e; -fx-border-width: 2px;");
    
    // Create form fields
    GridPane grid = new GridPane();
    grid.setHgap(15);
    grid.setVgap(15);
    grid.setPadding(new Insets(25, 25, 25, 25));
    
    // Form fields - pre-populated with existing data
    TextField firstNameField = new TextField(selectedUser.getFirstName());
    firstNameField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    TextField lastNameField = new TextField(selectedUser.getLastName());
    lastNameField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    TextField usernameField = new TextField(selectedUser.getUserName());
    usernameField.setDisable(true); // Username shouldn't be editable
    usernameField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px; -fx-opacity: 0.7;");
    
    ComboBox<String> genderComboBox = new ComboBox<>();
    genderComboBox.getItems().addAll("MALE", "FEMALE");
    genderComboBox.setValue(selectedUser.getGender());
    genderComboBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    TextField phoneField = new TextField(selectedUser.getPhoneNumber() != null ? selectedUser.getPhoneNumber() : "");
    phoneField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    TextField emailField = new TextField(selectedUser.getEmailAddress() != null ? selectedUser.getEmailAddress() : "");
    emailField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    TextArea addressArea = new TextArea(selectedUser.getAddress() != null ? selectedUser.getAddress() : "");
    addressArea.setPrefRowCount(3);
    addressArea.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    ComboBox<String> roleComboBox = new ComboBox<>();
    roleComboBox.getItems().addAll("ADMIN", "CASHIER");
    roleComboBox.setValue(selectedUser.getRole());
    roleComboBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    
    CheckBox resetPasswordCheckBox = new CheckBox("Reset Password");
    resetPasswordCheckBox.setStyle("-fx-text-fill: #1a237e; -fx-font-weight: bold;");
    
    PasswordField newPasswordField = new PasswordField();
    newPasswordField.setPromptText("New password");
    newPasswordField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    newPasswordField.setDisable(true);
    
    PasswordField confirmNewPasswordField = new PasswordField();
    confirmNewPasswordField.setPromptText("Confirm new password");
    confirmNewPasswordField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
    confirmNewPasswordField.setDisable(true);
    
    // Enable/disable password fields based on checkbox
    resetPasswordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
        newPasswordField.setDisable(!newVal);
        confirmNewPasswordField.setDisable(!newVal);
        if (!newVal) {
            newPasswordField.clear();
            confirmNewPasswordField.clear();
        }
    });
    
    // Create labels
    Label[] labels = {
        new Label("First Name:*"),
        new Label("Last Name:*"),
        new Label("Username:"),
        new Label("Gender:"),
        new Label("Phone Number:"),
        new Label("Email Address:"),
        new Label("Address:"),
        new Label("Role:*"),
        new Label("Password:")
    };
    
    for (Label label : labels) {
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a237e; -fx-font-size: 14px;");
    }
    
    // Add components to grid
    int row = 0;
    grid.add(labels[0], 0, row);
    grid.add(firstNameField, 1, row++);
    grid.add(labels[1], 0, row);
    grid.add(lastNameField, 1, row++);
    grid.add(labels[2], 0, row);
    grid.add(usernameField, 1, row++);
    grid.add(labels[3], 0, row);
    grid.add(genderComboBox, 1, row++);
    grid.add(labels[4], 0, row);
    grid.add(phoneField, 1, row++);
    grid.add(labels[5], 0, row);
    grid.add(emailField, 1, row++);
    grid.add(labels[6], 0, row);
    grid.add(addressArea, 1, row++);
    grid.add(labels[7], 0, row);
    grid.add(roleComboBox, 1, row++);
    grid.add(labels[8], 0, row);
    grid.add(resetPasswordCheckBox, 1, row++);
    grid.add(new Label("New Password:"), 0, row);
    grid.add(newPasswordField, 1, row++);
    grid.add(new Label("Confirm Password:"), 0, row);
    grid.add(confirmNewPasswordField, 1, row++);
    
    // Set column constraints
    ColumnConstraints col1 = new ColumnConstraints();
    col1.setMinWidth(140);
    ColumnConstraints col2 = new ColumnConstraints();
    col2.setMinWidth(280);
    grid.getColumnConstraints().addAll(col1, col2);
    
    dialogPane.setContent(grid);
    
    // Add buttons
    ButtonType updateButtonType = new ButtonType("Update User", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, cancelButtonType);
    
    // Style buttons
    Button updateButton = (Button) dialog.getDialogPane().lookupButton(updateButtonType);
    Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
    
    updateButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
    cancelButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
    
    // Add validation for password fields
    Runnable validatePasswords = () -> {
        if (resetPasswordCheckBox.isSelected() && 
            !newPasswordField.getText().isEmpty() && 
            !confirmNewPasswordField.getText().isEmpty()) {
            if (newPasswordField.getText().equals(confirmNewPasswordField.getText())) {
                confirmNewPasswordField.setStyle("-fx-border-color: #4caf50; -fx-border-radius: 5px; -fx-padding: 10px;");
            } else {
                confirmNewPasswordField.setStyle("-fx-border-color: #f44336; -fx-border-radius: 5px; -fx-padding: 10px;");
            }
        } else {
            confirmNewPasswordField.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-padding: 10px;");
        }
    };
    
    newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validatePasswords.run());
    confirmNewPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validatePasswords.run());
    
    // Set result converter
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == updateButtonType) {
            // Validate password match if reset is selected
            if (resetPasswordCheckBox.isSelected() && 
                !newPasswordField.getText().equals(confirmNewPasswordField.getText())) {
                showErrorAlert("Password Mismatch", "New passwords do not match!");
                return null;
            }
            
            User updatedUser = new User();
            updatedUser.setUserID(selectedUser.getUserID());
            updatedUser.setFirstName(firstNameField.getText().trim());
            updatedUser.setLastName(lastNameField.getText().trim());
            updatedUser.setUserName(selectedUser.getUserName()); // Keep original username
            updatedUser.setGender(genderComboBox.getValue());
            updatedUser.setPhoneNumber(phoneField.getText().trim());
            updatedUser.setEmailAddress(emailField.getText().trim());
            updatedUser.setAddress(addressArea.getText().trim());
            updatedUser.setRole(roleComboBox.getValue());
            
            if (resetPasswordCheckBox.isSelected() && !newPasswordField.getText().isEmpty()) {
                updatedUser.setPassword(newPasswordField.getText());
            } else {
                updatedUser.setPassword(selectedUser.getPassword()); // Keep existing password
            }
            
            return updatedUser;
        }
        return null;
    });
    
    // Show dialog and handle result
    Optional<User> result = dialog.showAndWait();
    result.ifPresent(user -> {
        try {
            // Update user in database
            if (Helper.updateUser(user)) {
                // Refresh table
                loadUsersData();
                
                // Show success message
                showSuccessAlert("User Updated", "User '" + user.getUserName() + "' has been successfully updated.");
            } else {
                showErrorAlert("Error Updating User", "Failed to update user.");
            }
            
        } catch (Exception e) {
            showErrorAlert("Error Updating User", "Failed to update user: " + e.getMessage());
        }
    });
}

// Delete User Dialog Implementation
private void deleteUser() {
    User selectedUser = usersTable.getSelectionModel().getSelectedItem();
    
    if (selectedUser == null) {
        showWarningAlert("No Selection", "Please select a user to delete.");
        return;
    }
    
    // Create confirmation dialog
    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
    confirmDialog.setTitle("Confirm User Deletion");
    confirmDialog.setHeaderText("Delete User: " + selectedUser.getUserName());
    confirmDialog.setContentText("Are you sure you want to delete this user?\n\n" +
                                "Username: " + selectedUser.getUserName() + "\n" +
                                "Full Name: " + selectedUser.getFirstName() + " " + selectedUser.getLastName() + "\n" +
                                "Role: " + selectedUser.getRole() + "\n\n" +
                                "This action cannot be undone!");
    
    // Style the dialog
    DialogPane dialogPane = confirmDialog.getDialogPane();
    dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #1a237e; -fx-border-width: 2px;");
    
    // Style the header
    Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
    if (headerLabel != null) {
        headerLabel.setStyle("-fx-text-fill: #1a237e; -fx-font-weight: bold; -fx-font-size: 16px;");
    }
    
    // Get and style buttons
    ButtonType deleteButtonType = new ButtonType("Delete User", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    
    confirmDialog.getButtonTypes().setAll(deleteButtonType, cancelButtonType);
    
    Button deleteButton = (Button) confirmDialog.getDialogPane().lookupButton(deleteButtonType);
    Button cancelButton = (Button) confirmDialog.getDialogPane().lookupButton(cancelButtonType);
    
    deleteButton.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
    cancelButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
    
    // Show confirmation dialog
    Optional<ButtonType> result = confirmDialog.showAndWait();
    
    if (result.isPresent() && result.get() == deleteButtonType) {
        try {
            // Delete user from database
            if (Helper.deleteUser(selectedUser.getUserID())) {
                // Refresh table
                loadUsersData();
                
                // Show success message
                showSuccessAlert("User Deleted", "User '" + selectedUser.getUserName() + "' has been successfully deleted.");
            } else {
                showErrorAlert("Error Deleting User", "Failed to delete user.");
            }
            
        } catch (Exception e) {
            showErrorAlert("Error Deleting User", "Failed to delete user: " + e.getMessage());
        }
    }
}

// Helper methods for showing alerts
private void showSuccessAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    
    DialogPane dialogPane = alert.getDialogPane();
    dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #1a237e; -fx-border-width: 2px;");
    
    Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
    okButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-font-weight: bold; " +
                     "-fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
    
    alert.showAndWait();
}

private void showErrorAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    
    DialogPane dialogPane = alert.getDialogPane();
    dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #c62828; -fx-border-width: 2px;");
    
    Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
    okButton.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; " +
                     "-fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
    
    alert.showAndWait();
}

private void showWarningAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    
    DialogPane dialogPane = alert.getDialogPane();
    dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #ff8f00; -fx-border-width: 2px;");
    
    Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
    okButton.setStyle("-fx-background-color: #ff8f00; -fx-text-fill: white; -fx-font-weight: bold; " +
                     "-fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
    
    alert.showAndWait();
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
    
}