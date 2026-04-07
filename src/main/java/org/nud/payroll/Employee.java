package org.nud.payroll;

public abstract class Employee {
    private final String employeeNumber;
    private final String employeeName;
    private final double basicRate;
    private final int cutOffPeriod;

    protected double workedHours = 0.0;
    protected double otHours = 0.0;
    protected double absentDays = 0.0;
    protected double undertimeHours = 0.0;

    public Employee(String employeeNumber, String employeeName, double basicRate, int cutOffPeriod) {
        this.employeeNumber = employeeNumber;
        this.employeeName = employeeName;
        this.basicRate = basicRate;
        this.cutOffPeriod = cutOffPeriod;
    }

    public void setTimeKeeping(double[] timeIns, double[] timeOuts) {
        this.workedHours = 0.0;
        this.otHours = 0.0;
        this.absentDays = 0.0;
        this.undertimeHours = 0.0;

        for (int i = 0; i < 15; i++) {
            double tin = timeIns[i];
            double tout = timeOuts[i];

            if (tin > 0 && tout > tin) {
                double dailyHours = tout - tin - 1.0; // 1-hour break
                if (dailyHours < 0) {
                    dailyHours = 0.0;
                }

                workedHours += dailyHours;

                if (dailyHours > 8.0) {
                    otHours += (dailyHours - 8.0);
                } else {
                    undertimeHours += (8.0 - dailyHours);
                }
            } else {
                absentDays += 1.0;
            }
        }
    }

    public abstract boolean hasLeaveBenefits();

    public abstract String getTypeName();

    public double calculateGrossPay() {
        double basicCutoff = basicRate / 2.0;
        double hourlyRate = getHourlyRate();
        double otPay = otHours * hourlyRate * 1.25;
        return basicCutoff + otPay;
    }

    public double calculateAbsencesDeduction(double leaveDaysUsed) {
        double dailyRate = getDailyRate();
        double effectiveAbsent = hasLeaveBenefits() ? Math.max(0, absentDays - leaveDaysUsed) : absentDays;
        return effectiveAbsent * dailyRate;
    }

    public double calculateUndertimeDeduction() {
        return undertimeHours * getHourlyRate();
    }

    protected double getDailyRate() {
        return basicRate / 26.0;
    }

    protected double getHourlyRate() {
        return getDailyRate() / 8.0;
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

    public double getWithholdingTax(double grossForCutoff) {
        if (grossForCutoff <= 10000) {
            return 0.0;
        } else if (grossForCutoff <= 20000) {
            return (grossForCutoff - 10000) * 0.10;
        } else {
            return 1000 + (grossForCutoff - 20000) * 0.15;
        }
    }

    // Getters
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getEmployeeName() {
        return employeeName;
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

    public double getOtHours() {
        return otHours;
    }

    public double getAbsentDays() {
        return absentDays;
    }

    public double getUndertimeHours() {
        return undertimeHours;
    }
}
