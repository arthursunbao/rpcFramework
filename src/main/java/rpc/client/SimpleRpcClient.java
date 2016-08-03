package main.java.rpc.client;

import main.java.rpc.network.AbstractRpcConnector;
import main.java.rpc.utils.RpcUtils;

/**
 * Created by bsun on 2016/8/3.
 */
public class SimpleRpcClient extends AbstractRpcClient {

    private AbstractRpcConnector connector;
    private AbstractClientRemoteExecutor executor;

    private void checkConnector(){
        if(connector == null){
            connector = RpcUtils.createConnector(connectorClass);
        }
    }

    @Override
    public AbstractClientRemoteExecutor getRemoteExecutor(){
        return executor;
    }

    @Override
    public void initConnector(int threadCount){
        checkConnector();
        connector.setHost(this.getHost());
        connector.setPort(this.getPort());
        connector.setExecutorThreadCount(threadCount);
        executor = new SimpleClientRemoteExecutor(connector);
    }


}
