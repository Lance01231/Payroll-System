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

* [ ] Program is console-based
* [ ] No file I/O used
* [ ] Menu-driven interface implemented
* [ ] Uses objects or `ArrayList` and not static variables only

## OOP Structure

* [ ] At least 3 user-defined classes
* [ ] Attributes declared as `private`
* [ ] Constructors implemented
* [ ] Getters and setters used
* [ ] Main class controls program flow only

## Payroll Functionality

* [ ] Regular employee logic implemented correctly
* [ ] Probationary employee logic implemented correctly
* [ ] Contractual employee logic implemented correctly
* [ ] Part-time logic implemented correctly
* [ ] Correct computation of gross pay
* [ ] Correct computation of deductions
* [ ] Correct computation of net pay

## Output and Usability

* [ ] Output follows the sample run format
* [ ] Clear labels for payroll components
* [ ] Readable prompts and layout

## Code Quality

* [ ] Proper indentation and formatting
* [ ] Meaningful variable, class, and method names
* [ ] Comments explaining major logic

# Grading Rubric

## OOP Design and Program Structure — 30 pts

* **30 – Excellent:** Proper class separation, encapsulation, clear OOP usage
* **22 – Satisfactory:** Classes exist but responsibilities overlap
* **15 – Developing:** Weak class design, limited OOP
* **5 – Needs Improvement:** Program mostly inside `main()`

## Payroll Computation Accuracy — 25 pts

* **25 – Excellent:** All computations correct
* **18 – Satisfactory:** Minor computation errors
* **12 – Developing:** Multiple incorrect computations
* **5 – Needs Improvement:** Incorrect payroll logic

## Employee Type Handling — 15 pts

* **15 – Excellent:** All employee types handled correctly
* **11 – Satisfactory:** One type handled incorrectly
* **7 – Developing:** Multiple types incorrect
* **3 – Needs Improvement:** No differentiation

## Input Validation and Stability — 5 pts

* **5 – Excellent:** Handles invalid input gracefully
* **3 – Satisfactory:** Minimal validation
* **1 – Needs Improvement:** Program crashes

## User Interaction and Output — 15 pts

* **15 – Excellent:** Clear menu and formatted output
* **11 – Satisfactory:** Output complete but poorly formatted
* **7 – Developing:** Confusing interface
* **3 – Needs Improvement:** Incomplete output

## Code Quality and Documentation — 10 pts

* **10 – Excellent:** Clean code, good naming, helpful comments
* **7 – Satisfactory:** Minor readability issues
* **4 – Developing:** Poor naming, few comments
* **1 – Needs Improvement:** Unreadable code
