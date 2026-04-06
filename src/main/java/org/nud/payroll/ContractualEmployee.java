package org.nud.payroll;

public class ContractualEmployee extends Employee {
    public ContractualEmployee(String id, String name, double rate, int cutOff) {
        super(id, name, rate, cutOff);
    }

    @Override
    public boolean hasLeaveBenefits() { return false; }

    @Override
    public String getTypeName() { return "Contractual"; }
}
