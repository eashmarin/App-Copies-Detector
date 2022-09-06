import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class Client {
    private String msg;

    private final byte[] inputmsg = new byte[100];

    private final DatagramSocket clientSocket;

    private final MulticastSocket rKnownSocket;
    private final MulticastSocket rUnknownSocket;
    private final MulticastSocket sKnownSocket;
    private final MulticastSocket sUnknownSocket;

    private static final int sKnownPort = 9000;
    private static final int sUnknownPort = 9001;
    private static final int rKnownPort = 9002;
    private static final int rUnknownPort = 9003;

    private Timer keepAliveTimer = new Timer();

    public Client() {
        try {
            int clientPort = 50000 + (int) (Math.random() * 100);
            clientSocket = new DatagramSocket(clientPort);

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

            msg = "Hello World! from client on port " + clientPort;
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

            //System.out.println("Client receives message: " + new String(packet.getData(), StandardCharsets.UTF_8));
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

    public void sayHiToServer() {
        System.out.println("sayHiToServer() is called");
        try {
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, InetAddress.getByName("224.0.1.1"), sUnknownPort);
            clientSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
