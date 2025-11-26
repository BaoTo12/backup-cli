# Multi-DB Backup CLI Utility

## Project Description

This project is a **command-line interface (CLI) tool** for backing up and restoring databases. It is designed for **enterprise-level applications** and supports multiple database management systems (DBMS) such as MySQL, PostgreSQL, SQLite, and MongoDB. The tool provides automated backup scheduling, compression, local and cloud storage options, logging, and optional notifications.

**Core Functionalities:**

1. **Database Connectivity**
   - Connect to multiple DBMS: MySQL, PostgreSQL, SQLite, MongoDB.
   - Accept connection parameters: host, port, username, password, database name.
   - Test database connection before backup.
   - Graceful error handling.

2. **Backup Operations**
   - Full backups (initial version).
   - Optional incremental and differential backups.
   - Compress backup files (ZIP/GZIP).
   - Timestamped filenames for versioning.
   - Store backups locally or upload to cloud storage.

3. **Restore Operations**
   - Restore from backup files.
   - Optional selective restoration for tables or collections.
   - Validate restore success.

4. **Storage Options**
   - Local filesystem storage.
   - Cloud storage (AWS S3, Google Cloud Storage, Azure Blob Storage).

5. **Logging & Notifications**
   - Log all backup and restore operations: start/end times, status, duration, errors.
   - Optional Slack notifications for success/failure.

6. **Automation & Scheduling**
   - Periodic backups via Spring Boot Scheduler (`@Scheduled`) or OS-level cron/Task Scheduler.

7. **CLI Usability**
   - Commands: `backup`, `restore`, `status`, `test-connection`, `help`.
   - User-friendly messages and clear instructions.

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
| Optional CLI Enhancements | **JLine / Picocli** | For colored output, progress bars, interactive prompts. |

**Architecture Overview:**
```
 ┌─────────────────────────┐
 │ CLI Layer (Spring Shell │
 │ / Picocli)             │
 └───────────┬────────────┘
             │
             ▼
 ┌─────────────────────────┐
 │ Service Layer           │
 │ - BackupService         │
 │ - RestoreService        │
 │ - StorageService        │
 │ - LoggingService        │
 │ - NotificationService   │
 └───────────┬────────────┘
             │
             ▼
 ┌─────────────────────────┐
 │ Repository / DB Layer   │
 │ - Spring Data JPA       │
 │ - Spring Data MongoDB   │
 │ - JDBC (for direct ops) │
 └───────────┬────────────┘
             │
             ▼
 ┌─────────────────────────┐
 │ Storage Layer           │
 │ - Local filesystem      │
 │ - Cloud Storage Beans   │
 └─────────────────────────┘
```

**Advantages:**
- Enterprise-ready, modular architecture.
- Multi-DBMS support.
- Cross-platform CLI tool.
- Easy extension for encryption, cloud, incremental backup.
- Automated scheduling and robust logging.
- Resume and recover from failures reliably.