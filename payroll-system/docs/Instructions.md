## Result 1: Project Specification

# Java Payroll System

Develop a **console-based Java Payroll program** that computes and displays the salary of an employee based on employee type.

## Employee Types

* **Regular**
* **Probationary**
* **Contractual**
* **Part-time**

## Cut-off Periods

* **1st to 15th**
* **16th to 30th**

### Work Schedule Assumptions

* Monday to Friday work week
* 8 hours per work day
* Work hours: **8:00 AM to 5:00 PM**
* 1 hour break

## Required Input Data

* Employee Number
* Employee Name
* Employee Type
* Basic Salary

  * Monthly rate for Regular, Probationary, and Contractual
  * Hourly rate for Part-time
* Cut-off Period
* Timekeeping per work day

  * Must consider **leaves for Regular employees**
* Loans

## Required Payroll Computation

Compute and display the following:

* **Hours Worked**

  * Regular hours
  * Overtime
  * Absences
  * Undertime
* **Gross Pay**
* **Deductions**

  * Tax
  * SSS
  * PhilHealth
  * PAG-IBIG
  * Loans
  * Undertime
  * Absences
* **Net Pay**

## Employee Type Rules

### a. Regular

* Has leave benefits
* Uses monthly rate

### b. Probationary

* Has leave benefits
* Uses monthly rate

### c. Contractual

* No leave benefits
* Uses monthly rate

### d. Part-time

* No work, no pay
* Uses hourly rate

## Required Research for Integration

Research and integrate the following into the payroll computation:

* Holiday overtime rates
* Tax table
* SSS table
* PAG-IBIG contribution
* PhilHealth contribution

## Result 2: Sample Console Run Format

# Sample Run

```text
ABC Company
Employee Payroll System

Employee ID:        1234-5678-90
Employee Name:      Juan Dela Cruz

Employee Type:
[R] Regular
[P] Probationary
[C] Contractual
[T] Part-time

Basic Salary:       Monthly / Hourly Rate

Cut-off Period:
[1] 1st-15th of the month
[2] 16th-30th of the month

Timekeeping:
                    Time In            Time Out
1                   _________          __________
2                   _________          __________
3                   _________          __________
4                   _________          __________
5                   _________          __________
6                   _________          __________
7                   _________          __________
8                   _________          __________
9                   _________          __________
10                  _________          __________

                    Time In            Time Out
11                  _________          __________
12                  _________          __________
13                  _________          __________
14                  _________          __________
15                  _________          __________

Total Hours:
Worked:             (computed)
Absent/Undertime:   (computed)
Overtime:           (computed)

Loans:              ________________

Basic Salary:       (computed)

Additional:
Overtime:           (computed)

Deductions:
Undertime/Late:     (computed)
Absences:           (computed)
SSS:                (computed)
W/Tax:              (computed)
Pag-IBIG:           (computed)
PhilHealth:         (computed)
Loans:              (input/computed)

Net Pay:            (computed)
```

## Result 3: Project Checklist and Grading Rubric

# Project Checklist

## Program Requirements

* [x] Program is console-based
* [x] No file I/O used
* [x] Menu-driven interface implemented
* [x] Uses objects or `ArrayList` and not static variables only

## OOP Structure

* [x] At least 3 user-defined classes
* [x] Attributes declared as `private`
* [x] Constructors implemented
* [x] Getters and setters used
* [x] Main class controls program flow only

## Payroll Functionality

* [x] Regular employee logic implemented correctly
* [x] Probationary employee logic implemented correctly
* [x] Contractual employee logic implemented correctly
* [x] Part-time logic implemented correctly
* [x] Correct computation of gross pay
* [x] Correct computation of deductions
* [x] Correct computation of net pay

## Output and Usability

* [x] Output follows the sample run format
* [x] Clear labels for payroll components
* [x] Readable prompts and layout

## Code Quality

* [x] Proper indentation and formatting
* [x] Meaningful variable, class, and method names
* [x] Comments explaining major logic
