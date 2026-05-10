package org.nud.payroll;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceRepository {

    public static class AttendanceRecord {
        public LocalDate recordDate;
        public Double timeIn;
        public Double timeOut;

        public AttendanceRecord(LocalDate recordDate, Double timeIn, Double timeOut) {
            this.recordDate = recordDate;
            this.timeIn = timeIn;
            this.timeOut = timeOut;
        }
    }

    /**
     * Logs the exact time an employee arrives at work.
     * If they try to clock in twice on the same day, we just ignore the second attempt!
     */
    public static void clockIn(String employeeId, LocalDate date, double timeIn) {
        // Check first, then insert — avoids duplicate clock-in on the same day.
        String checkSql = "SELECT 1 FROM ATTENDANCE WHERE employee_id = ? AND record_date = ?";
        String insertSql = "INSERT INTO ATTENDANCE (employee_id, record_date, time_in) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setString(1, employeeId);
                check.setDate(2, Date.valueOf(date));
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        return; // Already clocked in today
                    }
                }
            }
            try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                insert.setString(1, employeeId);
                insert.setDate(2, Date.valueOf(date));
                insert.setDouble(3, timeIn);
                insert.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clock in: " + e.getMessage(), e);
        }
    }

    /**
     * Logs the time an employee leaves for the day.
     * It updates their existing clock-in record. If they forgot to clock in this morning,
     * we still save their clock-out time so the HR team can investigate later!
     */
    public static void clockOut(String employeeId, LocalDate date, double timeOut) {
        String updateSql = "UPDATE ATTENDANCE SET time_out = ? WHERE employee_id = ? AND record_date = ?";
        String insertSql = "INSERT INTO ATTENDANCE (employee_id, record_date, time_out) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                update.setDouble(1, timeOut);
                update.setString(2, employeeId);
                update.setDate(3, Date.valueOf(date));
                int rows = update.executeUpdate();

                if (rows == 0) { // If they forgot to clock in but are clocking out
                    try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                        insert.setString(1, employeeId);
                        insert.setDate(2, Date.valueOf(date));
                        insert.setDouble(3, timeOut);
                        insert.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clock out: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches all attendance records for an employee (legacy / diagnostics).
     */
    public static List<AttendanceRecord> getAttendance(String employeeId) {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql =
                "SELECT record_date, time_in, time_out FROM ATTENDANCE WHERE employee_id = ? ORDER BY record_date ASC";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("record_date").toLocalDate();
                    Double in = rs.getObject("time_in") != null ? rs.getDouble("time_in") : null;
                    Double out = rs.getObject("time_out") != null ? rs.getDouble("time_out") : null;
                    records.add(new AttendanceRecord(date, in, out));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch attendance: " + e.getMessage(), e);
        }
        return records;
    }

    /**
     * Attendance rows within {@code from}–{@code to} inclusive, ordered by date (for payslip / cutoff views).
     */
    public static List<AttendanceRecord> getAttendance(String employeeId, LocalDate from, LocalDate to) {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = "SELECT record_date, time_in, time_out FROM ATTENDANCE WHERE employee_id = ?"
                + " AND record_date >= ? AND record_date <= ? ORDER BY record_date ASC";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("record_date").toLocalDate();
                    Double in = rs.getObject("time_in") != null ? rs.getDouble("time_in") : null;
                    Double out = rs.getObject("time_out") != null ? rs.getDouble("time_out") : null;
                    records.add(new AttendanceRecord(date, in, out));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch attendance: " + e.getMessage(), e);
        }
        return records;
    }

    /**
     * FOR DEBUGGING / PRESENTATION: Injects 15 days of perfect attendance (8AM to 5PM).
     */
    public static void generateMockAttendance(String employeeId) {
        String sql = "INSERT INTO ATTENDANCE (employee_id, record_date, time_in, time_out) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE time_in = VALUES(time_in), time_out = VALUES(time_out)";
        LocalDate start = LocalDate.now().minusDays(14); // 15 days total including today

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < 15; i++) {
                ps.setString(1, employeeId);
                ps.setDate(2, Date.valueOf(start.plusDays(i)));
                ps.setDouble(3, 8.0); // 8:00 AM
                ps.setDouble(4, 17.0); // 5:00 PM
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate mock attendance: " + e.getMessage(), e);
        }
    }
}
