package org.nud.payroll;

import javax.swing.*;
import java.awt.*;

/**
 * Dark-mode login screen — full-window gradient background with centered glass card.
 */
class LoginPanel extends JPanel {

    private final PayrollSystem  frame;
    private final PayrollService service;
    private final JTextField     usernameField = PayrollSystem.styledField(24);
    private final JPasswordField passwordField = PayrollSystem.styledPasswordField(24);
    private final JLabel         errorLabel    = PayrollSystem.lbl("", PayrollSystem.F_SMALL, PayrollSystem.C_DANGER);

    LoginPanel(PayrollSystem frame, PayrollService service) {
        this.frame   = frame;
        this.service = service;
        setBackground(PayrollSystem.C_BG);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        add(buildCard(), gbc);
    }

    /** Paints a subtle radial gradient background behind the card. */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Subtle blue glow at center
        RadialGradientPaint grad = new RadialGradientPaint(
                getWidth() / 2f, getHeight() / 2f,
                Math.max(getWidth(), getHeight()) * 0.6f,
                new float[]{0f, 1f},
                new Color[]{new Color(30, 55, 100, 80), new Color(13, 17, 23, 0)});
        g2.setPaint(grad);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setBackground(PayrollSystem.C_SURFACE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PayrollSystem.C_BORDER),
                BorderFactory.createEmptyBorder(48, 56, 48, 56)));
        card.setPreferredSize(new Dimension(440, 520));

        // ── Logo / branding ──────────────────────────────────────────────
        JLabel dot = PayrollSystem.lbl("●", new Font("Segoe UI", Font.BOLD, 28), PayrollSystem.C_PRIMARY);
        dot.setAlignmentX(CENTER_ALIGNMENT);

        JLabel company = PayrollSystem.lbl("ABC Company", PayrollSystem.F_TITLE, PayrollSystem.C_TEXT);
        company.setAlignmentX(CENTER_ALIGNMENT);

        JLabel tagline = PayrollSystem.lbl("Employee Payroll System", PayrollSystem.F_BODY, PayrollSystem.C_MUTED);
        tagline.setAlignmentX(CENTER_ALIGNMENT);

        // ── Divider ──────────────────────────────────────────────────────
        JSeparator divider = PayrollSystem.sep();
        divider.setAlignmentX(CENTER_ALIGNMENT);

        // ── Form title ───────────────────────────────────────────────────
        JLabel heading = PayrollSystem.lbl("Sign in to your account", PayrollSystem.F_H2, PayrollSystem.C_TEXT);
        heading.setAlignmentX(CENTER_ALIGNMENT);

        // ── Form fields ──────────────────────────────────────────────────
        JPanel userRow = fieldRow("Username", usernameField);
        JPanel passRow = fieldRow("Password", passwordField);

        // ── Error label ──────────────────────────────────────────────────
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);

        // ── Login button ─────────────────────────────────────────────────
        JButton loginBtn = PayrollSystem.makeBtn("Sign In", PayrollSystem.C_PRIMARY);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        loginBtn.setFont(PayrollSystem.F_LABEL);
        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());

        // ── Hint ─────────────────────────────────────────────────────────
        JLabel hint = PayrollSystem.lbl("Default admin: admin / admin123",
                PayrollSystem.F_SMALL, PayrollSystem.C_MUTED);
        hint.setAlignmentX(CENTER_ALIGNMENT);

        // ── Assemble ─────────────────────────────────────────────────────
        card.add(dot);
        card.add(Box.createVerticalStrut(8));
        card.add(company);
        card.add(Box.createVerticalStrut(4));
        card.add(tagline);
        card.add(Box.createVerticalStrut(24));
        card.add(divider);
        card.add(Box.createVerticalStrut(24));
        card.add(heading);
        card.add(Box.createVerticalStrut(28));
        card.add(userRow);
        card.add(Box.createVerticalStrut(16));
        card.add(passRow);
        card.add(Box.createVerticalStrut(10));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(16));
        card.add(hint);

        return card;
    }

    private JPanel fieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel();
        row.setBackground(PayrollSystem.C_SURFACE);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lbl = PayrollSystem.lbl(labelText, PayrollSystem.F_LABEL, PayrollSystem.C_TEXT);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        row.add(lbl);
        row.add(Box.createVerticalStrut(6));
        row.add(field);
        return row;
    }

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("⚠  Username and password are required.");
            return;
        }
        User account = service.authenticate(user, pass);
        if (account == null) {
            errorLabel.setText("⚠  Invalid username or password.");
            passwordField.setText("");
            return;
        }
        errorLabel.setText("");
        usernameField.setText("");
        passwordField.setText("");

        if (account.getRole() == User.Role.ADMIN) {
            frame.goAdmin();
        } else {
            java.time.LocalTime now = java.time.LocalTime.now();
            double currentTime = now.getHour() + (now.getMinute() / 60.0);
            service.clockIn(account.getLinkedEmployeeId(), java.time.LocalDate.now(), currentTime);
            frame.goEmployee(account.getLinkedEmployeeId());
        }
    }
}
