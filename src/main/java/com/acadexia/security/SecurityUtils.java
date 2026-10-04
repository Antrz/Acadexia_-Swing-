package com.acadexia.security;

import org.mindrot.jbcrypt.BCrypt;

import java.util.regex.Pattern;

/**
 * Core security utility providing BCrypt hashing, verification,
 * and input validation helpers against SQL injection and malicious payloads.
 */
public final class SecurityUtils {

    // Regex for safe alphanumeric identifiers (e.g. REG-2024-CS001, FAC-CS-101, username_123)
    private static final Pattern SAFE_IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,50}$");

    // Prohibited SQL injection tokens and meta-characters
    private static final Pattern DANGEROUS_SQL_PATTERN = Pattern.compile(
            "(?i)(--|/\\*|\\*/|;|'|#|xp_|exec(\\s|\\+)+(s|x)p|union(\\s|\\+)+select|drop(\\s|\\+)+table|insert(\\s|\\+)+into|delete(\\s|\\+)+from|select(\\s|\\+)+.*(\\s|\\+)+from|or\\s+['\"]?1['\"]?\\s*=\\s*['\"]?1)"
    );

    private SecurityUtils() {
        // Utility class
    }

    /**
     * Hashes a raw password using BCrypt with salt.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    /**
     * Verifies a plain password against a stored BCrypt hash.
     */
    public static boolean checkPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the given input contains dangerous SQL injection sequences.
     */
    public static boolean containsSqlInjection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return DANGEROUS_SQL_PATTERN.matcher(input).find();
    }

    /**
     * Validates safe identifiers (Registration Number, Faculty ID, Username).
     */
    public static boolean isValidIdentifier(String identifier) {
        if (identifier == null) {
            return false;
        }
        return SAFE_IDENTIFIER_PATTERN.matcher(identifier.trim()).matches();
    }

    /**
     * Strips dangerous characters and trims string for safe processing.
     */
    public static String sanitizeGeneralText(String text) {
        if (text == null) {
            return "";
        }
        // Remove null bytes, control characters, and dangerous SQL characters
        return text.replace("\0", "")
                   .replace("'", "")
                   .replace("\"", "")
                   .replace(";", "")
                   .trim();
    }
}
