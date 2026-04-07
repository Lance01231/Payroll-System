package org.nud.payroll;

import java.util.Scanner;

public class PayrollSystem {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("ABC Company");
            System.out.println("Employee Payroll System\n");

            System.out.print("Employee ID: ");
            String id = sc.nextLine();

            System.out.print("Employee Name: ");
            String name = sc.nextLine();

            System.out.println("\nEmployee Type:");
            System.out.println("[R]egular");
            System.out.println("[P]robationary");
            System.out.println("[C]ontractual");
            System.out.println("[T]art-time");
            System.out.print("Enter choice: ");
            char typeChoice = sc.nextLine().toUpperCase().charAt(0);

            EmployeeType empType =
                    switch (typeChoice) {
                        case 'R' -> EmployeeType.REGULAR;
                        case 'P' -> EmployeeType.PROBATIONARY;
                        case 'C' -> EmployeeType.CONTRACTUAL;
                        case 'T' -> EmployeeType.PARTTIME;
                        default -> {
                            System.out.println("Invalid type! Defaulting to Contractual.");
                            yield EmployeeType.CONTRACTUAL;
                        }
                    };

            System.out.print("\nBasic Salary (Monthly / Hourly for Part-time): ");
            double basicRate = sc.nextDouble();
            sc.nextLine();

            System.out.println("\nCut-off Period:");
            System.out.println("1 - 1st-15th of the month");
            System.out.println("2 - 16th-30th of the month");
            System.out.print("Enter choice (1 or 2): ");
            int cutOff = sc.nextInt();
            sc.nextLine();

            System.out.println("\nTimekeeping (15 days):");
            double[] timeIns = new double[15];
            double[] timeOuts = new double[15];

            for (int day = 1; day <= 15; day++) {
                System.out.print(day + " Time In  (e.g. 8.0 or 0.0 if absent): ");
                timeIns[day - 1] = sc.nextDouble();
                System.out.print("   Time Out (e.g. 17.0 or 0.0 if absent): ");
                timeOuts[day - 1] = sc.nextDouble();
            }

            Employee emp = EmployeeFactory.createEmployee(empType, id, name, basicRate, cutOff);
            emp.setTimeKeeping(timeIns, timeOuts);

            double leaveDaysUsed = 0.0;
            if (emp.hasLeaveBenefits()) {
                System.out.print("\nNumber of leave days used: ");
                leaveDaysUsed = sc.nextDouble();
            }

            System.out.print("Loans: ");
            double loans = sc.nextDouble();

            double netPay = PayrollCalculator.calculateNetPay(emp, leaveDaysUsed, loans);

            printPayrollSlip(emp, leaveDaysUsed, loans, netPay);

            System.out.println("\nPayroll processing complete. Thank you!");
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
        System.out.println("Employee Type   : " + emp.getTypeName());
        System.out.println("Basic Salary    : " + String.format("%.2f", emp.getBasicRate())
                + (emp instanceof PartTimeEmployee ? " (Hourly)" : " (Monthly)"));
        System.out.println(
                "Cut-off Period  : " + (emp.getCutOffPeriod() == 1 ? "1st-15th" : "16th-30th") + " of the month");

        System.out.println("\nTotal Hours:");
        System.out.printf("Worked                     : %.2f%n", emp.getWorkedHours());
        System.out.printf("Absent                     : %.2f days%n", emp.getAbsentDays());
        System.out.printf("Undertime                  : %.2f hours%n", emp.getUndertimeHours());
        System.out.printf("Overtime                   : %.2f%n", emp.getOtHours());

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
