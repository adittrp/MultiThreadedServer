package server;

import database.AuthService;
import model.AuthResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ServerState serverState;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private String currentRoom;
    private boolean connected;

    private final AuthService authService;

    public ClientHandler(Socket socket, ServerState serverState, AuthService authService) {
        this.socket = socket;
        this.serverState = serverState;
        this.authService = authService;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            login();

            String input;
            while (connected && (input = reader.readLine()) != null) {
                handleInput(input);
            }
        } catch (Exception e) {
            System.out.println("Error with client " + username + ": " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void login() {
        try {
            sendMessage("[SYSTEM] Welcome to the server!");
            sendMessage("[SYSTEM] Do you want to register or login?");

            boolean registering = false;

            while (true) {
                String input = reader.readLine();

                if (input == null) {
                    return;
                }

                input = input.trim();

                if (input.equalsIgnoreCase("register")) {
                    registering = true;
                } else if (input.equalsIgnoreCase("login")) {
                    registering = false;
                } else {
                    sendMessage("[SYSTEM] Type 'register' or 'login':");
                    continue;
                }

                AuthResult result;
                String enteredUsername;
                String enteredPassword;

                do {
                    sendMessage("[SYSTEM] Enter a username:");
                    while (true) {
                        input = reader.readLine();

                        if (input == null) {
                            return;
                        }

                        input = input.trim();

                        if (input.isEmpty()) {
                            sendMessage("[SYSTEM] Username cannot be empty, try again:");
                            continue;
                        }

                        enteredUsername = input;
                        break;
                    }

                    sendMessage("[SYSTEM] Enter a password:");
                    while (true) {
                        input = reader.readLine();

                        if (input == null) {
                            return;
                        }

                        input = input.trim();

                        if (input.isEmpty()) {
                            sendMessage("[SYSTEM] Password cannot be empty, try again:");
                            continue;
                        }

                        enteredPassword = input;
                        break;
                    }

                    if (registering) {
                        result = authService.register(enteredUsername, enteredPassword);
                    } else {
                        result = authService.login(enteredUsername, enteredPassword);
                    }

                    sendMessage("[SYSTEM] " + result.getMessage());

                    if (result.isSuccess()) {
                        this.username = result.getUser().getUsername();
                        currentRoom = "lobby";
                        connected = true;

                        ChatRoom lobby = serverState.getOrCreateRoom("lobby");
                        lobby.addMember(this);

                        serverState.addUser(this.username, this);

                        sendMessage("[SYSTEM] Authentication successful. You joined lobby.");
                        System.out.println(this.username + " logged in");
                        return;
                    }

                } while (!result.isSuccess());
            }

        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private void handleInput(String input) {
        input = input.trim();

        if (input.isEmpty()) {
            return;
        }

        if (input.startsWith("/")) {
            CommandHandler.handleCommand(this, input, serverState);
            return;
        }

        ChatRoom room = serverState.getOrCreateRoom(currentRoom);
        room.broadcast("[PUBLIC][" + currentRoom + "] " + username + ": " + input, username);
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(String room) {
        ChatRoom oldRoom = serverState.getRoom(currentRoom);
        oldRoom.removeMember(this);

        currentRoom = room;
        ChatRoom newRoom = serverState.getOrCreateRoom(currentRoom);
        newRoom.addMember(this);

        sendMessage("[SYSTEM] You moved to room '" + room + "'");
    }

    private void disconnect() {
        if (!connected && username == null) {
            closerResources();
            return;
        }

        connected = false;

        try {
            if (username != null) {
                ChatRoom room = serverState.getRoom(currentRoom);
                if (room != null) {
                    room.removeMember(this);
                }

                serverState.removeUser(username);
                System.out.println(username + " disconnected.");
            }
        } catch (Exception e) {
            System.out.println("Disconnect error: " + e.getMessage());
        } finally {
            closerResources();
        }
    }

    private void closerResources() {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (writer != null) {
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
