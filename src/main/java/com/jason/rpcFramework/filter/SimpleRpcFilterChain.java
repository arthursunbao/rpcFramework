package com.jason.rpcFramework.filter;

import com.jason.rpcFramework.RemoteCall;
import com.jason.rpcFramework.RpcObject;
import com.jason.rpcFramework.network.RpcSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bsun on 2016/8/8.
 */
public class SimpleRpcFilterChain implements RpcFilterChain {

    private List<RpcFilter> filters = new ArrayList<>();

    private ThreadLocal<Integer> rpcFilterIndex = new ThreadLocal<>();

    private int getAndIncrFilterIndex(){
        Integer index = rpcFilterIndex.get();
        if(index == null){
            index = 0;
        }
        rpcFilterIndex.set(index + 1);
        return index;
    }

    @Override
    public void nextFilter(RpcObject rpc, RemoteCall call, RpcSender sender){
        int index = getAndIncrFilterIndex();
        int size = filters.size();
        if(index > size - 1){
            throw new RuntimeException();
        }
        RpcFilter filter = filters.get(index);
        filter.doFilter(rpc, call, sender, this);
    }

    public void addRpcFilter(RpcFilter filter){
        filters.add(filter);
    }

    @Override
    public void startFilter(RpcObject rpc, RemoteCall call, RpcSender sender){
        try{
            rpcFilterIndex.set(0);
            this.nextFilter(rpc, call, sender);
        }
        finally{
            rpcFilterIndex.remove();
        }
    }
}
