package org.nud.payroll;

import java.util.Scanner;

public class PayrollSystem {
    // private static final String BLUE = "\u001B[94m";
    // private static final String GREEN = "\u001B[92m";
    // private static final String YELLOW = "\u001B[93m";
    // private static final String RED = "\u001B[91m";
    // private static final String RESET = "\u001B[0m";

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("ABC Company");
            System.out.println("Employee Payroll System\n");

            System.out.print("Employee ID: ");
            String id = InputValidator.validateInput(
                    sc,
                    employeeId -> InputValidator.isValidEmployeeId(employeeId),
                    "ID must be 3-10 alphanumeric characters!");

            System.out.print("Employee Name: ");
            String name = InputValidator.validateInput(
                    sc,
                    employeeName -> InputValidator.isValidName(employeeName),
                    "Name must be 6-50 letters only! (spaces, hyphens, apostrophes are allowed).");

            System.out.println("\nEmployee Type:");
            System.out.println("[R]egular");
            System.out.println("[P]robationary");
            System.out.println("[C]ontractual");
            System.out.println("[T]art-time");
            System.out.print("Enter choice: ");
            char typeChoice = InputValidator.validateChar(
                    sc, c -> c == 'R' || c == 'P' || c == 'C' || c == 'T', "Enter R, P, C, or T only!");

            EmployeeType empType =
                    switch (typeChoice) {
                        case 'R' -> EmployeeType.REGULAR;
                        case 'P' -> EmployeeType.PROBATIONARY;
                        case 'C' -> EmployeeType.CONTRACTUAL;
                        case 'T' -> EmployeeType.PARTTIME;
                        // it should never reach this point
                        default -> throw new IllegalStateException("Unexpected value: " + typeChoice);
                    };

            System.out.print("\nBasic Salary (Monthly / Hourly for Part-time): ");
            double basicRate = InputValidator.validateDouble(
                    sc, salary -> InputValidator.isValidSalary(salary), "Salary must be between 500 and 500,000!");

            System.out.println("\nCut-off Period:");
            System.out.println("1 - 1st-15th of the month");
            System.out.println("2 - 16th-30th of the month");
            System.out.print("Enter choice (1 or 2): ");
            int cutOff = InputValidator.validateInt(
                    sc, period -> InputValidator.isValidCutOff(period), "Must be 1 or 2 only!");

            System.out.println("\nTimekeeping (15 days):");
            double[] timeIns = new double[15];
            double[] timeOuts = new double[15];

            // custom validation for paired time in/out
            for (int day = 1; day <= 15; day++) {
                double timeIn;
                double timeOut;
                for (;;) {
                    System.out.print(day + " Time In  (e.g. 8, 11:45am): ");
                    timeIn = InputValidator.isValidTime(sc, 0.0, "Enter valid time (e.g. 8, 8:00am) or 'absent'.");
                    System.out.print("   Time Out (e.g. 17, 5pm): ");
                    timeOut =
                            InputValidator.isValidTime(sc, 0.0, "Enter valid time (e.g. 17, 5pm, 9:30pm) or 'absent'.");

                    if (InputValidator.isValidTimePair(timeIn, timeOut)) {
                        break;
                    }
                    System.out.println("Time Out must be after Time In...");
                }
                timeIns[day - 1] = timeIn;
                timeOuts[day - 1] = timeOut;
            }

            Employee emp = EmployeeFactory.createEmployee(empType, id, name, basicRate, cutOff);
            emp.setTimeKeeping(timeIns, timeOuts);

            double leaveDaysUsed = 0.0;
            if (emp.hasLeaveBenefits()) {
                System.out.print("\nNumber of leave days used: ");
                leaveDaysUsed = InputValidator.validateDouble(
                        sc, days -> InputValidator.isValidLeaveDays(days), "Leave days must be between 0 and 15!");
            }

            System.out.print("Loans: ");
            double loans = InputValidator.validateDouble(
                    sc, loanAmount -> InputValidator.isValidLoans(loanAmount), "Loans must be between 0 and 100,000!");

            double netPay = PayrollCalculator.calculateNetPay(emp, leaveDaysUsed, loans);

            printPayrollSlip(emp, leaveDaysUsed, loans, netPay);

            System.out.println("\nPayroll processing complete.");
        }
    }

    private static void printPayrollSlip(Employee emp, double leaveDaysUsed, double loans, double netPay) {
        double grossPay = emp.calculateGrossPay();
        double absencesDed = emp.calculateAbsencesDeduction(leaveDaysUsed);
        double undertimeDed = emp.calculateUndertimeDeduction();
        double sss = emp.getSSSContribution();
        double philhealth = emp.getPhilhealthContribution();
        double pagibig = emp.getPagibigContribution();
        double tax = emp.getWithholdingTax(grossPay);

        System.out.println("\n========================================");
        System.out.println("ABC Company");
        System.out.println("Employee Payroll System");
        System.out.println("========================================");

        System.out.println("Employee ID     : " + emp.getEmployeeNumber());
        System.out.println("Employee Name   : " + emp.getEmployeeName());
        System.out.println("Employee Type   : " + emp.getEmployeeType());
        System.out.println("Basic Salary    : " + String.format("%.2f", emp.getBasicRate())
                + (emp instanceof PartTimeEmployee ? " (Hourly)" : " (Monthly)"));
        System.out.println(
                "Cut-off Period  : " + (emp.getCutOffPeriod() == 1 ? "1st-15th" : "16th-30th") + " of the month");

        System.out.println("\nTotal Hours:");
        System.out.printf("Worked                     : %.2f%n", emp.getWorkedHours());
        System.out.printf("Absent                     : %.2f days%n", emp.getAbsentDays());
        System.out.printf("Undertime                  : %.2f hours%n", emp.getUndertimeHours());
        System.out.printf("Overtime                   : %.2f%n", emp.getOvertimeHours());

        System.out.println("\nBasic Salary: " + String.format("%.2f", emp.getBasicRate()));
        System.out.println("Additional:");
        System.out.printf("  Overtime                 : %.2f%n", grossPay - (emp.getBasicRate() / 2.0));

        System.out.println("\nDeductions:");
        System.out.printf("  Undertime/Late           : %.2f%n", undertimeDed);
        System.out.printf("  Absences                 : %.2f%n", absencesDed);
        System.out.printf("  SSS                      : %.2f%n", sss);
        System.out.printf("  Philhealth               : %.2f%n", philhealth);
        System.out.printf("  Pag-IBIG                 : %.2f%n", pagibig);
        System.out.printf("  W/Tax                    : %.2f%n", tax);
        System.out.printf("  Loans                    : %.2f%n", loans);
        System.out.println("========================================");
        System.out.printf("Net Pay                    : %.2f%n", netPay);
        System.out.println("========================================");
    }
}
