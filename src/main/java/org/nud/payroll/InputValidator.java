package org.nud.payroll;

import java.util.Scanner;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class InputValidator {

    private static final double MIN_SALARY = 500;
    private static final double MAX_SALARY = 500_000;
    private static final double MAX_LOANS = 100_000;
    private static final int MAX_LEAVE_DAYS = 15;

    // 3-10 alphanumeric characters and hyphens are allowed
    public static boolean isValidEmployeeId(String id) {
        return id != null && id.matches("^[A-Za-z0-9-]{3,10}$");
    }

    // 6-50 letters, spaces, hyphens, apostrophes only. no numbers
    public static boolean isValidName(String name) {
        return name != null && name.trim().matches("^[A-Za-z '-]{6,50}$");
    }

    // R, P, C, or T only (case-insensitive)
    public static boolean isValidEmployeeType(char type) {
        char upper = Character.toUpperCase(type);
        return upper == 'R' || upper == 'P' || upper == 'C' || upper == 'T';
    }

    // salary must be between 500 and 500,000
    public static boolean isValidSalary(double salary) {
        return salary >= MIN_SALARY && salary <= MAX_SALARY;
    }

    // cut-off can only be 1 or 2
    public static boolean isValidCutOff(int cutOff) {
        return cutOff == 1 || cutOff == 2;
    }

    // time must be between 0 and 24 hours (obviously)
    public static boolean isValidTime(double time) {
        return time >= 0.0 && time <= 24.0;
    }

    // both 0 (absent) or time out must be after time in
    public static boolean isValidTimePair(double timeIn, double timeOut) {
        if (timeIn == 0.0 && timeOut == 0.0) {
            return true; // absent
        }
        if (timeIn == 0.0 || timeOut == 0.0) {
            return false; // one is zero, invalid
        }
        return timeOut > timeIn; // time out must be after time in
    }

    // leave days cannot exceed 15 days per cutoff
    public static boolean isValidLeaveDays(double days) {
        return days >= 0.0 && days <= MAX_LEAVE_DAYS;
    }

    // loans must be non-negative and reasonable
    public static boolean isValidLoans(double loans) {
        return loans >= 0.0 && loans <= MAX_LOANS;
    }

    // repeatedly ask the user until valid string input is provided :)
    public static String validateInput(Scanner sc, Predicate<String> validator, String errorMsg) {
        String input;
        for (;;) {
            input = sc.nextLine().trim();
            if (validator.test(input)) {
                return input;
            }
            System.out.println(errorMsg);
            System.out.print("> ");
        }
    }

    public static double validateDouble(Scanner sc, DoublePredicate validator, String errorMsg) {
        for (;;) {
            if (sc.hasNextDouble()) {
                double input = sc.nextDouble();
                sc.nextLine();
                if (validator.test(input)) {
                    return input;
                }
                System.out.println(errorMsg);
            } else {
                System.out.println("Please enter a valid number.");
                sc.nextLine();
            }
            System.out.print("> ");
        }
    }

    public static int validateInt(Scanner sc, IntPredicate validator, String errorMsg) {
        for (;;) {
            if (sc.hasNextInt()) {
                int input = sc.nextInt();
                sc.nextLine();
                if (validator.test(input)) {
                    return input;
                }
                System.out.println(errorMsg);
            } else {
                System.out.println("Please enter a valid WHOLE number.");
                sc.nextLine();
            }
            System.out.print("> ");
        }
    }

    public static char validateChar(Scanner sc, IntPredicate validator, String errorMsg) {
        for (;;) {
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) {
                char c = Character.toUpperCase(input.charAt(0));
                if (validator.test(c)) {
                    return c;
                }
            }
            System.out.println(errorMsg);
            System.out.print("> ");
        }
    }
}
