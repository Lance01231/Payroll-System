## Requirements

- **JDK 21...**
- **Apache Maven** (`mvn` on your PATH)
- **MySQL** (local or remote; app connects via JDBC)

## Run from the project root

Maven must be executed in the directory that contains **`pom.xml`** (not under `src/...`).

```bash
cd /path/to/payroll   # folder with pom.xml
mvn compile exec:java
