package org.nud.payroll;

import java.awt.*;
import javax.swing.*;

/**
 * The LoginPanel is the front door of our application!
 * We've designed it to look sleek with a dark mode gradient background
 * and a nice centered glass-like card for the sign-in form.
 */
class LoginPanel extends JPanel {

    private final PayrollSystem frame;
    private final PayrollService service;
    private final JTextField usernameField = PayrollSystem.styledField(24);
    private final JPasswordField passwordField = PayrollSystem.styledPasswordField(24);
    private final JLabel errorLabel = PayrollSystem.lbl("", PayrollSystem.F_SMALL, PayrollSystem.C_DANGER);

    LoginPanel(PayrollSystem frame, PayrollService service) {
        this.frame = frame;
        this.service = service;
        setBackground(PayrollSystem.C_BG);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(buildCard(), gbc);
    }

    /** Let's paint a subtle radial gradient background to make it look modern and cool. */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Subtle blue glow at center
        RadialGradientPaint grad = new RadialGradientPaint(
                getWidth() / 2f,
                getHeight() / 2f,
                Math.max(getWidth(), getHeight()) * 0.6f,
                new float[] {0f, 1f},
                new Color[] {new Color(30, 55, 100, 80), new Color(13, 17, 23, 0)});
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

        // --- Logo & Branding ---
        JLabel dot = PayrollSystem.lbl("●", new Font("Segoe UI", Font.BOLD, 28), PayrollSystem.C_PRIMARY);
        dot.setAlignmentX(CENTER_ALIGNMENT);

        JLabel company = PayrollSystem.lbl("ABC Company", PayrollSystem.F_TITLE, PayrollSystem.C_TEXT);
        company.setAlignmentX(CENTER_ALIGNMENT);

        JLabel tagline = PayrollSystem.lbl("Employee Payroll System", PayrollSystem.F_BODY, PayrollSystem.C_MUTED);
        tagline.setAlignmentX(CENTER_ALIGNMENT);

        // --- A neat little divider ---
        JSeparator divider = PayrollSystem.sep();
        divider.setAlignmentX(CENTER_ALIGNMENT);

        // --- Form Title ---
        JLabel heading = PayrollSystem.lbl("Sign in to your account", PayrollSystem.F_H2, PayrollSystem.C_TEXT);
        heading.setAlignmentX(CENTER_ALIGNMENT);

        // --- The actual input fields for username and password ---
        JPanel userRow = fieldRow("Username", usernameField);
        JPanel passRow = fieldRow("Password", passwordField);

        // --- Where we show error messages (if any) ---
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);

        // --- The big friendly Sign In button ---
        JButton loginBtn = PayrollSystem.makeBtn("Sign In", PayrollSystem.C_PRIMARY);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        loginBtn.setFont(PayrollSystem.F_LABEL);
        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());

        // --- Putting it all together into the card ---
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
        java.util.Optional<User> accountOpt = service.authenticate(user, pass);
        if (accountOpt.isEmpty()) {
            errorLabel.setText("⚠  Invalid username or password.");
            passwordField.setText("");
            return;
        }
        User account = accountOpt.get();
        errorLabel.setText("");
        usernameField.setText("");
        passwordField.setText("");

        if (account.getRole() == User.Role.ADMIN) {
            frame.goAdmin();
        } else {
            String empId = account.getLinkedEmployeeId();
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalTime now = java.time.LocalTime.now();
            double currentTime = now.getHour() + (now.getMinute() / 60.0);
            int decision = JOptionPane.showConfirmDialog(
                    frame,
                    "Clock in now and record your attendance for today?",
                    "Clock in",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (decision == JOptionPane.YES_OPTION) {
                service.clockIn(empId, today, currentTime);
            }
            frame.goEmployee(empId);
        }
    }
}
