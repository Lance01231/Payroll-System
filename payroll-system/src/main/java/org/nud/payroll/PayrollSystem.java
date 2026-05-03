package org.nud.payroll;

import java.util.Scanner;

/**
 * Main entry point: Handles user input, employee creation, and payroll slip display
 * All computation logic is delegated to the Employee class hierarchy
 */
public class PayrollSystem {
    // private static final String BLUE = "\u001B[94m";
    // private static final String GREEN = "\u001B[92m";
    // private static final String YELLOW = "\u001B[93m";
    // private static final String RED = "\u001B[91m";
    // private static final String RESET = "\u001B[0m";

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("ABC Company");
        System.out.println("Employee Payroll System\n");

        System.out.print("Employee ID: ");
        String id = InputValidator.isValidEmployeeId(in);

        System.out.print("Employee Name: ");
        String name = InputValidator.isValidName(in);

        System.out.println("\nEmployee Type:");
        System.out.println("[R]egular");
        System.out.println("[P]robationary");
        System.out.println("[C]ontractual");
        System.out.println("[T]art-time");
        System.out.print("Enter choice: ");
        char typeChoice = InputValidator.isValidEmployeeType(in);

        System.out.print("\nBasic Salary (Monthly / Hourly for Part-time): ");
        double basicRate = InputValidator.isValidSalary(in);

        System.out.println("\nCut-off Period:");
        System.out.println("1 - 1st-15th of the month");
        System.out.println("2 - 16th-30th of the month");
        System.out.print("Enter choice (1 or 2): ");
        int cutOff = InputValidator.isValidCutOff(in);

        Employee emp =
                switch (typeChoice) {
                    case 'R' -> new RegularEmployee(id, name, basicRate, cutOff);
                    case 'P' -> new ProbationaryEmployee(id, name, basicRate, cutOff);
                    case 'C' -> new ContractualEmployee(id, name, basicRate, cutOff);
                    case 'T' -> new PartTimeEmployee(id, name, basicRate, cutOff);
                    // it should never reach this point
                    default -> throw new IllegalStateException("Unexpected value: " + typeChoice);
                };

        System.out.println("\nTimekeeping (15 days):");
        double[] timeIns = new double[15];
        double[] timeOuts = new double[15];

        // custom validation for paired time in/out
        for (int day = 1; day <= 15; day++) {
            double timeIn;
            double timeOut;
            for (;;) {
                System.out.print(day + " Time In  (e.g. 8, 11:45am): ");
                timeIn = InputValidator.isValidTime(
                        in, 0.0, "Enter valid time (e.g. 8, 8:00am) or 'absent'. Try again...");
                System.out.print("   Time Out (e.g. 17, 5pm): ");
                timeOut = InputValidator.isValidTime(
                        in, 0.0, "Enter valid time (e.g. 17, 5pm, 9:30pm) or 'absent'. Try again...");

                String errMsg = InputValidator.isValidWorkHours(timeIn, timeOut);
                if (errMsg == null) {
                    break;
                }
                System.out.println(errMsg);
            }
            timeIns[day - 1] = timeIn;
            timeOuts[day - 1] = timeOut;
        }

        emp.setTimeKeeping(timeIns, timeOuts);

        double leaveDaysUsed = 0.0;
        if (emp.hasLeaveBenefits()) {
            System.out.print("\nNumber of leave days used: ");
            leaveDaysUsed = InputValidator.isValidLeaveDays(in);
        }

        System.out.print("Loans: ");
        double loans = InputValidator.isValidLoans(in);

        double netPay = emp.getNetPay(leaveDaysUsed, loans);
        printPayrollSlip(emp, leaveDaysUsed, loans, netPay);
        in.close();
    }

    // whole numbers without decimals, otherwise 2 decimal places
    private static String format(double amount) {
        return amount % 1 == 0 ? String.format("%.0f", amount) : String.format("%.2f", amount);
    }

    private static void printPayrollSlip(Employee emp, double leaveDaysUsed, double loans, double netPay) {
        System.out.println("\n========================================");
        System.out.println("ABC Company");
        System.out.println("Employee Payroll System");
        System.out.println("========================================");

        System.out.println("Employee ID     : " + emp.getEmployeeNumber());
        System.out.println("Employee Name   : " + emp.getEmployeeName());
        System.out.println("Employee Type   : " + emp.employeeType());
        System.out.println("Basic Salary    : " + format(emp.getBasicRate())
                + (emp instanceof PartTimeEmployee ? " (Hourly)" : " (Monthly)"));
        System.out.println(
                "Cut-off Period  : " + (emp.getCutOffPeriod() == 1 ? "1st-15th" : "16th-30th") + " of the month");

        System.out.println("\nTotal Hours:");
        System.out.printf("Worked                     : %s%n", format(emp.getWorkedHours()));
        // System.out.printf(
        //         "Absent/Undertime           : %s days / %s hours%n",
        //         format(emp.getAbsentDays()), format(emp.getUndertimeHours()));
        System.out.printf(
                "Absent/Undertime           : %s%n", format(emp.getAbsentDays() * 8.0 + emp.getUndertimeHours()));
        System.out.printf("Overtime                   : %s%n", format(emp.getOvertimeHours()));

        System.out.println("\nBasic Salary: " + format(emp.getBasicRate()));
        System.out.println("Additional:");
        System.out.printf("  Overtime                 : %s%n", format(emp.getOvertimePay()));

        System.out.println("\nDeductions:");
        System.out.printf("  Undertime/Late           : %s%n", format(emp.getUndertimeDeduction()));
        System.out.printf("  Absences                 : %s%n", format(emp.getAbsencesDeduction(leaveDaysUsed)));
        System.out.printf("  SSS                      : %s%n", format(emp.getSSSContribution()));
        System.out.printf("  W/Tax                    : %s%n", format(emp.getWithholdingTax()));
        System.out.printf("  Pag-IBIG                 : %s%n", format(emp.getPagibigContribution()));
        System.out.printf("  PhilHealth               : %s%n", format(emp.getPhilhealthContribution()));
        System.out.printf("  Loans                    : %s%n", format(loans));
        System.out.println("========================================");
        System.out.printf("Net Pay                    : %s%n", format(netPay));
        System.out.println("========================================");
    }
}
