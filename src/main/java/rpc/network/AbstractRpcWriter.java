package main.java.rpc.network;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by baosun on 7/29/2016.
 */
public abstract class AbstractRpcWriter implements Service, RpcOutputNotify {

    private List<AbstractRpcConnector> connectors;
    protected Thread sendThread;
    private int interval = 50;
    private boolean stop = false;
    private AtomicBoolean started = new AtomicBoolean(false);

    public AbstractRpcWriter(){
        connectors = new CopyOnWriteArrayList<AbstractRpcConnector>();
    }

    public void registerWrite(AbstractRpcConnector connector){
        connectors.add(connector);
    }

    public void unRegWrite(AbstractRpcConnector connector){
        connectors.remove(connector);
    }

    public void notifySend(AbstractRpcConnector connector){
        sendThread.interrupt();
    }

    @Override
    public void startService(){
        if(!started.get()){
            sendThread = new WriteThread();
            sendThread.start();
            started.set(true);
        }
    }

    @Override
    public void stopService(){
        stop = true;
        sendThread.interrupt();
    }

    public abstract boolean doSend(AbstractRpcConnector connector);

    private class WriteThread extends Thread{

        @Override
        public void run(){
            boolean hasSend = false;
            while(!stop){
                try{
                    for(AbstractRpcConnector connector: connectors){
                        hasSend |= doSend(connector);
                    }
                    if(!hasSend){
                        Thread.currentThread().sleep(interval);
                        hasSend = false;
                    }
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }


        }
    }



}
