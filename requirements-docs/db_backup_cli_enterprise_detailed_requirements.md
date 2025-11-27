# Multi-DB Backup CLI Utility — Detailed Enterprise Requirements

> This document expands the enterprise requirements into actionable, developer/DevOps/QA-ready items. It contains concrete implementation guidance, examples, acceptance criteria, and checklists for Docker, Kubernetes, observability, security, CI/CD, testing, performance, and runbooks.

---

## Table of Contents
1. Overview
2. Naming, Versioning & File Conventions
3. Backup & Restore Detailed Behaviour
4. Storage & Cloud Integration
5. Security & Secrets Management
6. Observability, Metrics & Alerting
7. Containerization & Kubernetes Deployment
8. CI/CD & Release Process
9. Testing Strategy (QA / QC)
10. Performance & Load Testing
11. Monitoring & SLO/SLI/SLAs
12. Disaster Recovery & Runbook
13. Acceptance Criteria & Milestones
14. Appendices (example configs)

---

## 1. Overview
This document defines precise behaviors, config keys, file formats, CLI flags, API/metric names, retry policies, and runbooks so engineers can implement and operate the backup CLI at enterprise standards.

---

## 2. Naming, Versioning & File Conventions
- **Backup filename pattern:** `{{app}}_{{dbtype}}_{{dbname}}_{{yyyyMMdd}}T{{HHmmss}}Z_{{uuid}}.tar.gz` (example: `dbbackup_postgres_company_20251126T132530Z_a1b2c3.tar.gz`).
- **Metadata file:** alongside each backup produce `{{backup-file}}.meta.json` containing JSON with keys: `filename`, `version`, `dbType`, `dbHost`, `dbPort`, `dbName`, `startTime`, `endTime`, `durationMs`, `sizeBytes`, `sha256`, `compressAlgo`, `encryptAlgo`, `chunks` (if multipart), `status`.
- **Versioning:** Semantic version the CLI: `vMAJOR.MINOR.PATCH`. Include `--version` flag.
- **Retention policy config:** in `application.yml` as `backup.retention.days` and `backup.retention.max_count`.

---

## 3. Backup & Restore Detailed Behaviour
### 3.1 Backup Flow (Full)
1. Validate DB connection (timeout configurable: `db.connect.timeoutMs`).
2. Acquire advisory lock (DB-specific) to prevent concurrent heavy backups for same DB (config: `backup.lock.timeoutMs`).
3. Stream export to a temporary working directory to avoid partial uploads (path config: `backup.workdir`).
4. For relational DBs use native dump utilities where available (recommended):
   - PostgreSQL: `pg_dump --format=custom --no-owner --no-acl --blobs` (piped to compression)
   - MySQL: `mysqldump --single-transaction --routines --events`
   - SQLite: `sqlite3 database.db ".backup stdout"`
   - MongoDB: `mongodump --archive` OR query-based logical export
5. Calculate SHA‑256 checksum (streamed) and write metadata JSON.
6. Compress using gzip (`.tar.gz`) or zip (configurable `backup.compress=GZIP|ZIP|NONE`).
7. Optionally encrypt backup using AES-256-GCM with an encryption key managed via KMS or Vault (see Security section). Store ephemeral key info in metadata.
8. Upload to configured storage (local/CLOUD). Use multipart upload for >100MB (S3, GCS). Implement exponential backoff and retries (base 2s, max retries 5).
9. Verify upload integrity by comparing checksums (local sha256 vs remote object ETag behavior; for multipart compute our own sha256 in metadata).
10. Mark metadata status `COMPLETED` or `FAILED`. Write final log.

### 3.2 Incremental & Differential (Outline)
- Use DB native WAL/ binlog positions:
  - Postgres: base backup + `pg_wal`/WAL archiving (future work)
  - MySQL: `binlog` stream positions
- Store index of last successful LSN/binlog position in `state/` folder and perform incremental exports from that position.

### 3.3 Restore Flow
1. Validate backup file metadata and checksum.
2. Download and verify file (and decrypt if encrypted).
3. Prepare target DB: ensure empty or run conflict policy (`--on-conflict=skip|overwrite|prompt`).
4. Use native restore tools:
   - `pg_restore --clean --if-exists --dbname` for postgres custom dumps.
   - `mysql` client for MySQL SQL files.
   - `mongorestore --archive` for MongoDB.
5. Validate restore by running a verification SQL/command from `health-check/` script (e.g., `SELECT count(*) FROM important_table;`) and compare to metadata stats if available.
6. Log and notify outcome.

### 3.4 Partial / Selective Restore
- Supported for DBs with per-table export (pg_dump can dump a set of tables). CLI flags: `--tables t1,t2` or `--collections c1,c2`.
- Ensure referential integrity; warn user if foreign keys may be broken.

