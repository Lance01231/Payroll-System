package org.nud.payroll;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Admin dashboard — dark sidebar navigation with Add Employee and View Records panels.
 */
class AdminPanel extends JPanel {

    private final PayrollSystem  frame;
    private final PayrollService service;

    // ── Sidebar nav ───────────────────────────────────────────────────────
    private static final String NAV_ADD  = "ADD";
    private static final String NAV_VIEW = "VIEW";
    private static final String NAV_APPR = "APPR";
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel     contentPanel  = new JPanel(contentLayout);

    // ── Add Employee fields ───────────────────────────────────────────────
    private final JTextField  idField     = PayrollSystem.styledField(20);
    private final JTextField  nameField   = PayrollSystem.styledField(20);
    private final JComboBox<String> typeCombo = new JComboBox<>(
            new String[]{"Regular", "Probationary", "Contractual", "Part-time"});
    private final JTextField  salaryField = PayrollSystem.styledField(20);
    private final JComboBox<String> cutOffCombo = new JComboBox<>(
            new String[]{"1 — 1st to 15th", "2 — 16th to 30th"});
    private final JTextField     loginUserField = PayrollSystem.styledField(20);
    private final JPasswordField loginPassField = PayrollSystem.styledPasswordField(20);
    private final JTextField  scheduleField = PayrollSystem.styledField(20);
    private final JTextField  slField       = PayrollSystem.styledField(10);
    private final JTextField  vlField       = PayrollSystem.styledField(10);
    private final JTextField  elField       = PayrollSystem.styledField(10);
    private final JTextField  initLoanField = PayrollSystem.styledField(10);
    private final JLabel         formMsg        = PayrollSystem.lbl("", PayrollSystem.F_SMALL, PayrollSystem.C_DANGER);

    // ── View All table ────────────────────────────────────────────────────
    private final String[] COLS = {"Employee ID", "Name", "Type", "Basic Salary", "Cut-off"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    // ── Approvals table ───────────────────────────────────────────────────
    private final String[] APPR_COLS = {"Submission ID", "Employee ID", "Leave Days", "OT Hours", "Loans"};
    private final DefaultTableModel approvalsModel = new DefaultTableModel(APPR_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable apprTable = new JTable(approvalsModel);

    AdminPanel(PayrollSystem frame, PayrollService service) {
        this.frame   = frame;
        this.service = service;
        setBackground(PayrollSystem.C_BG);
        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);

        contentPanel.setBackground(PayrollSystem.C_BG);
        contentPanel.add(buildAddPanel(),  NAV_ADD);
        contentPanel.add(buildViewPanel(), NAV_VIEW);
        contentPanel.add(buildApprovalsPanel(), NAV_APPR);
        add(contentPanel, BorderLayout.CENTER);

        styleCombo(typeCombo);
        styleCombo(cutOffCombo);
        navigate(NAV_ADD);
    }

    void refresh() {
        tableModel.setRowCount(0);
        for (Employee emp : service.getAllEmployees()) {
            tableModel.addRow(new Object[]{
                emp.getEmployeeNumber(), emp.getEmployeeName(), emp.getEmployeeType(),
                String.format("₱%,.2f", emp.getBasicRate()),
                emp.getCutOffPeriod() == 1 ? "1st–15th" : "16th–30th"
            });
        }
        approvalsModel.setRowCount(0);
        for (SubmissionRepository.PayrollSubmission sub : service.getPendingSubmissions()) {
            approvalsModel.addRow(new Object[]{ sub.id, sub.employeeId, sub.leaveDays, sub.otHours, sub.loans });
        }
    }

    // ── Top bar ───────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PayrollSystem.C_NAV_BAR);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, PayrollSystem.C_BORDER),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));

        JLabel title = PayrollSystem.lbl("ABC Company", PayrollSystem.F_H2, PayrollSystem.C_TEXT);
        JLabel sub   = PayrollSystem.lbl("  ·  Admin Dashboard", PayrollSystem.F_BODY, PayrollSystem.C_MUTED);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);

        JButton logout = PayrollSystem.makeBtn("Logout", PayrollSystem.C_DANGER);
        logout.setPreferredSize(new Dimension(100, 34));
        logout.addActionListener(e -> frame.goLogin());

        bar.add(left,   BorderLayout.WEST);
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

