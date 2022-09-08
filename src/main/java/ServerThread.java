public class ServerThread extends Thread{

    private String mcastaddr;
    public ServerThread(String mcastaddr) {
        this.mcastaddr = mcastaddr;
    }
    @Override
    public void run() {
        Server server = new Server(mcastaddr);
        server.startSession();
    }
}
