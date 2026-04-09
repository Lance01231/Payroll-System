package org.nud.payroll;

public class RegularEmployee extends Employee {
    public RegularEmployee(String id, String name, double rate, int cutOff) {
        super(id, name, rate, cutOff);
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
