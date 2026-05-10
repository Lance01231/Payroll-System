package org.nud.payroll;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * The PayrollService is the brain of the operation!
 *
 * It acts as the middleman between the user interface (the screens you see)
 * and our database repositories. Whenever someone clicks a button to log in,
 * view a payslip, or clock in, this service does the actual work behind the scenes.
 */
public class PayrollService {

    private final EmployeeRepository employeeRepo = new EmployeeRepository();

    // ---------------------------------------------------------------
    // Authentication
    // ---------------------------------------------------------------

    /**
     * Checks if the username and password match any of our records.
     *
     * @param username entered by the user
     * @param password entered by the user
     * @return the authenticated User object, or null if credentials fail
     */
    public java.util.Optional<User> authenticate(String username, String password) {
        return AccountRepository.authenticate(username, password);
    }

    // ---------------------------------------------------------------
    // Employee record management (Admin only)
    // ---------------------------------------------------------------

    /**
     * Registers a new employee in the system and automatically sets up
     * a shiny new user account so they can log in to the portal later!
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

    /** Attendance for the employee's current calendar cutoff window (matches {@code cut_off} 1 or 2). */
    public java.util.List<AttendanceRepository.AttendanceRecord> getAttendanceForCurrentPayPeriod(
            String employeeId, int cutOffPeriod) {
        java.time.LocalDate from = PayrollPeriod.currentPeriodStart(cutOffPeriod);
        java.time.LocalDate to = PayrollPeriod.currentPeriodEnd(cutOffPeriod);
        return AttendanceRepository.getAttendance(employeeId, from, to);
    }

    public void generateMockAttendance(String employeeId) {
        AttendanceRepository.generateMockAttendance(employeeId);
    }

    // ---------------------------------------------------------------
    // Cut-off Submission (Approval Workflow)
    // ---------------------------------------------------------------

    /** @return false when an APPROVED submission must not be overwritten */
    public boolean submitPayroll(String employeeId, double leaveDays, double otHours, double loans) {
        return SubmissionRepository.submitPayroll(employeeId, leaveDays, otHours, loans);
    }

    public SubmissionRepository.PayrollSubmission getSubmission(String employeeId) {
        return SubmissionRepository.getSubmission(employeeId);
    }

    public java.util.List<SubmissionRepository.PayrollSubmission> getPendingSubmissions() {
        return SubmissionRepository.getAllPending();
    }

    public void updateSubmissionStatus(int submissionId, String newStatus) {
        SubmissionRepository.PayrollSubmission sub = SubmissionRepository.findById(submissionId);
        if (sub == null) {
            return;
        }
        if ("APPROVED".equals(newStatus) && !"APPROVED".equals(sub.status)) {
            Employee emp = employeeRepo.findById(sub.employeeId);
            boolean leaveBenefits = emp != null && emp.hasLeaveBenefits();
            try (Connection conn = DatabaseManager.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    EmployeeRepository.applyApprovedPayroll(
                            conn, sub.employeeId, sub.leaveDays, sub.loans, leaveBenefits);
                    SubmissionRepository.updateStatus(conn, submissionId, newStatus);
                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    throw new DatabaseOperationException("Failed to finalize approval.", e);
                }
            } catch (SQLException e) {
                throw new DatabaseOperationException("Failed to finalize approval.", e);
            }
        } else {
            SubmissionRepository.updateStatus(submissionId, newStatus);
        }
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

    /**
     * Computes payslip figures for the employee's current cutoff window using filed leave / OT / loan amounts.
     * Does not enforce APPROVED status — callers decide (employee UI locks until approved; admin preview uses pending rows).
     */
    public Optional<PayslipDetails> computePayslipDetails(
            String employeeId, double leaveDays, double otHours, double loansDeduction) {
        Employee emp = employeeRepo.findById(employeeId);
        if (emp == null) {
            return Optional.empty();
        }
        LocalDate from = PayrollPeriod.currentPeriodStart(emp.getCutOffPeriod());
        LocalDate to = PayrollPeriod.currentPeriodEnd(emp.getCutOffPeriod());
        List<AttendanceRepository.AttendanceRecord> records = AttendanceRepository.getAttendance(employeeId, from, to);
        int count = records.size();
        double[] ins = new double[count];
        double[] outs = new double[count];
        for (int i = 0; i < count; i++) {
            AttendanceRepository.AttendanceRecord r = records.get(i);
            ins[i] = r.timeIn != null ? r.timeIn : 0.0;
            outs[i] = r.timeOut != null ? r.timeOut : 0.0;
        }
        emp.setTimeKeeping(ins, outs);

        double gross = emp.calculateGrossPay(otHours);
        double absD = emp.calculateAbsencesDeduction(leaveDays);
        double utD = emp.calculateUndertimeDeduction();
        double sss = emp.getSSSContribution();
        double ph = emp.getPhilhealthContribution();
        double pi = emp.getPagibigContribution();
        double tax = emp.getWithholdingTax(gross);
        double net = emp.calculateNetPay(leaveDays, otHours, loansDeduction);
        boolean pt = emp instanceof PartTimeEmployee;
        double basicPortion = pt ? emp.getWorkedHours() * emp.getBasicRate() : emp.getBasicRate() / 2.0;
        double workedDays = emp.getWorkedHours() > 0 ? emp.getWorkedHours() / 8.0 : 0;

        return Optional.of(new PayslipDetails(
                emp.getEmployeeNumber(),
                emp.getEmployeeName(),
                from,
                to,
                emp.getBasicRate(),
                workedDays,
                gross,
                basicPortion,
                absD,
                utD,
                sss,
                ph,
                pi,
                tax,
                loansDeduction,
                net,
                emp.getEmployerSSS(),
                emp.getEmployerPhilHealth(),
                emp.getEmployerPagIbig(),
                emp.getEmployerECC(),
                leaveDays,
                otHours,
                pt));
    }
}
