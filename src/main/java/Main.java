public class Main {
    public static void main(String[] args)  {
        if (args.length == 1) {
            PortContext portContext = new PortContext();

            Server server = new Server(args[0], portContext);
            Thread serverThread = new Thread(server::startSession);
            serverThread.start();

            Client client = new Client(args[0], portContext);
            client.startSession();
        }
        else {
            System.out.println("Invalid argument, multicast address is needed");
        }
    }
}
