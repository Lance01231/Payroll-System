package org.nud.payroll;

/**
 * A friendly exception to throw when something goes wrong with our database operations.
 */
public class DatabaseOperationException extends RuntimeException {
    
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
