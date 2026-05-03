package org.nud.payroll;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Employee dashboard — dark sidebar navigation with Timekeeping, OT/Leave, and Payslip panels.
 */
class EmployeePanel extends JPanel {

    private final PayrollSystem  frame;
    private final PayrollService service;

    private Employee currentEmp      = null;
    private boolean  timekeepingDone = false;
    private double   pendingLeave    = 0.0;
    private double   pendingLoans    = 0.0;

    private static final int DAYS = 15;
    private final JLabel tkMsg  = PayrollSystem.lbl("", PayrollSystem.F_SMALL, PayrollSystem.C_DANGER);
    private final JLabel olMsg  = PayrollSystem.lbl("", PayrollSystem.F_SMALL, PayrollSystem.C_DANGER);
    private javax.swing.table.DefaultTableModel attendanceModel;

    private final JTextField  otField = PayrollSystem.styledField(10);
    private final JTextField  leaveField = PayrollSystem.styledField(10);
    private final JTextField  loansField = PayrollSystem.styledField(10);

    private final JPanel payslipContainer = new JPanel(new BorderLayout());

    private static final String NAV_TK   = "TK";
    private static final String NAV_LEAVE = "LEAVE";
    private static final String NAV_SLIP  = "SLIP";
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel     contentPanel  = new JPanel(contentLayout);

    private final JLabel welcomeLabel = PayrollSystem.lbl("", PayrollSystem.F_H2, PayrollSystem.C_TEXT);

    EmployeePanel(PayrollSystem frame, PayrollService service) {
        this.frame   = frame;
        this.service = service;
        setBackground(PayrollSystem.C_BG);
        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);

