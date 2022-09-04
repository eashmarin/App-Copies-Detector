import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Client {
    private final String msg = "Hello World! from client";

    private byte[] inputmsg = new byte[100];

    MulticastSocket socket;

    public Client() {
        try {
            socket  = new MulticastSocket(8000);

            //socket.connect(InetAddress.getLocalHost(), 8000);
            socket.joinGroup(InetAddress.getByName("224.0.1.1"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMsg() {
        try {
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length,  InetAddress.getLocalHost(), 8080);
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void receiveData() {
        try {
            socket.setSoTimeout(10);
            DatagramPacket packet = new DatagramPacket(inputmsg, msg.getBytes().length,  InetAddress.getLocalHost(), 8000);
            socket.receive(packet);

            System.out.println("Client receives message: " + new String(packet.getData(), StandardCharsets.UTF_8));
        } catch (SocketTimeoutException e) {
            // ServerThread serverThread = new ServerThread();
            // serverThread.start();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
