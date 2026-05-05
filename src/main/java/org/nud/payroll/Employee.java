package org.nud.payroll;

/**
 * The base template for everyone who works at the company!
 *
 * This class handles all the heavy lifting for computing payroll: figuring out
 * how much to pay, taking out deductions (like taxes and SSS), and arriving at
 * the final take-home pay.
 *
 * Specific types of employees (like Regular or Part-time) will build on top of
 * this to add their own special rules.
 */
public abstract class Employee {
    private final String employeeName;
    private final String employeeNumber;
    private final double basicRate;
    private final int cutOffPeriod;
    private final String workSchedule;
    private final int sickLeave;
    private final int vacationLeave;
    private final int emergencyLeave;
    private final double loanBalance;
    
    private double workedHours;
    private double undertimeHours;
    private double absentDays;

    public Employee(String employeeNumber, String employeeName, double basicRate, int cutOffPeriod,
                    String workSchedule, int sickLeave, int vacationLeave, int emergencyLeave, double loanBalance) {
        this.employeeName = employeeName;
        this.employeeNumber = employeeNumber;
        this.basicRate = basicRate;
        this.cutOffPeriod = cutOffPeriod;
        this.workSchedule = workSchedule != null ? workSchedule : "08:00 - 17:00";
        this.sickLeave = sickLeave;
        this.vacationLeave = vacationLeave;
        this.emergencyLeave = emergencyLeave;
        this.loanBalance = loanBalance;
    }

    public abstract String getEmployeeType();

    public abstract boolean hasLeaveBenefits();

    /**
     * Takes a look at all the clock-in and clock-out times to figure out
     * how many hours were worked, how many were missed, and if the employee
     * was absent for the whole day.
     */
    public void setTimeKeeping(double[] timeIns, double[] timeOuts) {
        this.workedHours = 0.0;
        this.undertimeHours = 0.0;
        this.absentDays = 0.0;

        for (int i = 0; i < timeIns.length; i++) {
            double tin = timeIns[i];
            double tout = timeOuts[i];

            if (tin > 0 && tout > tin) {
                double dailyHours = tout - tin - 1.0; // subtract 1h unpaid meal break
                if (dailyHours < 0) {
                    dailyHours = 0.0;
                }

                workedHours += dailyHours;

                if (dailyHours < 8.0) {
                    undertimeHours += (8.0 - dailyHours);
                }
                // Removed automatic OT calculation to fulfill strict requirement "Filing of OT".
            } else {
                absentDays += 1.0;
            }
        }
    }

    /**
     * The moment of truth! This calculates the final take-home pay by taking the 
     * gross pay and subtracting all the nasty deductions (taxes, loans, absences, etc.).
     */
    public double calculateNetPay(double leaveDaysUsed, double filedOtHours, double loansToDeduct) {
        double grossPay = calculateGrossPay(filedOtHours);
        double absencesDed = calculateAbsencesDeduction(leaveDaysUsed);
        double undertimeDed = calculateUndertimeDeduction();

        double sss = getSSSContribution();
        double philhealth = getPhilhealthContribution();
        double pagibig = getPagibigContribution();
        double tax = getWithholdingTax(grossPay);

        double totalDeductions = absencesDed + undertimeDed + sss + philhealth + pagibig + tax + loansToDeduct;
        return grossPay - totalDeductions;
    }

    /**
     * Calculates the gross pay for the 15-day cutoff period, which includes 
     * half of their monthly salary plus any approved overtime they worked.
     */
    public double calculateGrossPay(double filedOtHours) {
        double basicCutoff = basicRate / 2.0; 
        double hourlyRate = getHourlyRate();
        double overtimePay = filedOtHours * hourlyRate * 1.25; 
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

    /**
     * Updated per whiteboard requirement: [BASIC PAY / 4.33 (Weeks) / 5 (days)]
     *
     */
    protected double getDailyRate() {
        return (basicRate / 4.33) / 5.0;
    }

    // converts daily rate to hourly rate
    protected double getHourlyRate() {
        return getDailyRate() / 8.0; 
    }

    public double getWithholdingTax(double grossForCutoff) {
        if (grossForCutoff <= 10417) {
            return 0.0;
        } else if (grossForCutoff <= 16666) {
            return (grossForCutoff - 10417) * 0.15;
        } else if (grossForCutoff <= 33332) {
            return 937.50 + (grossForCutoff - 16667) * 0.20;
        } else if (grossForCutoff <= 83332) {
            return 4270.70 + (grossForCutoff - 33333) * 0.25;
        } else if (grossForCutoff <= 333332) {
            return 16770.70 + (grossForCutoff - 83333) * 0.30;
        } else {
            return 91770.70 + (grossForCutoff - 333333) * 0.35;
        }
    }

    public double getSSSContribution() {
        double msc;
        if (basicRate < 5000) { msc = 5000; } 
        else if (basicRate > 35000) { msc = 35000; } 
        else { msc = Math.floor((basicRate - 250) / 500) * 500 + 500; }

        double regularSS = Math.min(msc, 20000) * 0.05; 
        double mpf = Math.max(0, msc - 20000) * 0.05; 
        return regularSS + mpf;
    }

    public double getPhilhealthContribution() {
        double mbs = Math.max(10000.0, Math.min(100000.0, basicRate));
        return mbs * 0.05 * 0.50;
    }

    public double getPagibigContribution() {
        double mfs = Math.min(basicRate, 10000.0);
        if (mfs <= 1500.0) {
            return mfs * 0.01;
        } else {
            return mfs * 0.02;
        }
    }

    public String getEmployeeName() { return employeeName; }
    public String getEmployeeNumber() { return employeeNumber; }
    public double getBasicRate() { return basicRate; }
    public int getCutOffPeriod() { return cutOffPeriod; }
    public String getWorkSchedule() { return workSchedule; }
    public int getSickLeave() { return sickLeave; }
    public int getVacationLeave() { return vacationLeave; }
    public int getEmergencyLeave() { return emergencyLeave; }
    public double getLoanBalance() { return loanBalance; }
    public double getWorkedHours() { return workedHours; }
    public double getUndertimeHours() { return undertimeHours; }
    public double getAbsentDays() { return absentDays; }
    
    // Employer Contributions
    public double getEmployerSSS() { return getSSSContribution() * 2.0; } // Roughly double
    public double getEmployerPhilHealth() { return getPhilhealthContribution(); } // Employer match 1:1
    public double getEmployerPagIbig() { return getPagibigContribution(); } // Employer match 1:1
    public double getEmployerECC() { return 10.0; } // Employee Compensation Commission (Fixed)
}