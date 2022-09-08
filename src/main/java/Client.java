import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class Client {
    private String msg;

    private DatagramSocket clientSocket;
    private int clientPort;

    private final MulticastSocket mcastSocketForRegularMsg;
    private final MulticastSocket mcastSocketForHiMsg;

    private static final int sKnownPort = 9000;
    private static final int sUnknownPort = 9001;
    private static final int rKnownPort = 9002;
    private static final int rUnknownPort = 9003;

    private final String mcastaddr;

    public Client(String mcastaddr) { //int regularMsgGetPort, int regularMsgSendPort, int hiMsgGetPort, int hiMsgSendPort
        try {
            System.out.println("Init client...");
            this.mcastaddr = mcastaddr;

            mcastSocketForRegularMsg = new MulticastSocket(rKnownPort);
            mcastSocketForHiMsg = new MulticastSocket(rUnknownPort);

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

    public void setPort(int clientPort) {
        msg = "Hello from client " + clientPort;
        clientSocket.close();
        try {
            clientSocket = new DatagramSocket(clientPort);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendKeepAliveMsg() {
        try {
            String msgToSend =  msg +  ", msg_id = " + String.valueOf((int) (Math.random() * 100));
            DatagramPacket packet = new DatagramPacket(msgToSend.getBytes(), msgToSend.getBytes().length,  InetAddress.getByName(this.mcastaddr), sKnownPort);       //send to server
            clientSocket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveKeepAliveMsg() {
        byte[] buf = new byte[100];
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            mcastSocketForRegularMsg.receive(packet);
        } catch (SocketTimeoutException e) {
            System.out.println("Server is dead, restarting...");
            ServerThread serverThread = new ServerThread(mcastaddr);
            serverThread.start();
            clientSocket.close();
            Client newClient = new Client(mcastaddr);
            newClient.setPort(clientPort);
            newClient.sayHiToServer();
            newClient.startSession();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startSession() {
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

    public void initClientSocket() {
        clientPort = getUniquePortFromServer();
        try {
            clientSocket = new DatagramSocket(clientPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public int getUniquePortFromServer() {
        byte[] buff = new byte[100];
        DatagramPacket packet = new DatagramPacket(buff, buff.length);
        try {
            mcastSocketForHiMsg.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ByteBuffer.wrap(buff).getInt();
    }

    public void sayHiToServer() {
        byte[] buf = new byte[100];
        try {
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, InetAddress.getByName(mcastaddr), sUnknownPort);
            clientSocket.send(packet);

            packet = new DatagramPacket(buf, buf.length);
            mcastSocketForHiMsg.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
