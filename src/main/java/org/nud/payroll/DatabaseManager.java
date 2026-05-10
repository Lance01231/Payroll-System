package org.nud.payroll;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Meet the DatabaseManager! It sets up our persistent MySQL database.
 *
 * <p>Configure MySQL access with environment variables {@code PAYROLL_JDBC_URL}, {@code PAYROLL_DB_USER},
 * and {@code PAYROLL_DB_PASSWORD}. Password fallback (same precedence): env {@code PAYROLL_DB_PASSWORD}, then JVM
 * system property {@code payroll.db.password} (useful for {@code mvn exec:java} without exporting env vars).
 */
public class DatabaseManager {

    private static final String JDBC_URL = firstNonBlank(
            System.getenv("PAYROLL_JDBC_URL"),
            "jdbc:mysql://localhost:3306/payrolldb?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false");

    private static final String USER = firstNonBlank(System.getenv("PAYROLL_DB_USER"), "root");

    private static final String PASS = resolveDbPassword();

    private DatabaseManager() {}

    private static String resolveDbPassword() {
        String fromEnv = envOrEmpty("PAYROLL_DB_PASSWORD");
        if (!fromEnv.isEmpty()) {
            return fromEnv;
        }
        String fromProp = System.getProperty("payroll.db.password");
        return fromProp != null ? fromProp : "";
    }

    private static String envOrEmpty(String key) {
        String v = System.getenv(key);
        return v != null ? v : "";
    }

    private static String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        return fallback;
    }

    /**
     * Creates tables and seeds the ADMIN account when missing.
     * Safe to call multiple times (uses IF NOT EXISTS).
     */
    public static void init() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            // First up, the ACCOUNTS table to store who can log in!
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ACCOUNTS ("
                    + "  username         VARCHAR(50)  PRIMARY KEY,"
                    + "  password         VARCHAR(255) NOT NULL,"
                    + "  role             VARCHAR(10)  NOT NULL,"
                    + "  linked_employee_id VARCHAR(20)"
                    + ")");

            // Next, the EMPLOYEES table. Holds all the important staff info!
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS EMPLOYEES ("
                    + "  employee_id   VARCHAR(20)  PRIMARY KEY,"
                    + "  employee_name VARCHAR(100) NOT NULL,"
                    + "  employee_type VARCHAR(20)  NOT NULL,"
                    + "  basic_rate    DOUBLE       NOT NULL,"
                    + "  cut_off       INT          NOT NULL,"
                    + "  work_schedule VARCHAR(50),"
                    + "  sick_leave    INT          DEFAULT 0,"
                    + "  vacation_leave INT         DEFAULT 0,"
                    + "  emergency_leave INT        DEFAULT 0,"
                    + "  loan_balance  DOUBLE       DEFAULT 0"
                    + ")");

            // The ATTENDANCE table keeps track of when people clock in and out.
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ATTENDANCE ("
                    + "  id            INT AUTO_INCREMENT PRIMARY KEY,"
                    + "  employee_id   VARCHAR(20)  NOT NULL,"
                    + "  record_date   DATE         NOT NULL,"
                    + "  time_in       DOUBLE,"
                    + "  time_out      DOUBLE,"
                    + "  UNIQUE(employee_id, record_date)"
                    + ")");

            // Finally, the SUBMISSIONS table for leaves, OT, and loans.
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS SUBMISSIONS ("
                    + "  id            INT AUTO_INCREMENT PRIMARY KEY,"
                    + "  employee_id   VARCHAR(20)  NOT NULL,"
                    + "  leave_days    DOUBLE,"
                    + "  ot_hours      DOUBLE       DEFAULT 0,"
                    + "  loans         DOUBLE,"
                    + "  status        VARCHAR(20)  DEFAULT 'PENDING'"
                    + ")");

            widenAccountsPasswordColumn(stmt);

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT IGNORE INTO ACCOUNTS (username, password, role, linked_employee_id) VALUES (?, ?, 'ADMIN', NULL)")) {
                ps.setString(1, "admin");
                ps.setString(2, SecurityUtils.hashPassword("admin123"));
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialise database: " + e.getMessage(), e);
        }
    }

    /** Older installs used VARCHAR(100); BCrypt and migrations need more room. */
    private static void widenAccountsPasswordColumn(Statement stmt) {
        try {
            stmt.executeUpdate("ALTER TABLE ACCOUNTS MODIFY COLUMN password VARCHAR(255) NOT NULL");
        } catch (SQLException ignored) {
            // Table may not exist yet on some paths, or already correct — safe to ignore.
        }
    }

    /**
     * Opens and returns a JDBC connection to the database.
     * Callers are responsible for closing it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASS);
    }
}
