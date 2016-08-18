package com.jason.rpcFramework.filter;

import com.jason.rpcFramework.RemoteCall;
import com.jason.rpcFramework.RpcObject;
import com.jason.rpcFramework.network.RpcSender;

/**
 * Created by bsun on 2016/8/8.
 * Filter Interface.
 * Filter is used for filtering Rpc Contents by custom filter defination by user. Like the streaming process
 *
 */
public interface RpcFilter {
    /*
        Filtering Interface
     */
    public void doFilter(RpcObject rpc, RemoteCall call, RpcSender sender, RpcFilterChain chain);

}
