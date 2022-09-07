import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class Client {
    private String msg;

    private final byte[] inputmsg = new byte[100];

    private DatagramSocket clientSocket;
    private int clientPort;

    private final MulticastSocket rKnownSocket;
    private final MulticastSocket rUnknownSocket;

    private static final int sKnownPort = 9000;
    private static final int sUnknownPort = 9001;
    private static final int rKnownPort = 9002;
    private static final int rUnknownPort = 9003;

    private final Timer keepAliveTimer = new Timer();

    public Client() { //int regularMsgGetPort, int regularMsgSendPort, int hiMsgGetPort, int hiMsgSendPort
        try {
            rKnownSocket = new MulticastSocket(rKnownPort);
            rUnknownSocket = new MulticastSocket(rUnknownPort);

            rKnownSocket.joinGroup(InetAddress.getByName("224.0.1.1"));
            rUnknownSocket.joinGroup(InetAddress.getByName("224.0.1.1"));

            rKnownSocket.setSoTimeout(3000);
            rUnknownSocket.setSoTimeout(3000);

            InitClientSocket();

            msg = "Hello World! from client " + clientPort;            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendKeepAliveMsg() {
        try {
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length,  InetAddress.getByName("224.0.1.1"), sKnownPort);       //send to server
            clientSocket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void receiveKeepAliveMsg() {
        try {
            DatagramPacket packet = new DatagramPacket(inputmsg, msg.getBytes().length);         // receive from server
            rKnownSocket.receive(packet);
        } catch (SocketTimeoutException e) {
            System.out.println("Server is dead, restarting...");
            ServerThread serverThread = new ServerThread();
            serverThread.start();
            sayHiToServer();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startSession() {
        keepAliveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendKeepAliveMsg();
                receiveKeepAliveMsg();
            }
        },0, 1000);
    }

    public void InitClientSocket() {
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
            rUnknownSocket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ByteBuffer.wrap(buff).getInt();
    }

    public void sayHiToServer() {
        try {
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, InetAddress.getByName("224.0.1.1"), sUnknownPort);
            clientSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
