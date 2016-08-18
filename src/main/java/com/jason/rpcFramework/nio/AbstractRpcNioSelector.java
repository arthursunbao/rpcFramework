package com.jason.rpcFramework.nio;

import com.jason.rpcFramework.Exception.RpcNetExceptionHandler;
import com.jason.rpcFramework.network.RpcNetBase;
import com.jason.rpcFramework.network.RpcNetListener;
import com.jason.rpcFramework.network.RpcOutputNotify;
import com.jason.rpcFramework.network.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by baosun on 7/29/2016.
 */
public abstract class AbstractRpcNioSelector implements Service, RpcOutputNotify, RpcNetExceptionHandler {

    public abstract void register(RpcNioAcceptor acceptor);

    public abstract void unRegister(RpcNioAcceptor acceptor);

    public abstract void register(RpcNioConnector connector);

    public abstract void unRegister(RpcNioConnector connector);

    public AbstractRpcNioSelector(){
        netListeneers = new LinkedList<RpcNetListener>();
    }

    protected List<RpcNetListener> netListeners;

    public void addRpcNetListener(RpcNetListener listener){
        netListeners.add(listener);
    }

    public void fireNetListener(RpcNetBase network, Exception e){
        for(RpcNetListener listener : netListeners){
            listener.onClose(network, e);
        }
    }

}
