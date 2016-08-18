package com.jason.rpcFramework.network;

import com.jason.rpcFramework.RpcObject;

/**
 * Created by baosun on 7/29/2016.
 */
public interface RpcCallListener {

    public void onRpcMessage(RpcObject rpc, RpcSender sender);

}
