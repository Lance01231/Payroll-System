package org.nud.payroll;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;

/**
 * Main JFrame — boots FlatLaf Dark, manages CardLayout navigation,
 * and provides the shared dark-mode design system.
 */
public class PayrollSystem extends JFrame {

    // ── Dark mode palette (GitHub-dark-inspired) ──────────────────────────
    static final Color C_BG       = new Color(13,  17,  23);   // #0D1117 deepest bg
    static final Color C_SURFACE  = new Color(22,  27,  34);   // #161B22 cards
    static final Color C_SURFACE2 = new Color(33,  38,  45);   // #21262D elevated
    static final Color C_BORDER   = new Color(48,  54,  61);   // #30363D borders
    static final Color C_PRIMARY  = new Color(88, 166, 255);   // #58A6FF blue accent
    static final Color C_SUCCESS  = new Color(63, 185,  80);   // #3FB950 green
    static final Color C_DANGER   = new Color(248, 81,  73);   // #F85149 red
    static final Color C_WARNING  = new Color(227, 179,  65);  // #E3B341 yellow
    static final Color C_TEXT     = new Color(230, 237, 243);  // #E6EDF3 primary text
    static final Color C_MUTED    = new Color(139, 148, 158);  // #8B949E muted text
    static final Color C_NAV      = new Color(13,  17,  23);   // sidebar bg
    static final Color C_NAV_ITEM = new Color(33,  38,  45);   // nav hover
    static final Color C_INPUT    = new Color(33,  38,  45);   // input bg
    static final Color C_ROW_ALT  = new Color(22,  27,  34);   // table alt row

    // Convenience aliases kept for panel compat
    static final Color C_CARD  = C_SURFACE;
    static final Color C_NAV_BAR = C_SURFACE;   // top header strip

    // ── Typography ────────────────────────────────────────────────────────
    static final Font F_TITLE = new Font("Segoe UI", Font.BOLD,  26);
    static final Font F_H2    = new Font("Segoe UI", Font.BOLD,  16);
    static final Font F_LABEL = new Font("Segoe UI", Font.BOLD,  13);
    static final Font F_BODY  = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    static final Font F_MONO  = new Font("JetBrains Mono,Consolas,Monospaced", Font.PLAIN, 13);

    // ── Card keys ─────────────────────────────────────────────────────────
    static final String CARD_LOGIN    = "LOGIN";
    static final String CARD_ADMIN    = "ADMIN";
    static final String CARD_EMPLOYEE = "EMPLOYEE";

    // ── State ─────────────────────────────────────────────────────────────
    private final CardLayout    cardLayout    = new CardLayout();
    private final JPanel        root          = new JPanel(cardLayout);
    private final PayrollService service;
    private final AdminPanel    adminPanel;
    private final EmployeePanel employeePanel;

    public PayrollSystem(PayrollService svc) {
        super("ABC Company — Employee Payroll System");
        this.service = svc;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);

        adminPanel    = new AdminPanel(this, service);
        employeePanel = new EmployeePanel(this, service);

        root.setBackground(C_BG);
        root.add(new LoginPanel(this, service), CARD_LOGIN);
        root.add(adminPanel,                    CARD_ADMIN);
        root.add(employeePanel,                 CARD_EMPLOYEE);

        setContentPane(root);
    }

    // ── Navigation ────────────────────────────────────────────────────────
    void goLogin()                     { cardLayout.show(root, CARD_LOGIN); }
    void goAdmin()                     { adminPanel.refresh(); cardLayout.show(root, CARD_ADMIN); }
    void goEmployee(String employeeId) { employeePanel.load(employeeId); cardLayout.show(root, CARD_EMPLOYEE); }

    // ── Shared UI factory helpers ─────────────────────────────────────────

    static JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(F_BODY);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160, 38));
        // Hover effect
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            final Color base = bg;
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(base.brighter());
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(base);
            }
        });
        return b;
    }

    static JTextField styledField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(F_BODY);
        tf.setBackground(C_INPUT);
        tf.setForeground(C_TEXT);
        tf.setCaretColor(C_TEXT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return tf;
    }

    static JPasswordField styledPasswordField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setFont(F_BODY);
        pf.setBackground(C_INPUT);
        pf.setForeground(C_TEXT);
        pf.setCaretColor(C_TEXT);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return pf;
    }

    static JLabel lbl(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    static JPanel surface(int padH, int padV) {
        JPanel p = new JPanel();
        p.setBackground(C_SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER),
                BorderFactory.createEmptyBorder(padV, padH, padV, padH)));
        return p;
    }

    /** Horizontal separator line. */
    static JSeparator sep() {
        JSeparator s = new JSeparator();
        s.setForeground(C_BORDER);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    /**
     * Parses a time string without a Scanner.
     * Returns 0.0 = absent, decimal hours = valid, null = invalid.
     */
    static Double parseTime(String raw) {
        if (raw == null || raw.isBlank()) return 0.0;
        String s = raw.trim().toLowerCase().replace(" ", "");
        if (s.equals("absent") || s.equals("a")) return 0.0;
        if (s.matches("^\\d+(\\.\\d+)?$")) {
            double v = Double.parseDouble(s);
            return (v >= 0 && v <= 24) ? v : null;
        }
        if (s.matches("^(1[0-2]|0?[1-9])(:[0-5][0-9])?(am|pm)$")) {
            int sfx    = s.length() - 2;
            int colon  = s.indexOf(':');
            int hour   = Integer.parseInt(s.substring(0, colon >= 0 ? colon : sfx));
            int minute = colon >= 0 ? Integer.parseInt(s.substring(colon + 1, sfx)) : 0;
            boolean pm = s.endsWith("pm");
            hour = (hour % 12) + (pm ? 12 : 0);
            return hour + minute / 60.0;
        }
        return null;
    }

    // ── Entry point ───────────────────────────────────────────────────────
    public static void main(String[] args) {
        DatabaseManager.init();

        // Install FlatLaf Dark — transforms ALL Swing components automatically
        FlatDarkLaf.setup();
        UIManager.put("Button.arc",          10);
        UIManager.put("Component.arc",       8);
        UIManager.put("TextComponent.arc",   6);
        UIManager.put("ScrollBar.thumbArc",  999);
        UIManager.put("ScrollBar.width",     10);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("@accentColor",        "#58A6FF");
        UIManager.put("defaultFont",         F_BODY);

        SwingUtilities.invokeLater(() -> new PayrollSystem(new PayrollService()).setVisible(true));
    }
}
