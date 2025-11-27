package com.chibao.backup_ulity_cli.service;

import org.springframework.stereotype.Service;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;

@Service
public class DatabaseConnectionService {

    public String testConnection(String dbType, String host, String port, String username, String password,
            String dbName) {
        try {
            if (dbType.equalsIgnoreCase("mongodb")) {
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
            }

            String url = "";
            String driverClassName = "";
            switch (dbType.toLowerCase()) {
                case "mysql":
                    if (port.isEmpty())
                        port = "3306";
                    url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
                    driverClassName = "com.mysql.cj.jdbc.Driver";
                    break;
                case "postgres":
                case "postgresql":
                    if (port.isEmpty())
                        port = "5432";
                    url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
                    driverClassName = "org.postgresql.Driver";
                    break;
                case "sqlite":
                    url = "jdbc:sqlite:" + dbName;
                    driverClassName = "org.sqlite.JDBC";
                    break;
                default:
                    return "Unsupported database type: " + dbType;
            }

            javax.sql.DataSource dataSource = DataSourceBuilder.create()
                    .url(url)
                    .username(username)
                    .password(password)
                    .driverClassName(driverClassName)
                    .build();

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            // Close the datasource if it's a HikariDataSource (default in Spring Boot) to
            // prevent leaks in CLI
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }

            return "Successfully connected to " + dbType + " database: " + dbName;

        } catch (Exception e) {
            return "Failed to connect to " + dbType + " database: " + e.getMessage();
        }
    }
}
