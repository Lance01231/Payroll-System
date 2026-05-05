package org.nud.payroll;

/**
 * Monthly rate with leave benefits; inherits standard payroll computations
 */
public class RegularEmployee extends Employee {
    public RegularEmployee(
            String id, String name, double rate, int cutOff, String schedule, int sl, int vl, int el, double loan) {
        super(id, name, rate, cutOff, schedule, sl, vl, el, loan);
    }

    @Override
    public String getEmployeeType() {
        return "Regular";
    }

    @Override
    public boolean hasLeaveBenefits() {
        return true;
    }
}
