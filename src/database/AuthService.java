package database;

import model.AuthResult;
import model.User;
import util.PasswordHashing;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResult register(String username, String password) {
        username = normalizeUsername(username);

        if (username == null || username.isBlank()) {
            return AuthResult.failure("Username cannot be empty.");
        }

        if (password == null || password.isBlank()) {
            return AuthResult.failure("Password cannot be empty.");
        }

        if (username.length() < 3 || username.length() > 20) {
            return AuthResult.failure("Username must be between 3 and 20 characters.");
        }

        if (!username.matches("[A-Za-z0-9_]+")) {
            return AuthResult.failure("Username can only contain letters, numbers, and underscores.");
        }

        if (password.length() < 6) {
            return AuthResult.failure("Password must be at least 6 characters long.");
        }

        if (userRepository.userExists(username)) {
            return AuthResult.failure("Username is already taken.");
        }

        String salt = PasswordHashing.generateSalt();
        String passwordHash = PasswordHashing.hashPassword(password, salt);

        boolean created = userRepository.createUser(username, passwordHash, salt);
        if (!created) {
            return AuthResult.failure("Failed to create account.");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return AuthResult.failure("Account created, but failed to load user.");
        }

        return AuthResult.success("Registration successful.", user);
    }

    public AuthResult login(String username, String password) {
        username = normalizeUsername(username);

        if (username == null || username.isBlank()) {
            return AuthResult.failure("Username cannot be empty.");
        }

        if (password == null || password.isBlank()) {
            return AuthResult.failure("Password cannot be empty.");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return AuthResult.failure("User not found.");
        }

        boolean validPassword = PasswordHashing.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (!validPassword) {
            return AuthResult.failure("Incorrect password.");
        }

        return AuthResult.success("Login successful.", user);
    }

    private String normalizeUsername(String username) {
        if (username == null) return null;

        return username.trim();
    }
}
