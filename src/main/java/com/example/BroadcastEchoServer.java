package com.example;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BroadcastEchoServer extends Thread {
    
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];
    
    private ConcurrentMap<String, ClientInfo> connectedClients = new ConcurrentHashMap<>();
    
    private static class ClientInfo {
        InetAddress address;
        int port;
        long lastSeen;
        
        ClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
            this.lastSeen = System.currentTimeMillis();
        }
        
        void updateLastSeen() {
            this.lastSeen = System.currentTimeMillis();
        }
    }
    
    public BroadcastEchoServer() throws SocketException {
        try {
            socket = new DatagramSocket(4445);
            System.out.println("BroadcastEchoServer started on port 4445");
        } catch (SocketException e) {
            System.err.println("Port 4445 is already in use. Please check if another server is running.");
            throw e;
        }
    }
    
    public void run() {
        running = true;
        
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                
                InetAddress senderAddress = packet.getAddress();
                int senderPort = packet.getPort();
                String message = new String(packet.getData(), 0, packet.getLength());
                
                System.out.println("Received from " + senderAddress + ":" + senderPort + " -> " + message);
                
                String clientKey = senderAddress.getHostAddress() + ":" + senderPort;
                connectedClients.put(clientKey, new ClientInfo(senderAddress, senderPort));
                
                if (message.equals("end")) {
                    running = false;
                    continue;
                }
                
                broadcastToAllClients(message, senderAddress, senderPort);
                
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        }
        socket.close();
    }
    
    private void broadcastToAllClients(String message, InetAddress senderAddress, int senderPort) {
        String formattedMessage = "[" + senderAddress.getHostAddress() + ":" + senderPort + "] " + message;
        byte[] broadcastData = formattedMessage.getBytes();
        
        long now = System.currentTimeMillis();
        connectedClients.entrySet().removeIf(entry -> now - entry.getValue().lastSeen > 30000);
        
        System.out.println("Broadcasting to " + connectedClients.size() + " clients: " + formattedMessage);
        
        for (ClientInfo client : connectedClients.values()) {
            try {
                DatagramPacket packet = new DatagramPacket(
                    broadcastData, 
                    broadcastData.length, 
                    client.address, 
                    client.port
                );
                socket.send(packet);
            } catch (IOException e) {
                System.err.println("Failed to send to client " + client.address + ":" + client.port);
                e.printStackTrace();
            }
        }
    }
    
    public void shutdown() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
    
    public static void main(String[] args) {
        try {
            new BroadcastEchoServer().start();
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
