package org.nud.payroll;

/**
 * This is what lets people log into the app!
 *
 * It stores their username, their secure password hash, and most importantly,
 * their role (whether they are an ADMIN pulling the strings, or an EMPLOYEE checking their payslip).
 */
public class User {
    public enum Role {
        ADMIN,
        EMPLOYEE
    }

    private final String username;
    private final String password;
    private final Role role;
    /** For EMPLOYEE role: links this account to an Employee record by ID. */
    private final String linkedEmployeeId;

    public User(String username, String password, Role role, String linkedEmployeeId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.linkedEmployeeId = linkedEmployeeId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
    /** Returns the employee ID this account is linked to, or null for ADMIN. */
    public String getLinkedEmployeeId() {
        return linkedEmployeeId;
    }
}
