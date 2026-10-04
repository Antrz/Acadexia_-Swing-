package com.acadexia.ui.common;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Modern Swing design system configuration powered by FlatLaf Dark.
 * Enforces crisp typography, harmonious dark color palettes, rounded borders, and custom component styles.
 */
public final class UITheme {

    // Harmonious curated palette
    public static final Color BG_DARK = new Color(24, 24, 37);       // Base canvas
    public static final Color CARD_BG = new Color(34, 34, 52);       // Elevated cards
    public static final Color CARD_BORDER = new Color(50, 50, 74);   // Card outline
    public static final Color PRIMARY = new Color(99, 102, 241);     // Modern Indigo accent
    public static final Color PRIMARY_HOVER = new Color(79, 70, 229);
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    public static final Color TEXT_MUTED = new Color(148, 163, 184);

    // Status colors
    public static final Color SUCCESS = new Color(16, 185, 129);
    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color INFO = new Color(59, 130, 246);

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_STAT_NUM = new Font("Segoe UI", Font.BOLD, 26);

    private UITheme() {}

    /**
     * Initializes FlatLaf Dark theme and injects global UI defaults.
     */
    public static void setup() {
        try {
            FlatDarkLaf.setup();

            // Set global component styling
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.selectedBackground", PRIMARY);
            UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
            UIManager.put("Table.selectionBackground", new Color(49, 46, 129));
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("Table.rowHeight", 34);
            UIManager.put("TableHeader.font", FONT_BOLD);
            UIManager.put("TableHeader.height", 36);

        } catch (Exception e) {
            System.err.println("Could not initialize FlatLaf Dark: " + e.getMessage());
        }
    }
}
