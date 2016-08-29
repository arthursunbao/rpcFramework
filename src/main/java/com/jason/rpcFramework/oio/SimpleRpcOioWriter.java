package com.jason.rpcFramework.oio;

import com.jason.rpcFramework.network.AbstractRpcConnector;

/**
 * Created by bsun on 2016/8/29.
 */
public class SimpleRpcOioWriter extends AbstractRpcOioWriter {

    @Override
    public boolean doSend(AbstractRpcConnector connector){
        return super.exeSend(connector);
    }

}
