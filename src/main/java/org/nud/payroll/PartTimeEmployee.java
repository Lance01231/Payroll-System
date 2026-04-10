package org.nud.payroll;

/**
 * Paid hourly with no leave benefits or government contributions...
 */
public class PartTimeEmployee extends Employee {
    public PartTimeEmployee(String id, String name, double rate, int cutOff) {
        super(id, name, rate, cutOff);
    }

    @Override
    public String getEmployeeType() {
        return "Part-time";
    }

    @Override
    public boolean hasLeaveBenefits() {
        return false;
    }

    // gross pay for part-time is computed from actual hours worked at hourly rate,
    // plus overtime at 1.25x (labor code art. 87) there is no semi-monthly salary split
    @Override
    public double calculateGrossPay() {
        double hourlyRate = getBasicRate();
        double overtimePay = getOvertimeHours() * hourlyRate * 1.25;
        return getWorkedHours() * hourlyRate + overtimePay;
    }

    // "no work, no pay"
    @Override
    public double calculateAbsencesDeduction(double leaveDaysUsed) {
        return 0.0;
    }

    // no undertime deduction
    @Override
    public double calculateUndertimeDeduction() {
        return 0.0;
    }

    // part-time employees are not covered by SSS
    @Override
    public double getSSSContribution() {
        return 0.0;
    }

    // part-time employees are not covered by PhilHealth
    @Override
    public double getPhilhealthContribution() {
        return 0.0;
    }

    // part-time employees are not covered by Pag-IBIG
    @Override
    public double getPagibigContribution() {
        return 0.0;
    }
}
