package main.java.rpc.nio;

import main.java.rpc.Exception.RpcNetExceptionHandler;
import main.java.rpc.network.RpcNetBase;
import main.java.rpc.network.RpcNetListener;
import main.java.rpc.network.RpcOutputNotify;
import main.java.rpc.network.Service;

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
