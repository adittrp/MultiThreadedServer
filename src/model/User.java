package model;

public class User {
    private final int id;
    private final String username;
    private final String passwordHash;
    private final String salt;
    private final String createdAt;

    public User(int id, String username, String passwordHash, String salt, String createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
