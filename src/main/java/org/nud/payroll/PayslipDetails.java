package org.nud.payroll;

import java.time.LocalDate;

/** Immutable payslip figures computed from attendance + submission inputs for one cutoff window. */
public record PayslipDetails(
        String employeeId,
        String employeeName,
        LocalDate periodStart,
        LocalDate periodEnd,
        double basicRate,
        double workedDaysDisplay,
        double grossPay,
        /** Semi-monthly basic portion used for OT display line (rate × half-month or part-time worked). */
        double basicPortionBeforeOt,
        double absencesDeduction,
        double undertimeDeduction,
        double sssEmployee,
        double philhealthEmployee,
        double pagibigEmployee,
        double withholdingTax,
        double loanDeduction,
        double netPay,
        double employerSss,
        double employerPhilhealth,
        double employerPagibig,
        double employerEcc,
        double submissionLeaveDays,
        double submissionOtHours,
        boolean partTime) {}
