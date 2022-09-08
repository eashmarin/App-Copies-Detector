public class PortContext {
    private int regularMsgSendPort = 9000;
    private int hiMsgSendPort = 9001;
    private int regularMsgGetPort = 9002;
    private int hiMsgGetPort = 9003;

    public int getHiMsgGetPort() {
        return hiMsgGetPort;
    }

    public void setHiMsgGetPort(int hiMsgGetPort) {
        this.hiMsgGetPort = hiMsgGetPort;
    }

    public int getRegularMsgGetPort() {
        return regularMsgGetPort;
    }

    public void setRegularMsgGetPort(int regularMsgGetPort) {
        this.regularMsgGetPort = regularMsgGetPort;
    }

    public int getHiMsgSendPort() {
        return hiMsgSendPort;
    }

    public void setHiMsgSendPort(int hiMsgSendPort) {
        this.hiMsgSendPort = hiMsgSendPort;
    }

    public int getRegularMsgSendPort() {
        return regularMsgSendPort;
    }

    public void setRegularMsgSendPort(int regularMsgSendPort) {
        this.regularMsgSendPort = regularMsgSendPort;
    }
}
