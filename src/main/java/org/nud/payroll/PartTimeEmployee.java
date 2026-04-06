package org.nud.payroll;

public class PartTimeEmployee extends Employee {
    public PartTimeEmployee(String id, String name, double rate, int cutOff) {
        super(id, name, rate, cutOff);
    }

    @Override
    public boolean hasLeaveBenefits() { return false; }

    @Override
    public String getTypeName() { return "Part-time"; }

    @Override
    public double calculateGrossPay() {
        double hourlyRate = getBasicRate();
        double otPay = getOtHours() * hourlyRate * 1.25;
        return getWorkedHours() * hourlyRate + otPay;
    }

    @Override
    public double calculateAbsencesDeduction(double leaveDaysUsed) {
        return 0.0;
    }

    @Override
    public double calculateUndertimeDeduction() {
        return 0.0;
    }

    @Override
    public double getSSSContribution() { return 0.0; }
    @Override
    public double getPhilhealthContribution() { return 0.0; }
    @Override
    public double getPagibigContribution() { return 0.0; }
}
