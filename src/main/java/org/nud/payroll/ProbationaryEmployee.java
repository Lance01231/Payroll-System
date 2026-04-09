package org.nud.payroll;

public class ProbationaryEmployee extends Employee {
    public ProbationaryEmployee(String id, String name, double rate, int cutOff) {
        super(id, name, rate, cutOff);
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
