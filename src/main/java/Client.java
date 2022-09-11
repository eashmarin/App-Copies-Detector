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

            mcastSocketForRegularMsg = new MulticastSocket(portContext.getKeepAliveMsgGetPort());
            mcastSocketForHiMsg = new MulticastSocket(portContext.getHiMsgGetPort());

            mcastSocketForRegularMsg.joinGroup(InetAddress.getByName(this.mcastaddr));
            mcastSocketForHiMsg.joinGroup(InetAddress.getByName(this.mcastaddr));

            initClientSocket();

            msg = "Hello from client " + clientPort;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startSession() {
        while (true) {
            swapHiMsgWithServer();
            sendKeepAliveMsg();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            receiveKeepAliveMsg();
        }
    }

    private void swapHiMsgWithServer() {
        try {
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, InetAddress.getByName(mcastaddr), portContext.getHiMsgSendPort());
            clientSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendKeepAliveMsg() {
        try {
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, InetAddress.getByName(mcastaddr), portContext.getKeepAliveMsgSendPort());
            clientSocket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveKeepAliveMsg() {
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            mcastSocketForRegularMsg.receive(packet);
        }  catch (IOException e) {
            throw new RuntimeException(e);
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

}