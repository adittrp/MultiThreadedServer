package database;

import model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private Connection connection;

    public MessageRepository(Connection connection) {
        this.connection = connection;
    }

    public boolean saveMessage(
            String senderUsername,
            String roomName,
            String recipientUsername,
            String messageType,
            String content
    ) {
        String sql = """
                INSERT INTO messages (
                    sender_username,
                    room_name,
                    recipient_username,
                    message_type,
                    content
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, senderUsername);
            statement.setString(2, roomName);
            statement.setString(3, recipientUsername);
            statement.setString(4, messageType);
            statement.setString(5, content);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Failed to save message: " + e.getMessage());
            return false;
        }
    }

    public List<Message> getMessagesForRoom(String roomName, int limit) {
        List<Message> roomMessages = new ArrayList<Message>();
        String sql = """
                SELECT *
                FROM messages
                WHERE room_name = ?
                ORDER BY created_at DESC
                LIMIT ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roomName);
            statement.setInt(2, limit);

            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    roomMessages.add(mapRowToMessage(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get room messages for: " + roomName, e);
        }

        return roomMessages;
    }

    public List<Message> getDirectMessage(String userA, String userB, int limit) {
        List<Message> directMessages = new ArrayList<Message>();
        String sql = """
                SELECT *
                FROM messages
                WHERE
                    (sender_username = ? AND recipient_username = ?)
                    OR
                    (sender_username = ? AND recipient_username = ?)
                ORDER BY created_at DESC
                LIMIT ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userA);
            statement.setString(2, userB);
            statement.setString(3, userB);
            statement.setString(4, userA);
            statement.setInt(5, limit);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    directMessages.add(mapRowToMessage(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get direct messages", e);
        }
        return directMessages;
    }

    private Message mapRowToMessage(ResultSet rs) throws SQLException {
        return new Message(
                rs.getInt("id"),
                rs.getString("sender_username"),
                rs.getString("room_name"),
                rs.getString("recipient_username"),
                rs.getString("message_type"),
                rs.getString("content"),
                rs.getString("created_at")
        );
    }
}
