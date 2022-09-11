import java.io.IOException;
import java.util.Properties;

public class PortContext {
    private int keepAliveMsgSendPort;
    private int hiMsgSendPort;
    private int keepAliveMsgGetPort;
    private int hiMsgGetPort;

    public PortContext() {
        Properties properties = new Properties();
        try {
            properties.load(PortContext.class.getResourceAsStream("/ports.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        keepAliveMsgSendPort = Integer.parseInt(properties.getProperty("keepAliveMsgSendPort"));
        hiMsgSendPort = Integer.parseInt(properties.getProperty("hiMsgSendPort"));
        keepAliveMsgGetPort = Integer.parseInt(properties.getProperty("keepAliveMsgGetPort"));
        hiMsgGetPort = Integer.parseInt(properties.getProperty("hiMsgGetPort"));
    }

    public int getHiMsgGetPort() {
        return hiMsgGetPort;
    }

    public void setHiMsgGetPort(int hiMsgGetPort) {
        this.hiMsgGetPort = hiMsgGetPort;
    }

    public int getKeepAliveMsgGetPort() {
        return keepAliveMsgGetPort;
    }

    public void setKeepAliveMsgGetPort(int keepAliveMsgGetPort) {
        this.keepAliveMsgGetPort = keepAliveMsgGetPort;
    }

    public int getHiMsgSendPort() {
        return hiMsgSendPort;
    }

    public void setHiMsgSendPort(int hiMsgSendPort) {
        this.hiMsgSendPort = hiMsgSendPort;
    }

    public int getKeepAliveMsgSendPort() {
        return keepAliveMsgSendPort;
    }

    public void setKeepAliveMsgSendPort(int keepAliveMsgSendPort) {
        this.keepAliveMsgSendPort = keepAliveMsgSendPort;
    }
}
