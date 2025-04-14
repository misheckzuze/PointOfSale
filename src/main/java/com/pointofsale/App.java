package com.pointofsale;

import com.pointofsale.data.Database;
import javafx.application.Application;
import com.pointofsale.helper.Helper;

public class App {
    public static void main(String[] args) {
        try {
            // Step 1: Initialize the local DB
            System.out.println("Initializing database...");
            Database.initializeDatabase();
            
            // Check terminal activation status
            boolean isActivated = Helper.isTerminalActivated();

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
