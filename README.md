# clone the project
git clone https://github.com/jrsmDev/Payroll-System
cd payroll-system

# compile
mvn compile

# run with:
mvn -q exec:java
# or
java -cp target/classes org.nud.payroll.PayrollSystem
