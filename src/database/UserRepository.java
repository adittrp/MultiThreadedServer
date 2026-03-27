package database;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {
    private final Connection connection;

    public UserRepository(Connection connection) {
        this.connection = connection;
    }

    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e){
            throw new RuntimeException("Failed to check if user exists: " + username, e);
        }
    }

    public User findByUsername(String username) {
        String sql = """
                SELECT id, username, password_hash, salt, created_at
                FROM users
                WHERE username = ?
                LIMIT 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) { return null; }

                return new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password_hash"),
                        resultSet.getString("salt"),
                        resultSet.getString("created_at")
                );
            }
        } catch (SQLException e){
            throw new RuntimeException("Failed to find user: " + username, e);
        }
    }

    public boolean createUser(String username, String passwordHash, String salt) {
        String sql = """
                INSERT INTO users (username, password_hash, salt)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, salt);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e){
            System.out.println("Failed to create user: " + e.getMessage());
            return false;
        }
    }
}
