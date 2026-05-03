package org.nud.payroll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SubmissionRepository {

    public static class PayrollSubmission {
        public int id;
        public String employeeId;
        public double leaveDays;
        public double otHours;
        public double loans;
        public String status; // PENDING, APPROVED, REJECTED

        public PayrollSubmission(int id, String employeeId, double leaveDays, double otHours, double loans, String status) {
            this.id = id;
            this.employeeId = employeeId;
            this.leaveDays = leaveDays;
            this.otHours = otHours;
            this.loans = loans;
            this.status = status;
        }
    }

    /**
     * Submits or updates the payroll request for the given employee.
     * Keeps it in PENDING state. If there's an existing one, overrides it if it was rejected or pending.
     */
    public static void submitPayroll(String employeeId, double leaveDays, double otHours, double loans) {
        // Find existing to see if we should update or insert
        String checkSql = "SELECT id FROM SUBMISSIONS WHERE employee_id = ?";
        String updateSql = "UPDATE SUBMISSIONS SET leave_days = ?, ot_hours = ?, loans = ?, status = 'PENDING' WHERE employee_id = ?";
        String insertSql = "INSERT INTO SUBMISSIONS (employee_id, leave_days, ot_hours, loans, status) VALUES (?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseManager.getConnection()) {
            boolean exists = false;
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setString(1, employeeId);
                try (ResultSet rs = check.executeQuery()) {
                    exists = rs.next();
                }
            }
            if (exists) {
                try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                    update.setDouble(1, leaveDays);
                    update.setDouble(2, otHours);
                    update.setDouble(3, loans);
                    update.setString(4, employeeId);
                    update.executeUpdate();
                }
            } else {
                try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                    insert.setString(1, employeeId);
                    insert.setDouble(2, leaveDays);
                    insert.setDouble(3, otHours);
                    insert.setDouble(4, loans);
                    insert.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to submit payroll: " + e.getMessage(), e);
        }
    }

    public static PayrollSubmission getSubmission(String employeeId) {
        String sql = "SELECT id, leave_days, ot_hours, loans, status FROM SUBMISSIONS WHERE employee_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PayrollSubmission(
                            rs.getInt("id"),
                            employeeId,
                            rs.getDouble("leave_days"),
                            rs.getDouble("ot_hours"),
                            rs.getDouble("loans"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch submission: " + e.getMessage(), e);
        }
        return null;
    }

    public static List<PayrollSubmission> getAllPending() {
        List<PayrollSubmission> list = new ArrayList<>();
        String sql = "SELECT id, employee_id, leave_days, ot_hours, loans, status FROM SUBMISSIONS WHERE status = 'PENDING'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new PayrollSubmission(
                        rs.getInt("id"),
                        rs.getString("employee_id"),
                        rs.getDouble("leave_days"),
                        rs.getDouble("ot_hours"),
                        rs.getDouble("loans"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch pending submissions: " + e.getMessage(), e);
        }
        return list;
    }

    public static void updateStatus(int id, String newStatus) {
        String sql = "UPDATE SUBMISSIONS SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update status: " + e.getMessage(), e);
        }
    }
}
