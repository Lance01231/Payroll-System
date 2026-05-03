package org.nud.payroll;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Bootstraps the H2 in-memory SQL database.
 *
 * <p>H2 runs entirely inside the JVM — no files are written to disk and no
 * external server is required. All data lives in RAM and disappears when the
 * program exits, satisfying the "no file I/O / no external database" rule.
 *
 * <p>Call {@code DatabaseManager.init()} once at startup (from
 * {@code PayrollSystem.main}). Every repository then obtains its connection
 * from {@code DatabaseManager.getConnection()}.
 *
 * <p>Schema:
 * <ul>
 *   <li>{@code ACCOUNTS} — stores user credentials and roles.
 *   <li>{@code EMPLOYEES} — stores employee master records.
 * </ul>
 */
public class DatabaseManager {

    /** JDBC URL for a named, shared, purely in-memory H2 database. */
    private static final String JDBC_URL =
            "jdbc:h2:mem:payrolldb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE";

    private static final String USER = "sa";
    private static final String PASS = "";

    private DatabaseManager() {}

    /**
     * Creates tables and seeds the hardcoded ADMIN account.
     * Safe to call multiple times (uses IF NOT EXISTS).
     */
    public static void init() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            // --- ACCOUNTS table ---
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ACCOUNTS ("
                            + "  username         VARCHAR(50)  PRIMARY KEY,"
                            + "  password         VARCHAR(100) NOT NULL,"
                            + "  role             VARCHAR(10)  NOT NULL,"
                            + "  linked_employee_id VARCHAR(20)"
                            + ")");

            // --- EMPLOYEES table ---
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS EMPLOYEES ("
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

            // --- ATTENDANCE table ---
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ATTENDANCE ("
                            + "  id            INT AUTO_INCREMENT PRIMARY KEY,"
                            + "  employee_id   VARCHAR(20)  NOT NULL,"
                            + "  record_date   DATE         NOT NULL,"
                            + "  time_in       DOUBLE,"
                            + "  time_out      DOUBLE,"
                            + "  UNIQUE(employee_id, record_date)"
                            + ")");

            // --- SUBMISSIONS table ---
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS SUBMISSIONS ("
                            + "  id            INT AUTO_INCREMENT PRIMARY KEY,"
                            + "  employee_id   VARCHAR(20)  NOT NULL,"
                            + "  leave_days    DOUBLE,"
                            + "  ot_hours      DOUBLE       DEFAULT 0,"
                            + "  loans         DOUBLE,"
                            + "  status        VARCHAR(20)  DEFAULT 'PENDING'"
                            + ")");

            // --- Seed the hardcoded ADMIN account (ignore if already exists) ---
            stmt.executeUpdate(
                    "MERGE INTO ACCOUNTS (username, password, role, linked_employee_id)"
                            + " KEY(username)"
                            + " VALUES ('admin', 'admin123', 'ADMIN', NULL)");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialise in-memory database: " + e.getMessage(), e);
        }
    }

    /**
     * Opens and returns a JDBC connection to the shared in-memory database.
     * Callers are responsible for closing it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASS);
    }
}
