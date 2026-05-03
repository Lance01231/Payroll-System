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

    public abstract String employeeType();

    public abstract boolean hasLeaveBenefits();

    /**
     * Validates time pairs and processes them into worked hours, overtime,
     * undertime, and absent days for 15 day period
     *
     * Computation rules per day (Labor Code Art. 83, 84):
     * - Hours before 8am are ignored even if employee arrived early
     * - Time-in at or past 3pm is absent for that day
     * - 1 hour unpaid meal break is only deducted if employee arrived before 1pm
     * - Overtime only starts after completing 8 regular hours regardless of time
     * - Hours below 8h in a day count as undertime
     */
    public void setTimeKeeping(double[] timeIns, double[] timeOuts) {
        this.workedHours = 0.0;
        this.overtimeHours = 0.0;
        this.undertimeHours = 0.0;
        this.absentDays = 0.0;

        for (int i = 0; i < 15; i++) {
            double timeIn = timeIns[i];
            double timeOut = timeOuts[i];

            // if both are 0, count that day as absent and skip processing that day
            if (timeIn == 0.0 && timeOut == 0.0) {
                absentDays += 1.0;
                continue;
            }

            // hours before 8am don't count as worked hours, it doesn't matter if an employee arrive early
            double effectiveTimeIn = Math.max(timeIn, 8.00);

            // time-in past 3pm is counted as absent for that day (3pm is too late to start a shift)
            if (effectiveTimeIn >= 15.0) {
                absentDays += 1.0;
                continue;
            }

            // 1h meal break is only deducted if shift started before 1pm as meal break spans around 12pm-1pm
            double mealBreak = (effectiveTimeIn <= 12.0 && timeOut >= 13.0) ? 1.0 : 0.0;
            double dailyHours = Math.max(0, timeOut - effectiveTimeIn - mealBreak);

            workedHours += dailyHours;

            // only consider it's overtime after completing 8h of work (Labor Code Art. 83)
            if (dailyHours > 8.00) {
                overtimeHours += (dailyHours - 8.00);
            } else {
                undertimeHours += (8.00 - dailyHours);
            }
        }
    }

    /**
     * Computes the entire net pay; Gross Pay - all deductions
     * Deductions includes; absences, undertime, SSS, PhilHealth, Pag-IBIG,
     * withholding tax, and loans.
     */
    protected double calculateNetPay(double leaveDaysUsed, double loans) {
        double absencesDeduction = calculateAbsencesDeduction(leaveDaysUsed);
        double undertimeDeduction = calculateUndertimeDeduction();
        double grossPay = calculateGrossPay();
        double tax = calculateWithholdingTax();
        double sss = calculateSSSContribution();
        double philhealth = calculatePhilhealthContribution();
        double pagibig = calculatePagibigContribution();
        double totalDeductions = absencesDeduction + undertimeDeduction + sss + philhealth + pagibig + tax + loans;

        // employees are not suppose to receive negative paycheck...
        return Math.max(0, grossPay - totalDeductions);
    }

    /**
     * For monthly rate employees, basic salary is split per semi-monthly cutoff + overtime pay
     * Part-time employees override this with their hourly-rate formula
     */
    protected double calculateGrossPay() {
        double basicCutoff = basicRate / 2.0; // semi-monthly cutoff (2 pay periods per month)
        double hourlyRate = calculateHourlyRate();
        // Labor Code Art. 87 Overtime Work: https://library.laborlaw.ph/p-d-442-labor-code-book-3
        double overtimePay = overtimeHours * hourlyRate * 1.25; // 25% OT
        return basicCutoff + overtimePay;
    }

    /**
     * Eligible employees (regular/probationary) have leave benefits,
     * their leave days can "cover" their absences, but only up to 5 days.
     *
     * Any filed leaves beyond 5 are counted as an absent if and only if
     * there are actual absences to be deducted. The employee is only penalized
     * for actual absences not covered by paid leave.
     *
     * Ineligible employees (contractual/part-time) do not have leave benefits.
     * All absences are simply deducted regardless of leaves filed.
     */
    protected double calculateAbsencesDeduction(double leaveDaysUsed) {
        double dailyRate = calculateDailyRate();
        double effectiveAbsent =
                hasLeaveBenefits() ? Math.max(0, absentDays - Math.min(leaveDaysUsed, 5.0)) : absentDays;
        return effectiveAbsent * dailyRate;
    }

    // deducts undertime or late hours at hourly rate
    protected double calculateUndertimeDeduction() {
        return undertimeHours * calculateHourlyRate();
    }

    /**
     * Converts monthly basic rate to daily rate based on 5-day workweek
     *
     * 5-day workweek (Mon–Fri), two rest days per week:
     *  365 days - 104 rest days = 261 working days
     *  261 / 12 months = 21.75
     *
     * Other factors:
     *   Work Schedule    | Annual Days | Monthly Divisor
     *   Paid monthly     |     365     |      30.42
     *   6 days (Mon-Sat) |     313     |      26.08
     *   5 days (Mon-Fri) |     261     |      21.75
     */
    private double calculateDailyRate() {
        return basicRate / 21.75;
    }

    // converts daily rate to hourly rate
    private double calculateHourlyRate() {
        return calculateDailyRate() / 8.0;
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
    private double calculateWithholdingTax() {
        double grossForCutoff = calculateGrossPay();
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
     * Monthly Salary Credit (MSC) ranges from 5,000 to 35,000 in 500 increment bracket
     * Employee share is 5% of the MSC, split into:
     *   - Regular SS: 5% of MSC, capped at 20,000
     *   - Mandatory Provident Fund (MPF): 5% of the MSC exceeding 20,000
     *
     * https://www.sss.gov.ph/wp-content/uploads/2024/12/CI-2024-006-Publication.pdf
     */
    protected double calculateSSSContribution() {
        double msc;
        if (basicRate < 5000) { // minimum floor
            msc = 5000;
        } else if (basicRate > 35000) { // maximum ceiling
            msc = 35000;
        } else {
            // bracket move in 500 increment, this formula finds the nearest bracket
            msc = Math.floor((basicRate - 250) / 500) * 500 + 500;
        }

        // calculate Regular SS (capped at MSC 20,000) + MPF (excess above 20,000)
        double regularSS = Math.min(msc, 20000) * 0.05; // capped at 20,000
        double mpf = Math.max(0, msc - 20000) * 0.05; // 5% of excess
        return regularSS + mpf;
    }

    /**
     * Computes employee PhilHealth contribution
     *
     * MBS is floored at 10,000 and capped at 100,000
     * 5% total rate on Monthly Basic Salary (MBS) and 50% employee share
     *
     * https://www.philhealth.gov.ph/advisories/2025/PA2025-0002.pdf
     */
    protected double calculatePhilhealthContribution() {
        double mbs = Math.max(10000.0, Math.min(100000.0, basicRate));
        return mbs * 0.05 * 0.50;
    }

    /**
     * Computes employee Pag-IBIG contribution
     *
     * Employee share rate based on monthly Fund Salary bracket (HDMF Circular No. 460):
     *   Fund Salary   | Employee Rate
     *   <= 1,500      | 1%
     *   > 1,500       | 2% (capped at MFS 10,000; max 200/month)
     *
     * https://mpm.ph/wp-content/uploads/2024/01/HDMF-Circular-No.-460-Pag-ibig-HDMF-Table-2024.pdf
     */
    protected double calculatePagibigContribution() {
        double mfs = Math.min(basicRate, 10000.0);

        if (mfs <= 1500.0) {
            return mfs * 0.01;
        } else {
            return mfs * 0.02;
        }
    }

    // getters
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

    public double getOvertimePay() {
        return getOvertimeHours() * calculateHourlyRate() * 1.25;
    }

    public double getNetPay(double leaveDaysUsed, double loans) {
        return calculateNetPay(leaveDaysUsed, loans);
    }

    public double getAbsencesDeduction(double leaveDaysUsed) {
        return calculateAbsencesDeduction(leaveDaysUsed);
    }

    public double getUndertimeDeduction() {
        return calculateUndertimeDeduction();
    }

    public double getGrossPay() {
        return calculateGrossPay();
    }

    public double getWithholdingTax() {
        return calculateWithholdingTax();
    }

    public double getSSSContribution() {
        return calculateSSSContribution();
    }

    public double getPhilhealthContribution() {
        return calculatePhilhealthContribution();
    }

    public double getPagibigContribution() {
        return calculatePagibigContribution();
    }
}