        contentPanel.setBackground(PayrollSystem.C_BG);
        contentPanel.add(buildTimekeepingPanel(), NAV_TK);
        contentPanel.add(buildLeavePanel(),       NAV_LEAVE);
        contentPanel.add(buildPayslipPanel(),     NAV_SLIP);
        add(contentPanel, BorderLayout.CENTER);
    }

    void load(String employeeId) {
        currentEmp      = service.findEmployee(employeeId);
        pendingLeave    = 0.0;
        pendingLoans    = 0.0;
        leaveField.setText("0");
        otField.setText("0");
        loansField.setText("0");
        payslipContainer.removeAll();
        payslipContainer.revalidate();
        payslipContainer.repaint();
        tkMsg.setText(""); olMsg.setText("");
        leaveField.setEnabled(currentEmp != null && currentEmp.hasLeaveBenefits());
        welcomeLabel.setText(currentEmp != null ? "Welcome, " + currentEmp.getEmployeeName() : "");
        refreshAttendanceTable();
        
        SubmissionRepository.PayrollSubmission sub = service.getSubmission(employeeId);
        if (sub != null && sub.status.equals("APPROVED")) {
            olMsg.setForeground(PayrollSystem.C_SUCCESS);
            olMsg.setText("✔  Your payroll submission is APPROVED.");
        } else if (sub != null && sub.status.equals("PENDING")) {
            olMsg.setForeground(PayrollSystem.C_WARNING);
            olMsg.setText("⚠  Your payroll submission is PENDING Admin approval.");
        }
        
        navigate(NAV_TK);
    }

    // ── Top bar ───────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PayrollSystem.C_NAV_BAR);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, PayrollSystem.C_BORDER),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));

        JLabel sub = PayrollSystem.lbl("  ·  Employee Dashboard", PayrollSystem.F_BODY, PayrollSystem.C_MUTED);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(welcomeLabel);
        left.add(sub);

        JButton logout = PayrollSystem.makeBtn("Logout", PayrollSystem.C_DANGER);
        logout.setPreferredSize(new Dimension(100, 34));
        logout.addActionListener(e -> frame.goLogin());

        bar.add(left, BorderLayout.WEST);
        bar.add(logout, BorderLayout.EAST);
        return bar;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(PayrollSystem.C_NAV);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, PayrollSystem.C_BORDER),
                BorderFactory.createEmptyBorder(20, 0, 20, 0)));
        sidebar.setPreferredSize(new Dimension(200, 0));

        sidebar.add(navItem("🕐  Timekeeping",       NAV_TK));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navItem("📝  OT / Leave / Loans", NAV_LEAVE));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navItem("💰  View Payslip",       NAV_SLIP));
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JPanel navItem(String label, String card) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(PayrollSystem.C_NAV);
        item.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = PayrollSystem.lbl(label, PayrollSystem.F_BODY, PayrollSystem.C_MUTED);
        item.add(lbl, BorderLayout.WEST);
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                item.setBackground(PayrollSystem.C_NAV_ITEM); lbl.setForeground(PayrollSystem.C_TEXT);
            }
            @Override public void mouseExited(MouseEvent e) {
                item.setBackground(PayrollSystem.C_NAV); lbl.setForeground(PayrollSystem.C_MUTED);
            }
            @Override public void mouseClicked(MouseEvent e) { navigate(card); }
        });
        return item;
    }

    private void navigate(String card) { contentLayout.show(contentPanel, card); }

    // ── Timekeeping panel ─────────────────────────────────────────────────
    private JScrollPane buildTimekeepingPanel() {
        JPanel card = PayrollSystem.surface(32, 24);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(620, Integer.MAX_VALUE));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(PayrollSystem.C_BG);

        card.add(PayrollSystem.lbl("Daily Attendance", PayrollSystem.F_TITLE, PayrollSystem.C_TEXT));
        card.add(Box.createVerticalStrut(4));
        card.add(PayrollSystem.lbl("Your Time In was automatically logged when you signed in.",
                PayrollSystem.F_BODY, PayrollSystem.C_MUTED));
        card.add(Box.createVerticalStrut(20));
        card.add(PayrollSystem.sep());
        card.add(Box.createVerticalStrut(20));

        tkMsg.setAlignmentX(LEFT_ALIGNMENT);
        card.add(tkMsg);
        card.add(Box.createVerticalStrut(14));

        JButton timeOutBtn = PayrollSystem.makeBtn("⏰  Clock Out Now", PayrollSystem.C_WARNING);
        timeOutBtn.setFont(PayrollSystem.F_LABEL);
        timeOutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        timeOutBtn.setAlignmentX(LEFT_ALIGNMENT);
        timeOutBtn.addActionListener(e -> clockOutNow());
        card.add(timeOutBtn);
        
        card.add(Box.createVerticalStrut(30));
        card.add(PayrollSystem.lbl("Recent Punches", PayrollSystem.F_H2, PayrollSystem.C_TEXT));
        card.add(Box.createVerticalStrut(10));
        
        // Attendance Table
        String[] cols = {"Date", "Time In", "Time Out"};
        attendanceModel = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(attendanceModel);
        t.setBackground(PayrollSystem.C_SURFACE2);
        t.setForeground(PayrollSystem.C_TEXT);
        JScrollPane ts = new JScrollPane(t);
        ts.setPreferredSize(new Dimension(500, 200));
        ts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        ts.setAlignmentX(LEFT_ALIGNMENT);
        card.add(ts);

        outer.add(Box.createVerticalStrut(30));
        outer.add(card);
        outer.add(Box.createVerticalStrut(30));

        JScrollPane scroll = new JScrollPane(outer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void clockOutNow() {
        tkMsg.setForeground(PayrollSystem.C_DANGER);
        if (currentEmp == null) return;
        java.time.LocalTime now = java.time.LocalTime.now();
        double currentTime = now.getHour() + (now.getMinute() / 60.0);
        service.clockOut(currentEmp.getEmployeeNumber(), java.time.LocalDate.now(), currentTime);
        tkMsg.setForeground(PayrollSystem.C_SUCCESS);
        tkMsg.setText("✔  Successfully Clocked Out at " + String.format("%02d:%02d", now.getHour(), now.getMinute()));
        refreshAttendanceTable();
    }
    
    private void refreshAttendanceTable() {
        if (currentEmp == null || attendanceModel == null) return;
        attendanceModel.setRowCount(0);
        java.util.List<AttendanceRepository.AttendanceRecord> records = service.getAttendance(currentEmp.getEmployeeNumber());
        for (AttendanceRepository.AttendanceRecord r : records) {
            String tin = "--";
            if (r.timeIn != null) {
                int h = (int) r.timeIn.doubleValue();
                int m = (int) Math.round((r.timeIn % 1) * 60);
                if (m == 60) { h++; m = 0; }
                tin = String.format("%02d:%02d", h, m);
            }
            String tout = "--";
            if (r.timeOut != null) {
                int h = (int) r.timeOut.doubleValue();
                int m = (int) Math.round((r.timeOut % 1) * 60);
                if (m == 60) { h++; m = 0; }
                tout = String.format("%02d:%02d", h, m);
            }
            attendanceModel.addRow(new Object[]{ r.recordDate.toString(), tin, tout });
        }
    }

    // ── OT / Leave / Loans panel ──────────────────────────────────────────
    private JScrollPane buildLeavePanel() {
        JPanel card = PayrollSystem.surface(40, 32);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(PayrollSystem.C_BG);

        card.add(PayrollSystem.lbl("OT / Leave / Loans", PayrollSystem.F_TITLE, PayrollSystem.C_TEXT));
        card.add(Box.createVerticalStrut(4));
        card.add(PayrollSystem.lbl("Submit leave and loan deductions for this cutoff period.",
                PayrollSystem.F_BODY, PayrollSystem.C_MUTED));
        card.add(Box.createVerticalStrut(24));
        card.add(PayrollSystem.sep());
        card.add(Box.createVerticalStrut(24));

        // Leave row
        JLabel leaveLabel = PayrollSystem.lbl("Leave Days to Apply  (0–15)", PayrollSystem.F_LABEL, PayrollSystem.C_TEXT);
        JLabel leaveHint  = PayrollSystem.lbl("Only applied if your employee type has leave benefits.",
                PayrollSystem.F_SMALL, PayrollSystem.C_MUTED);
        leaveLabel.setAlignmentX(LEFT_ALIGNMENT);
        leaveField.setAlignmentX(LEFT_ALIGNMENT);
        leaveField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        leaveHint.setAlignmentX(LEFT_ALIGNMENT);
        card.add(leaveLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(leaveField);
        card.add(Box.createVerticalStrut(4));
        card.add(leaveHint);
        card.add(Box.createVerticalStrut(20));

        // OT row
        JLabel otLabel = PayrollSystem.lbl("Filing of OT (Hours)", PayrollSystem.F_LABEL, PayrollSystem.C_TEXT);
        otLabel.setAlignmentX(LEFT_ALIGNMENT);
        otField.setAlignmentX(LEFT_ALIGNMENT);
        otField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        card.add(otLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(otField);
        card.add(Box.createVerticalStrut(20));

        // Loans row
        JLabel loansLabel = PayrollSystem.lbl("Loan Deduction  (₱0 – ₱100,000)", PayrollSystem.F_LABEL, PayrollSystem.C_TEXT);
        loansLabel.setAlignmentX(LEFT_ALIGNMENT);
        loansField.setAlignmentX(LEFT_ALIGNMENT);
        loansField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        card.add(loansLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(loansField);
        card.add(Box.createVerticalStrut(12));

        olMsg.setAlignmentX(LEFT_ALIGNMENT);
        card.add(olMsg);
        card.add(Box.createVerticalStrut(20));

        JButton submit = PayrollSystem.makeBtn("Submit Timesheet to Admin", PayrollSystem.C_PRIMARY);
        submit.setFont(PayrollSystem.F_LABEL);
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        submit.setAlignmentX(LEFT_ALIGNMENT);
        submit.addActionListener(e -> submitLeave());
        card.add(submit);

        outer.add(Box.createVerticalStrut(30));
        outer.add(card);
        outer.add(Box.createVerticalStrut(30));

        JScrollPane scroll = new JScrollPane(outer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void submitLeave() {
        olMsg.setForeground(PayrollSystem.C_DANGER);
        double leave = 0.0, ot = 0.0, loans;
        if (currentEmp != null && currentEmp.hasLeaveBenefits()) {
            try { leave = Double.parseDouble(leaveField.getText().trim()); }
            catch (NumberFormatException ex) { olMsg.setText("⚠  Leave days must be a number."); return; }
            if (!InputValidator.isValidLeaveDays(leave)) { olMsg.setText("⚠  Leave days: 0–15."); return; }
        }
        try { ot = Double.parseDouble(otField.getText().trim()); }
        catch (NumberFormatException ex) { olMsg.setText("⚠  OT hours must be a number."); return; }

        try { loans = Double.parseDouble(loansField.getText().trim()); }
        catch (NumberFormatException ex) { olMsg.setText("⚠  Loan amount must be a number."); return; }
        if (!InputValidator.isValidLoans(loans)) { olMsg.setText("⚠  Loans: ₱0–₱100,000."); return; }

        pendingLeave = leave;
        pendingLoans = loans;
        service.submitPayroll(currentEmp.getEmployeeNumber(), leave, ot, loans);
        olMsg.setForeground(PayrollSystem.C_SUCCESS);
        olMsg.setText(String.format("✔  Submitted! Sent to Admin for approval."));
    }

    // ── Payslip panel ─────────────────────────────────────────────────────
    private JPanel buildPayslipPanel() {
        payslipContainer.setBackground(PayrollSystem.C_SURFACE);

        JPanel outer = new JPanel(new BorderLayout(0, 14));
        outer.setBackground(PayrollSystem.C_BG);
        outer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topRow.setOpaque(false);
        JLabel title = PayrollSystem.lbl("Payslip", PayrollSystem.F_H2, PayrollSystem.C_TEXT);
        topRow.add(title);

        JButton gen = PayrollSystem.makeBtn("Generate Payslip", PayrollSystem.C_PRIMARY);
        gen.addActionListener(e -> generatePayslip());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(gen);

        JScrollPane scroll = new JScrollPane(payslipContainer);
        scroll.setBorder(BorderFactory.createLineBorder(PayrollSystem.C_BORDER));
        scroll.getViewport().setBackground(PayrollSystem.C_SURFACE);

        outer.add(topRow,  BorderLayout.NORTH);
        outer.add(scroll,  BorderLayout.CENTER);
        outer.add(btnRow,  BorderLayout.SOUTH);
        return outer;
    }

    private void generatePayslip() {
        payslipContainer.removeAll();
        if (currentEmp == null)  { showError("Error: no employee loaded."); return; }
        
        SubmissionRepository.PayrollSubmission sub = service.getSubmission(currentEmp.getEmployeeNumber());
        if (sub == null || !sub.status.equals("APPROVED")) {
            showError("⚠ Payslip is locked.\n\nYou must Submit your Timesheet in the OT/Leave tab\nand wait for the Admin to Approve it before generating a payslip.");
            return;
        }

        // Apply attendance from DB to the in-memory object so it calculates correctly
        java.util.List<AttendanceRepository.AttendanceRecord> records = service.getAttendance(currentEmp.getEmployeeNumber());
        int count = records.size();
        double[] ins = new double[count];
        double[] outs = new double[count];
        for (int i = 0; i < count; i++) {
            AttendanceRepository.AttendanceRecord r = records.get(i);
            ins[i] = r.timeIn != null ? r.timeIn : 0.0;
            outs[i] = r.timeOut != null ? r.timeOut : 0.0;
        }
        currentEmp.setTimeKeeping(ins, outs);

        double gross    = currentEmp.calculateGrossPay(sub.otHours);
        double absD     = currentEmp.calculateAbsencesDeduction(sub.leaveDays);
        double utD      = currentEmp.calculateUndertimeDeduction();
        double sss      = currentEmp.getSSSContribution();
        double ph       = currentEmp.getPhilhealthContribution();
        double pi       = currentEmp.getPagibigContribution();
        double tax      = currentEmp.getWithholdingTax(gross);
        double net      = currentEmp.calculateNetPay(sub.leaveDays, sub.otHours, sub.loans);
        boolean pt      = currentEmp instanceof PartTimeEmployee;
        double basic    = pt ? currentEmp.getWorkedHours() * currentEmp.getBasicRate() : currentEmp.getBasicRate() / 2.0;

        JPanel grid = new JPanel(new GridLayout(1, 3, 10, 0));
        grid.setBackground(PayrollSystem.C_SURFACE);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Column 1: Regular and Overtime Pay
        JPanel col1 = new JPanel();
        col1.setLayout(new BoxLayout(col1, BoxLayout.Y_AXIS));
        col1.setBackground(PayrollSystem.C_SURFACE);
        col1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PayrollSystem.C_BORDER), "Regular and Overtime Pay", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, PayrollSystem.F_LABEL, PayrollSystem.C_TEXT));
        col1.add(payRow("Rate", f(currentEmp.getBasicRate())));
        col1.add(payRow("No of Days", currentEmp.getWorkedHours() > 0 ? f(currentEmp.getWorkedHours() / 8.0) : "0"));
        col1.add(payRow("Regular OT", f(gross - basic)));
        col1.add(payRow("Special Holidays", "0.00")); // Dummy
        col1.add(payRow("NSD", "0.00")); // Dummy
        col1.add(payRow("Total OT Pay", f(gross - basic)));
        col1.add(payRow("ECOLA", "0.00")); // Dummy
        col1.add(payRow("Allowance", "0.00")); // Dummy
        col1.add(payRow("Other Pay", "0.00")); // Dummy
        col1.add(Box.createVerticalGlue());
        col1.add(PayrollSystem.sep());
        col1.add(payRow("Gross Pay", f(gross)));

        // Column 2: Employee Contribution
        JPanel col2 = new JPanel();
        col2.setLayout(new BoxLayout(col2, BoxLayout.Y_AXIS));
        col2.setBackground(PayrollSystem.C_SURFACE);
        col2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PayrollSystem.C_BORDER), "Employee Contribution", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, PayrollSystem.F_LABEL, PayrollSystem.C_TEXT));
        col2.add(payRow("SSS", f(sss)));
        col2.add(payRow("Phil-Health", f(ph)));
        col2.add(payRow("Withholding Tax", f(tax)));
        col2.add(payRow("pagbig Fund", f(pi)));
        col2.add(payRow("pagbig Loan", "0.00")); // Dummy
        col2.add(payRow("SSSLoan", "0.00")); // Dummy
        col2.add(payRow("Deduction", f(sub.loans))); // Mapped generic loan here
        col2.add(payRow("Other Deduction", f(absD + utD))); // Mapped absences/lates here
        col2.add(Box.createVerticalGlue());
        col2.add(PayrollSystem.sep());
        col2.add(payRow("Total Deduction", f((gross - net))));
        col2.add(PayrollSystem.sep());
        col2.add(payRow("Net Pay", f(net)));

        // Column 3: Employer Contribution
        JPanel col3 = new JPanel();
        col3.setLayout(new BoxLayout(col3, BoxLayout.Y_AXIS));
        col3.setBackground(PayrollSystem.C_SURFACE);
        col3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PayrollSystem.C_BORDER), "Employer Contribution", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, PayrollSystem.F_LABEL, PayrollSystem.C_TEXT));
        col3.add(payRow("SSS", f(currentEmp.getEmployerSSS())));
        col3.add(payRow("Phil-Health", f(currentEmp.getEmployerPhilHealth())));
        col3.add(payRow("pagbig Fund", f(currentEmp.getEmployerPagIbig())));
        col3.add(payRow("ECC", f(currentEmp.getEmployerECC())));
        col3.add(Box.createVerticalGlue());

        grid.add(col1);
        grid.add(col2);
        grid.add(col3);

        payslipContainer.add(grid, BorderLayout.CENTER);
        payslipContainer.revalidate();
        payslipContainer.repaint();
    }

    private void showError(String msg) {
        JLabel err = PayrollSystem.lbl(msg, PayrollSystem.F_BODY, PayrollSystem.C_WARNING);
        err.setHorizontalAlignment(SwingConstants.CENTER);
        payslipContainer.add(err, BorderLayout.CENTER);
        payslipContainer.revalidate();
        payslipContainer.repaint();
    }

    private JPanel payRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel l = PayrollSystem.lbl(label, PayrollSystem.F_SMALL, PayrollSystem.C_MUTED);
        JLabel v = PayrollSystem.lbl(value, PayrollSystem.F_BODY, PayrollSystem.C_TEXT);
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private String f(double v) {
        return v % 1 == 0 ? String.format("₱%,.0f", v) : String.format("₱%,.2f", v);
    }
}
