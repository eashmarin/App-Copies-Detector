public class Main {
    public static void main(String[] args) {
        if (args.length == 1) {
            System.out.println("using mcast address " + args[0]);
            if (!Server.isAlive(args[0])) {
                System.out.println("Server is sleeping");
                ServerThread serverThread = new ServerThread(args[0]);
                serverThread.start();
            } else
                System.out.println("Server is alive!");

            Client client = new Client(args[0]);
            client.sayHiToServer();
            client.startSession();
        }
        else
            System.out.println("invalid argument");
    }
}
