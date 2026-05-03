package org.nud.payroll;

/**
 * Monthly rate with leave benefits (same as regular employee); inherits standard payroll computations
 * */
public class ProbationaryEmployee extends Employee {
    public ProbationaryEmployee(String id, String name, double rate, int cutOff) {
        super(id, name, rate, cutOff);
    }

    @Override
    public String employeeType() {
        return "Probationary";
    }

    @Override
    public boolean hasLeaveBenefits() {
        return true;
    }
}
