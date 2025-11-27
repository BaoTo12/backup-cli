# Multi-DB Backup CLI Utility

**Example CLI Usage:**
```
backup-cli test-connection --dbtype mysql --host localhost --port 3306 --username root --password secret --dbname mydb

backup-cli backup --dbtype postgres --host localhost --username admin --dbname company --output /backups --compress gzip

backup-cli restore --file /backups/company_20251126.zip --dbtype postgres --host localhost --username admin --dbname company

backup-cli status --log /backups/logs/backup.log
```
---

## Tech Stack

| Layer / Feature | Technology / Library | Notes |
|-----------------|-------------------|-------|
| Application Framework | **Spring Boot 3.x** | Dependency injection, modular structure, auto-configuration. |
| CLI / Command Handling | **Spring Shell** or **Picocli with Spring Boot** | Interactive CLI and structured commands/subcommands. |
| Database Connectivity | **Spring Data JPA + JDBC Drivers**, **Spring Data MongoDB** | Connect to MySQL, PostgreSQL, SQLite, and MongoDB; easy abstraction and modularity. |
| Compression | **java.util.zip** | Built-in ZIP/GZIP compression. |
| Cloud Storage | AWS SDK v2 (S3), Google Cloud Storage SDK, Azure Blob SDK | Optional cloud storage integration using Spring Boot beans. |
| Logging | **SLF4J + Logback** | Enterprise logging, file logging, rolling files, log levels. |
| Configuration | Spring Boot **application.yml / properties** | Store default paths, DB connections, cloud credentials, environment-specific configs. |
| Scheduling / Automation | **Spring Boot Scheduler (@Scheduled)** | For automated periodic backups, cron expressions supported. |
| JSON Handling | **Jackson** | For config, logging, and cloud metadata. |
| Unit & Integration Testing | **JUnit 5 + Mockito + Spring Boot Test** | Test DB connections, backup/restore logic, and cloud integration. |
| Dependency Management & Build | **Maven or Gradle** | Handles project dependencies and builds executable JAR. |
| CLI Enhancements | **JLine / Picocli** | For colored output, progress bars, interactive prompts. |
