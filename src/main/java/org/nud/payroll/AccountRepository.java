package org.nud.payroll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The AccountRepository handles all the database operations for user accounts!
 * We're using an in-memory H2 database here, so no external servers or files needed.
 * It's all kept in RAM for simplicity and speed.
 */
public class AccountRepository {

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
        String sql = "INSERT INTO ACCOUNTS (username, password, role, linked_employee_id)"
                + " VALUES (?, ?, 'EMPLOYEE', ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, linkedEmployeeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add employee account: " + e.getMessage(), e);
        }
    }

    // --- Time to read some data! ---

    /**
     * Authenticates a user by username and password.
     *
     * @return the matching {@link User} object, or {@code null} if credentials are wrong
     */
    public static User authenticate(String username, String password) {
        String sql = "SELECT role, linked_employee_id FROM ACCOUNTS"
                + " WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User.Role role = User.Role.valueOf(rs.getString("role"));
                    String linkedId = rs.getString("linked_employee_id"); // may be NULL
                    return new User(username, password, role, linkedId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Authentication query failed: " + e.getMessage(), e);
        }
        return null;
    }

    // --- Clean up time (Delete) ---

    public static void deleteByEmployeeId(String employeeId) {
        String sql = "DELETE FROM ACCOUNTS WHERE linked_employee_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete account: " + e.getMessage(), e);
        }
    }
}