        sidebar.add(navItem("➕  Add Employee",    NAV_ADD));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navItem("📋  View All Records", NAV_VIEW));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navItem("✅  Approvals",       NAV_APPR));
        sidebar.add(Box.createVerticalGlue());

        JLabel ver = PayrollSystem.lbl("  Payroll System v1.0", PayrollSystem.F_SMALL, PayrollSystem.C_MUTED);
        ver.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(ver);
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
                item.setBackground(PayrollSystem.C_NAV_ITEM);
                lbl.setForeground(PayrollSystem.C_TEXT);
            }
            @Override public void mouseExited(MouseEvent e) {
                item.setBackground(PayrollSystem.C_NAV);
                lbl.setForeground(PayrollSystem.C_MUTED);
            }
            @Override public void mouseClicked(MouseEvent e) { navigate(card); }
        });
        return item;
    }

    private void navigate(String card) { contentLayout.show(contentPanel, card); }

    // ── Add Employee panel ────────────────────────────────────────────────
    private JScrollPane buildAddPanel() {
        JPanel card = PayrollSystem.surface(40, 32);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(560, Integer.MAX_VALUE));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(PayrollSystem.C_BG);

        card.add(PayrollSystem.lbl("New Employee", PayrollSystem.F_TITLE, PayrollSystem.C_TEXT));
        card.add(Box.createVerticalStrut(4));
        card.add(PayrollSystem.lbl("Fill in all fields to register a new staff member.",
                PayrollSystem.F_BODY, PayrollSystem.C_MUTED));
        card.add(Box.createVerticalStrut(28));
        card.add(PayrollSystem.sep());
        card.add(Box.createVerticalStrut(24));

        card.add(addRow("Employee ID",    idField,     "1–20 alphanumeric characters (hyphens OK)"));
        card.add(Box.createVerticalStrut(16));
        card.add(addRow("Full Name  (Last, First, Middle)", nameField, "2–50 letters"));
        card.add(Box.createVerticalStrut(16));
        card.add(comboRow("Employee Type",  typeCombo));
        card.add(Box.createVerticalStrut(16));
        card.add(addRow("Basic Salary",  salaryField,  "₱500 – ₱500,000  (Hourly for Part-time)"));
        card.add(Box.createVerticalStrut(16));
        card.add(comboRow("Cut-off Period", cutOffCombo));
        card.add(Box.createVerticalStrut(16));
        card.add(addRow("Work Schedule",  scheduleField, "e.g. 08:00 - 17:00"));
        card.add(Box.createVerticalStrut(16));
        card.add(addRow("Sick Leave Balance",  slField, "Number of days"));
        card.add(Box.createVerticalStrut(16));
        card.add(addRow("Vacation Leave Balance",  vlField, "Number of days"));
        card.add(Box.createVerticalStrut(16));
        card.add(addRow("Emergency Leave Balance",  elField, "Number of days"));
        card.add(Box.createVerticalStrut(16));
        card.add(addRow("Initial Loan Balance",  initLoanField, "e.g. 1000"));
        card.add(Box.createVerticalStrut(28));
        card.add(PayrollSystem.sep());
        card.add(Box.createVerticalStrut(20));

        card.add(PayrollSystem.lbl("Login Credentials", PayrollSystem.F_H2, PayrollSystem.C_TEXT));
        card.add(Box.createVerticalStrut(4));
        card.add(PayrollSystem.lbl("The employee will use these to sign in.",
                PayrollSystem.F_SMALL, PayrollSystem.C_MUTED));
        card.add(Box.createVerticalStrut(16));
        card.add(addRow("Username", loginUserField, "At least 3 characters"));
        card.add(Box.createVerticalStrut(16));
        card.add(addRow("Password", loginPassField, "At least 6 characters"));
        card.add(Box.createVerticalStrut(12));

        formMsg.setAlignmentX(LEFT_ALIGNMENT);
        card.add(formMsg);
        card.add(Box.createVerticalStrut(16));

        JButton saveBtn = PayrollSystem.makeBtn("Save Employee", PayrollSystem.C_SUCCESS);
        saveBtn.setFont(PayrollSystem.F_LABEL);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> saveEmployee());
        card.add(saveBtn);

        outer.add(Box.createVerticalStrut(30));
        outer.add(card);
        outer.add(Box.createVerticalStrut(30));

        JScrollPane scroll = new JScrollPane(outer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel addRow(String labelText, JComponent field, String hint) {
        JPanel p = new JPanel();
        p.setBackground(PayrollSystem.C_SURFACE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(LEFT_ALIGNMENT);

        JLabel l = PayrollSystem.lbl(labelText, PayrollSystem.F_LABEL, PayrollSystem.C_TEXT);
        JLabel h = PayrollSystem.lbl(hint, PayrollSystem.F_SMALL, PayrollSystem.C_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        h.setAlignmentX(LEFT_ALIGNMENT);

        p.add(l);
        p.add(Box.createVerticalStrut(5));
        p.add(field);
        p.add(Box.createVerticalStrut(3));
        p.add(h);
        return p;
    }

    private JPanel comboRow(String labelText, JComboBox<String> combo) {
        JPanel p = new JPanel();
        p.setBackground(PayrollSystem.C_SURFACE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(LEFT_ALIGNMENT);

        JLabel l = PayrollSystem.lbl(labelText, PayrollSystem.F_LABEL, PayrollSystem.C_TEXT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        combo.setAlignmentX(LEFT_ALIGNMENT);

        p.add(l);
        p.add(Box.createVerticalStrut(5));
        p.add(combo);
        return p;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(PayrollSystem.F_BODY);
        combo.setBackground(PayrollSystem.C_INPUT);
        combo.setForeground(PayrollSystem.C_TEXT);
    }

    private void saveEmployee() {
        formMsg.setForeground(PayrollSystem.C_DANGER);
        String id   = idField.getText().trim();
        String name = nameField.getText().trim();
        String sal  = salaryField.getText().trim();
        String user = loginUserField.getText().trim();
        String pass = new String(loginPassField.getPassword()).trim();

        if (!InputValidator.isValidEmployeeId(id))  { formMsg.setText("⚠  Invalid Employee ID (1–20 alphanumeric)."); return; }
        if (!InputValidator.isValidName(name))       { formMsg.setText("⚠  Invalid Name (2–50 letters)."); return; }

        double salary;
        try { salary = Double.parseDouble(sal); }
        catch (NumberFormatException ex) { formMsg.setText("⚠  Salary must be a number."); return; }
        if (!InputValidator.isValidSalary(salary))   { formMsg.setText("⚠  Salary must be ₱500 – ₱500,000."); return; }
        if (user.length() < 3)                       { formMsg.setText("⚠  Username must be at least 3 characters."); return; }
        if (pass.length() < 6)                       { formMsg.setText("⚠  Password must be at least 6 characters."); return; }

        String type   = (String) typeCombo.getSelectedItem();
        int    cutOff = cutOffCombo.getSelectedIndex() + 1;
        String sched  = scheduleField.getText().trim();
        int sl = 0, vl = 0, el = 0;
        double loan = 0;
        try {
            if (!slField.getText().trim().isEmpty()) sl = Integer.parseInt(slField.getText().trim());
            if (!vlField.getText().trim().isEmpty()) vl = Integer.parseInt(vlField.getText().trim());
            if (!elField.getText().trim().isEmpty()) el = Integer.parseInt(elField.getText().trim());
            if (!initLoanField.getText().trim().isEmpty()) loan = Double.parseDouble(initLoanField.getText().trim());
        } catch (NumberFormatException ex) {
            formMsg.setText("⚠  Leaves and Loans must be numbers."); return;
        }

        Employee emp  = switch (type) {
            case "Regular"      -> new RegularEmployee(id, name, salary, cutOff, sched, sl, vl, el, loan);
            case "Probationary" -> new ProbationaryEmployee(id, name, salary, cutOff, sched, sl, vl, el, loan);
            case "Contractual"  -> new ContractualEmployee(id, name, salary, cutOff, sched, sl, vl, el, loan);
            default             -> new PartTimeEmployee(id, name, salary, cutOff, sched, sl, vl, el, loan);
        };
        try {
            service.registerEmployee(emp, user, pass);
            formMsg.setForeground(PayrollSystem.C_SUCCESS);
            formMsg.setText("✔  Employee [" + id + "] saved successfully!");
            idField.setText(""); nameField.setText(""); salaryField.setText("");
            scheduleField.setText(""); slField.setText(""); vlField.setText(""); elField.setText(""); initLoanField.setText("");
            loginUserField.setText(""); loginPassField.setText("");
            typeCombo.setSelectedIndex(0); cutOffCombo.setSelectedIndex(0);
        } catch (Exception ex) {
            formMsg.setForeground(PayrollSystem.C_DANGER);
            String errorMsg = ex.getMessage();
            if (errorMsg != null && errorMsg.contains("Unique index or primary key violation")) {
                formMsg.setText("⚠  Error: Employee ID or Username already exists.");
            } else {
                formMsg.setText("⚠  Error saving employee.");
                System.err.println("Save error: ");
                ex.printStackTrace();
            }
        }
    }

    // ── View All Records panel ────────────────────────────────────────────
    private JPanel buildViewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(PayrollSystem.C_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Style table
        table.setFont(PayrollSystem.F_BODY);
        table.setBackground(PayrollSystem.C_SURFACE);
        table.setForeground(PayrollSystem.C_TEXT);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(40, 70, 120));
        table.setSelectionForeground(PayrollSystem.C_TEXT);
        table.getTableHeader().setFont(PayrollSystem.F_LABEL);
        table.getTableHeader().setBackground(PayrollSystem.C_SURFACE2);
        table.getTableHeader().setForeground(PayrollSystem.C_MUTED);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, PayrollSystem.C_BORDER));

        // Alternating row renderer
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                if (!sel) {
                    setBackground(row % 2 == 0 ? PayrollSystem.C_SURFACE : PayrollSystem.C_BG);
                    setForeground(PayrollSystem.C_TEXT);
                }
                return this;
            }
        };
        for (int i = 0; i < COLS.length; i++) table.getColumnModel().getColumn(i).setCellRenderer(r);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(PayrollSystem.C_BORDER));
        scroll.getViewport().setBackground(PayrollSystem.C_SURFACE);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        JButton refreshBtn = PayrollSystem.makeBtn("⟳  Refresh", PayrollSystem.C_PRIMARY);
        refreshBtn.addActionListener(e -> refresh());
        
        JButton delBtn = PayrollSystem.makeBtn("🗑 Delete Selected", PayrollSystem.C_DANGER);
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            String empId = (String) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete employee " + empId + " and their account?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                service.deleteEmployee(empId);
                refresh();
            }
        });

        JButton mockBtn = PayrollSystem.makeBtn("Mock Attendance", PayrollSystem.C_WARNING);
        mockBtn.setToolTipText("Injects 15 days of perfect attendance for presentation purposes.");
        mockBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            String empId = (String) tableModel.getValueAt(row, 0);
            service.generateMockAttendance(empId);
            JOptionPane.showMessageDialog(this, "Generated 15 days of attendance for " + empId);
        });

        btnRow.add(refreshBtn);
        btnRow.add(Box.createHorizontalStrut(10));
        btnRow.add(delBtn);
        btnRow.add(Box.createHorizontalStrut(10));
        btnRow.add(mockBtn);

        panel.add(PayrollSystem.lbl("All Employee Records", PayrollSystem.F_H2, PayrollSystem.C_TEXT),
                BorderLayout.NORTH);
        panel.add(scroll,  BorderLayout.CENTER);
        panel.add(btnRow,  BorderLayout.SOUTH);
        return panel;
    }

    // ── Approvals panel ───────────────────────────────────────────────────
    private JPanel buildApprovalsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(PayrollSystem.C_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        apprTable.setFont(PayrollSystem.F_BODY);
        apprTable.setBackground(PayrollSystem.C_SURFACE);
        apprTable.setForeground(PayrollSystem.C_TEXT);
        apprTable.setRowHeight(36);
        apprTable.setShowGrid(false);
        apprTable.setIntercellSpacing(new Dimension(0, 0));
        apprTable.setSelectionBackground(new Color(40, 70, 120));
        apprTable.setSelectionForeground(PayrollSystem.C_TEXT);
        apprTable.getTableHeader().setFont(PayrollSystem.F_LABEL);
        apprTable.getTableHeader().setBackground(PayrollSystem.C_SURFACE2);
        apprTable.getTableHeader().setForeground(PayrollSystem.C_MUTED);
        apprTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, PayrollSystem.C_BORDER));

        JScrollPane scroll = new JScrollPane(apprTable);
        scroll.setBorder(BorderFactory.createLineBorder(PayrollSystem.C_BORDER));
        scroll.getViewport().setBackground(PayrollSystem.C_SURFACE);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        
        JButton refreshBtn = PayrollSystem.makeBtn("⟳  Refresh", PayrollSystem.C_PRIMARY);
        refreshBtn.addActionListener(e -> refresh());

        JButton approveBtn = PayrollSystem.makeBtn("✔ Approve", PayrollSystem.C_SUCCESS);
        approveBtn.addActionListener(e -> {
            int row = apprTable.getSelectedRow();
            if (row < 0) return;
            int subId = (Integer) approvalsModel.getValueAt(row, 0);
            service.updateSubmissionStatus(subId, "APPROVED");
            refresh();
        });

        JButton rejectBtn = PayrollSystem.makeBtn("✖ Reject", PayrollSystem.C_DANGER);
        rejectBtn.addActionListener(e -> {
            int row = apprTable.getSelectedRow();
            if (row < 0) return;
            int subId = (Integer) approvalsModel.getValueAt(row, 0);
            service.updateSubmissionStatus(subId, "REJECTED");
            refresh();
        });

        btnRow.add(refreshBtn);
        btnRow.add(Box.createHorizontalStrut(10));
        btnRow.add(approveBtn);
        btnRow.add(Box.createHorizontalStrut(10));
        btnRow.add(rejectBtn);

        panel.add(PayrollSystem.lbl("Pending Payroll Submissions", PayrollSystem.F_H2, PayrollSystem.C_TEXT),
                BorderLayout.NORTH);
        panel.add(scroll,  BorderLayout.CENTER);
        panel.add(btnRow,  BorderLayout.SOUTH);
        return panel;
    }
}
