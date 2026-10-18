
package com.campus.emergency.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void createTables() {
        String usersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL
                        CHECK (role IN ('ADMIN', 'RESPONDER', 'REPORTER')),
                    is_active INTEGER NOT NULL DEFAULT 1
                        CHECK (is_active IN (0, 1)),
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                );
                """;

        String locationsTable = """
                CREATE TABLE IF NOT EXISTS locations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    building TEXT NOT NULL,
                    floor TEXT,
                    room TEXT,
                    latitude REAL,
                    longitude REAL
                );
                """;

        String respondersTable = """
                CREATE TABLE IF NOT EXISTS responders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER UNIQUE,
                    name TEXT NOT NULL,
                    responder_type TEXT NOT NULL
                        CHECK (responder_type IN (
                            'SECURITY', 'MEDICAL',
                            'MAINTENANCE', 'FIRE_SAFETY'
                        )),
                    location_id INTEGER,
                    is_available INTEGER NOT NULL DEFAULT 1
                        CHECK (is_available IN (0, 1)),
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (location_id) REFERENCES locations(id)
                );
                """;

        String incidentsTable = """
                CREATE TABLE IF NOT EXISTS incidents (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    reported_by INTEGER NOT NULL,
                    category TEXT NOT NULL
                        CHECK (category IN (
                            'FIRE', 'MEDICAL', 'ELECTRICAL',
                            'SECURITY', 'WATER'
                        )),
                    description TEXT NOT NULL,
                    location_id INTEGER NOT NULL,
                    priority TEXT NOT NULL DEFAULT 'MEDIUM'
                        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
                    status TEXT NOT NULL DEFAULT 'REPORTED'
                        CHECK (status IN (
                            'REPORTED', 'ASSIGNED',
                            'IN_PROGRESS', 'RESOLVED'
                        )),
                    assigned_responder_id INTEGER,
                    resolution_notes TEXT,
                    reported_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    resolved_at TEXT,
                    FOREIGN KEY (reported_by) REFERENCES users(id),
                    FOREIGN KEY (location_id) REFERENCES locations(id),
                    FOREIGN KEY (assigned_responder_id)
                        REFERENCES responders(id)
                );
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(usersTable);
            statement.execute(locationsTable);
            statement.execute(respondersTable);
            statement.execute(incidentsTable);

            System.out.println("Database tables created successfully!");

        } catch (SQLException e) {
            System.out.println("Failed to create database tables.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        createTables();
    }
}
