package server;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoom {
    private final String name;
    private final Set<ClientHandler> members;

    public ChatRoom(String name) {
        this.name = name;
        this.members = ConcurrentHashMap.newKeySet();
    }

    public String getName() {
        return name;
    }

    public void addMember(ClientHandler client) {
        members.add(client);
        broadcast("[SYSTEM] " + client.getUsername() + " joined the room.", client.getUsername());
    }

    public void removeMember(ClientHandler client) {
        members.remove(client);
        broadcast("[SYSTEM] " + client.getUsername() + " left the room.", client.getUsername());
    }

    public void broadcast(String message, String sender) {
        for (ClientHandler client : members) {
            if (client.getUsername().equals(sender)) {
                continue;
            }
            client.sendMessage(message);
        }
    }

    public Set<String> getMemberNames() {
        Set<String> names = new HashSet<String>();

        for (ClientHandler client: members) {
            names.add(client.getUsername());
        }

        return names;
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }
}
