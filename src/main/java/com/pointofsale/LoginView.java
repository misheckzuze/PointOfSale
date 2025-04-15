package com.pointofsale;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.InputStream;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import com.pointofsale.helper.Helper;
import javafx.application.Application;



/**
 * A modern, professional login view for the Point of Sale system.
 * This view appears after terminal activation is confirmed.
 */
public class LoginView extends Application {
    
    private Stage stage;
    private Scene scene;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label statusMessage;
    
    /**
     * Constructor for the login view
     * 
     * @param stage The primary stage
     */
     
     @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        createLoginScene();
        stage.show();
    }
    /**
     * Creates the login scene with all UI components
     */
    private void createLoginScene() {
        // Root container with two main sections (left sidebar and right content)
        HBox root = new HBox();
        root.setStyle("-fx-background-color: white;");
        
        // Create the sidebar and main content
        VBox sidebar = createSidebar();
        VBox mainContent = createMainContent();
        
        // Add both sides to the root
        root.getChildren().addAll(sidebar, mainContent);
        
        // Create scene
        scene = new Scene(root, 900, 600);
        
        // Set the scene to the stage
        stage.setScene(scene);
        stage.setTitle("POS System Login");
        stage.setResizable(false);
        
        // Focus on username field when shown
        Platform.runLater(() -> usernameField.requestFocus());
    }
    
    /**
     * Creates the visually appealing sidebar with branding and decorative elements
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(300);
        sidebar.setAlignment(Pos.CENTER);
        sidebar.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a237e, #3949ab);");
        
        // Create a placeholder logo instead of trying to load an image
    Rectangle placeholderLogo = new Rectangle(120, 120);
    placeholderLogo.setFill(Color.WHITE);
    placeholderLogo.setOpacity(0.9);
    placeholderLogo.setArcWidth(20);
    placeholderLogo.setArcHeight(20);
    
    Label logoText = new Label("POS\nSYSTEM");
    logoText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #3949ab;");
    logoText.setAlignment(Pos.CENTER);
    
    StackPane logoStack = new StackPane(placeholderLogo, logoText);
    sidebar.getChildren().add(logoStack);        
        // Tagline
        Label taglineLabel = new Label("Streamlining Your Business");
        taglineLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white; -fx-opacity: 0.9;");
        taglineLabel.setPadding(new Insets(15, 0, 40, 0));
        
        // Features list
        VBox featuresList = new VBox(15);
        featuresList.setAlignment(Pos.CENTER_LEFT);
        featuresList.setPadding(new Insets(10, 20, 10, 40));
        
        String featureStyle = "-fx-font-size: 14px; -fx-text-fill: white; -fx-opacity: 0.85;";
        
        Label feature1 = new Label("• Fast & Secure Transactions");
        feature1.setStyle(featureStyle);
        
        Label feature2 = new Label("• Real-time Inventory Management");
        feature2.setStyle(featureStyle);
        
        Label feature3 = new Label("• Comprehensive Reports");
        feature3.setStyle(featureStyle);
        
        Label feature4 = new Label("• Multi-device Synchronization");
        feature4.setStyle(featureStyle);
        
        featuresList.getChildren().addAll(feature1, feature2, feature3, feature4);
        
        // Footer with version info
        Label versionLabel = new Label("Version 2.5.0");
        versionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-opacity: 0.6;");
        VBox.setMargin(versionLabel, new Insets(40, 0, 20, 0));
        
        // Add all components to sidebar
        sidebar.getChildren().addAll(taglineLabel, featuresList, versionLabel);
        
        // Add decorative dots pattern to bottom of sidebar
        GridPane dotsPattern = createDecorativePattern();
        dotsPattern.setOpacity(0.15);
        sidebar.getChildren().add(dotsPattern);
        
        return sidebar;
    }
    
    /**
     * Creates a decorative dots pattern as a background element
     */
    private GridPane createDecorativePattern() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 5; j++) {
                Rectangle dot = new Rectangle(4, 4);
                dot.setFill(Color.WHITE);
                dot.setOpacity((i + j) % 2 == 0 ? 0.8 : 0.4);
                grid.add(dot, j, i);
            }
        }
        
        return grid;
    }
    
    /**
     * Creates the main content area with login form
     */
    private VBox createMainContent() {
        VBox mainContent = new VBox();
        mainContent.setPrefWidth(600);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(0, 60, 0, 60));
        mainContent.setSpacing(15);
        
        // Welcome header
        Label welcomeLabel = new Label("Welcome Back");
        welcomeLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        
        // Instruction text
        Label instructionLabel = new Label("Please enter your credentials to continue");
        instructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
        
        // Add some space
        Region spacer = new Region();
        spacer.setPrefHeight(40);
        
        // Login form container
        VBox loginForm = new VBox(20);
        loginForm.setMaxWidth(400);
        loginForm.setAlignment(Pos.CENTER);
        
        // Username field
        VBox usernameBox = new VBox(8);
        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555555;");
        
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(40);
        usernameField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                              "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: #f8f8f8;");
        
        usernameBox.getChildren().addAll(usernameLabel, usernameField);
        
        // Password field
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555555;");
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                              "-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-background-color: #f8f8f8;");
        
        passwordBox.getChildren().addAll(passwordLabel, passwordField);
        
        // Remember me checkbox and forgot password link
        HBox optionsBox = new HBox();
        optionsBox.setAlignment(Pos.CENTER_LEFT);
        
        CheckBox rememberMeCheckbox = new CheckBox("Remember me");
        rememberMeCheckbox.setStyle("-fx-font-size: 13px; -fx-text-fill: #757575;");
        
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        
        Hyperlink forgotPasswordLink = new Hyperlink("Forgot Password?");
        forgotPasswordLink.setStyle("-fx-font-size: 13px; -fx-text-fill: #3949ab;");
        forgotPasswordLink.setOnAction(e -> showForgotPasswordDialog());
        
        optionsBox.getChildren().addAll(rememberMeCheckbox, hSpacer, forgotPasswordLink);
        
        // Login button
        loginButton = new Button("LOG IN");
        loginButton.setPrefHeight(45);
        loginButton.setPrefWidth(400);
        loginButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                           "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand; " +
                           "-fx-background-radius: 5px;");
        
        // Add hover effect
        loginButton.setOnMouseEntered(e -> 
            loginButton.setStyle("-fx-background-color: #3949ab; -fx-text-fill: white; " +
                               "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand; " +
                               "-fx-background-radius: 5px;")
        );
        
        loginButton.setOnMouseExited(e -> 
            loginButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                               "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand; " +
                               "-fx-background-radius: 5px;")
        );
        
        // Drop shadow effect for button
        DropShadow shadow = new DropShadow();
        shadow.setRadius(5.0);
        shadow.setOffsetY(3.0);
        shadow.setColor(Color.color(0.0, 0.0, 0.0, 0.3));
        loginButton.setEffect(shadow);
        
        // Status message for login results
        statusMessage = new Label();
        statusMessage.setStyle("-fx-font-size: 13px; -fx-text-fill: #d32f2f;");
        statusMessage.setVisible(false);
        statusMessage.setManaged(false);
        
        loginButton.setOnAction(e -> {
          String username = usernameField.getText().trim();
          String password = passwordField.getText().trim();
    
          // Check if username and password are not empty
          if (username.isEmpty() || password.isEmpty()) {
          Alert alert = new Alert(AlertType.ERROR);
          alert.setTitle("Login Error");
          alert.setHeaderText(null);
          alert.setContentText("Username or password cannot be empty.");
          alert.showAndWait();
          return;
        }

        // Call the isValidUser function to check if the user is valid
        boolean validUser = Helper.isValidUser(username, password);

        if (!validUser) {
          // Incorrect credentials
          Alert alert = new Alert(AlertType.ERROR);
          alert.setTitle("Login Error");
          alert.setHeaderText(null);
          alert.setContentText("Invalid username or password.");
          alert.showAndWait();
        } else {
          // Successful login
          Alert alert = new Alert(AlertType.INFORMATION);
          alert.setTitle("Login Successful");
          alert.setHeaderText(null);
          alert.setContentText("Login successful!");
          alert.showAndWait();
        
        // Simulate successful login with fade transition
          FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), loginButton);
          fadeOut.setFromValue(1.0);
          fadeOut.setToValue(0.7);
          fadeOut.setCycleCount(1);
        
          fadeOut.setOnFinished(event -> {
            // Navigate to main application screen
            System.out.println("Login successful, navigating to main screen...");
            // You would replace this with actual logic to navigate to the main application
             // Proceed to SalesDashboard
            POSDashboard dashboard = new POSDashboard();
            try {
                dashboard.start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        fadeOut.play();
        }
    });
        
        
        // Contact support text
        HBox supportBox = new HBox();
        supportBox.setAlignment(Pos.CENTER);
        supportBox.setPadding(new Insets(30, 0, 0, 0));
        
        Label supportLabel = new Label("Having trouble logging in? ");
        supportLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #757575;");
        
        Hyperlink contactLink = new Hyperlink("Contact Support");
        contactLink.setStyle("-fx-font-size: 13px; -fx-text-fill: #3949ab;");
        contactLink.setOnAction(e -> showContactSupportDialog());
        
        supportBox.getChildren().addAll(supportLabel, contactLink);
        
        // Add all elements to the form
        loginForm.getChildren().addAll(usernameBox, passwordBox, optionsBox, loginButton, statusMessage);
        
        // Add components to main content
        mainContent.getChildren().addAll(welcomeLabel, instructionLabel, spacer, loginForm, supportBox);
        
        return mainContent;
    }
    
    
    /**
     * Shows the forgot password dialog
     */
    private void showForgotPasswordDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reset Password");
        alert.setHeaderText("Password Reset");
        alert.setContentText("The password reset functionality will be implemented soon.");
        alert.showAndWait();
    }
    
    /**
     * Shows the contact support dialog
     */
    private void showContactSupportDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contact Support");
        alert.setHeaderText("Support Information");
        alert.setContentText("For assistance, please contact our support team at: support@possystem.com or call 1-800-POS-HELP");
        alert.showAndWait();
    }
    
    /**
     * Shows the login view
     */
    public void show() {
        stage.show();
    }
}