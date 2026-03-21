package server;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerState {
    private final ConcurrentHashMap<String, ClientHandler> users;
    private final ConcurrentHashMap<String, ChatRoom> rooms;

    public ServerState(ConcurrentHashMap<String, ClientHandler> users, ConcurrentHashMap<String, ChatRoom> rooms) {
        this.users = users;
        this.rooms = rooms;
    }

    public boolean addUser(String username, ClientHandler client) {
        return users.putIfAbsent(username, client) == null;
    }

    public void removeUser(String username) {
        users.remove(username);
    }

    public ClientHandler getUser(String username) {
        return users.get(username);
    }

    public ChatRoom getOrCreateRoom(String roomName) {
        return rooms.computeIfAbsent(roomName, ChatRoom::new);
    }

    public ChatRoom getRoom(String roomName) {
        return rooms.get(roomName);
    }

    public void removeRoomIfEmpty(String roomName) {
        ChatRoom room = rooms.get(roomName);
        if (room != null && room.isEmpty()) {
            rooms.remove(roomName);
        }
    }

    public ConcurrentHashMap<String, ClientHandler> getUsers() {
        return users;
    }

    public ConcurrentHashMap<String, ChatRoom> getRooms() {
        return rooms;
    }
}
