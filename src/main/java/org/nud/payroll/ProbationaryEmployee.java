package org.nud.payroll;

/**
 * Represents new hires who are still in their probationary period.
 *
 * They are treated very similarly to regular employees when it comes to payroll
 * (monthly salary and leave benefits), but their contract status is different!
 */
public class ProbationaryEmployee extends Employee {
    public ProbationaryEmployee(
            String id, String name, double rate, int cutOff, String schedule, int sl, int vl, int el, double loan) {
        super(id, name, rate, cutOff, schedule, sl, vl, el, loan);
    }

    @Override
    public String getEmployeeType() {
        return "Probationary";
    }

    @Override
    public boolean hasLeaveBenefits() {
        return true;
    }
}