### 3.5 Consistency Guarantees
- For large transactional DBs use native snapshot capabilities (pg_dump with consistent snapshot or LVM snapshot) to ensure point-in-time consistent backups.

---

## 4. Storage & Cloud Integration
### 4.1 Local Storage
- `backup.local.path` default `/var/lib/dbbackup/` or user-specified.
- Implement rotate & prune: a background job prunes files older than retention policy.

### 4.2 AWS S3 (example)
- Use AWS SDK v2 with async HTTP client.
- Support multipart upload for files > 100MB. Use part size 50MB default and concurrency 4.
- S3 object key prefix: `{{env}}/{{app}}/{{dbtype}}/{{dbname}}/{{yyyy}}/{{MM}}/{{filename}}`.
- Lifecycle rules: allow user to configure on bucket for Glacier transition and expiration. Document recommended rules.
- IAM policy example (least privilege): `s3:PutObject`, `s3:GetObject`, `s3:ListBucket`, `s3:DeleteObject` on the backup bucket/prefix.

### 4.3 GCS & Azure
- Similar: use respective SDKs and multipart/resumable uploads. Document differences in ETag and checksum behavior.

### 4.4 Object Storage Alternative for Dev/Test
- Recommend MinIO (S3-compatible) running in Docker for local testing and CI integration.

---

## 5. Security & Secrets Management
### 5.1 Secrets Handling
- **Never log plain-text credentials.**
- Support three secrets delivery methods (priority order):
  1. External KMS/Vault (HashiCorp Vault, AWS KMS + Secrets Manager)
  2. Environment variables injected by process manager or container orchestration
  3. Encrypted config file (local dev only)
- Implementation: Provide `SecretProvider` interface with implementations for `VaultSecretProvider`, `EnvSecretProvider`, `FileSecretProvider`.

### 5.2 Backup Encryption
- AES-256-GCM for confidentiality and integrity.
- Key management: keys stored in KMS; per-backup ephemeral data encryption keys (DEK) wrapped by KMS key encryption key (KEK). Store KEK reference in metadata.
- Provide `--encrypt` flag and `backup.encrypt.algorithm` config.

### 5.3 Key Rotation
- Support re-encrypting backups during rotation via background job: fetch DEK encrypted with old KEK, unwrap with KMS, re-wrap with new KEK, and update metadata. Provide CLI `rotate-keys` command.

### 5.4 Least Privilege & IAM
- Provide IAM policy examples for S3 buckets, GCS service accounts, Azure roles.
- For Kubernetes, use K8s ServiceAccount and IRSA (IAM Roles for Service Accounts) for AWS EKS.

---

## 6. Observability, Metrics & Alerting
### 6.1 Metrics (Micrometer)
- **Metric names & tags** (Prometheus style):
  - `dbbackup_backup_duration_seconds{dbtype="postgres",result="success|failure"}`
  - `dbbackup_backup_size_bytes{dbtype="postgres"}`
  - `dbbackup_backup_retry_count{dbtype="postgres"}`
  - `dbbackup_upload_duration_seconds{provider="s3"}`
  - `dbbackup_restore_duration_seconds{dbtype="mysql",result="success"}`
- Expose `/actuator/prometheus` (Spring Boot Actuator) for scraping.

### 6.2 Logging
- Use JSON logging format with fields: `timestamp`, `level`, `logger`, `thread`, `traceId` (if available), `event`, `backupId`, `message`, `metadata`.
- Sample Log Event: `{"ts":"2025-11-26T13:25:30Z","level":"INFO","event":"BACKUP_STARTED","backupId":"a1b2","db":"company","size":0}`

### 6.3 Tracing
- Add optional distributed tracing with OpenTelemetry. Export to Jaeger.
- Trace spans: `backup:export`, `backup:compress`, `backup:encrypt`, `backup:upload`.

### 6.4 Alerts (Prometheus Alertmanager)
- **Example rules:**
  - `DBBackupFailed` → fire if `increase(dbbackup_backup_duration_seconds{result="failure"}[1h]) > 0`.
  - `HighBackupDuration` → fire if `dbbackup_backup_duration_seconds` > threshold (configurable) for 3 incidents.
- Send alerts to Slack channel via Alertmanager webhook.

### 6.5 Dashboards (Grafana)
- Panels: Last 24h successful vs failed backups, average duration, bytes uploaded per day, per-db success rate, last backup per DB.
- Variables: `env`, `dbtype`, `dbname`.

---

## 7. Containerization & Kubernetes Deployment
### 7.1 Docker
- **Dockerfile (multi-stage):**
  - Build stage: use `maven:3.8-jdk-17` to build fat jar.
  - Runtime stage: lightweight image `eclipse-temurin:17-jre-alpine` or `distroless`.
- Expose minimal ports (actuator) only.
- Provide sample `docker-compose.yml`:
  - Services: `dbbackup-cli` (for ad-hoc run), `minio` (dev S3), `postgres` (test DB), `prometheus`, `grafana`.

