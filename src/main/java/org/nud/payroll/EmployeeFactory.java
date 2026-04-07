package org.nud.payroll;

public class EmployeeFactory {
    public static Employee createEmployee(
            EmployeeType type, String id, String name, double basicRate, int cutOffPeriod) {
        return switch (type) {
            case REGULAR -> new RegularEmployee(id, name, basicRate, cutOffPeriod);
            case PROBATIONARY -> new ProbationaryEmployee(id, name, basicRate, cutOffPeriod);
            case CONTRACTUAL -> new ContractualEmployee(id, name, basicRate, cutOffPeriod);
            case PARTTIME -> new PartTimeEmployee(id, name, basicRate, cutOffPeriod);
        };
    }
}
