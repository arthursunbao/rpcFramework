package com.jason.rpcFramework.network;

import com.jason.rpcFramework.Exception.RpcNetExceptionHandler;
import com.jason.rpcFramework.RpcObject;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the Base class for RPC for defining a RPC Communication Bases including executorService, RpcListeners, Connectors
 * Created by baosun on 7/29/2016.
 */
public abstract class RpcNetBase extends AbstractRpcNetworkBase implements RpcNetExceptionHandler {

    private ExecutorService executorService;

    private boolean executorSharable;

    protected List<RpcCallListener> callListeners;
    protected List<RpcNetListener> netListeners;

    private static final int DEFAULT_EXECUTOR_THREAD_COUNT = 3;

    private int executorThreadCount = DEFAULT_EXECUTOR_THREAD_COUNT;

    public RpcNetBase() {
        this.callListeners = new LinkedList<RpcCallListener>();
        this.netListeners = new LinkedList<RpcNetListener>();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setExecutorSharable(boolean executorSharable) {
        this.executorSharable = executorSharable;
    }

    public boolean isExecutorSharable() {
        return executorSharable;
    }

    public int getExecutorThreadCount() {
        return executorThreadCount;
    }

    public void setExecutorThreadCount(int executorThreadCount) {
        this.executorThreadCount = executorThreadCount;
    }

    public void addRpcCallListener(RpcCallListener listener){
        callListeners.add(listener);
    }

    public List<RpcCallListener> getCallListeners(){
        return callListeners;
    }

    public void fireCallListeners(RpcObject rpc, RpcSender sender){
        for(RpcCallListener listener : callListeners){
            listener.onRpcMessage(rpc,sender);
        }
    }

    public void startListeners(){
        for(RpcCallListener listener: callListeners){
            if(listener instanceof Service){
                Service service = (Service)listener;
                service.startService();
            }

        }
    }

    public void stopListeners(){
        for(RpcCallListener listener: callListeners) {
            if (listener instanceof Service) {
                Service service = (Service) listener;
                service.stopService();
            }
        }
    }

    public void addConnectorListeners(AbstractRpcConnector connector){
        for(RpcCallListener listener: callListeners){
            connector.addRpcListener(listener);
        }
    }

    public void addRpcNetListener(RpcNetListener listener){
        netListeners.add(listener);
    }

    public void fireCloseNetListeners(Exception e){
        for(RpcNetListener listener : netListeners){
            listener.onClose(this,e);
        }
    }

    public void fireStartNetListeners(){
        for(RpcNetListener listener : netListeners){
            listener.onStart(this);
        }
    }

    @Override
    public void startService(){
        if(this.executorService == null){
            if(this.executorThreadCount < 1){
                this.executorThreadCount = DEFAULT_EXECUTOR_THREAD_COUNT;
            }
        }
        executorService = Executors.newFixedThreadPool(executorThreadCount);
    }

    public void stopService(){
        if(!this.isExecutorSharable() && executorService != null){
            executorService.shutdown();
        }
    }
}
