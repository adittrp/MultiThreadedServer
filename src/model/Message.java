package model;

public class Message {
    private final int id;
    private final String senderUsername;
    private final String roomName;
    private final String recipientUsername;
    private final String messageType;
    private final String content;
    private final String createdAt;

    public Message(int id, String senderUsername, String roomName, String recipientUsername, String messageType, String content, String createdAt) {
        this.id = id;
        this.senderUsername = senderUsername;
        this.roomName = roomName;
        this.recipientUsername = recipientUsername;
        this.messageType = messageType;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}