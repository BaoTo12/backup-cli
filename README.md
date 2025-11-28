# 🛡️ Multi-DB Backup CLI

> A powerful, containerized CLI tool for backing up and restoring PostgreSQL, MySQL, and MongoDB databases. Built with Spring Boot and Spring Shell.

![Version](https://img.shields.io/badge/version-0.0.1--SNAPSHOT-blue)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)

## 🚀 Features

-   **Multi-Database Support**: Seamlessly backup and restore PostgreSQL, MySQL, and MongoDB.
-   **Dockerized**: Runs entirely within Docker for consistent environments and easy deployment.
-   **Interactive CLI**: User-friendly command-line interface powered by Spring Shell.
-   **Local & Cloud Storage**: Save backups locally or extend to cloud providers (S3 support ready).
-   **Secure**: Handles credentials securely via environment variables.
-   **Observability**: Built-in Prometheus metrics for monitoring backup success/failure rates.

---

## 🛠️ Prerequisites

-   **Docker** & **Docker Compose** installed on your machine.
-   *(Optional)* **Java 17+** if running outside Docker.

---

## 🏁 Quick Start

### 1. Clone & Build
```bash
git clone <repository-url>
cd dbbackup-cli
docker-compose up -d --build
```

### 2. Access the CLI
We provide a handy PowerShell script to access the CLI directly:

```powershell
.\dbbackup.ps1
```

Or use the raw Docker command:
```bash
docker exec -it dbbackup-cli-app java -jar app.jar
```

---

## 📖 Usage Guide

### Interactive Mode
Run `.\dbbackup.ps1` to enter the interactive shell:

```text
shell:> help
```

### Backup Command
Backup a database to local storage:

```bash
# PostgreSQL
backup --dbType postgres --host postgres_test --port 5432 --database testdb --username user --password secret

# MySQL
backup --dbType mysql --host mysql_test --port 3306 --database testdb --username root --password secret

# MongoDB
backup --dbType mongo --host mongo_test --port 27017 --database testdb --username root --password secret
```

### Restore Command
Restore a database from a backup ID:

```bash
restore --backupId <BACKUP_ID> --targetHost postgres_test --targetPort 5432 --targetDatabase testdb --username user --password secret
```

### List Backups
View all available backups:

```bash
list-backups
```

---

## ⚙️ Configuration

The application is configured via `application.yml` and environment variables in `docker-compose.yml`.

| Variable | Description | Default |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | Metadata DB URL | `jdbc:postgresql://db:5432/db_backup_meta` |
| `STORAGE_PROVIDER` | Storage backend | `local` |
| `STORAGE_LOCAL_PATH` | Local backup path | `/var/lib/dbbackup` |

---

## 🏗️ Architecture

-   **Core**: Spring Boot 3.2 + Spring Shell
-   **Database**: PostgreSQL (Metadata storage)
-   **Containerization**: Docker + Docker Compose
-   **Base Image**: Alpine Linux (Eclipse Temurin JRE 17)

### 🧩 Hexagonal Architecture (Ports & Adapters)

This project follows the **Hexagonal Architecture** pattern to ensure the core domain logic remains independent of external frameworks and tools.

-   **Domain Layer (`domain`)**: Contains the core business logic and entities. It has no dependencies on Spring Boot or external libraries.
-   **Ports (`domain.port`)**: Interfaces that define how the domain interacts with the outside world.
    -   **Inbound Ports**: Use Cases (e.g., `BackupUseCase`, `RestoreUseCase`) invoked by the CLI.
    -   **Outbound Ports**: Interfaces for external services (e.g., `StoragePort`, `DatabaseDumpPort`, `MetricsPort`).
-   **Adapters (`adapter`)**: Implementations of the ports.
    -   **Inbound Adapters**: The CLI layer (`BackupCliAdapter`) that drives the application.
    -   **Outbound Adapters**: Concrete implementations for storage (`LocalStorageAdapter`), database operations (`PostgresDumpAdapter`), and observability (`PrometheusMetricsAdapter`).

### Directory Structure
```
dbbackup-cli/
├── src/                  # Source code
├── Dockerfile            # Multi-stage Docker build (Alpine)
├── docker-compose.yml    # Services (App, Postgres, MySQL, Mongo)
└── dbbackup.ps1          # Helper script for easy access
```

---

## 🤝 Contributing

1.  Fork the repository.
2.  Create a feature branch (`git checkout -b feature/amazing-feature`).
3.  Commit your changes.
4.  Push to the branch.
5.  Open a Pull Request.

---

Made with ❤️ by the **ChiBao Team**.
