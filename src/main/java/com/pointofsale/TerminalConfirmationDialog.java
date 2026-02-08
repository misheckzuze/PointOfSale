package com.pointofsale;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.pointofsale.helper.Helper;
import com.pointofsale.helper.ApiClient;

public class TerminalConfirmationDialog {

    private Stage dialogStage;
    private TerminalInfo terminalInfo;

    private final String PRIMARY_COLOR = "#1a237e";
    private final String SECONDARY_COLOR = "#3949ab";
    private final String TEXT_DARK = "#555555";
    private final String TEXT_MEDIUM = "#757575";
    private final String FIELD_BG = "#f8f8f8";
    private final String FIELD_BORDER = "#e0e0e0";
    private final String SUCCESS_COLOR = "#4CAF50";

    public static class TerminalInfo {
        private String terminalSite;
        private String tinLabel;
        private String tillLabel;

        public TerminalInfo(String terminalSite, String tinLabel, String tillLabel) {
            this.terminalSite = terminalSite;
            this.tinLabel = tinLabel;
            this.tillLabel = tillLabel;
        }

        public String getTerminalSite() { return terminalSite; }
        public String getTinLabel() { return tinLabel; }
        public String getTillLabel() { return tillLabel; }
    }

    public TerminalConfirmationDialog(Stage parentStage, TerminalInfo terminalInfo) {
        this.terminalInfo = terminalInfo;

        dialogStage = new Stage();
        dialogStage.initOwner(parentStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.setTitle("Terminal Activation Confirmation");
        dialogStage.setResizable(false);

        Scene scene = new Scene(createDialogContent(), 450, 480); // increased height
        dialogStage.setScene(scene);
    }

    private VBox createDialogContent() {
    VBox content = new VBox(15);
    content.setPadding(new Insets(30));
    content.setAlignment(Pos.TOP_CENTER);
    content.setStyle("-fx-background-color: white;");

    Label headerLabel = new Label("Terminal Activation Successful");
    headerLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + PRIMARY_COLOR + ";");

    Label subHeaderLabel = new Label("Please confirm the terminal details below:");
    subHeaderLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_MEDIUM + ";");

    Separator separator = new Separator();
    separator.setPrefWidth(400);

    GridPane detailsGrid = createDetailsGrid();

    HBox successBox = new HBox();
    successBox.setAlignment(Pos.CENTER);
    successBox.setSpacing(8);
    Label successIcon = new Label("✓");
    successIcon.setStyle("-fx-font-size: 24px; -fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold;");
    Label successLabel = new Label("Activation Complete");
    successLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: " + SUCCESS_COLOR + ";");
    successBox.getChildren().addAll(successIcon, successLabel);

    Label noteLabel = new Label("Your terminal has been activated successfully. You can now proceed to the login screen.");
    noteLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_MEDIUM + "; -fx-text-alignment: center;");
    noteLabel.setWrapText(true);
    noteLabel.setMaxWidth(360);
    noteLabel.setAlignment(Pos.CENTER);

    Button confirmButton = new Button("PROCEED TO LOGIN");
    confirmButton.setPrefWidth(220);
    confirmButton.setPrefHeight(45);
    confirmButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
            "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand; " +
            "-fx-background-radius: 5px;");

    confirmButton.setOnMouseEntered(e ->
            confirmButton.setStyle("-fx-background-color: " + SECONDARY_COLOR + "; -fx-text-fill: white; " +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand; " +
                    "-fx-background-radius: 5px;")
    );

    confirmButton.setOnMouseExited(e ->
            confirmButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; " +
                    "-fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand; " +
                    "-fx-background-radius: 5px;")
    );

    DropShadow shadow = new DropShadow();
    shadow.setRadius(5.0);
    shadow.setOffsetY(3.0);
    shadow.setColor(Color.color(0.0, 0.0, 0.0, 0.3));
    confirmButton.setEffect(shadow);

    confirmButton.setOnAction(event -> confirmTerminalActivation());

    HBox buttonBox = new HBox(confirmButton);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.setPadding(new Insets(15, 0, 20, 0));
    VBox.setVgrow(buttonBox, Priority.NEVER);

    content.getChildren().addAll(
            headerLabel,
            subHeaderLabel,
            separator,
            detailsGrid,
            successBox,
            noteLabel,
            buttonBox
    );

    // Return content directly (no scroll pane)
    return content;
}

    private GridPane createDetailsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 15, 25, 15));
        grid.setStyle("-fx-background-color: " + FIELD_BG + "; -fx-border-color: " + FIELD_BORDER + "; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");
        grid.setPrefWidth(390);
        grid.setAlignment(Pos.CENTER);

        String labelStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_DARK + ";";
        String valueStyle = "-fx-font-size: 14px; -fx-text-fill: " + SECONDARY_COLOR + ";";

        Label siteLabel = new Label("Terminal Site:");
        siteLabel.setStyle(labelStyle);
        Label siteValue = new Label(terminalInfo.getTerminalSite());
        siteValue.setStyle(valueStyle);
        grid.add(siteLabel, 0, 0);
        grid.add(siteValue, 1, 0);

        Label tinTitleLabel = new Label("TIN:");
        tinTitleLabel.setStyle(labelStyle);
        Label tinValue = new Label(terminalInfo.getTinLabel());
        tinValue.setStyle(valueStyle);
        grid.add(tinTitleLabel, 0, 1);
        grid.add(tinValue, 1, 1);

        Label tillTitleLabel = new Label("Till Label:");
        tillTitleLabel.setStyle(labelStyle);
        Label tillValue = new Label(terminalInfo.getTillLabel());
        tillValue.setStyle(valueStyle);
        grid.add(tillTitleLabel, 0, 2);
        grid.add(tillValue, 1, 2);

        return grid;
    }

    private void confirmTerminalActivation() {
    String activationCode = Helper.getActivationCode();
    String secretKey = Helper.getSecretKey();
    String terminalId = Helper.getTerminalId();
    String token = Helper.getToken();
    String tin = Helper.getTin();
    String siteId = Helper.getTerminalSiteId();

    String xSignature = Helper.computeXSignature(activationCode, secretKey);

    ApiClient apiClient = new ApiClient();
    apiClient.confirmActivation(xSignature, terminalId, token, success -> {
        if (success) {
            System.out.println("Terminal activation confirmed!");

            // Call getTerminalSiteProducts statically
            apiClient.getTerminalSiteProducts(tin, siteId, token, productsFetched -> {
                if (productsFetched) {
                    System.out.println("Products fetched and saved.");

                    Platform.runLater(() -> {
                        dialogStage.close();
                        Stage loginStage = new Stage();
                        loginStage.setTitle("POS System Login");
                        LoginView loginView = new LoginView();
                        loginView.start(loginStage);
                    });
                } else {
                    System.err.println("Failed to fetch products.");
                }
            });

        } else {
            System.out.println("Failed to confirm activation.");
            dialogStage.close();
        }
    });
}


    public void showAndWait() {
        dialogStage.showAndWait();
    }
}
