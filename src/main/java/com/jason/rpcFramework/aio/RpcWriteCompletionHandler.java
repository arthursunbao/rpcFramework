package com.jason.rpcFramework.aio;

/**
 * Created by baosun on 8/4/2016.
 */
public class RpcWriteCompletionHandler {

    @Override
    public void completed(Integer num, RpcAioConnector connector) {
        if(num!=null){
            connector.writeCallback(num);
        }
    }

    @Override
    public void failed(Throwable e, RpcAioConnector connector) {
        connector.handleFail(e, connector);
    }

}
