package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class ChatServer {
    private final int port;
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private final ServerState serverState;

    // Volatile makes sure changes to running in one thread are seen in other threads too
    private volatile boolean running;

    public ChatServer(int port, ServerState serverState) {
        this.port = port;
        this.serverState = serverState;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);

            // a Cached thread pool creates threads as needed, resuses old ones if possible, and grows dynamically
            pool = java.util.concurrent.Executors.newCachedThreadPool();
            running = true;

            System.out.println("Server started on port: " + port);

            while (running) {
                try {
                    // Waits till a client connects, then return a Socket (connection between the server and one specific client)
                    var socket = serverSocket.accept();
                    System.out.println("Client connected");

                    ClientHandler handler = new ClientHandler(socket, serverState);

                    // hands client task to thread pool so main server loop can go back to waiting for more clients
                    pool.submit(handler);
                } catch (Exception e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;

        try {
            // Is socket created and is it still open?
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            // does the thread pool exist and is it active?
            if (pool != null && !pool.isShutdown()) {
                pool.shutdownNow();
            }

            System.out.println("Server stopped");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(
                5000,
                new ServerState(
                        new ConcurrentHashMap<String, ClientHandler>(),
                        new ConcurrentHashMap<String, ChatRoom>()
                )
        );

        // Starts server on new thread, so the code can go ahead with the scanner input code
        new Thread(server::start).start();

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter in 'exit' to stop the server: ");
        while (true) {
            String message = sc.nextLine();
            if (message.equalsIgnoreCase("exit")) {
                System.out.println("Shutting off the server...");
                server.stop();
                break;
            }

            System.out.print("Enter in 'exit' to stop the server: ");

        }

        sc.close();
    }
}
