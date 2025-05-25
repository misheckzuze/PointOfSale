package com.pointofsale;

import com.pointofsale.data.Database;
import com.pointofsale.model.SecuritySettings;
import com.pointofsale.helper.Helper;
import com.pointofsale.helper.ApiClient;
import javafx.application.Application;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {
    private static ScheduledExecutorService scheduler;

    public static void main(String[] args) {
        try {
            // Step 1: Initialize the local DB
            System.out.println("Initializing database...");
            Database.initializeDatabase();

            // Step 2: Load security settings
            SecuritySettings settings = Helper.getSettings();

            // Step 3: Check terminal activation status
            boolean isActivated = Helper.isTerminalActivated();

            // Step 4: Start background scheduler for API retry
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                System.out.println("🔄 Running auto-resend for pending transactions...");
                ApiClient apiClient = new ApiClient();
                apiClient.retryPendingTransactions();
            }, 0, 2, TimeUnit.MINUTES);

            // Step 5: Apply Require Login setting
            if (isActivated) {
                if (settings.requireLogin) {
                    System.out.println("Require login enabled. Launching Login View...");
                    Application.launch(LoginView.class, args);
                } else {
                    System.out.println("Require login disabled. Skipping to dashboard...");
                    Application.launch(POSDashboard.class, args); 
                }
            } else {
                System.out.println("Terminal not activated. Launching Activation View...");
                Application.launch(TerminalActivationView.class, args);
            }

        } catch (Exception e) {
            System.err.println("❌ Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
