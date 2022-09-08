import java.net.SocketException;

public class Main {
    public static void main(String[] args) throws SocketException {
        if (args.length == 1) {
            if (!Server.isAlive(args[0])) {
                Server server = new Server(args[0]);
                ServerThread serverThread = new ServerThread(server);
                serverThread.start();
            }

            Client client = new Client(args[0], Server.getPortContext());
            client.startSession();
        }
        else
            System.out.println("invalid argument");
    }
}
