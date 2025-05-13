package com.pointofsale.utils;

import javafx.scene.control.Dialog;
import javafx.scene.control.Alert;
import javafx.application.Platform;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;


public class StyleUtils {
    public static void styleDialogButtons(Dialog<?> dialog) {
        styleDialogButtons((Alert) dialog);
    }
    
    public static void styleDialogButtons(Alert alert) {
        Platform.runLater(() -> {
            DialogPane dialogPane = alert.getDialogPane();
            
            for (ButtonType buttonType : dialogPane.getButtonTypes()) {
                Button button = (Button) dialogPane.lookupButton(buttonType);
                
                if (button != null) {
                    if (buttonType == ButtonType.OK || buttonType == ButtonType.YES) {
                        button.getStyleClass().add("primary-button");
                    } else if (buttonType == ButtonType.CANCEL || buttonType == ButtonType.NO) {
                        button.getStyleClass().add("secondary-button");
                    } else {
                        button.getStyleClass().add("default-button");
                    }
                }
            }
        });
    }
}
