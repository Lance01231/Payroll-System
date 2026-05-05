package org.nud.payroll;

/**
 * Represents staff working under a specific, limited-time contract.
 *
 * They get a steady monthly salary, but unlike regular employees,
 * they don't accumulate paid leave benefits.
 */
public class ContractualEmployee extends Employee {
    public ContractualEmployee(
            String id, String name, double rate, int cutOff, String schedule, int sl, int vl, int el, double loan) {
        super(id, name, rate, cutOff, schedule, sl, vl, el, loan);
    }

    @Override
    public String getEmployeeType() {
        return "Contractual";
    }

    @Override
    public boolean hasLeaveBenefits() {
        return false;
    }
}
