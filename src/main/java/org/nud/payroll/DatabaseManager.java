package org.nud.payroll;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Meet the DatabaseManager! It sets up our persistent MySQL database.
 *
 * All data is saved safely to your local MySQL server.
 * Be sure to update the USER and PASS below to match your MySQL credentials!
 */
public class DatabaseManager {

    /** JDBC URL for your local MySQL server. */
    private static final String JDBC_URL =
            "jdbc:mysql://localhost:3306/payrolldb?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false";

    private static final String USER = "root";
    private static final String PASS = "maskelz1905_"; // <-- Update this to your MySQL password

    private DatabaseManager() {}

    /**
     * Creates tables and seeds the hardcoded ADMIN account.
     * Safe to call multiple times (uses IF NOT EXISTS).
     */
    public static void init() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            // First up, the ACCOUNTS table to store who can log in!
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ACCOUNTS ("
                    + "  username         VARCHAR(50)  PRIMARY KEY,"
                    + "  password         VARCHAR(100) NOT NULL,"
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

            // Let's make sure our main admin can always log in!
            stmt.executeUpdate(
                    "INSERT IGNORE INTO ACCOUNTS (username, password, role, linked_employee_id)"
                            + " VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', NULL)");

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
