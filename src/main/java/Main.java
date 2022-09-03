public class Main {
    public static void main(String[] args) {
        Client client = new Client();

        if (!client.isServerAlive()) {
            System.out.println("Server is sleeping");

            ServerThread serverThread = new ServerThread();
            serverThread.start();

            //while (true) {
            client.sendMsg();

            while (true)
                client.receiveData();
            //}
        }
    }
}
