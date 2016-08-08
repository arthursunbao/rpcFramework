package main.java.rpc.aio;

import main.java.rpc.RpcObject;
import main.java.rpc.network.AbstractRpcConnector;
import main.java.rpc.nio.RpcNioBuffer;
import main.java.rpc.utils.RpcUtils;

/**
 * Created by baosun on 8/4/2016.
 */
public class RpcAioConnector extends AbstractRpcConnector {

    private AynchronousSocketChannel channel;

    private ByteBuffer readBuf;
    private ByteBuffer writeBuf;
    private RpcNioBuffer nioReadBuffer;
    private RpcNioBuffer nioWriteBuffer;

    private RpcReadCompletionHandler readHandler;
    private RpcWriteCompletionHander writeHandler;

    private AsychronousChannelGroup channelGroup;

    private AtomicBoolean inWrite = new AtomicBoolean(false);

    private int channelGroupThreads = 5;

    public RpcAioConnector(RpcAioWriter writer,AsynchronousSocketChannel channel){
        super(writer);
        this.channel = channel;
        this.initBuf(); //Init the buffer
    }

    public RpcAioConnector(){
        this(new RpcAioWriter(), null);
    }

    private void initBuf(){
        writeBuf = ByteBuffer.allocate(RpcUtils.MEM_32KB);
        readBuf = ByteBuffer.allocate(RpcUtils.MEM_32KB);
        nioReadBuffer = new RpcNioBuffer(RpcUtils.MEM_32KB);
        nioWriteBuffer = new RpcNioBuffer((RpcUtils.MEM_32KB));
    }

    private void checkWriter(){
        if(this.getRpcWriter() == null){
            this.setRpcWriter(new RpcAioWriter());
        }

        if(this.writeHandler == null){
            writeHandler = new RpcWriteCompletionHander();
        }
        if(this.readHandlder == null){
            readHandler = new RpcReadCompletionHandler();
        }
    }

    private void checkChannelGroup(){
        //检查group
        if(channelGroup==null){
            try {
                channelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(channelGroupThreads));
            } catch (IOException e) {
                throw new RpcException(e);
            }
        }
    }

    @Override
    public void startService(){
        super.startService();
        this.checkWriter();
        try{
            if(channel == null){
                this.checkChannelGroup();
                channel = AsynchronousSocketChannel.open(channelGroup);
                channel.connect(new InetSocketAddress(this.getHost(), this.getPort()));
            }
            InetSocketAddress remoteAddress = (InetSocketAddress)channel.getRemoteAddress();
            InetSocketAddress localAddress = (InetSocketAddress)channel.getLocalAddress();
            String remote = RpcUtils.genAddressString("remoteAddress-> ", remoteAddress);
            String local = RpcUtils.genAddressString("localAddress-> ", localAddress);
            remotePort = remoteAddress.getPort();
            remoteHost = remoteAddress.getAddress().getHostAddress();
            this.getRpcWriter().registerWrite(this);
            this.getRpcWriter().startService();
            this.fireStartNetListeners();
            this.channel.read(readBuf, this, readHandler);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void setReadHandler(RpcReadCompletionHandler readHandler) {
        this.readHandler = readHandler;
    }

    public void setWriteHandler(RpcWriteCompletionHandler writeHandler) {
        this.writeHandler = writeHandler;
    }

    public void readCallback(int num){
        if(num < 1){
            if(num < 0){

            }
            else{
                this.channel.read(readBuf, this, readHandler);
            }
        }
        else{
            readBuf.flip();
            byte[] readBytes = new byte[num];
            readBuf.get(readBytes);
            nioReadBuffer.write(readBytes);
            readBuf.clear();
            while(nioReadBuffer.hasRpcObject()){
                RpcObject rpc = nioReadBuffer.readRpcOjbect();
                this.fireCall(rpc);
            }
            this.channel.read(readBuf, this, readHandler);
        }

        public void writeCallback(int num){
            //老的数据没有发送完毕，继续发送
            if(this.writeBuf.hasRemaining()){
                channel.write(writeBuf, this, writeHandler);
            }else{
                //检查有无新数据，无数据不发送
                writeBuf.clear();
                //检测是否需要写可以写
                if(this.isNeedToSend()){
                    RpcObject rpc = this.getToSend();
                    nioWriteBuffer.writeRpcObject(rpc);
                    writeBuf.put(nioWriteBuffer.readBytes());
                    writeBuf.flip();
                    channel.write(writeBuf, this, writeHandler);
                }else{
                    inWrite.compareAndSet(true, false);
                }
            }
        }

        public void exeSend(){
            //需要发送数据
            if(!inWrite.get()&&this.isNeedToSend()){
                inWrite.compareAndSet(false, true);
                RpcObject rpc = this.getToSend();
                nioWriteBuffer.writeRpcObject(rpc);
                writeBuf.put(nioWriteBuffer.readBytes());
                writeBuf.flip();
                channel.write(writeBuf, this, writeHandler);
            }
        }

        @Override
        public void stopService() {
            super.stopService();
            this.getRpcWriter().unRegWrite(this);
            this.sendQueueCache.clear();
            this.rpcContext.clear();
            try {
                channel.close();
                writeBuf.clear();
                readBuf.clear();
                nioReadBuffer.clear();
                nioWriteBuffer.clear();
            } catch (IOException e) {
                //
            }
            this.stop = true;
        }






    }




}
