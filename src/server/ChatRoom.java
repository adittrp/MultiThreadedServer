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
    }

    public void removeMember(ClientHandler client) {
        members.remove(client);
    }

    public void broadcast(String message) {
        for (ClientHandler client : members) {
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
