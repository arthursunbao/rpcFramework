package main.java.rpc.network;

import javax.net.ssl.SSLContext;

/**
 * Created by baosun on 7/29/2016.
 */
public abstract class AbstractRpcNetworkBase implements Service {

    private String host;
    private int port;
    protected int sslMode;
    protected SSLContext sslContext;

    public SSLContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public int getSslMode() {
        return sslMode;
    }

    public void setSslMode(int sslMode) {
        this.sslMode = sslMode;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
