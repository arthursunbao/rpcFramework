package com.jason.rpcFramework.Exception;

import com.jason.rpcFramework.RemoteCall;
import com.jason.rpcFramework.RpcObject;

/**
 * Created by bsun on 2016/8/29.
 */
public interface RpcExceptionHandler {

    public void handleException(RpcObject rpc, RemoteCall call, Throwable e);

}
