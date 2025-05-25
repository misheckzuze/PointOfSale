package com.pointofsale;


import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.pointofsale.model.Session;

public class LockScreen {

    public static void showLockScreen(Stage primaryStage) {
        Stage lockStage = new Stage();
        lockStage.initModality(Modality.APPLICATION_MODAL);
        lockStage.setTitle("🔒 Session Locked");

        Label label = new Label("Session timed out. Please enter your password:");
        PasswordField passwordField = new PasswordField();
        Button unlockButton = new Button("Unlock");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        unlockButton.setOnAction(e -> {
            String inputPassword = passwordField.getText();
            if (inputPassword.equals(Session.currentPassword)) {
                Session.updateActivity();
                lockStage.close();
            } else {
                errorLabel.setText("Incorrect password.");
            }
        });

        VBox layout = new VBox(10, label, passwordField, unlockButton, errorLabel);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new javafx.geometry.Insets(20));

        lockStage.setScene(new Scene(layout, 300, 200));
        lockStage.initOwner(primaryStage);
        lockStage.setResizable(false);
        lockStage.showAndWait(); // Pause application until unlocked
    }
}

