import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

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

    private static final int basePort = 10000 + (int) (Math.random() * 40000);

    private final String mcastaddr;

    private Timer keepAliveTimer = new Timer();

    private HashSet<InetSocketAddress> aliveCopies = new HashSet<>();

    public Server(String mcastaddr) {
        try {
            this.mcastaddr = mcastaddr;

            serverSocket = new DatagramSocket(serverPort);

            mcastSocketForRegularMsg = new MulticastSocket(rKnownPort);
            mcastSocketForHiMsg = new MulticastSocket(rUnknownPort);

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
        while(true) {
            receiveKeepAliveMsg();
            sendKeepAliveMsgs();
        }
    }
    private void sendKeepAliveMsgs() {
        sendToKnownClients();
        sendToUnknownClients();
    }

    private void sendToKnownClients() {
        try {
            DatagramPacket packet = new DatagramPacket(msgToSend.getBytes(), msgToSend.getBytes().length, InetAddress.getByName(mcastaddr), sKnownPort);
            serverSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToUnknownClients() {
        int portToSend = basePort + portCounter;
        byte[] data = ByteBuffer.allocate(4).putInt(portToSend).array();
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(mcastaddr), sUnknownPort);
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
            mcastSocketForRegularMsg.setSoTimeout(5000);

            packet = new DatagramPacket(buffer, buffer.length);

            aliveCopies.clear();

            while (aliveCopies.size() < copiesCounter) {
                mcastSocketForRegularMsg.receive(packet);
                InetSocketAddress copyAddress = (InetSocketAddress) packet.getSocketAddress();
                //System.out.println("server receive " + copyAddress.toString());


                if (aliveCopies.add(copyAddress))
                    System.out.println(String.format("Server receives packet (\"%s\") ", new String(packet.getData(), StandardCharsets.UTF_8)));

                //prevPacketChecksum = getCheckSum(packet.getData());
            }
        } catch (SocketTimeoutException e) {
            System.out.println("someone has dropped");
            copiesCounter--;
            printAliveClientsAddresses();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long getCheckSum(byte[] bytes) {
        Checksum crs32 = new CRC32();
        crs32.update(bytes, 0, bytes.length);
        return crs32.getValue();
    }

    private void receiveFromUnknownClients() {
        try {
            mcastSocketForHiMsg.setSoTimeout(1000);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            mcastSocketForHiMsg.receive(packet);

            InetSocketAddress clientAddress = (InetSocketAddress) packet.getSocketAddress();

                //System.out.println("socket address " + clientAddress + " is saved(" + aliveCopies.size() + ")");

                String response = "SAVED";

                packet = new DatagramPacket(response.getBytes(), response.getBytes().length, InetAddress.getByName(mcastaddr), sUnknownPort);
                serverSocket.send(packet);

                copiesCounter++;
                portCounter++;
                aliveCopies.add(clientAddress);
                printAliveClientsAddresses();
        }
        catch (IOException e) {
            // no messages from unknown hosts
        }
    }

    private void printAliveClientsAddresses() {
        System.out.println("Alive clients: ");
        for (InetSocketAddress address: aliveCopies) {
            System.out.println(address);
        }
    }

    public static boolean isAlive(String mcastaddr) {
        byte[] buf = new byte[100];

        try {
            MulticastSocket multicastSocket = new MulticastSocket(sUnknownPort);          // TODO: use socket class instance (should make instance static first)
            multicastSocket.joinGroup(InetAddress.getByName(mcastaddr));
            multicastSocket.setSoTimeout(5000);

            DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
            multicastSocket.receive(receivedPacket);

            //System.out.println("is alive receive packet: " + ByteBuffer.wrap(receivedPacket.getData()).toString());
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
