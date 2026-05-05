package org.nud.payroll;

/**
 * Represents our flexible part-time staff!
 * 
 * They are paid purely based on the hours they work. Since they are part-time, 
 * they don't get the same leave benefits or government contributions as full-time staff.
 */
public class PartTimeEmployee extends Employee {
    public PartTimeEmployee(String id, String name, double rate, int cutOff,
                            String schedule, int sl, int vl, int el, double loan) {
        super(id, name, rate, cutOff, schedule, sl, vl, el, loan);
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
    public double calculateGrossPay(double filedOtHours) {
        double hourlyRate = getBasicRate();
        double overtimePay = filedOtHours * hourlyRate * 1.25;
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
