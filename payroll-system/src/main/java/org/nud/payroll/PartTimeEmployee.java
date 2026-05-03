package org.nud.payroll;

/**
 * Paid hourly with no leave benefits or government contributions...
 */
public class PartTimeEmployee extends Employee {
    public PartTimeEmployee(String id, String name, double rate, int cutOff) {
        super(id, name, rate, cutOff);
    }

    @Override
    public String employeeType() {
        return "Part-time";
    }

    @Override
    public boolean hasLeaveBenefits() {
        return false;
    }

    // hourly rate for part-time is the basic rate, no monthly to hourly conversion
    @Override
    public double getOvertimePay() {
        return getOvertimeHours() * getBasicRate() * 1.25;
    }

    // gross pay for part-time is computed from actual hours worked at hourly rate,
    // plus overtime at 1.25x (labor code art. 87) there is no semi-monthly salary split
    @Override
    protected double calculateGrossPay() {
        double hourlyRate = getBasicRate();
        double overtimePay = getOvertimeHours() * hourlyRate * 1.25;
        return getWorkedHours() * hourlyRate + overtimePay;
    }

    // "no work, no pay"
    @Override
    protected double calculateAbsencesDeduction(double leaveDaysUsed) {
        return 0.0;
    }

    // no undertime deduction
    @Override
    protected double calculateUndertimeDeduction() {
        return 0.0;
    }

    // part-time employees are not covered by SSS
    @Override
    protected double calculateSSSContribution() {
        return 0.0;
    }

    // part-time employees are not covered by PhilHealth
    @Override
    protected double calculatePhilhealthContribution() {
        return 0.0;
    }

    // part-time employees are not covered by Pag-IBIG
    @Override
    protected double calculatePagibigContribution() {
        return 0.0;
    }
}
