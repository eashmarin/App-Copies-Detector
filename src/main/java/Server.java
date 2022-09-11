import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Server {
    private int copiesCounter;

    private final byte[] buffer = new byte[100];

    private final DatagramSocket serverSocket;
    private final MulticastSocket mcastSocketForKeepAliveMsg;
    private final MulticastSocket mcastSocketForHiMsg;

    private static final int serverPort = 1 + (int) (Math.random() * 65000);

    private final int keepAliveMsgGetPort;
    private final int hiMsgGetPort;
    private final int keepAliveMsgSendPort ;
    private final int hiMsgSendPort;

    private final String mcastaddr;

    private final HashSet<InetSocketAddress> aliveCopies = new HashSet<>();
    private final HashSet<InetSocketAddress> savedCopies = new HashSet<>();

    public Server(String mcastaddr, PortContext portContext) {
        this.mcastaddr = mcastaddr;

        this.keepAliveMsgGetPort = portContext.getKeepAliveMsgSendPort();
        this.hiMsgGetPort = portContext.getHiMsgSendPort();
        this.keepAliveMsgSendPort = portContext.getKeepAliveMsgGetPort();
        this.hiMsgSendPort = portContext.getHiMsgGetPort();

        try {
            serverSocket = new DatagramSocket(serverPort);
            mcastSocketForKeepAliveMsg = new MulticastSocket(keepAliveMsgGetPort);
            mcastSocketForHiMsg = new MulticastSocket(hiMsgGetPort);

            mcastSocketForKeepAliveMsg.joinGroup(InetAddress.getByName(mcastaddr));
            mcastSocketForHiMsg.joinGroup(InetAddress.getByName(mcastaddr));

            mcastSocketForKeepAliveMsg.setSoTimeout(500);
            mcastSocketForHiMsg.setSoTimeout(1000);
            serverSocket.setSoTimeout(3000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startSession() {
        while (true) {
            receiveMessages();
            sendMessages();
        }
    }

    private void sendMessages() {
        sendKeepAliveMsg();
        sendAvailablePortToNewCopies();
    }

    private void sendKeepAliveMsg() {
        try {
            String msg = "Hello from server";
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, InetAddress.getByName(mcastaddr), keepAliveMsgSendPort);
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

    public void receiveMessages() {
        receiveKeepAliveMsg();
        receiveHiMsg();
    }

    private void receiveKeepAliveMsg() {
        try {
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

            aliveCopies.clear();

            while (aliveCopies.size() < copiesCounter) {
                mcastSocketForKeepAliveMsg.receive(receivedPacket);
                aliveCopies.add((InetSocketAddress) receivedPacket.getSocketAddress());
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Someone has disconnected...");
            copiesCounter--;
            savedCopies.removeIf(address -> !aliveCopies.contains(address));
            printAliveClientsAddresses();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveHiMsg() {
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                mcastSocketForHiMsg.receive(packet);

                InetSocketAddress clientAddress = (InetSocketAddress) packet.getSocketAddress();

                if (!savedCopies.contains(clientAddress)) {
                    aliveCopies.add((InetSocketAddress) packet.getSocketAddress());
                    savedCopies.add(clientAddress);
                    copiesCounter++;

                    System.out.println("Someone has connected...");
                    printAliveClientsAddresses();
                }
            }

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
}