package com.pointofsale;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.pointofsale.helper.Helper;
import com.pointofsale.helper.ApiClient;

/**
 * A confirmation dialog that appears after successful terminal activation.
 * Shows terminal details and requires user confirmation.
 */
public class TerminalConfirmationDialog {
    
    private Stage dialogStage;
    private TerminalInfo terminalInfo;
    
    /**
     * Class to hold terminal information details
     */
    public static class TerminalInfo {
        private String terminalSite;
        private String tinLabel;
        private String tillLabel;
        
        public TerminalInfo(String terminalSite, String tinLabel, String tillLabel) {
            this.terminalSite = terminalSite;
            this.tinLabel = tinLabel;
            this.tillLabel = tillLabel;
        }
        
        // Getters
        public String getTerminalSite() { return terminalSite; }
        public String getTinLabel() { return tinLabel; }
        public String getTillLabel() { return tillLabel; }
    }
    
    /**
     * Constructor for the confirmation dialog
     * 
     * @param parentStage The parent stage this dialog will be modal to
     * @param terminalInfo The terminal information to display
     */
    public TerminalConfirmationDialog(Stage parentStage, TerminalInfo terminalInfo) {
        this.terminalInfo = terminalInfo;
        
        dialogStage = new Stage();
        dialogStage.initOwner(parentStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.setTitle("Terminal Activation Confirmation");
        dialogStage.setResizable(false);
        
        // Create and set the dialog scene
        Scene scene = new Scene(createDialogContent(), 450, 400); // Increased height to ensure all elements are visible
        dialogStage.setScene(scene);
    }
    
    /**
     * Creates the content for the dialog
     */
    private VBox createDialogContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.TOP_CENTER);
        
        // Header
        Label headerLabel = new Label("Terminal Activation Successful");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Sub header
        Label subHeaderLabel = new Label("Please confirm the terminal details below:");
        subHeaderLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        // Separator
        Separator separator = new Separator();
        separator.setPrefWidth(400);
        
        // Terminal details grid
        GridPane detailsGrid = createDetailsGrid();
        
        // Success icon or message
        HBox successBox = new HBox();
        successBox.setAlignment(Pos.CENTER);
        Label successIcon = new Label("✓");
        successIcon.setStyle("-fx-font-size: 24px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        Label successLabel = new Label(" Activation Complete");
        successLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60;");
        successBox.getChildren().addAll(successIcon, successLabel);
        
        // Note about activation
        Label noteLabel = new Label("Your terminal has been activated successfully. " +
                                    "You can now close this dialog.");
        noteLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;");
        noteLabel.setWrapText(true);
        noteLabel.setAlignment(Pos.CENTER);
        
        // Button container for better visibility and alignment
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 10, 0));
        
        // Confirmation button
        Button confirmButton = new Button("Confirm");
        confirmButton.setPrefWidth(150);
        confirmButton.setPrefHeight(40);
        confirmButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        
        confirmButton.setOnAction(event -> {
            // On button click, perform the terminal activation confirmation
            confirmTerminalActivation();
        });
        
        // Add the button to the button container
        buttonBox.getChildren().add(confirmButton);
        
        // Add all components to the main layout
        content.getChildren().addAll(
                headerLabel,
                subHeaderLabel,
                separator,
                detailsGrid,
                successBox,
                noteLabel,
                buttonBox // Using the button container instead of just the button
        );
        
        return content;
    }
    
    /**
     * Creates a grid layout for the terminal details
     */
    private GridPane createDetailsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 10, 20, 10));
        grid.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");
        grid.setPrefWidth(380);
        grid.setAlignment(Pos.CENTER);
        
        // Style for labels
        String labelStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;";
        String valueStyle = "-fx-font-size: 14px; -fx-text-fill: #2c3e50;";
        
        // Terminal Site
        Label siteLabel = new Label("Terminal Site:");
        siteLabel.setStyle(labelStyle);
        Label siteValue = new Label(terminalInfo.getTerminalSite());
        siteValue.setStyle(valueStyle);
        grid.add(siteLabel, 0, 0);
        grid.add(siteValue, 1, 0);
        
        // TIN Label
        Label tinTitleLabel = new Label("TIN:");
        tinTitleLabel.setStyle(labelStyle);
        Label tinValue = new Label(terminalInfo.getTinLabel());
        tinValue.setStyle(valueStyle);
        grid.add(tinTitleLabel, 0, 1);
        grid.add(tinValue, 1, 1);
        
        // Till Label
        Label tillTitleLabel = new Label("Till Label:");
        tillTitleLabel.setStyle(labelStyle);
        Label tillValue = new Label(terminalInfo.getTillLabel());
        tillValue.setStyle(valueStyle);
        grid.add(tillTitleLabel, 0, 2);
        grid.add(tillValue, 1, 2);
        
        return grid;
    }
    
    /**
     * Confirms the terminal activation when the confirm button is clicked.
     */
    private void confirmTerminalActivation() {
        // Generate the signature and terminalId using Helper methods
        String activationCode = Helper.getActivationCode();
        String secretKey = Helper.getSecretKey();
        String terminalId = Helper.getTerminalId();
         String token = Helper.getToken();

        String xSignature = Helper.computeXSignature(activationCode, secretKey);

        // Create an instance of ApiClient to call the confirmation method
        ApiClient apiClient = new ApiClient();
        apiClient.confirmActivation(xSignature, terminalId, token, success -> {
            if (success) {
                System.out.println("Terminal activation confirmed!");
                // You can show the next form or dialog here if needed
            } else {
                System.out.println("Failed to confirm activation.");
                // You can show an error message to the user here
            }
        });

        // Close the dialog after processing
        dialogStage.close();
    }
    /**
     * Shows the dialog and waits for user interaction
     */
    public void showAndWait() {
        dialogStage.showAndWait();
    }
}