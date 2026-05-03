package org.nud.payroll;

/**
 * Represents a system user account.
 *
 * Stores credentials and the role (ADMIN or EMPLOYEE) used for
 * Role-Based Access Control (RBAC) throughout the payroll system.
 */
public class User {
    public enum Role {
        ADMIN, EMPLOYEE
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

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    /** Returns the employee ID this account is linked to, or null for ADMIN. */
    public String getLinkedEmployeeId() { return linkedEmployeeId; }
}
