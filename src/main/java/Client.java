import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class Client {
    private final String msg = "Hello World! from client";

    private byte[] inputmsg = new byte[24];

    MulticastSocket socket;

    public Client() {
        try {
            socket  = new MulticastSocket();

            socket.connect(InetAddress.getLocalHost(), 8000);


            socket.joinGroup(InetAddress.getByName("224.0.1.1"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isServerAlive() {
        try {
            socket.setSoTimeout(1000);

            DatagramPacket receivedPacket = new DatagramPacket(inputmsg, inputmsg.length);

            socket.receive(receivedPacket);

        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public void sendMsg() {
        System.out.println("sendMsg() is called");

        try {
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length,  InetAddress.getLocalHost(), 8000);
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void receiveData() {
        System.out.println("receiveData() is called");
        try {
            socket.setSoTimeout(0);
            DatagramPacket packet = new DatagramPacket(inputmsg, msg.getBytes().length,  InetAddress.getLocalHost(), 8000);
            socket.receive(packet);
            System.out.println(Arrays.toString(packet.getData()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
