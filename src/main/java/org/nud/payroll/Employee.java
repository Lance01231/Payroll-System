package org.nud.payroll;

public abstract class Employee {
    private final String employeeName;
    private final String employeeNumber;
    private final double basicRate;
    private final int cutOffPeriod;
    protected double workedHours;
    protected double overtimeHours;
    protected double undertimeHours;
    protected double absentDays;

    public Employee(String employeeNumber, String employeeName, double basicRate, int cutOffPeriod) {
        this.employeeName = employeeName;
        this.employeeNumber = employeeNumber;
        this.basicRate = basicRate;
        this.cutOffPeriod = cutOffPeriod;
    }

    public abstract String getEmployeeType();

    public abstract boolean hasLeaveBenefits();

    public void setTimeKeeping(double[] timeIns, double[] timeOuts) {
        this.workedHours = 0.0;
        this.overtimeHours = 0.0;
        this.undertimeHours = 0.0;
        this.absentDays = 0.0;

        for (int i = 0; i < 15; i++) {
            double tin = timeIns[i];
            double tout = timeOuts[i];

            if (tin > 0 && tout > tin) {
                double dailyHours = tout - tin - 1.0; // 1h break
                if (dailyHours < 0) {
                    dailyHours = 0.0;
                }

                workedHours += dailyHours;

                if (dailyHours > 8.0) {
                    overtimeHours += (dailyHours - 8.0);
                } else {
                    undertimeHours += (8.0 - dailyHours);
                }
            } else {
                absentDays += 1.0;
            }
        }
    }

    public double calculateGrossPay() {
        double basicCutoff = basicRate / 2.0;
        double hourlyRate = getHourlyRate();
        double overtimePay = overtimeHours * hourlyRate * 1.25;
        return basicCutoff + overtimePay;
    }

    public double calculateAbsencesDeduction(double leaveDaysUsed) {
        double dailyRate = getDailyRate();
        double effectiveAbsent = hasLeaveBenefits() ? Math.max(0, absentDays - leaveDaysUsed) : absentDays;
        return effectiveAbsent * dailyRate;
    }

    public double calculateUndertimeDeduction() {
        return undertimeHours * getHourlyRate();
    }

    // helpers
    protected double getDailyRate() {
        return basicRate / 26.0;
    }

    protected double getHourlyRate() {
        return getDailyRate() / 8.0;
    }

    // getters
    public double getWithholdingTax(double grossForCutoff) {
        if (grossForCutoff <= 10000) {
            return 0.0;
        } else if (grossForCutoff <= 20000) {
            return (grossForCutoff - 10000) * 0.10;
        } else {
            return 1000 + (grossForCutoff - 20000) * 0.15;
        }
    }

    public double getSSSContribution() {
        return basicRate * 0.045;
    }

    public double getPhilhealthContribution() {
        return basicRate * 0.025;
    }

    public double getPagibigContribution() {
        return basicRate * 0.02;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public double getBasicRate() {
        return basicRate;
    }

    public int getCutOffPeriod() {
        return cutOffPeriod;
    }

    public double getWorkedHours() {
        return workedHours;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public double getUndertimeHours() {
        return undertimeHours;
    }

    public double getAbsentDays() {
        return absentDays;
    }
}
