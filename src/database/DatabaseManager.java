package database;

import java.sql.*;

public class DatabaseManager {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
        System.out.println("Connected!");
        conn.close();
    }

    private static final String DB_URL = "jdbc:sqlite:chatserver.db";

    private Connection connection;

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) { return; }

        connection = DriverManager.getConnection(DB_URL);
        System.out.println("Connected to SQLite Database");
    }

    public void initialize() throws SQLException {
        createUsersTable();
        createMessagesTable();
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        if (connection == null) { return; }

        try {
            if (!connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.out.println("Error closing database: " + e);
        }
    }

    public void createUsersTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                );
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public void createMessagesTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sender_username TEXT NOT NULL,
                room_name TEXT,
                recipient_username TEXT,
                message_type TEXT NOT NULL,
                content TEXT NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                );
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
