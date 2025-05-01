package com.pointofsale;

import com.pointofsale.data.Database;
import javafx.application.Application;
import com.pointofsale.helper.Helper;
import com.pointofsale.helper.ApiClient;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class App {
    public static void main(String[] args) {
        try {
            // Step 1: Initialize the local DB
            System.out.println("Initializing database...");
            Database.initializeDatabase();
            
            // Check terminal activation status
            boolean isActivated = Helper.isTerminalActivated();
            
            // Start scheduler BEFORE launching the UI
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
            System.out.println("🔄 Running auto-resend for pending transactions...");
            ApiClient apiClient = new ApiClient();
            apiClient.retryPendingTransactions();
            }, 0, 2, TimeUnit.MINUTES);

            if (isActivated) {
                System.out.println("Terminal is already activated. Launching Login View...");
                Application.launch(LoginView.class, args);
            } else {
                System.out.println("Terminal not activated. Launching Activation View...");
                Application.launch(TerminalActivationView.class, args);
            }

            
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
