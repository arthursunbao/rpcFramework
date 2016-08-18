package com.jason.rpcFramework.client;

import com.jason.rpcFramework.network.AbstractRpcNetworkBase;
import com.jason.rpcFramework.network.AbstractRpcConnector;

/**
 * Created by bsun on 2016/8/3.
 */
public abstract class AbstractRpcClient extends AbstractRpcNetworkBase {

    private RpcSerializer serializer;

    private SimpleClientRemoteProxy proxy = new SimpleClientRemoteProxy();

    protected Class<? extends AbstractRpcConnector> connectorClass;

    private int executorThreadCount = 2;

    public abstract AbstractClientRemoteExecutor getRemoteExecutor();

    public Class<? extends AbstractRpcConnector> getConnectorClass(){
        return connectorClass;
    }

    public void setConnectorClass(Class<? extends AbstractRpcConnector> connectorClass){
        this.connectorClass = connectorClass;
    }

    public abstract void initConnector(int threadCount);

    public <T> T register<Class<T> iface){
        return proxy.registerRemote(iface);
    }

    public <T> T register<Class<T> iface, String version){
        return proxy.registerRemote(iface, version);
    }

    @Override
    public void startService(){
        initConnector(executorThreadCount);
        AbstractClientRemoteExecutor executor = getRemoteExecutor();
        if(serializer != null){
            executor.setSerializer(serializer);
        }
        proxy.setRemoteExecutor(executor);
        proxy.startService();
    }

    @Override
    public void stopService(){
        proxy.stopService();
    }

    public RpcSerializer getSerializer(){
        return serializer;
    }

    public void setSerializer(RpcSerializer serializer) {
        this.serializer = serializer;
    }

}
