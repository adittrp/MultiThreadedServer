package server;

import java.util.concurrent.ConcurrentHashMap;

public class CommandHandler {
    public static void handleCommand(ClientHandler sender, String input, ServerState serverState) {
        String[] splitInput = input.split(" ");
        String inputCommand = splitInput[0];

        switch (inputCommand) {
            case "/help":
                if (splitInput.length == 1) {
                    helpCommand(sender);
                } else {
                    sender.sendMessage("[SYSTEM] incorrect help command, example: '/help'");
                }
                break;
            case "/users":
                if (splitInput.length == 1) {
                    usersCommand(sender, serverState);
                } else {
                    sender.sendMessage("[SYSTEM] incorrect users command, example: '/users'");
                }
                break;
            case "/msg":
                if (splitInput.length > 2) {
                    msgCommand(sender, input.split(" ", 3), serverState);
                } else {
                    sender.sendMessage("[SYSTEM] incorrect msg command, example: '/msg <user> <message>'");
                }
                break;
            case "/make":
                if (splitInput.length == 2) {
                    makeCommand(sender, splitInput, serverState);
                } else {
                    sender.sendMessage("[SYSTEM] incorrect make command, example: '/make <room>'");
                }
                break;
            case "/join":
                if (splitInput.length == 2) {
                    joinCommand(sender, splitInput, serverState);
                } else {
                    sender.sendMessage("[SYSTEM] incorrect join command, example: '/join <room>'");
                }
                break;
            case "/rooms":
                if (splitInput.length == 1) {
                    roomsCommand(sender, serverState);
                } else {
                    sender.sendMessage("[SYSTEM] incorrect rooms command, example: '/rooms'");
                }
                break;
            default:
                sender.sendMessage("[SYSTEM] command does not exist, try /help for all commands");
                break;
        }
    }

    public static void helpCommand(ClientHandler sender) {
        sender.sendMessage("All commands:");
        sender.sendMessage("'/help' -> Shows all commands available");
        sender.sendMessage("'/users' -> Shows all online users");
        sender.sendMessage("'/msg <user> <message>' -> Message an online user");
        sender.sendMessage("'/make <room>' -> Allows you to make a new room");
        sender.sendMessage("'/join <room>' -> Allows you to join an existing room");
        sender.sendMessage("'/rooms' -> Shows all open rooms");
    }

    public static void usersCommand(ClientHandler sender, ServerState serverState) {
        sender.sendMessage("All online users:");

        ConcurrentHashMap<String, ClientHandler> users = serverState.getUsers();
        for (String clientName: users.keySet()) {
            sender.sendMessage(clientName + " in room: " + users.get(clientName).getCurrentRoom());
        }
    }

    public static void msgCommand(ClientHandler sender, String[] splitInput, ServerState serverState) {
        ConcurrentHashMap<String, ClientHandler> users = serverState.getUsers();

        if (users.containsKey(splitInput[1])) {
            users.get(splitInput[1]).sendMessage("[PRIVATE][" + sender.getCurrentRoom() + "] " + sender.getUsername() + ": " + splitInput[2]);
        } else {
            sender.sendMessage("No such user online...");
        }
    }

    public static void makeCommand(ClientHandler sender, String[] splitInput, ServerState serverState) {
        String room = splitInput[1];
        if (serverState.getRooms().containsKey(room)) {
            sender.sendMessage("Room already exists.");

        } else {
            sender.setCurrentRoom(splitInput[1]);
        }
    }

    public static void joinCommand(ClientHandler sender, String[] splitInput, ServerState serverState) {
        String room = splitInput[1];
        if (serverState.getRooms().containsKey(room)) {
            sender.setCurrentRoom(splitInput[1]);
        } else {
            sender.sendMessage("Room does not exist.");
        }
    }

    public static void roomsCommand(ClientHandler sender, ServerState serverState) {
        sender.sendMessage("All open rooms:");

        for (String roomName: serverState.getRooms().keySet()) {
            sender.sendMessage(roomName);
        }
    }
}
