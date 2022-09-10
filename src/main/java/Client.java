import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class Client {
    private final String msg;
    private final byte[] buffer = new byte[100];

    private DatagramSocket clientSocket;
    private int clientPort;

    private final MulticastSocket mcastSocketForRegularMsg;
    private final MulticastSocket mcastSocketForHiMsg;

    private final PortContext portContext;

    private final String mcastaddr;

    public Client(String mcastaddr, PortContext portContext) {
        try {
            this.mcastaddr = mcastaddr;
            this.portContext = portContext;

            mcastSocketForRegularMsg = new MulticastSocket(portContext.getRegularMsgGetPort());
            mcastSocketForHiMsg = new MulticastSocket(portContext.getHiMsgGetPort());

            mcastSocketForRegularMsg.joinGroup(InetAddress.getByName(this.mcastaddr));
            mcastSocketForHiMsg.joinGroup(InetAddress.getByName(this.mcastaddr));

            mcastSocketForRegularMsg.setSoTimeout(8000);
            mcastSocketForHiMsg.setSoTimeout(3000);

            initClientSocket();

            msg = "Hello from client " + clientPort;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendKeepAliveMsg() {
        try {
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, InetAddress.getByName(mcastaddr), portContext.getRegularMsgSendPort());       //send to server
            clientSocket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveKeepAliveMsg() {
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            mcastSocketForRegularMsg.receive(packet);
        } catch (SocketTimeoutException e) {
            System.out.println("Server is dead, restarting...");
            launchServer();
            sendHiMsgToServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void launchServer() {
        try {
            Server server = new Server(mcastaddr);
            ServerThread serverThread = new ServerThread(server);
            serverThread.start();
        } catch (SocketException ex) {
            System.out.println("error while launching server (prob. server is already raised up by another copy)");
            sendHiMsgToServer();
        }
    }

    public void startSession() {
        sendHiMsgToServer();
        while (true) {
            receiveKeepAliveMsg();
            sendKeepAliveMsg();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void initClientSocket() {
        clientPort = getAvailablePortFromServer();
        try {
            clientSocket = new DatagramSocket(clientPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private int getAvailablePortFromServer() {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            mcastSocketForHiMsg.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ByteBuffer.wrap(buffer).getInt();
    }

    private void sendHiMsgToServer() {
        try {
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, InetAddress.getByName(mcastaddr), portContext.getHiMsgSendPort());
            clientSocket.send(packet);

            packet = new DatagramPacket(buffer, buffer.length);
            mcastSocketForHiMsg.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}