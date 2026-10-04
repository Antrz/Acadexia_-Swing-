package com.acadexia.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Properties;

/**
 * Database connection factory for MySQL.
 * Each call to getConnection() returns a FRESH connection that callers
 * are responsible for closing (safe to use in try-with-resources in DAOs).
 * 
 * initialize() is called once at startup to create the DB schema.
 */
public final class DatabaseConnection {

    private static final Properties properties = new Properties();
    private static boolean initialized = false;

    // Connection parameters cached after properties are loaded
    private static String dbUrl;
    private static String dbUser;
    private static String dbPass;

    static {
        loadProperties();
    }

    private DatabaseConnection() {}

    private static void loadProperties() {
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                properties.load(in);
            } else {
                // Default fallback configuration
                properties.setProperty("db.host", "localhost");
                properties.setProperty("db.port", "3306");
                properties.setProperty("db.name", "acadexia_db");
                properties.setProperty("db.user", "root");
                properties.setProperty("db.password", "");
                properties.setProperty("db.useSSL", "false");
                properties.setProperty("db.allowPublicKeyRetrieval", "true");
                properties.setProperty("db.serverTimezone", "UTC");
                properties.setProperty("db.autoInit", "true");
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load db.properties, using fallback defaults: " + e.getMessage());
        }

        // Cache connection parameters
        String host = properties.getProperty("db.host", "localhost");
        String port = properties.getProperty("db.port", "3306");
        String dbName = properties.getProperty("db.name", "acadexia_db");
        dbUser = properties.getProperty("db.user", "root");
        dbPass = properties.getProperty("db.password", "");
        dbUrl = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true",
                host, port, dbName
        );
    }

    /**
     * Initializes the MySQL database and executes schema.sql once at startup.
     * This method is idempotent and thread-safe.
     */
    public static synchronized void initialize() throws SQLException {
        if (initialized) return;

        String host = properties.getProperty("db.host", "localhost");
        String port = properties.getProperty("db.port", "3306");
        String dbName = properties.getProperty("db.name", "acadexia_db");

        // Step 1: Connect to MySQL server without database to ensure database exists
        String serverUrl = String.format(
                "jdbc:mysql://%s:%s/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, port
        );

        try (Connection serverConn = DriverManager.getConnection(serverUrl, dbUser, dbPass);
             Statement stmt = serverConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
        }

        // Step 2: Run schema.sql if tables do not exist
        if (Boolean.parseBoolean(properties.getProperty("db.autoInit", "true"))) {
            executeSchemaScript();
        }

        initialized = true;
    }

    /**
     * Returns a FRESH database connection. The caller is responsible for closing it.
     * Safe to use in try-with-resources blocks.
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    /**
     * Executes the SQL statements from schema.sql using a fresh dedicated connection.
     */
    private static void executeSchemaScript() {
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (in == null) {
                System.err.println("schema.sql not found in resources!");
                return;
            }

            String host = properties.getProperty("db.host", "localhost");
            String port = properties.getProperty("db.port", "3306");
            String dbName = properties.getProperty("db.name", "acadexia_db");
            String schemaUrl = String.format(
                    "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    host, port, dbName
            );

            try (Connection schemaConn = DriverManager.getConnection(schemaUrl, dbUser, dbPass);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                 Statement stmt = schemaConn.createStatement()) {

                StringBuilder sqlBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                        continue;
                    }
                    sqlBuilder.append(line).append("\n");
                    if (trimmed.endsWith(";")) {
                        String sql = sqlBuilder.toString().trim();
                        // Remove trailing semicolon
                        sql = sql.substring(0, sql.length() - 1);
                        try {
                            stmt.execute(sql);
                        } catch (SQLException ex) {
                            // If constraint or table already exists, log and proceed
                            if (!ex.getMessage().contains("already exists") && !ex.getMessage().contains("Duplicate foreign key")) {
                                System.err.println("Notice executing schema statement: " + ex.getMessage());
                            }
                        }
                        sqlBuilder.setLength(0);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing schema script: " + e.getMessage());
        }
    }

    /**
     * Tests whether the database is currently reachable.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static Properties getProperties() {
        return properties;
    }
}
