package com.pointofsale;

import com.pointofsale.data.Database;
import javafx.application.Application;

public class App {
    public static void main(String[] args) {
        try {
            // Step 1: Initialize the local DB
            System.out.println("Initializing database...");
            Database.initializeDatabase();
            
            // Step 2: Launch the Activation UI
            System.out.println("Launching TerminalActivation...");
            Application.launch(TerminalActivationView.class, args);
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
