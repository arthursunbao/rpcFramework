package main.java.rpc.nio;

import com.sun.org.apache.bcel.internal.generic.Select;
import main.java.rpc.RpcObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by baosun on 8/1/2016.
 */
public class SimpleRpcNioSelector extends AbstractRpcNioSelector {

    private Selector selector;
    private boolean stop = false;
    private boolean started = false;
    private ConcurrentHashMap<SocketChannel, RpcNioConnector> connectorCache;
    private List<RpcNioConnector> connectors;
    private ConcurrentHashMap<ServerSocketChannel, RpcNioAcceptor> acceptorCache;
    private List<RpcNioAcceptor> acceptors;
    private static final int READ_OP = SelectionKey.OP_READ;
    private static final int READ_WRITE_OP = SelectionKey.OP_READ || SelectionKey.OP_WRITE;
    private LinkedList<Runnable> selectedTasks = new LinkedList<>();

    private AbstractRpcNioSelector delegageSelector;

    public SimpleRpcNioSelector(){
        super();
        try{
            selector = Selector.open();
            connectorCache = new ConcurrentHashMap<SocketChannel, RpcNioConnector>();
            acceptorCache = new ConcurrentHashMap<ServerSocketChannel, RpcNioAcceptor>();
            connectors = new CopyOnWriteArrayList<RpcNioConnector>();
            acceptors = new CopyOnWriteArrayList<RpcNioAcceptor>();
        }
        catch(IOException e){
            e.printStackTrace();
        }

    public void register(final RpcNioAcceptor acceptor){
        final ServerSocketChannel channel = acceptor.getServerSocketChannel();
        this.addSelectTask(new Runnable(){
            public void run() {
                try {
                    channel.register(selector, SelectionKey.OP_ACCEPT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            });
            this.notifySend(null);
        acceptorCache.put(acceptor.getServerSocketChannel(), acceptor);
        acceptors.add(acceptor);
        }
    }

    public void unRegister(RpcNioAcceptor acceptor){
        ServerSocketChannel channel = acceptor.getServerSocketChannel();
        acceptorCache.remove(channel);
        acceptors.remove(channel);
    }

    public void register(final RpcNioConnector connector){
        this.addSelectTash(new Runnable(){
           public void run(){
               try{
                   SelectionKey selectionKey = connector.getChannel().register(selector,READ_OP);
                   SimpleRpcNioSelector.this.initNewSocketChannel(connector.getChannel, connector, selectionKey );
               }
               catch(Exception e){
                   e.printStackTrace();
               }
           }
        });
        this.notifySend(null);
    }

    public void unRegister(RpcNioConnector connector){
        connectorCache.remove(connector);
        connectors.remove(connector);
    }

    private void initNewSocketChannel(SocketChannel channel, RpcNioConnector connector, SelectionKey selectionKey){
        if (connector.getAcceptor() != null) {
            connector.getAcceptor().addConnectorListeners(connector);
        }
        connector.setSelectionKey(selectionKey);
        connectorCache.put(channel, connector);
        connectors.add(connector);
    }

    @Override
    public synchronized void startService(){
        if(!started){
            new SelectionThread().start();
            started = true;
        }
    }

    @Override
    public synchronized void stopService(){
        this.stop = true;
    }

    private boolean doAccept(SelectionKey selectionKey){
        ServerSocketChannel server = (ServerSocketChannel)selectionKey.channel();
        RpcNioAcceptor acceptor = acceptorCache.get(server);
        try{
            SocketChannel client = server.accept();
            if(client != null) {
                client.configureBlocking(false);
                if (delegageSelector != null) {
                    RpcNioConnector connector = new RpcNioConnector(client, delegageSelector);
                    connector.setAcceptor(acceptor);
                    connector.setExecutorService(acceptor.getExecutorService());
                    connector.setExecutorSharable(true);
                    delegageSelector.register(connector);
                    connector.startService();
                } else {
                    RpcNioConnector connector = new RpcNioConnector(client, this);
                    connector.setAcceptor(acceptor);
                    connector.setExecutorService(acceptor.getExecutorService());
                    connector.setExecutorSharable(true);
                    this.register(connector);
                    connector.startService();
                }
                return true;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private void fireRpc(RpcNioConnector connector, RpcObject rpc){
        rpc.setHost(connector.getRemoteHost());
        rpc.setPort(connector.getRemotePort());
        rpc.setRpcContext(connector.getRpcContext());
        connector.fireCall(rpc);
    }

    private boolean doRead(SelectionKey selectionKey){
        boolean result = false;
        SocketChannel client = (SocketChannel)selectionKey.channel();
        RpcNioConnector connector = connectorCache.get(selectionKey);
        if(connector != null){
            try{
                RpcNioBuffer connectorReadBuffer = connector.getRpcNioReadBuffer();
                ByteBuffer channelReadBuf = connector.getChannelReadBuffer();
                while(!stop){
                    int read = 0;
                    while((read = client.read(channelReadBuf) > 0)){
                        channelReadBuf.flip();
                        byte[] readBytes = new byte[read];
                        channelReadBuf.get(readBytes);
                        connectorReadBuffer.write(readBytes);
                        channelReadBuf.clear();
                        while(connectorReadBuf.hasRpcObject()){
                            RpcObject rpc = connectorReadBuf.readRpcObject();
                            this.fireRpc(connector, rpc);
                        }
                        if(read<1){
                            if(read<0){
                                this.handSelectionKeyException(selectionKey, new RpcException());
                            }
                            break;
                        }
                    }

                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        return result;
    }
//
}
