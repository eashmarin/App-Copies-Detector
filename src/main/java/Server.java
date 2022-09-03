import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
    private final String msgToRecieve = "Hello World! from client";
    private final String msgToSend = "Hello World! from server";

    private int copiesCounter;

    private byte[] inputmsg = new byte[24];
    private DatagramSocket serverSocket;

    private Timer keepAliveTimer = new Timer();

    public Server() {
        try {
            serverSocket = new DatagramSocket(8000);
            serverSocket.setSoTimeout(0);

            keepAliveTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendMsg();
                }
            }, 1000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMsg() {
        try {
            DatagramPacket packet = new DatagramPacket(msgToSend.getBytes(), msgToSend.getBytes().length, InetAddress.getByName("224.0.1.1"), 8080);
            serverSocket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listen() {
        while (true) {
            try {
                System.out.println("listen() is called");
                DatagramPacket packet = new DatagramPacket(inputmsg, msgToRecieve.getBytes().length);
                serverSocket.receive(packet);

                copiesCounter++;

                System.out.println(String.format("packet #%d is accepted to Server (%s) ", copiesCounter, new String(packet.getData(), StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
