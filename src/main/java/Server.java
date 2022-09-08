import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Server {
    private int copiesCounter;

    private final byte[] buffer = new byte[100];

    private final DatagramSocket serverSocket;
    private final MulticastSocket mcastSocketForRegularMsg;
    private final MulticastSocket mcastSocketForHiMsg;

    private static final int serverPort = 8080;

    private static final int regularMsgGetPort = 9000;
    private static final int hiMsgGetPort = 9001;
    private static final int regularMsgSendPort = 9002;
    private static final int hiMsgSendPort = 9003;

    private final String mcastaddr;

    private final HashSet<InetSocketAddress> aliveCopies = new HashSet<>();

    public Server(String mcastaddr) throws SocketException {
        serverSocket = new DatagramSocket(serverPort);

        this.mcastaddr = mcastaddr;

        try {
            mcastSocketForRegularMsg = new MulticastSocket(regularMsgGetPort);
            mcastSocketForHiMsg = new MulticastSocket(hiMsgGetPort);

            mcastSocketForRegularMsg.joinGroup(InetAddress.getByName(mcastaddr));
            mcastSocketForHiMsg.joinGroup(InetAddress.getByName(mcastaddr));

            mcastSocketForRegularMsg.setSoTimeout(3000);
            mcastSocketForHiMsg.setSoTimeout(3000);
            serverSocket.setSoTimeout(3000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startSession() {
        while (true) {
            receiveKeepAliveMsg();
            sendKeepAliveMsg();
        }
    }

    private void sendKeepAliveMsg() {
        sendRegularMsg();
        sendAvailablePortToNewCopies();
    }

    private void sendRegularMsg() {
        try {
            String msg = "Hello from server";
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, InetAddress.getByName(mcastaddr), regularMsgSendPort);
            serverSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAvailablePortToNewCopies() {
        int portToSend = 10000 + (int) (Math.random() * 40000);
        byte[] data = ByteBuffer.allocate(4).putInt(portToSend).array();
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(mcastaddr), hiMsgSendPort);
            serverSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveKeepAliveMsg() {
        receiveRegularMsg();
        receiveHiMsg();
    }

    private void receiveRegularMsg() {
        try {
            mcastSocketForRegularMsg.setSoTimeout(5000);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            aliveCopies.clear();

            while (aliveCopies.size() < copiesCounter) {
                mcastSocketForRegularMsg.receive(packet);
                aliveCopies.add((InetSocketAddress) packet.getSocketAddress());
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Someone has disconnected...");
            copiesCounter--;
            printAliveClientsAddresses();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveHiMsg() {
        try {
            mcastSocketForHiMsg.setSoTimeout(1000);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            mcastSocketForHiMsg.receive(packet);

            aliveCopies.add((InetSocketAddress) packet.getSocketAddress());
            copiesCounter++;

            System.out.println("Someone has connected...");
            printAliveClientsAddresses();

            String response = "SAVED";
            packet = new DatagramPacket(response.getBytes(), response.getBytes().length, InetAddress.getByName(mcastaddr), hiMsgSendPort);
            serverSocket.send(packet);
        } catch (IOException e) {
            // no hi messages
        }
    }

    private void printAliveClientsAddresses() {
        System.out.println("Alive clients: ");
        for (InetSocketAddress address : aliveCopies) {
            System.out.println(address);
        }
    }

    public static boolean isAlive(String mcastaddr) {
        byte[] buf = new byte[100];

        try {
            MulticastSocket multicastSocket = new MulticastSocket(hiMsgSendPort);
            multicastSocket.joinGroup(InetAddress.getByName(mcastaddr));
            multicastSocket.setSoTimeout(5000);

            DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
            multicastSocket.receive(receivedPacket);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public static PortContext getPortContext() {
        PortContext context = new PortContext();

        context.setRegularMsgSendPort(regularMsgGetPort);
        context.setHiMsgSendPort(hiMsgGetPort);
        context.setRegularMsgGetPort(regularMsgSendPort);
        context.setHiMsgGetPort(hiMsgSendPort);

        return context;
    }
}