package org.nud.payroll;

/**
 * Monthly rate with no leave benefits; inherits standard payroll computations
 * */
public class ContractualEmployee extends Employee {
    public ContractualEmployee(String id, String name, double rate, int cutOff) {
        super(id, name, rate, cutOff);
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
