package client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SimpleChatClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

        try (
                Socket socket = new Socket(host, port);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                Scanner sc = new Scanner(System.in);
        ) {
            System.out.println("Connected to server.");

            Thread thread = new Thread(() -> {
                try {
                    String response;
                    while ((response = reader.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (Exception e) {
                    System.out.println("Disconnected from server");
                }
            });

            thread.start();

            while (true) {
                String input = sc.nextLine();
                writer.println(input);
            }

        } catch (Exception e) {
            System.out.println("Could not connect: " + e.getMessage());
        }
    }
}
