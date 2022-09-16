import java.io.IOException;
import java.util.Properties;

public class PortContext {
    private final int keepAliveMsgSendPort;
    private final int hiMsgSendPort;
    private final int keepAliveMsgGetPort;
    private final int hiMsgGetPort;

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

    public int getKeepAliveMsgGetPort() {
        return keepAliveMsgGetPort;
    }

    public int getHiMsgSendPort() {
        return hiMsgSendPort;
    }

    public int getKeepAliveMsgSendPort() {
        return keepAliveMsgSendPort;
    }
}
