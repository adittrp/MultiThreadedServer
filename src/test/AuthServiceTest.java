package test;

import model.AuthResult;
import database.AuthService;
import database.DatabaseManager;
import database.UserRepository;

public class AuthServiceTest {
    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();

        try {
            db.connect();
            db.initialize();

            UserRepository userRepository = new UserRepository(db.getConnection());
            AuthService authService = new AuthService(userRepository);

            AuthResult registerResult = authService.register("adit", "password123");
            System.out.println("Register success: " + registerResult.isSuccess());
            System.out.println("Register message: " + registerResult.getMessage());

            AuthResult loginResult = authService.login("adit", "password123");
            System.out.println("Login success: " + loginResult.isSuccess());
            System.out.println("Login message: " + loginResult.getMessage());

            AuthResult badLogin = authService.login("adit", "wrongpass");
            System.out.println("Bad login success: " + badLogin.isSuccess());
            System.out.println("Bad login message: " + badLogin.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }
}