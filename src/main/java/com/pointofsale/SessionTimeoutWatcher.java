package com.pointofsale;

import java.time.Duration;
import java.time.LocalDateTime;
import com.pointofsale.model.Session;
import javafx.application.Platform;
import javafx.stage.Stage;

public class SessionTimeoutWatcher extends Thread {
    private final int timeoutMinutes;
    private final Stage primaryStage;

    public SessionTimeoutWatcher(int timeoutMinutes, Stage primaryStage) {
        this.timeoutMinutes = timeoutMinutes;
        this.primaryStage = primaryStage;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(60 * 1000); // Check every minute

                if (Session.lastActivityTime != null) {
                    long inactivity = Duration.between(Session.lastActivityTime, LocalDateTime.now()).toMinutes();
                    if (inactivity >= timeoutMinutes) {
                        System.out.println("🔒 Session locked due to inactivity.");
                        Platform.runLater(() -> LockScreen.showLockScreen(primaryStage));
                        break; // Stop this thread after lock
                    }
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}


