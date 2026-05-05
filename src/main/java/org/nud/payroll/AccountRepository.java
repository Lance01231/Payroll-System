package org.nud.payroll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * The AccountRepository handles all the database operations for user accounts!
 * We're now connected to a persistent MySQL database, meaning all accounts
 * are securely saved to your local MySQL server.
 */
public class AccountRepository {

    // --- Our handy SQL queries kept in one place ---
    private static final String SQL_INSERT_ACCOUNT =
            "INSERT INTO ACCOUNTS (username, password, role, linked_employee_id) VALUES (?, ?, 'EMPLOYEE', ?)";
    private static final String SQL_AUTH_ACCOUNT =
            "SELECT role, linked_employee_id FROM ACCOUNTS WHERE username = ? AND password = ?";
    private static final String SQL_DELETE_ACCOUNT = "DELETE FROM ACCOUNTS WHERE linked_employee_id = ?";

    // --- Let's add some data! ---
    /**
     * Inserts a new EMPLOYEE-role account linked to the given employee ID.
     * Called by the Admin when registering a new staff member.
     *
     * @param username         chosen login username
     * @param password         chosen login password
     * @param linkedEmployeeId the Employee ID this account belongs to
     */
    public static void addEmployeeAccount(String username, String password, String linkedEmployeeId) {
        String hashedPassword = SecurityUtils.hashPassword(password);

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL_INSERT_ACCOUNT)) {
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.setString(3, linkedEmployeeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Oops, failed to save the employee account!", e);
        }
    }

    // --- Time to read some data! ---

    /**
     * Tries to log a user in!
     *
     * @return an Optional containing the User if they logged in successfully, or empty if they didn't.
     */
    public static Optional<User> authenticate(String username, String password) {
        String hashedPassword = SecurityUtils.hashPassword(password);

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL_AUTH_ACCOUNT)) {
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User.Role role = User.Role.valueOf(rs.getString("role"));
                    String linkedId = rs.getString("linked_employee_id"); // may be NULL
                    return Optional.of(new User(username, hashedPassword, role, linkedId));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Uh oh, we had a problem checking those credentials.", e);
        }
        return Optional.empty();
    }

    // --- Clean up time (Delete) ---

    public static void deleteByEmployeeId(String employeeId) {
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL_DELETE_ACCOUNT)) {
            ps.setString(1, employeeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete the account.", e);
        }
    }
}
