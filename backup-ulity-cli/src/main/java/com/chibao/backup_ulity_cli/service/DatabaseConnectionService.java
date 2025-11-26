package com.chibao.backup_ulity_cli.service;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
public class DatabaseConnectionService {

    public String testConnection(String dbType, String host, String port, String username, String password,
            String dbName) {
        String url = "";
        try {
            switch (dbType.toLowerCase()) {
                case "mysql":
                    if (port.isEmpty())
                        port = "3306";
                    url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
                    break;
                case "postgres":
                case "postgresql":
                    if (port.isEmpty())
                        port = "5432";
                    url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
                    break;
                case "sqlite":
                    url = "jdbc:sqlite:" + dbName; // dbName is the file path for SQLite
                    break;
                case "mongodb":
                    if (port.isEmpty())
                        port = "27017";
                    String connectionString = "mongodb://" + (username.isEmpty() ? "" : username + ":" + password + "@")
                            + host + ":" + port + "/" + dbName;
                    try (com.mongodb.client.MongoClient mongoClient = com.mongodb.client.MongoClients
                            .create(connectionString)) {
                        // Trigger a command to verify connection
                        mongoClient.getDatabase(dbName).runCommand(new org.bson.Document("ping", 1));
                        return "Successfully connected to MongoDB database: " + dbName;
                    } catch (Exception e) {
                        return "Failed to connect to MongoDB: " + e.getMessage();
                    }
                default:
                    return "Unsupported database type: " + dbType;
            }

            if (!dbType.equalsIgnoreCase("sqlite")) {
                try (Connection conn = DriverManager.getConnection(url, username, password)) {
                    return "Successfully connected to " + dbType + " database: " + dbName;
                }
            } else {
                try (Connection conn = DriverManager.getConnection(url)) {
                    return "Successfully connected to " + dbType + " database: " + dbName;
                }
            }

        } catch (SQLException e) {
            return "Failed to connect to " + dbType + " database: " + e.getMessage();
        }
    }
}
