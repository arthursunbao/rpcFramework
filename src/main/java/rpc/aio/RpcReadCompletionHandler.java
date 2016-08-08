package main.java.rpc.aio;

/**
 * Created by baosun on 8/4/2016.
 */
public class RpcReadCompletionHandler implements CompletionHandler<Integer,RpcAioConnector> {

    @Override
    public void completed(Integer num, RpcAioConnector connector) {
        if(num!=null){
            connector.readCallback(num);
        }
    }

    @Override
    public void failed(Throwable e, RpcAioConnector connector) {
        connector.handleFail(e, connector);
    }

}
