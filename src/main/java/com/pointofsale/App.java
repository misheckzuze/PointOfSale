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
        if (args.length == 1 && "--verify-runtime".equals(args[0])) {
            try {
                Class.forName("org.sqlite.JDBC");
                Class.forName("javafx.application.Platform");
                Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
                System.out.println("Point of Sale runtime verified; no database or UI opened.");
                return;
            } catch (Exception ex) { throw new IllegalStateException("Incomplete application package", ex); }
        }
        try {
            // Step 1: Initialize the local DB
            System.out.println("Initializing database...");
            Database.initializeDatabase();
            
            // Step 2: Load security settings
            SecuritySettings settings = Helper.getSettings();

            // Step 3: Check terminal activation status
            boolean isActivated = Helper.isTerminalActivated();

            // Step 4: Start background scheduler for API retry
            scheduler = Executors.newScheduledThreadPool(2, task -> { Thread thread = new Thread(task, "pos-background"); thread.setDaemon(true); return thread; });
            com.pointofsale.helper.ProductSyncService.getInstance().start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> { scheduler.shutdownNow(); com.pointofsale.helper.ProductSyncService.getInstance().stop(); }));
            scheduler.scheduleAtFixedRate(() -> {
                System.out.println("🔄 Running auto-resend for pending transactions...");
                ApiClient apiClient = new ApiClient();
                apiClient.retryPendingTransactions();
            }, 0, 2, TimeUnit.MINUTES);
            
            // NEW — keeps Helper's cached terminal-blocking status fresh in the background,
            // so checkout can read it instantly via Helper.isCheckoutAllowedCached()
            // instead of making a live network call during checkout.
            scheduler.scheduleAtFixedRate(() -> {
                System.out.println("🔒 Refreshing terminal blocking status...");
                Helper.refreshTerminalBlockingStatus();
            }, 0, 1, TimeUnit.MINUTES);

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
