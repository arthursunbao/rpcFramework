package main.java.rpc.nio;

import main.java.rpc.network.AbstractRpcConnector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * Created by baosun on 7/29/2016.
 */
public class RpcNioAcceptor extends AbstractRpcConnector {
    private ServerSocketChannel serverSocketChannel;
    private AbstractRpcNioSelector selector;

    public RpcNioAcceptor(){
        this(null);
    }

    public RpcNioAcceptor(AbstractRpcNioSelector selector){
        try{
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            this.selector = selector;
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void startService(){
        super.startService();
        try{
            if(selector == null){
                selector = new SimpleRpcNioSelector();
            }
            selector.startService();
            serverSocketChannel.socket().bind(new InetSocketAddress(this.getHost(),this.getPort()));
            selector.register(this);
            this.startListeners();
            this.fireStartNetListeners();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void stopService() {
        super.stopService();
        if(serverSocketChannel!=null){
            try {
                serverSocketChannel.close();
                if(selector!=null){
                    selector.stopService();
                }
            } catch (IOException e) {
                //do mothing
            }
        }
        this.stopListeners();
    }

    @Override
    public void handleNetException(Exception e) {
        this.stopService();
        throw new RpcException(e);
    }

    public ServerSocketChannel getServerSocketChannel() {
        return serverSocketChannel;
    }

}
