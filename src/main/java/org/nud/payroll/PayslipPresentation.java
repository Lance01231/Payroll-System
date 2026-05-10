package org.nud.payroll;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/** Builds the three-column payslip breakdown used on employee and admin screens. */
final class PayslipPresentation {

    private PayslipPresentation() {}

    /** @param showAdminFootnote extra line showing filed leave/OT/loan (approvals preview). */
    static JPanel buildPayslipBody(PayslipDetails d, boolean showAdminFootnote) {
        double gross = d.grossPay();
        double net = d.netPay();
        double basic = d.basicPortionBeforeOt();
        double otPayLine = gross - basic;

        JPanel grid = new JPanel(new GridLayout(1, 3, 10, 0));
        grid.setBackground(PayrollSystem.C_SURFACE);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel col1 = titledColumn("Regular and Overtime Pay");
        col1.add(payRow("Rate", formatMoney(d.basicRate())));
        col1.add(payRow("No of Days", d.workedDaysDisplay() > 0 ? formatMoney(d.workedDaysDisplay()) : "0"));
        col1.add(payRow("Regular OT", formatMoney(otPayLine)));
        col1.add(payRow("Special Holidays", "₱0"));
        col1.add(payRow("NSD", "₱0"));
        col1.add(payRow("Total OT Pay", formatMoney(otPayLine)));
        col1.add(payRow("ECOLA", "₱0"));
        col1.add(payRow("Allowance", "₱0"));
        col1.add(payRow("Other Pay", "₱0"));
        col1.add(Box.createVerticalGlue());
        col1.add(PayrollSystem.sep());
        col1.add(payRow("Gross Pay", formatMoney(gross)));

        JPanel col2 = titledColumn("Employee Contribution");
        col2.add(payRow("SSS", formatMoney(d.sssEmployee())));
        col2.add(payRow("Phil-Health", formatMoney(d.philhealthEmployee())));
        col2.add(payRow("Withholding Tax", formatMoney(d.withholdingTax())));
        col2.add(payRow("pagbig Fund", formatMoney(d.pagibigEmployee())));
        col2.add(payRow("pagbig Loan", "₱0"));
        col2.add(payRow("SSSLoan", "₱0"));
        col2.add(payRow("Deduction", formatMoney(d.loanDeduction())));
        col2.add(payRow("Other Deduction", formatMoney(d.absencesDeduction() + d.undertimeDeduction())));
        col2.add(Box.createVerticalGlue());
        col2.add(PayrollSystem.sep());
        col2.add(payRow("Total Deduction", formatMoney(gross - net)));
        col2.add(PayrollSystem.sep());
        col2.add(payRow("Net Pay", formatMoney(net)));

        JPanel col3 = titledColumn("Employer Contribution");
        col3.add(payRow("SSS", formatMoney(d.employerSss())));
        col3.add(payRow("Phil-Health", formatMoney(d.employerPhilhealth())));
        col3.add(payRow("pagbig Fund", formatMoney(d.employerPagibig())));
        col3.add(payRow("ECC", formatMoney(d.employerEcc())));
        col3.add(Box.createVerticalGlue());

        grid.add(col1);
        grid.add(col2);
        grid.add(col3);

        var periodLbl = PayrollSystem.lbl(
                "Pay period: " + d.periodStart() + " – " + d.periodEnd(), PayrollSystem.F_SMALL, PayrollSystem.C_MUTED);
        periodLbl.setBorder(BorderFactory.createEmptyBorder(0, 20, 12, 20));

        JPanel headerStack = new JPanel();
        headerStack.setLayout(new BoxLayout(headerStack, BoxLayout.Y_AXIS));
        headerStack.setOpaque(false);
        headerStack.add(periodLbl);
        if (showAdminFootnote) {
            var previewHint = PayrollSystem.lbl(
                    String.format(
                            "Preview uses filed leave %.2f d · OT %.2f h · loan deduction ₱%.2f",
                            d.submissionLeaveDays(), d.submissionOtHours(), d.loanDeduction()),
                    PayrollSystem.F_SMALL,
                    PayrollSystem.C_MUTED);
            previewHint.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
            headerStack.add(previewHint);
        }

        JPanel slipBody = new JPanel(new BorderLayout());
        slipBody.setBackground(PayrollSystem.C_SURFACE);
        slipBody.add(headerStack, BorderLayout.NORTH);
        slipBody.add(grid, BorderLayout.CENTER);

        return slipBody;
    }

    private static JPanel titledColumn(String title) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(PayrollSystem.C_SURFACE);
        col.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PayrollSystem.C_BORDER),
                title,
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                PayrollSystem.F_LABEL,
                PayrollSystem.C_TEXT));
        return col;
    }

    private static JPanel payRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        javax.swing.JLabel l = PayrollSystem.lbl(label, PayrollSystem.F_SMALL, PayrollSystem.C_MUTED);
        javax.swing.JLabel v = PayrollSystem.lbl(value, PayrollSystem.F_BODY, PayrollSystem.C_TEXT);
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    static String formatMoney(double v) {
        return v % 1 == 0 ? String.format("₱%,.0f", v) : String.format("₱%,.2f", v);
    }
}
