package main.java.rpc.server;

import main.java.rpc.nio.AbstractRpcNioSelector;
import main.java.rpc.nio.SimpleRpcNioSelector;

/**
 * Created by bsun on 2016/8/17.
 */
public class SimpleRpcServer extends AbstractRpcServer{

    private AbstractRpcNioSelector nioSelector;

    @Override
    public AbstractRpcNioSelector getNioSelector(){
        if(nioSelector == null){
            nioSelector = new SimpleRpcNioSelector();
        }
        return nioSelector;
    }
}
