package server;

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

    public ClientHandler(Socket socket, ServerState serverState) {
        this.socket = socket;
        this.serverState = serverState;
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
            sendMessage("[SYSTEM] Enter a username: ");

            while (true) {
                String inputName = reader.readLine();

                if (inputName == null) {
                    return;
                }

                inputName = inputName.trim();
                if (inputName.isEmpty()) {
                    sendMessage("[SYSTEM] Username cannot be empty, try again: ");
                    continue;
                }

                if (!serverState.addUser(inputName, this)) {
                    sendMessage("[SYSTEM] Username already exists, try again: ");
                    continue;
                }

                username = inputName;
                currentRoom = "lobby";
                connected = true;

                ChatRoom lobby = serverState.getOrCreateRoom("lobby");
                lobby.addMember(this);
                sendMessage("[SYSTEM] Username accepted. You joined lobby.");

                System.out.println(username + " logged in");
                break;
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
                    room.broadcast("[SYSTEM] " + username + " left the room", username);
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
