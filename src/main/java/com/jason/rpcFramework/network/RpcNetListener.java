package com.jason.rpcFramework.network;

/**
 * Network Listner to update cluster information
 * Created by baosun on 7/29/2016.
 */
public interface RpcNetListener {

    public void onClose(RpcNetBase network, Exception e);
    public void onStart(RpcNetBase network);

}
