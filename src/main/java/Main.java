public class Main {
    public static void main(String[] args) {
        if (!Server.isAlive()) {
            System.out.println("Server is sleeping");

            ServerThread serverThread = new ServerThread();
            serverThread.start();

            //while (true) {
            //}
        }

        Client client = new Client();

        client.sendMsg();

        while (true) {
            client.sendMsg();
            client.receiveData();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
