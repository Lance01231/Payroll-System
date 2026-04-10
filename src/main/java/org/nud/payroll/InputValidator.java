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

    // a helper method to translate friendly format to 24h format
    public static double isValidTime(Scanner sc, double defaultValue, String errorMsg) {
        for (;;) {
            String input = sc.nextLine().trim();
            String normalized = input.toLowerCase().replace(" ", "");

            // if the user sends empty or pressed "enter", use the default value which is "absent"
            if (input.isEmpty()) {
                return defaultValue;
            }

            // self explanatory
            if (normalized.equals("absent") || normalized.equals("a")) {
                return 0.0;
            }

            // still accept 24h format for compat
            if (normalized.matches("^\\d+(\\.\\d+)?$")) {
                double value = Double.parseDouble(normalized);
                if (value >= 0.0 && value <= 24.0) {
                    return value;
                }
                // regex pattern for accepting 12 hour format with minutes (e.g. 12:45am, 12pm)
            } else if (normalized.matches("^(1[0-2]|0?[1-9])(:[0-5][0-9])?(am|pm)$")) {
                int suffixIndex = normalized.length() - 2;
                int colonIndex = normalized.indexOf(':');
                int hour = Integer.parseInt(normalized.substring(0, colonIndex >= 0 ? colonIndex : suffixIndex));
                int minute = colonIndex >= 0 ? Integer.parseInt(normalized.substring(colonIndex + 1, suffixIndex)) : 0;
                boolean isPm = normalized.endsWith("pm");
                // 12am maps to 00:mm, 12pm maps to 12:mm.
                hour = (hour % 12) + (isPm ? 12 : 0);
                return hour + (minute / 60.0);
            }

            System.out.println(errorMsg);
            System.out.print("> ");
        }
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
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println(errorMsg);
                System.out.print("> ");
                continue;
            }
            try {
                // validate first if the input is actually a double
                double input = Double.parseDouble(line);
                // then verify it against isValid* methods (defined on the top) the caller gave
                if (validator.test(input)) {
                    return input;
                }
                // it is a double but failed the specified validation rule
                System.out.println(errorMsg);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
            System.out.print("> ");
        }
    }

    public static int validateInt(Scanner sc, IntPredicate validator, String errorMsg) {
        for (;;) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println(errorMsg);
                System.out.print("> ");
                continue;
            }
            try {
                int input = Integer.parseInt(line);
                if (validator.test(input)) {
                    return input;
                }
                System.out.println(errorMsg);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid WHOLE number.");
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
