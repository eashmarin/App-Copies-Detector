import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server {
    private final String msgToSend = "Hello World! from server";

    private int copiesCounter;

    private final byte[] buffer = new byte[40];

    private final DatagramSocket serverSocket;
    private final MulticastSocket rKnownSocket;
    private final MulticastSocket rUnknownSocket;
    private final MulticastSocket sKnownSocket;
    private final MulticastSocket sUnknownSocket;

    private static final int serverPort = 8080;
    private static final int rKnownPort = 9000;
    private static final int rUnknownPort = 9001;
    private static final int sKnownPort = 9002;
    private static final int sUnknownPort = 9003;

    private Timer keepAliveTimer = new Timer();

    private final List<InetSocketAddress> addressList = new LinkedList<>();

    public Server() {
        try {
            serverSocket = new DatagramSocket(serverPort);

            rKnownSocket = new MulticastSocket(rKnownPort);
            rUnknownSocket = new MulticastSocket(rUnknownPort);
            sKnownSocket = new MulticastSocket(sKnownPort);
            sUnknownSocket = new MulticastSocket(sUnknownPort);

            rKnownSocket.joinGroup(InetAddress.getByName("224.0.1.1"));
            rUnknownSocket.joinGroup(InetAddress.getByName("224.0.1.1"));
            sKnownSocket.joinGroup(InetAddress.getByName("224.0.1.1"));
            sUnknownSocket.joinGroup(InetAddress.getByName("224.0.1.1"));

            rKnownSocket.setSoTimeout(3000);
            rUnknownSocket.setSoTimeout(3000);
            serverSocket.setSoTimeout(3000);

            keepAliveTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    sendKeepAliveMsgs();
                    receiveKeepAliveMsg();
                }
            },0, 1000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendKeepAliveMsgs() {
        sendToKnownClients();
        sendToUnknownClients();
    }

    private void sendToKnownClients() {
        try {
            DatagramPacket packet = new DatagramPacket(msgToSend.getBytes(), msgToSend.getBytes().length, InetAddress.getByName("224.0.1.1"), sKnownPort);
            serverSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToUnknownClients() {
        try {
            DatagramPacket packet = new DatagramPacket(msgToSend.getBytes(), msgToSend.getBytes().length, InetAddress.getByName("224.0.1.1"), sUnknownPort);
            serverSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveKeepAliveMsg() {
        receiveFromKnownClients();
        receiveFromUnknownClients();
    }

    private void receiveFromKnownClients() {
        DatagramPacket packet = null;

        try {
            rKnownSocket.setSoTimeout(1000);

            packet = new DatagramPacket(buffer, buffer.length);

            addressList.clear();

            for (int i = 0; i < copiesCounter; i++) {
                rKnownSocket.receive(packet);
                //InetAddress senderAddress = InetAddress.getByName(packet.getData().toString());
                //System.out.println(senderAddress);
                //addressList.add(new InetSocketAddress());
                addressList.add((InetSocketAddress) packet.getSocketAddress());
                //System.out.println(String.format("Server receives packet (\"%s\") ", new String(packet.getData(), StandardCharsets.UTF_8)));
            }
        } catch (SocketTimeoutException e) {
            System.out.println("someone has dropped");
            copiesCounter--;
            printAliveClientsAddresses();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFromUnknownClients() {
        try {
            rUnknownSocket.setSoTimeout(1000);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            rUnknownSocket.receive(packet);

            InetSocketAddress clientAddress = (InetSocketAddress) packet.getSocketAddress();

            System.out.println("socket address " + clientAddress + " is saved(" + packet.getAddress() + ")");
            copiesCounter++;
            addressList.add(clientAddress);
            printAliveClientsAddresses();
        }
        catch (IOException e) {
            // no messages from unknown hosts
        }
    }

    private void printAliveClientsAddresses() {
        System.out.println("Alive clients: ");
        for (InetSocketAddress address: addressList) {
            System.out.println(address);
        }
    }

    public static boolean isAlive() {
        byte[] buf = new byte[100];

        try {
            MulticastSocket multicastSocket = new MulticastSocket(sUnknownPort);          // TODO: use socket class instance (should make instance static first)
            multicastSocket.joinGroup(InetAddress.getByName("224.0.1.1"));
            multicastSocket.setSoTimeout(5000);

            DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
            multicastSocket.receive(receivedPacket);
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
