package org.nud.payroll;

public class PartTimeEmployee extends Employee {
    public PartTimeEmployee(String id, String name, double rate, int cutOff) {
        super(id, name, rate, cutOff);
    }

    @Override
    public String getEmployeeType() {
        return "Part-time";
    }

    @Override
    public boolean hasLeaveBenefits() {
        return false;
    }

    @Override
    public double calculateGrossPay() {
        double hourlyRate = getBasicRate();
        double overtimePay = getOvertimeHours() * hourlyRate * 1.25;
        return getWorkedHours() * hourlyRate + overtimePay;
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
    public double getSSSContribution() {
        return 0.0;
    }

    @Override
    public double getPhilhealthContribution() {
        return 0.0;
    }

    @Override
    public double getPagibigContribution() {
        return 0.0;
    }
}