### 7.2 Kubernetes
- **Deployment types:**
  - **CronJob** (K8s native) for scheduled backups (recommended for cluster deployments).
  - **Job** for one-off operations.
- **K8s objects to provide:**
  - `Namespace`: `dbbackup`.
  - `Secret`/`ExternalSecret` for credentials (or use CSI driver for Vault).
  - `ConfigMap` for non-sensitive config.
  - `ServiceAccount` & `RBAC` rules.
  - `CronJob` manifest with resource `requests/limits` and concurrency policy `Forbid`.
  - `PersistentVolumeClaim` if using PVC for temporary storage (or mount ephemeral `emptyDir`).
- **Helm chart structure:** `charts/dbbackup/` with templates for CronJob, ConfigMap, Secret, RBAC, ServiceAccount, Service, HPA (if needed).

### 7.3 Resource Recommendations
- Small DB: `cpu: 500m, memory: 512Mi`
- Large DB export jobs: `cpu: 2000m, memory: 4Gi` with `ephemeral-storage` and temp volume size configurable.
- Node affinity and taints for backup jobs targeting nodes with high I/O.

---

## 8. CI/CD & Release Process
### 8.1 Pipeline Stages (GitHub Actions example)
1. `build`: compile and run unit tests.
2. `integration-test`: bring up Testcontainers / docker-compose and run integration tests.
3. `static-analysis`: run Checkstyle, SpotBugs, SonarQube scan.
4. `package`: build executable JAR and Docker image.
5. `publish`: push Docker image to registry (DockerHub/ECR/GCR).
6. `deploy`: optionally deploy Helm chart to staging cluster.
7. `smoke-tests`: run smoke e2e tests in staging.

### 8.2 Versioning and Releases
- Use GitFlow or trunk-based with semantic version tags.
- On release tag, create GitHub release, push Docker image tag, publish release notes.

### 8.3 Secrets in CI
- Store cloud keys as encrypted secrets in GitHub Actions/CI. Use short-lived tokens rotated regularly.

---

## 9. Testing Strategy (QA / QC)
### 9.1 Unit Tests
- Use JUnit 5 and Mockito. Aim for >70% coverage on critical modules (backup, storage, encryption).

### 9.2 Integration Tests
- Use Testcontainers to start real DBs (Postgres, MySQL, MongoDB) and MinIO.
- Test full backup → upload → download → restore flow.

### 9.3 End-to-End Tests
- Use CI to run scripted CLI flows against a staging environment.
- Test scenarios: small DB backup, large DB backup, interrupted upload and resume, restore to new DB.

### 9.4 QA Checklist (Manual)
- Validate credential handling and no secrets in logs.
- Verify retention policy enforcement.
- Verify metadata creation and integrity checksum matches.
- Validate scheduler triggers at correct intervals.
- Verify alerts and Slack notifications.

### 9.5 QA Automation Tools
- Use Bats or ShellSpec for shell CLI tests.
- Use Robot Framework or Cypress for orchestrating sequences.

---

## 10. Performance & Load Testing
- Use **JMeter** or **Locust** to simulate concurrent backup requests (for a SaaS case where multiple DB backups run concurrently).
- Test IO throughput and effect on source DB under load (with read-only snapshot operations).
- Measure metrics: backup throughput MB/s, CPU, memory, I/O.
- Optimize: increase concurrency, tune JDBC fetch size, use streaming, offload to dedicated backup node.

---

## 11. Monitoring, SLO/SLI/SLA
- Define SLOs: e.g., `99.9%` successful backups for scheduled jobs per week.
- SLIs: number of successful backups vs scheduled, average backup duration.
- Create dashboard with error rate and recent failed jobs.

---

## 12. Disaster Recovery & Runbook
- **Runbook tasks:**
  1. If backup job fails, retrieve last known good backup from S3/MinIO.
  2. For data corruption: attempt restore to staging first.
  3. If KMS inaccessible: follow escalation to KMS admin for key unwrap.
- **Playbook:** step-by-step commands for `restore --file`, configuration to point to alternate storage, and contact list for escalations.

---

## 13. Acceptance Criteria & Milestones
### MVP Acceptance Criteria
- `backup` command performs full backup of Postgres/MySQL and creates `.tar.gz` with `.meta.json`.
- `test-connection` validates DB connectivity and fails clearly on bad creds.
- Logs contain JSON events for start/complete with backupId.
- CLI packaged as executable JAR and Docker image.

### Advanced Feature Acceptance
- Backup uploaded to MinIO/S3 with checksum verification.
- `restore` restores database and post-restore healthchecks pass.
- Prometheus metrics exposed and Grafana dashboard created.
- CI pipeline builds Docker image and runs integration tests on PRs.

---

