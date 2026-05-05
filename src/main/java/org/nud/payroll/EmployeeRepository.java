package org.nud.payroll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Think of this as the company's filing cabinet for employee records!
 *
 * It talks directly to our MySQL database to save and retrieve employee details
 * like their name, role, and salary. It only saves the core details, while
 * things like daily timekeeping are handled on the fly when we do payroll.
 */
public class EmployeeRepository {

    // ---------------------------------------------------------------
    // Write
    // ---------------------------------------------------------------

    /**
     * Saves a brand new employee into the database.
     * This is called by the Admin when they finish filling out the new hire form!
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
     * Looks up an employee by their unique ID number.
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
     * Grabs a list of every single employee in the company!
     * This powers the Admin's "View All Records" screen.
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
     * A handy little helper method that looks at a row of data from the database
     * and turns it back into the correct type of Employee object!
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
