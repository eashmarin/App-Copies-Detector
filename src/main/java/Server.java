import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Server {
    private final String msgToSend = "Hello World! from server";

    private int copiesCounter;
    private int portCounter;

    private final byte[] buffer = new byte[40];

    private final DatagramSocket serverSocket;
    private final MulticastSocket mcastSocketForRegularMsg;
    private final MulticastSocket mcastSocketForHiMsg;

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

            mcastSocketForRegularMsg = new MulticastSocket(rKnownPort);
            mcastSocketForHiMsg = new MulticastSocket(rUnknownPort);

            mcastSocketForRegularMsg.joinGroup(InetAddress.getByName("224.0.1.1"));
            mcastSocketForHiMsg.joinGroup(InetAddress.getByName("224.0.1.1"));

            mcastSocketForRegularMsg.setSoTimeout(3000);
            mcastSocketForHiMsg.setSoTimeout(3000);
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
        int portToSend = 50000 + portCounter;
        byte[] data = ByteBuffer.allocate(4).putInt(portToSend).array();
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("224.0.1.1"), sUnknownPort);
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
            mcastSocketForRegularMsg.setSoTimeout(1000);

            packet = new DatagramPacket(buffer, buffer.length);

            addressList.clear();

            for (int i = 0; i < copiesCounter; i++) {
                mcastSocketForRegularMsg.receive(packet);
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
            mcastSocketForHiMsg.setSoTimeout(1000);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            mcastSocketForHiMsg.receive(packet);

            InetSocketAddress clientAddress = (InetSocketAddress) packet.getSocketAddress();

            System.out.println("socket address " + clientAddress + " is saved(" + packet.getAddress() + ")");
            copiesCounter++;
            portCounter++;
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
