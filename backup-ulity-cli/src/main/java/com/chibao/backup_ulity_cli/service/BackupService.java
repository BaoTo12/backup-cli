package com.chibao.backup_ulity_cli.service;

import org.springframework.stereotype.Service;

@Service
public class BackupService {

    public String performBackup(String dbType, String host, String username, String password, String dbName,
            String outputDir, String compress) {
        try {
            String fileName = dbName + "_" + System.currentTimeMillis() + ".sql";
            String fullPath = outputDir + "/" + fileName;

            // Ensure output directory exists
            new java.io.File(outputDir).mkdirs();

            ProcessBuilder pb;
            if (dbType.equalsIgnoreCase("mysql")) {
                pb = new ProcessBuilder(
                        "mysqldump",
                        "-h", host,
                        "-u", username,
                        "-p" + password, // Note: no space for password in mysqldump
                        dbName,
                        "-r", fullPath);
            } else if (dbType.equalsIgnoreCase("postgres") || dbType.equalsIgnoreCase("postgresql")) {
                pb = new ProcessBuilder(
                        "pg_dump",
                        "-h", host,
                        "-U", username,
                        "-d", dbName,
                        "-f", fullPath);
                // Postgres might require PGPASSWORD env var
                pb.environment().put("PGPASSWORD", password);
            } else if (dbType.equalsIgnoreCase("sqlite")) {
                pb = new ProcessBuilder(
                        "sqlite3",
                        dbName,
                        ".backup '" + fullPath + "'");
            } else if (dbType.equalsIgnoreCase("mongodb")) {
                fileName = dbName + "_" + System.currentTimeMillis(); // Folder for mongo
                fullPath = outputDir + "/" + fileName;
                pb = new ProcessBuilder(
                        "mongodump",
                        "--host", host,
                        "--username", username,
                        "--password", password,
                        "--db", dbName,
                        "--out", fullPath);
            } else {
                return "Unsupported database type: " + dbType;
            }

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Capture output
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // TODO: Compress file if requested
                return "Backup successful: " + fullPath;
            } else {
                return "Backup failed. Exit code: " + exitCode + "\nOutput: " + output.toString();
            }

        } catch (Exception e) {
            return "Backup failed: " + e.getMessage();
        }
    }
}
