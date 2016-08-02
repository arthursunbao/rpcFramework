package main.java.rpc.nio;

import main.java.rpc.network.AbstractRpcConnector;
import main.java.rpc.network.RpcNetListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by baosun on 7/29/2016.
 */
public class RpcNioConnector extends AbstractRpcConnector {

    private SocketChannel channel;
    private AbstractRpcNioSelector selector;
    private ByteBuffer channelWriterBuffer;
    private ByteBuffer channelReadBuffer;
    private SelectionKey selectionKey;

    private RpcNioBuffer rpcNioReadBuffer;
    private RpcNioBuffer rpcNioWriterBuffer;

    private RpcNioAcceptor acceptor;

    public RpcNioConnector(SocketChannel socketChannel, AbstractRpcNioSelector selector){
        this(selector);
        this.channel = socketChannel;
    }

    public RpcNioConnector(AbstractRpcNioSelector selector){
        super(null);
        if(selector == null){
            this.selector = new SimpleRpcNioSelector();
        }
        else{
            this.selector = selector;
        }
        this.initBuf();
    }

    @Override
    public void addRpcNetListener(RpcNetListener listener){
        super.addRpcNetListener(listener);
        this.selector.addRpcNetListener();
    }

    public RpcNioConnector(){
        this(null);
    }

    public void initBuf(){
        channelReadBuffer = ByteBuffer.allocate(10000);
        channelWriterBuffer = ByteBuffer.allocate(10000);
        rpcNioReadBuffer = new RpcNioBuffer(10000);
        rpcNioWriterBuffer = new RpcNioBuffer(10000);
    }

    @Override
    public void startService(){
        super.startService();
        try{
            if(channel == null){
                channel = SocketChannel.open();
                channel.connect(new InetSocketAddress(this.getHost(), this.getPort());
                channel.configureBlocking(false);
                while(!channel.isConnected()){
                }
                selector.startService();
                selector.register(this);
            }
            InetSocketAddress remoteAddress = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
            InetSocketAddress localAddress = (InetSocketAddress)channel.socket().getLocalSocketAddress();
            remotePort = remoteAddress.getPort();
            remoteHost = remoteAddress.getAddress().getHostAddress();
            this.fireStartNetListeners();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }


    @Override
    public void notifySend(){
        selector.notifySend();
    }

    public ByteBuffer getChannelWriteBuffer() {
        return channelWriterBuffer;
    }

    public ByteBuffer getChannelReadBuffer() {
        return channelReadBuffer;
    }

    @Override
    public void stopService(){
        super.stopService();
        this.selector.unRegister(this);
        this.sendQueueCache.clear();
        this.rpcContext.clear();
        try{
            channel.close();
            channelWriterBuffer.clear();
            channelReadBuffer.clear();
            rpcNioReadBuffer.clear();
            rpcNioWriterBuffer.clear();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        this.stop = true;
    }

    @Override
    public void handleConnectorException(Exception e) {
        this.stopService();
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public AbstractRpcNioSelector getSelector() {
        return selector;
    }

    public void setSelector(AbstractRpcNioSelector selector) {
        this.selector = selector;
    }

    public ByteBuffer getChannelWriterBuffer() {
        return channelWriterBuffer;
    }

    public void setChannelWriterBuffer(ByteBuffer channelWriterBuffer) {
        this.channelWriterBuffer = channelWriterBuffer;
    }

    public void setChannelReadBuffer(ByteBuffer channelReadBuffer) {
        this.channelReadBuffer = channelReadBuffer;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public RpcNioBuffer getRpcNioReadBuffer() {
        return rpcNioReadBuffer;
    }

    public void setRpcNioReadBuffer(RpcNioBuffer rpcNioReadBuffer) {
        this.rpcNioReadBuffer = rpcNioReadBuffer;
    }

    public RpcNioBuffer getRpcNioWriterBuffer() {
        return rpcNioWriterBuffer;
    }

    public void setRpcNioWriterBuffer(RpcNioBuffer rpcNioWriterBuffer) {
        this.rpcNioWriterBuffer = rpcNioWriterBuffer;
    }

    public RpcNioAcceptor getAcceptor() {
        return acceptor;
    }

    public void setAcceptor(RpcNioAcceptor acceptor) {
        this.acceptor = acceptor;
    }

}
