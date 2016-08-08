package main.java.rpc.aio;

import main.java.rpc.network.AbstractRpcConnector;
import main.java.rpc.network.AbstractRpcWriter;

/**
 * Created by baosun on 8/4/2016.
 */
public class RpcAioWriter extends AbstractRpcWriter {

    public RpcAioWriter(){
        super();
    }

    @Override

    public boolean doSend(AbstractRpcConnector connector){
        RpcAioConnector aioConnector = (RpcAioConnector) connector;
        aioConnector.exeSend();
        return true;
    }
}
