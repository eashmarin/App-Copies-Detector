import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server {
    private final String msgToRecieve = "Hello World! from client";
    private final String msgToSend = "Hello World! from server";

    private int copiesCounter;

    private byte[] inputmsg = new byte[24];
    private DatagramSocket serverSocket;

    private Timer keepAliveTimer = new Timer();

    private final List<SocketAddress> addressList = new LinkedList<>();

    public Server() {

        try {
            serverSocket = new DatagramSocket(8080);
            serverSocket.setSoTimeout(0);

            keepAliveTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    sendMsg();
                }
            },0, 1000);
            //keepAliveTimer.schedule(
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isAlive() {
        byte[] buf = new byte[100];

        try {
            MulticastSocket socket = new MulticastSocket(8000);
            socket.joinGroup(InetAddress.getByName("224.0.1.1"));

            socket.setSoTimeout(5000);

            DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);

            socket.receive(receivedPacket);

        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private void sendMsg() {
        try {
            DatagramPacket packet = new DatagramPacket(msgToSend.getBytes(), msgToSend.getBytes().length, InetAddress.getByName("224.0.1.1"), 8000);
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

                SocketAddress clientAddress = packet.getSocketAddress();

                if (!addressList.contains(clientAddress)) {
                    System.out.println("address " + clientAddress + " is saved");
                    addressList.add(clientAddress);
                }

                copiesCounter++;

                System.out.println(String.format("Server receives packet from copy #%d (\"%s\") ", copiesCounter, new String(packet.getData(), StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
