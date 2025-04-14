package com.pointofsale;

import com.pointofsale.helper.ApiClient;
import javafx.application.Application;
import javafx.application.Platform;
import com.pointofsale.helper.Helper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;


public class TerminalActivationView extends Application {

    private TextField activationCodeField;
    private Button activateTerminalBtn;
    private CheckBox termsAgreeCheckbox;
    private Label statusValueLabel;

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
            stage.setTitle("EIS Point of Sale - Terminal Activation");

            VBox mainContainer = createMainContainer();

            Scene scene = new Scene(mainContainer, 600, 500);
            applyStyles(mainContainer);

            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Log error and show any exception
        }
    }

    private VBox createMainContainer() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("Terminal Activation");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        StackPane logoContainer = new StackPane();
        logoContainer.setPrefHeight(150);
        logoContainer.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        Label logoText = new Label("EIS POS");
        logoText.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        logoContainer.getChildren().add(logoText);

        Separator separator1 = new Separator();
        separator1.setPrefWidth(550);

        Label activationCodeLabel = new Label("Enter Activation Code:");
        activationCodeLabel.setStyle("-fx-font-size: 14px;");
        activationCodeField = new TextField();
        activationCodeField.setPromptText("XXXX-XXXX-XXXX-XXXX");
        activationCodeField.setPrefWidth(300);
        activationCodeField.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");

        HBox termsBox = new HBox(10);
        termsBox.setAlignment(Pos.CENTER);
        termsAgreeCheckbox = new CheckBox("I agree to the Terms and Conditions");
        termsAgreeCheckbox.setStyle("-fx-font-size: 14px;");
        Hyperlink viewTermsLink = new Hyperlink("View Terms & Conditions");
        viewTermsLink.setStyle("-fx-font-size: 14px;");
        termsBox.getChildren().addAll(termsAgreeCheckbox, viewTermsLink);

        activateTerminalBtn = new Button("Activate Terminal");
        activateTerminalBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-font-weight: bold;");
        activateTerminalBtn.setDisable(true);

        Separator separator2 = new Separator();
        separator2.setPrefWidth(550);

        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER);
        Label statusLabel = new Label("Status:");
        statusLabel.setStyle("-fx-font-size: 14px;");
        statusValueLabel = new Label("Not Activated");
        statusValueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        statusBox.getChildren().addAll(statusLabel, statusValueLabel);

        TitledPane systemInfoPane = createSystemInfoPane();
        systemInfoPane.setPrefWidth(400);

        container.getChildren().addAll(
                titleLabel,
                logoContainer,
                separator1,
                createFormSection(activationCodeLabel, activationCodeField),
                termsBox,
                activateTerminalBtn,
                separator2,
                statusBox,
                systemInfoPane
        );

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
                
                    TerminalConfirmationDialog dialog = new TerminalConfirmationDialog(
                      (Stage) activateTerminalBtn.getScene().getWindow(),
                      info
                    );
                
                dialog.showAndWait();
                    statusValueLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                } else {
                    statusValueLabel.setText("Activation Failed");
                    statusValueLabel.setTextFill(javafx.scene.paint.Color.RED);
                }
            });
        });
    }

    private HBox createFormSection(Label label, Control control) {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);
        section.getChildren().addAll(label, control);
        return section;
    }

    private TitledPane createSystemInfoPane() {
        TitledPane infoPane = new TitledPane("System Information", new VBox());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

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
        l.setStyle("-fx-font-size: 14px;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 14px;");
        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }

    private void applyStyles(VBox container) {
        container.setStyle("-fx-background-color: white;");
    }
}

