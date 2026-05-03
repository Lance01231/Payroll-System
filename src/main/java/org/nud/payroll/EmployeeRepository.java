package org.nud.payroll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL-backed repository for Employee master records.
 *
 * <p>All data is stored in the H2 in-memory EMPLOYEES table managed by
 * {@link DatabaseManager}. No file I/O; no external server required.
 *
 * <p>The table schema stores only the data needed to reconstruct an
 * {@link Employee} object (ID, name, type, basic rate, cut-off period).
 * Timekeeping state is kept in memory on the live {@link Employee} object
 * for the duration of a session, which is correct since it is always
 * re-entered each cutoff period.
 */
public class EmployeeRepository {

    // ---------------------------------------------------------------
    // Write
    // ---------------------------------------------------------------

    /**
     * Persists a new Employee record to the database.
     * Used by the Admin dashboard when registering new staff.
     *
     * @param emp the Employee object to save
     */
    public void save(Employee emp) {
        String sql = "INSERT INTO EMPLOYEES"
                + " (employee_id, employee_name, employee_type, basic_rate, cut_off,"
                + " work_schedule, sick_leave, vacation_leave, emergency_leave, loan_balance)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getEmployeeNumber());
            ps.setString(2, emp.getEmployeeName());
            ps.setString(3, emp.getEmployeeType());
            ps.setDouble(4, emp.getBasicRate());
            ps.setInt(5, emp.getCutOffPeriod());
            ps.setString(6, emp.getWorkSchedule());
            ps.setInt(7, emp.getSickLeave());
            ps.setInt(8, emp.getVacationLeave());
            ps.setInt(9, emp.getEmergencyLeave());
            ps.setDouble(10, emp.getLoanBalance());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save employee [" + emp.getEmployeeNumber() + "]: "
                    + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // Read
    // ---------------------------------------------------------------

    /**
     * Retrieves an Employee by their unique employee ID.
     *
     * @param id the employee number to search for (case-insensitive)
     * @return the reconstructed {@link Employee} object, or {@code null} if not found
     */
    public Employee findById(String id) {
        String sql = "SELECT employee_id, employee_name, employee_type, basic_rate, cut_off,"
                + " work_schedule, sick_leave, vacation_leave, emergency_leave, loan_balance"
                + " FROM EMPLOYEES WHERE UPPER(employee_id) = UPPER(?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildEmployee(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find employee [" + id + "]: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns all employees currently stored in the database.
     * Used by the Admin "View All Records" feature.
     *
     * @return unmodifiable list of all employees
     */
    public List<Employee> findAll() {
        String sql = "SELECT employee_id, employee_name, employee_type, basic_rate, cut_off,"
                + " work_schedule, sick_leave, vacation_leave, emergency_leave, loan_balance"
                + " FROM EMPLOYEES ORDER BY employee_id";
        List<Employee> result = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(buildEmployee(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all employees: " + e.getMessage(), e);
        }
        return java.util.Collections.unmodifiableList(result);
    }

    // ---------------------------------------------------------------
    // Delete
    // ---------------------------------------------------------------

    public void delete(String id) {
        String sql = "DELETE FROM EMPLOYEES WHERE UPPER(employee_id) = UPPER(?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete employee [" + id + "]: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------

    /**
     * Reconstructs the correct {@link Employee} subclass from a SQL row.
     * Maps the stored {@code employee_type} string back to the right subclass.
     */
    private static Employee buildEmployee(ResultSet rs) throws SQLException {
        String empId   = rs.getString("employee_id");
        String empName = rs.getString("employee_name");
        String empType = rs.getString("employee_type");
        double rate    = rs.getDouble("basic_rate");
        int    cutOff  = rs.getInt("cut_off");
        String sched   = rs.getString("work_schedule");
        int sl         = rs.getInt("sick_leave");
        int vl         = rs.getInt("vacation_leave");
        int el         = rs.getInt("emergency_leave");
        double loan    = rs.getDouble("loan_balance");

        return switch (empType) {
            case "Regular"      -> new RegularEmployee(empId, empName, rate, cutOff, sched, sl, vl, el, loan);
            case "Probationary" -> new ProbationaryEmployee(empId, empName, rate, cutOff, sched, sl, vl, el, loan);
            case "Contractual"  -> new ContractualEmployee(empId, empName, rate, cutOff, sched, sl, vl, el, loan);
            case "Part-time"    -> new PartTimeEmployee(empId, empName, rate, cutOff, sched, sl, vl, el, loan);
            default -> throw new IllegalStateException("Unknown employee type in DB: " + empType);
        };
    }
}
