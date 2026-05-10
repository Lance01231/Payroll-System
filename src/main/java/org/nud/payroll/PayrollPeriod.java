package org.nud.payroll;

import java.time.LocalDate;
import java.time.YearMonth;

/** Cut-off date ranges (1st–15th vs 16th–month-end) for attendance and payslip scope. */
public final class PayrollPeriod {

    private PayrollPeriod() {}

    /** Start date (inclusive) of the current calendar cutoff window for this employee. */
    public static LocalDate currentPeriodStart(int cutOffPeriod) {
        YearMonth ym = YearMonth.now();
        return cutOffPeriod == 1 ? ym.atDay(1) : ym.atDay(16);
    }

    /** End date (inclusive) of the current calendar cutoff window for this employee. */
    public static LocalDate currentPeriodEnd(int cutOffPeriod) {
        YearMonth ym = YearMonth.now();
        return cutOffPeriod == 1 ? ym.atDay(15) : ym.atEndOfMonth();
    }
}
