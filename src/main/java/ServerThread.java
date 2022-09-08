public class ServerThread extends Thread{

    private final Server server;
    public ServerThread(Server server) {
        this.server = server;
    }
    @Override
    public void run() {
        server.startSession();
    }
}
