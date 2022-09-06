public class Main {
    public static void main(String[] args) {
        if (!Server.isAlive()) {
            System.out.println("Server is sleeping");
            ServerThread serverThread = new ServerThread();
            serverThread.start();
        }
        else
            System.out.println("Server is alive!");

        Client client = new Client();
        client.sayHiToServer();
        client.startSession();
    }
}
