package com.jason.rpcFramework.client;

import com.jason.rpcFramework.network.AbstractRpcConnector;
import com.jason.rpcFramework.network.RpcCallListener;
import com.jason.rpcFramework.network.Service;

/**
 * Created by bsun on 2016/8/3.
 */
public class SimpleClientRemoteExecutor extends AbstractClientRemoteExecutor implements RemoteExecutor, RpcCallListener, Service {

    private AbstractRpcConnector connector;
    public SimpleClientRemoteExecutor(AbstractRpcConnector connector){
        super();
        connector.addRpcCallListener(this);
        this.connector = connector;
    }

    public AbstractRpcConnector getConnector(){
        return connector;
    }

    public void setConnector(AbstractRpcConnector connector){
        this.connector = connector;
    }

    @Override
    public void startService(){
        connector.startService();
    }

    @Override
    public void stopService(){
        connector.stopService();
    }

    @Override
    public AbstractRpcConnector getRpcConnector(RemoteCall call) {
        return connector;
    }

}
