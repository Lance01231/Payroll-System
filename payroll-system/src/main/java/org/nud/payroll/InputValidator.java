package org.nud.payroll;

import java.util.Scanner;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class InputValidator {
    // 3-10 alphanumeric characters and hyphens are allowed
    public static String isValidEmployeeId(Scanner in) {
        String errMsg = "ID must be 3-10 alphanumeric characters. Try again...";
        return validateInput(in, id -> id != null && id.matches("^[A-Za-z0-9-]{3,10}$"), errMsg);
    }

    // 6-50 letters, spaces, hyphens, apostrophes only. no numbers
    public static String isValidName(Scanner in) {
        String errMsg = "Name must be 6-50 letters only. (spaces, hyphens, apostrophes are allowed). Try again...";
        return validateInput(in, name -> name != null && name.trim().matches("^[A-Za-z '-]{6,50}$"), errMsg);
    }

    // R, P, C, or T only (case-insensitive)
    public static char isValidEmployeeType(Scanner in) {
        String errMsg = "Only choose R, P, C, or T. Try again...";
        return validateChar(in, c -> c == 'R' || c == 'P' || c == 'C' || c == 'T', errMsg);
    }

    // salary must be between 500 and 500,000
    public static double isValidSalary(Scanner in) {
        String errMsg = "Salary must be between 500 and 500,000. Try again...";
        return validateDouble(in, salary -> salary >= 500 && salary <= 500_000, errMsg);
    }

    // cut-off can only be 1 or 2
    public static int isValidCutOff(Scanner in) {
        String errMsg = "Only choose 1 or 2. Try again...";
        return validateInt(in, period -> period == 1 || period == 2, errMsg);
    }

    // leave days cannot exceed 15 days per cutoff
    public static double isValidLeaveDays(Scanner in) {
        String errMsg = "Leave days must be between 0 and 15. Try again...";
        return validateInt(in, days -> days >= 0.0 && days <= 15, errMsg);
    }

    // loans must be non-negative and reasonable
    public static double isValidLoans(Scanner in) {
        String errMsg = "Loans must be between 0 and 100,000. Try again...";
        return validateDouble(in, loans -> loans >= 0.0 && loans <= 100_000, errMsg);
    }

    // validates work hours against establishment operating hours and logical time ordering
    public static String isValidWorkHours(double timeIn, double timeOut) {

        if (timeIn == 0.0 && timeOut == 0.0) {
            return null; // absent
        }
        if (timeIn == 0.0 || timeOut == 0.0) {
            return "Both Time In and Time Out are required!";
        }

        // establishment operating hours is 5am to 10pm
        if (timeIn < 5.0) {
            return "Time in cannot be before 5am!";
        }
        if (timeOut > 22.0) {
            return "Time out cannot be after 10pm!";
        }

        // time out must be after time in obviously
        if (timeOut <= timeIn) {
            return "Time out must be after time in...";
        }
        return null;
    }

    // a helper method to translate friendly format to 24h format
    public static double isValidTime(Scanner in, double defaultValue, String errMsg) {
        for (;;) {
            String input = in.nextLine().trim();
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

            System.out.println(errMsg);
            System.out.print("> ");
        }
    }

    // repeatedly ask the user until valid string input is provided :)
    public static String validateInput(Scanner in, Predicate<String> validator, String errMsg) {
        String input;
        for (;;) {
            input = in.nextLine().trim();
            if (validator.test(input)) {
                return input;
            }
            System.out.println(errMsg);
            System.out.print("> ");
        }
    }

    public static char validateChar(Scanner in, IntPredicate validator, String errMsg) {
        for (;;) {
            String input = in.nextLine().trim();
            if (!input.isEmpty()) {
                char c = Character.toUpperCase(input.charAt(0));
                if (validator.test(c)) {
                    return c;
                }
            }
            System.out.println(errMsg);
            System.out.print("> ");
        }
    }

    public static double validateDouble(Scanner in, DoublePredicate validator, String errMsg) {
        for (;;) {
            String line = in.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println(errMsg);
                System.out.print("> ");
                continue;
            }
            try {
                // validate first if the input is actually a double
                double input = Double.parseDouble(line);
                // then verify it against the predicate the caller gave
                if (validator.test(input)) {
                    return input;
                }
                // it is a double but failed the specified validation rule
                System.out.println(errMsg);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
            System.out.print("> ");
        }
    }

    public static int validateInt(Scanner in, IntPredicate validator, String errMsg) {
        for (;;) {
            String line = in.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println(errMsg);
                System.out.print("> ");
                continue;
            }
            try {
                int input = Integer.parseInt(line);
                if (validator.test(input)) {
                    return input;
                }
                System.out.println(errMsg);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number!");
            }
            System.out.print("> ");
        }
    }
}
