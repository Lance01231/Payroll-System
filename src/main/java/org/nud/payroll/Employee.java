package org.nud.payroll;

/**
 * Base class for all employee types.
 *
 * Encapsulates payroll computation logic: timekeeping, gross pay,
 * deductions (SSS, PhilHealth, Pag-IBIG, withholding tax), and net pay.
 *
 * Subclasses override specific behaviors based on employee type rules,
 * such as leave benefit eligibility and contribution applicability.
 */
public abstract class Employee {
    private final String employeeName;
    private final String employeeNumber;
    private final double basicRate;
    private final int cutOffPeriod;
    private double workedHours;
    private double overtimeHours;
    private double undertimeHours;
    private double absentDays;

    public Employee(String employeeNumber, String employeeName, double basicRate, int cutOffPeriod) {
        this.employeeName = employeeName;
        this.employeeNumber = employeeNumber;
        this.basicRate = basicRate;
        this.cutOffPeriod = cutOffPeriod;
    }

    public abstract String getEmployeeType();

    public abstract boolean hasLeaveBenefits();

    /**
     * Processes all time-in/time-out arrays into worked hours, overtime,
     * undertime, and absent days for 15d cutoff period
     *
     * Rules per day (Labor Code Art. 83, 84):
     * - Regular work day is 8 hours; 1-hour meal break is unpaid
     * - Hours beyond 8 in a day count as overtime
     * - Hours below 8 in a day count as undertime or late
     * - If time-in or time-out is zero then the day is counted as an absence
     */
    public void setTimeKeeping(double[] timeIns, double[] timeOuts) {
        this.workedHours = 0.0;
        this.overtimeHours = 0.0;
        this.undertimeHours = 0.0;
        this.absentDays = 0.0;

        for (int i = 0; i < 15; i++) {
            double tin = timeIns[i];
            double tout = timeOuts[i];

            if (tin > 0 && tout > tin) {
                double dailyHours = tout - tin - 1.0; // subtract 1h unpaid meal break
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

    /**
     * Computes the entire net pay; Gross Pay - all deductions
     * Deductions includes; absences, undertime, SSS, PhilHealth, Pag-IBIG,
     * withholding tax, and loans.
     */
    public double calculateNetPay(double leaveDaysUsed, double loans) {
        double grossPay = calculateGrossPay();
        double absencesDed = calculateAbsencesDeduction(leaveDaysUsed);
        double undertimeDed = calculateUndertimeDeduction();

        double sss = getSSSContribution();
        double philhealth = getPhilhealthContribution();
        double pagibig = getPagibigContribution();
        double tax = getWithholdingTax(grossPay);

        double totalDeductions = absencesDed + undertimeDed + sss + philhealth + pagibig + tax + loans;
        return grossPay - totalDeductions;
    }

    /**
     * For monthly rate employees, basic salary is split per semi-monthly cutoff + overtime pay
     * Part-time employees override this with their hourly-rate formula
     */
    public double calculateGrossPay() {
        double basicCutoff = basicRate / 2.0; // semi-monthly cutoff (2 pay periods per month)
        double hourlyRate = getHourlyRate();
        // Labor Code Art. 87 Overtime Work: https://library.laborlaw.ph/p-d-442-labor-code-book-3
        double overtimePay = overtimeHours * hourlyRate * 1.25; // 25% OT
        return basicCutoff + overtimePay;
    }

    /**
     * Employees with leave benefits can offset absent days with leave days used,
     * only the remaining unoffset absences are deducted
     * Employees without leave benefits are deducted for all absent days
     */
    public double calculateAbsencesDeduction(double leaveDaysUsed) {
        double dailyRate = getDailyRate();
        double effectiveAbsent = hasLeaveBenefits() ? Math.max(0, absentDays - leaveDaysUsed) : absentDays;
        return effectiveAbsent * dailyRate;
    }

    // deducts undertime or late hours at hourly rate
    public double calculateUndertimeDeduction() {
        return undertimeHours * getHourlyRate();
    }

    /**
     * Converts monthly basic rate to daily rate
     * Assumes 26 working days per month
     * TODO: "grep source"; no specific DOLE mandate for 26 days(?)
     */
    protected double getDailyRate() {
        return basicRate / 26.0;
    }

    // converts daily rate to hourly rate
    protected double getHourlyRate() {
        return getDailyRate() / 8.0; // 8h of work (Labor Code Art. 83)
    }

    /**
     * Computes employee withholding tax based on semi-monthly gross pay
     *
     *   Taxable Income Range | Rate |  Base Tax | Marginal Rate
     *   <= 10,417            |  0%  |      0.00 | -
     *   10,417 - 16,666      | 15%  |      0.00 | 10,417
     *   16,667 - 33,332      | 20%  |    937.50 | 16,667
     *   33,333 - 83,332      | 25%  |  4,270.70 | 33,333
     *   83,333 - 333,332     | 30%  | 16,770.70 | 83,333
     *   >= 333,333           | 35%  | 91,770.70 | 333,333
     *
     * https://hr-payroll.net/blogs/article/9-bir-withholding-tax-table-for-the-year-2023-onward
     */
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

    /**
     * Computes employee SSS contribution
     *
     * TODO: Replace with actual SSS contribution table which uses monthly Salary
     * Credit (MSC) brackets rather than a flat percentage
     */
    public double getSSSContribution() {
        return basicRate * 0.045;
    }

    /**
     * Computes employee PhilHealth contribution
     *
     * TODO: Replace with actual PhilHealth contribution table which has an income
     * floor 10k and ceiling of 80k, and a 5% total rate split
     * equally between employer and employee (2.5% each)
     */
    public double getPhilhealthContribution() {
        return basicRate * 0.025;
    }

    /**
     * Computes employee Pag-IBIG contribution
     *
     * TODO: Replace with actual Pag-IBIG contribution table
     */
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
