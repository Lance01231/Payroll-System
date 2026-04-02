package com.payroll;

public class PayrollCalculator {
    public static double calculateNetPay(Employee emp, double leaveDaysUsed, double loans) {
        double grossPay = emp.calculateGrossPay();
        double absencesDed = emp.calculateAbsencesDeduction(leaveDaysUsed);
        double undertimeDed = emp.calculateUndertimeDeduction();

        double sss = emp.getSSSContribution();
        double philhealth = emp.getPhilhealthContribution();
        double pagibig = emp.getPagibigContribution();
        double tax = emp.getWithholdingTax(grossPay);

        double totalDeductions = absencesDed + undertimeDed + sss + philhealth + pagibig + tax + loans;
        return grossPay - totalDeductions;
    }
}