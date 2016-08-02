package main.java.rpc.network;

import main.java.rpc.RpcObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by baosun on 7/29/2016.
 */
public abstract class AbstractRpcConnector extends RpcNetBase implements Service, RpcSender {

        protected boolean stop = false;
    protected String remoteHost;
    protected int remotePort;
    protected ConcurrentHashMap<String, Object> rpcContext;
    private RpcOutputNotify outputNotify;
    protected ConcurrentLinkedQueue<RpcObject> sendQueueCache = new ConcurrentLinkedQueue<>();

    private AbstractRpcWriter rpcWriter;

    public AbstractRpcConnector(AbstractRpcWriter rpcWriter){
        super();
        this.rpcWriter = rpcWriter;
        rpcContext = new ConcurrentHashMap<String,Object>();
    }

    public boolean isNeedToSend(){
        RpcObject peek = sendQueueCache.peek();
        return peek!= null;
    }

    public RpcObject getToSend(){
        return sendQueueCache.poll();
    }

    @Override
    public boolean sendRpcObject(RpcObject rpc, int timeout){
        int cost = 0;
        while(!sendQueueCache.offer(rpc)){
            cost = cost + 3;
            try{
                Thread.currentThread().sleep(3);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            if(timeout > 0 && cost > timeout){
                System.out.println("Request time out");
            }
        }
        this.notifySend();
        return true;
    }

    public void notifySend(){
        if(rpcWriter != null){
            rpcWriter.notifySend(this);
        }
    }





    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public ConcurrentHashMap<String, Object> getRpcContext() {
        return rpcContext;
    }

    public void setRpcContext(ConcurrentHashMap<String, Object> rpcContext) {
        this.rpcContext = rpcContext;
    }

    public RpcOutputNotify getOutputNotify() {
        return outputNotify;
    }

    public void setOutputNotify(RpcOutputNotify outputNotify) {
        this.outputNotify = outputNotify;
    }

    public ConcurrentLinkedQueue<RpcObject> getSendQueueCache() {
        return sendQueueCache;
    }

    public void setSendQueueCache(ConcurrentLinkedQueue<RpcObject> sendQueueCache) {
        this.sendQueueCache = sendQueueCache;
    }

    public AbstractRpcWriter getRpcWriter() {
        return rpcWriter;
    }

    public void setRpcWriter(AbstractRpcWriter rpcWriter) {
        this.rpcWriter = rpcWriter;
    }



}
