package com.acadexia;

import com.acadexia.config.DatabaseConnection;
import com.acadexia.config.DatabaseSeeder;
import com.acadexia.ui.auth.LandingFrame;
import com.acadexia.ui.common.UITheme;

import javax.swing.*;

/**
 * Acadexia - College Management System
 * Main Application Entry Point.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Setup Modern FlatLaf Dark Look and Feel
        UITheme.setup();

        // 2. Initialize Database & Seed Starter Records in background
        new Thread(() -> {
            try {
                System.out.println("Connecting to MySQL Database...");
                DatabaseConnection.initialize();
                DatabaseSeeder.seedIfEmpty();
                System.out.println("Acadexia database initialization complete.");
            } catch (Exception e) {
                System.err.println("Database Notice: " + e.getMessage());
                System.err.println("Ensure MySQL is running on localhost:3306 or check db.properties.");
            }
        }).start();

        // 3. Launch Landing Portal on Swing EDT
        SwingUtilities.invokeLater(() -> {
            LandingFrame landing = new LandingFrame();
            landing.setVisible(true);
        });
    }
}
