package main.java.rpc.nio;

import com.sun.org.apache.bcel.internal.generic.Select;
import main.java.rpc.RpcObject;
import main.java.rpc.network.AbstractRpcConnector;

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
import java.util.Set;
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
    private LinkedList<Runnable> selectTasks = new LinkedList<>();

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

    private boolean doWrite(SelectionKey selectionKey){
        boolean result = false;
        SocketChannel channel = (SocketChannel)selectionKey.channel();
        RpcNioConnector connector = connectorCache.get(channel);
        if(connector.isNeedToSend()){
            try{
                RpcNioBuffer connectorWriteBuffer = connector.getRpcNioWriterBuffer();
                ByteBuffer channelWriteBuffer = connector.getChannelWriteBuffer();
                while(connector.isNeedToSend()){
                    RpcObject rpc = connector.getToSend();
                    connectorWriteBuffer.writeRpcObject(rpc);
                    channelWriteBuffer.put(connectorWriteBuffer.readBytes());
                    channelWriteBuffer.flip();
                    int wantWrite = channelWriteBuffer.limit()-channelWriteBuffer.position();
                    int write = 0;
                    while(write < wantWrite){
                        write += channel.write(channelWriteBuffer);
                    }
                    channelWriteBuffer.clear();
                    result = true;
                }
                if(!connector.isNeedToSend()){
                    selectionKey.interestOps(READ_OP);
                }
            }catch (Exception e){
                e.printStackTrace();
            }


        }

        return result;

    }

    private boolean doDispatchSelectionKey(SelectionKey selectionKey){
        boolean result = false;
        try{
            if(selectionKey.isAcceptable()){
                result = doAccept(selectionKey)
            }
            if(selectionKey.isReadable()){
                result = doRead(selectionKey);
            }
            if(selectionKey.isWritable()){
                result = doWrite(selectionKey);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

private class SelectionThread extends Thread {
    @Override
    public void run() {
        logger.info("select thread has started :"+Thread.currentThread().getId());
        while (!stop) {
            if(SimpleRpcNioSelector.this.hasTask()){
                SimpleRpcNioSelector.this.runSelectTasks();
            }
            boolean needSend = checkSend();
            try {
                if (needSend) {
                    selector.selectNow();
                } else {
                    selector.select();
                }
            } catch (IOException e) {
                SimpleRpcNioSelector.this.handleNetException(e);
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (SelectionKey selectionKey : selectionKeys) {
                doDispatchSelectionKey(selectionKey);
            }
        }
    }
}

    private boolean checkSend(){
        boolean needSend = false;
        for(RpcNioConnector connector:connectors){
            if(connector.isNeedToSend()){
                SelectionKey selectionKey = connector.getChannel().keyFor(selector);
                selectionKey.interestOps(READ_WRITE_OP);
                needSend = true;
            }
        }
        return needSend;
    }

    @Override
    public void notifySend(AbstractRpcConnector connector) {
        selector.wakeup();
    }

    private void addSelectTask(Runnable task){
        selectTask.offer(task);
    }

    private boolean hasTask(){
        Runnable peek = selectTasks.peek();
        return peek!=null;
    }

    private void runSelectTasks(){
        Runnable peek = selectTasks.peek();
        while(peek!=null){
            peek = selectTasks.pop();
            peek.run();
            peek = selectTasks.peek();
        }
    }

    public void setDelegageSelector(AbstractRpcNioSelector delegageSelector) {
        this.delegageSelector = delegageSelector;
    }

    @Override
    public void handleNetException(Exception e) {

    }



}
