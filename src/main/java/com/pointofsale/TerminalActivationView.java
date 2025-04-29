package com.pointofsale;

import com.pointofsale.helper.ApiClient;
import javafx.application.Application;
import javafx.application.Platform;
import com.pointofsale.helper.Helper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class TerminalActivationView extends Application {

    private TextField activationCodeField;
    private Button activateTerminalBtn;
    private CheckBox termsAgreeCheckbox;
    private Label statusValueLabel;

    // Brand colors from LoginView for consistency
    private final String PRIMARY_COLOR = "#1a237e";     // Deep indigo
    private final String SECONDARY_COLOR = "#3949ab";   // Medium indigo
    private final String TEXT_DARK = "#555555";         // Dark gray for labels
    private final String TEXT_MEDIUM = "#757575";       // Medium gray for instructions
    private final String FIELD_BG = "#f8f8f8";          // Light background for fields
    private final String FIELD_BORDER = "#e0e0e0";      // Light border for fields

    // No-argument constructor (JavaFX uses this by default)
    public TerminalActivationView() {
        System.out.println("TerminalActivation constructor called");
    }

    // Add initialization method
    @Override
    public void init() throws Exception {
        System.out.println("TerminalActivation init called");
        super.init();
    }

    @Override
    public void start(Stage stage) {
        try {
            System.out.println("TerminalActivation start method called");
            stage.setTitle("POS System - Terminal Activation");

            BorderPane mainLayout = new BorderPane();
            
            // Create sidebar for branding consistency with LoginView
            VBox sidebar = createSidebar();
            VBox contentContainer = createMainContent();
            
            mainLayout.setLeft(sidebar);
            mainLayout.setCenter(contentContainer);

            Scene scene = new Scene(mainLayout, 900, 600);
            
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Log error and show any exception
        }
    }
    
    /**
     * Creates the sidebar with branding and features matching LoginView
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(300);
        sidebar.setAlignment(Pos.CENTER);
        sidebar.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " + 
                        PRIMARY_COLOR + ", " + SECONDARY_COLOR + ");");
        
        // Logo placeholder (matches LoginView)
        StackPane logoStack = new StackPane();
        javafx.scene.shape.Rectangle placeholderLogo = new javafx.scene.shape.Rectangle(120, 120);
        placeholderLogo.setFill(Color.WHITE);
        placeholderLogo.setOpacity(0.9);
        placeholderLogo.setArcWidth(20);
        placeholderLogo.setArcHeight(20);
        
        Label logoText = new Label("POS\nSYSTEM");
        logoText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + SECONDARY_COLOR + ";");
        logoText.setAlignment(Pos.CENTER);
        
        logoStack.getChildren().addAll(placeholderLogo, logoText);
        
        // Tagline
        Label taglineLabel = new Label("Streamlining Your Business");
        taglineLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: white; -fx-opacity: 0.9;");
        taglineLabel.setPadding(new Insets(15, 0, 40, 0));
        
        // Steps for activation
        VBox stepsList = new VBox(15);
        stepsList.setAlignment(Pos.CENTER_LEFT);
        stepsList.setPadding(new Insets(10, 20, 10, 40));
        
        String stepStyle = "-fx-font-size: 14px; -fx-text-fill: white; -fx-opacity: 0.85;";
        
        Label step1 = new Label("• Enter activation code");
        step1.setStyle(stepStyle);
        
        Label step2 = new Label("• Accept terms and conditions");
        step2.setStyle(stepStyle);
        
        Label step3 = new Label("• Activate your terminal");
        step3.setStyle(stepStyle);
        
        Label step4 = new Label("• Begin using your POS system");
        step4.setStyle(stepStyle);
        
        stepsList.getChildren().addAll(step1, step2, step3, step4);
        
        // Footer with version info
        Label versionLabel = new Label("Version 2.5.0");
        versionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-opacity: 0.6;");
        VBox.setMargin(versionLabel, new Insets(40, 0, 20, 0));
        
        // Add all components to sidebar
        sidebar.getChildren().addAll(logoStack, taglineLabel, stepsList, versionLabel);
        
        // Add decorative dots pattern to bottom of sidebar (same as LoginView)
        GridPane dotsPattern = createDecorativePattern();
        dotsPattern.setOpacity(0.15);
        sidebar.getChildren().add(dotsPattern);
        
        return sidebar;
    }
    
    /**
     * Creates a decorative dots pattern as a background element (same as LoginView)
     */
    private GridPane createDecorativePattern() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 5; j++) {
                javafx.scene.shape.Rectangle dot = new javafx.scene.shape.Rectangle(4, 4);
                dot.setFill(Color.WHITE);
                dot.setOpacity((i + j) % 2 == 0 ? 0.8 : 0.4);
                grid.add(dot, j, i);
            }
        }
        
        return grid;
    }

    private VBox createMainContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(60));
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle("-fx-background-color: white;");

        // Welcome header
        Label titleLabel = new Label("Terminal Activation");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + PRIMARY_COLOR + ";");

        // Instruction text
        Label instructionLabel = new Label("Please enter your activation code to continue");
        instructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_MEDIUM + ";");

        // Add some space
        Region spacer = new Region();
        spacer.setPrefHeight(40);

        // Activation form container
        VBox activationForm = new VBox(20);
        activationForm.setMaxWidth(400);
        activationForm.setAlignment(Pos.CENTER);

        // Activation code field with label
        VBox codeBox = new VBox(8);
        Label activationCodeLabel = new Label("Activation Code");
        activationCodeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_DARK + ";");
        
        activationCodeField = new TextField();
        activationCodeField.setPromptText("XXXX-XXXX-XXXX-XXXX");
        activationCodeField.setPrefHeight(40);
        activationCodeField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                              "-fx-border-color: " + FIELD_BORDER + "; -fx-border-width: 1px; -fx-background-color: " + FIELD_BG + ";");
        
        codeBox.getChildren().addAll(activationCodeLabel, activationCodeField);

        // Terms and conditions checkbox
        HBox termsBox = new HBox(10);
        termsBox.setAlignment(Pos.CENTER_LEFT);
        termsAgreeCheckbox = new CheckBox("I agree to the Terms and Conditions");
        termsAgreeCheckbox.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_MEDIUM + ";");
        
        Hyperlink viewTermsLink = new Hyperlink("View Terms & Conditions");
        viewTermsLink.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SECONDARY_COLOR + ";");
        
        termsBox.getChildren().addAll(termsAgreeCheckbox, viewTermsLink);

        // Activate terminal button with styling consistent with LoginView
        activateTerminalBtn = new Button("ACTIVATE TERMINAL");
        activateTerminalBtn.setPrefHeight(45);
        activateTerminalBtn.setPrefWidth(400);
        activateTerminalBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                           "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand; " +
                           "-fx-background-radius: 5px;");
        activateTerminalBtn.setDisable(true);
        
        // Add hover effect
        activateTerminalBtn.setOnMouseEntered(e -> 
            activateTerminalBtn.setStyle("-fx-background-color: " + SECONDARY_COLOR + "; -fx-text-fill: white; " +
                               "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand; " +
                               "-fx-background-radius: 5px;")
        );
        
        activateTerminalBtn.setOnMouseExited(e -> 
            activateTerminalBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                               "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand; " +
                               "-fx-background-radius: 5px;")
        );
        
        // Drop shadow effect for button
        DropShadow shadow = new DropShadow();
        shadow.setRadius(5.0);
        shadow.setOffsetY(3.0);
        shadow.setColor(Color.color(0.0, 0.0, 0.0, 0.3));
        activateTerminalBtn.setEffect(shadow);

        // Status section
        VBox statusBox = new VBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label statusLabel = new Label("Activation Status:");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_DARK + ";");
        
        statusValueLabel = new Label("Not Activated");
        statusValueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        
        statusBox.getChildren().addAll(statusLabel, statusValueLabel);

        // System info section styled consistently
        TitledPane systemInfoPane = createSystemInfoPane();
        systemInfoPane.setPrefWidth(400);
        systemInfoPane.setStyle("-fx-text-fill: " + TEXT_DARK + ";");

        // Add components to form
        activationForm.getChildren().addAll(codeBox, termsBox, activateTerminalBtn, statusBox);
        
        // Support info similar to LoginView
        HBox supportBox = new HBox();
        supportBox.setAlignment(Pos.CENTER);
        supportBox.setPadding(new Insets(30, 0, 0, 0));
        
        Label supportLabel = new Label("Need assistance? ");
        supportLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_MEDIUM + ";");
        
        Hyperlink contactLink = new Hyperlink("Contact Support");
        contactLink.setStyle("-fx-font-size: 13px; -fx-text-fill: " + SECONDARY_COLOR + ";");
        
        supportBox.getChildren().addAll(supportLabel, contactLink);

        // Add all components to main container
        container.getChildren().addAll(
                titleLabel,
                instructionLabel,
                spacer,
                activationForm,
                systemInfoPane,
                supportBox
        );

        // Add event handlers
        activateTerminalBtn.setOnAction(event -> handleActivateButtonClick());

        termsAgreeCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            activateTerminalBtn.setDisable(!newVal);
        });

        return container;
    }

    private void handleActivateButtonClick() {
        String activationCode = activationCodeField.getText();

        if (activationCode.isEmpty()) {
            statusValueLabel.setText("Activation Code is required");
            statusValueLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        ApiClient apiClient = new ApiClient();
        apiClient.activate(activationCode, success -> {
            Platform.runLater(() -> {
                if (success) {
                    // Show the confirmation dialog with terminal details
                    String site = Helper.getTerminalSiteName();
                    String tin = Helper.getTin();
                    String label = Helper.getTerminalLabel();
                    statusValueLabel.setText("Activated");
                    TerminalConfirmationDialog.TerminalInfo info = new TerminalConfirmationDialog.TerminalInfo(
                       site,
                       tin,
                       label
                    );
                    
                     Stage currentStage = (Stage) activateTerminalBtn.getScene().getWindow();
                
                    TerminalConfirmationDialog dialog = new TerminalConfirmationDialog(currentStage, info);
                
                    dialog.showAndWait();
                    currentStage.close();
                } else {
                    statusValueLabel.setText("Activation Failed");
                    statusValueLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            });
        });
    }

    private TitledPane createSystemInfoPane() {
        TitledPane infoPane = new TitledPane("System Information", new VBox());
        infoPane.setStyle("-fx-text-fill: " + TEXT_DARK + ";");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        addGridInfo(grid, "OS:", Helper.getOSName(), 0);
        addGridInfo(grid, "Version:", Helper.getOSVersion(), 1);
        addGridInfo(grid, "Architecture:", Helper.getOSArchitecture(), 2);
        addGridInfo(grid, "Application Version:", Helper.getAppVersion(), 3);
        addGridInfo(grid, "JavaFX Version:", Helper.getJavaFXVersion(), 4);

        infoPane.setContent(grid);
        infoPane.setExpanded(false);
        return infoPane;
    }

    private void addGridInfo(GridPane grid, String label, String value, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_DARK + ";");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_MEDIUM + ";");
        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }
}