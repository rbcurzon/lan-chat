package com.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class EchoServer extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];

    public EchoServer() throws SocketException {
        try {
            socket = new DatagramSocket(4445);
            System.out.println("EchoServer started on port 4445");
        } catch (SocketException e) {
            System.err.println("Port 4445 is already in use. Please check if another server is running.");
            throw e;
        }
    }

    public void run() {
        running = true;

        while (running) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            String received = new String(packet.getData(), 0, packet.getLength());
            if (received.equals("end")) {
                running = false;
                continue;
            }
            try {                
                socket.send(packet);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        socket.close();
    }

    public void shutdown() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public static void main(String[] args) {
        try {
            new EchoServer().start();
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Echo Server started on port 4445");
    }
}