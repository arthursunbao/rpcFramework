package com.jason.rpcFramework.network;

import com.jason.rpcFramework.RpcObject;

/**
 * Created by baosun on 7/29/2016.
 */
public interface RpcSender {

    public boolean sendRpcObject(RpcObject rpc, int timeout);

}
