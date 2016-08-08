package main.java.rpc.filter;

import main.java.rpc.RemoteCall;
import main.java.rpc.RpcObject;
import main.java.rpc.network.RpcSender;

/**
 * FilterChain like Responsibility Chain Model to filter out user-needed information step by step
 * Created by bsun on 2016/8/8.
 */
public interface RpcFilterChain {

    public void nextFilter(RpcObject rpc, RemoteCall call, RpcSender sender);


    /**
     * Adding an RPC Filter
     * @param filter
     */
    public void addRpcFilter(RpcFilter filter);


    /**
     * Start the Filter
     * @param rpc
     * @param call
     * @param sender
     */
    public void startFilter(RpcObject rpc, RemoteCall call, RpcSender sender);


}
