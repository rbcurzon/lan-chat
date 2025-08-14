package com.example;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort = 4445;
    private boolean running = false;
    private String clientId;
    
    public ChatClient() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        serverAddress = InetAddress.getLocalHost();
        clientId = "Client-" + socket.getLocalPort(); // Use port as unique ID
    }
    
    public void startListening() {
        running = true;
        
        Thread listenerThread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("\n" + message);
                    System.out.print(clientId + " - Enter message: ");
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error receiving message: " + e.getMessage());
                    }
                }
            }
        });
        
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    public void sendMessage(String message) throws IOException {
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverPort);
        socket.send(packet);
    }
    
    public void close() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
    
    public static void main(String[] args) {
        try {
            ChatClient client = new ChatClient();
            Scanner scanner = new Scanner(System.in);
            
            System.out.println(client.clientId + " started. Type 'quit' to exit.");
            System.out.println("Connecting to chat server...");
            
            // Start listening for messages
            client.startListening();
            
            // Send join message
            client.sendMessage(client.clientId + " has joined the chat");
            
            while (true) {
                System.out.print(client.clientId + " - Enter message: ");
                String message = scanner.nextLine();
                
                if (message.equalsIgnoreCase("quit")) {
                    client.sendMessage(client.clientId + " has left the chat");
                    break;
                }
                
                client.sendMessage(message);
            }
            
            client.close();
            scanner.close();
            System.out.println(client.clientId + " disconnected.");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
