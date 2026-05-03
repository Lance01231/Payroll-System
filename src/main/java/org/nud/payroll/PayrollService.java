package org.nud.payroll;

/**
 * Service layer — the "integration glue" between the UI and the backend.
 *
 * Connects the dashboard UI to AccountRepository and EmployeeRepository.
 * All business decisions (who can log in, how net pay is calculated) are
 * delegated here so that the UI layer stays thin.
 */
public class PayrollService {

    private final EmployeeRepository employeeRepo = new EmployeeRepository();

    // ---------------------------------------------------------------
    // Authentication
    // ---------------------------------------------------------------

    /**
     * Authenticates a login attempt.
     *
     * @param username entered by the user
     * @param password entered by the user
     * @return the authenticated User object, or null if credentials fail
     */
    public User authenticate(String username, String password) {
        return AccountRepository.authenticate(username, password);
    }

    // ---------------------------------------------------------------
    // Employee record management (Admin only)
    // ---------------------------------------------------------------

    /**
     * Persists a new Employee record and creates a linked EMPLOYEE
     * account so the employee can log in later.
     *
     * @param emp      the constructed Employee object to save
     * @param username login username for the employee account
     * @param password login password for the employee account
     */
    public void registerEmployee(Employee emp, String username, String password) {
        employeeRepo.save(emp);
        AccountRepository.addEmployeeAccount(username, password, emp.getEmployeeNumber());
    }

    /**
     * Returns all employees in the repository for the Admin "View All" screen.
     */
    public java.util.List<Employee> getAllEmployees() {
        return employeeRepo.findAll();
    }

    /**
     * Deletes an employee and their associated account.
     */
    public void deleteEmployee(String employeeId) {
        AccountRepository.deleteByEmployeeId(employeeId);
        employeeRepo.delete(employeeId);
    }

    // ---------------------------------------------------------------
    // Real-Time Attendance
    // ---------------------------------------------------------------

    public void clockIn(String employeeId, java.time.LocalDate date, double timeIn) {
        AttendanceRepository.clockIn(employeeId, date, timeIn);
    }

    public void clockOut(String employeeId, java.time.LocalDate date, double timeOut) {
        AttendanceRepository.clockOut(employeeId, date, timeOut);
    }

    public java.util.List<AttendanceRepository.AttendanceRecord> getAttendance(String employeeId) {
        return AttendanceRepository.getAttendance(employeeId);
    }

    public void generateMockAttendance(String employeeId) {
        AttendanceRepository.generateMockAttendance(employeeId);
    }

    // ---------------------------------------------------------------
    // Cut-off Submission (Approval Workflow)
    // ---------------------------------------------------------------

    public void submitPayroll(String employeeId, double leaveDays, double otHours, double loans) {
        SubmissionRepository.submitPayroll(employeeId, leaveDays, otHours, loans);
    }

    public SubmissionRepository.PayrollSubmission getSubmission(String employeeId) {
        return SubmissionRepository.getSubmission(employeeId);
    }

    public java.util.List<SubmissionRepository.PayrollSubmission> getPendingSubmissions() {
        return SubmissionRepository.getAllPending();
    }

    public void updateSubmissionStatus(int submissionId, String status) {
        SubmissionRepository.updateStatus(submissionId, status);
    }

    // ---------------------------------------------------------------
    // Payroll calculation (Employee dashboard)
    // ---------------------------------------------------------------

    /**
     * Looks up the employee by ID and computes their net pay.
     *
     * @param employeeId    the ID of the employee
     * @param leaveDaysUsed number of leave days applied this cutoff
     * @param loans         total loan deduction for this cutoff
     * @return computed net pay, or 0.0 if the employee is not found
     */
    public double getNetPay(String employeeId, double leaveDaysUsed, double filedOtHours, double loans) {
        Employee emp = employeeRepo.findById(employeeId);
        if (emp == null) {
            return 0.0;
        }
        return emp.calculateNetPay(leaveDaysUsed, filedOtHours, loans);
    }

    /**
     * Retrieves an Employee object by ID (used by the Employee dashboard
     * to display the full payslip via the Employee's own methods).
     *
     * @param employeeId the ID to search for
     * @return the Employee, or null if not found
     */
    public Employee findEmployee(String employeeId) {
        return employeeRepo.findById(employeeId);
    }
}