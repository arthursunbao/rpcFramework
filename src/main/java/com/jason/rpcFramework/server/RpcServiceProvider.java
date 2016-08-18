package com.jason.rpcFramework.server;

import com.jason.rpcFramework.Exception.RpcNetExceptionHandler;
import com.jason.rpcFramework.RemoteCall;
import com.jason.rpcFramework.RpcObject;
import com.jason.rpcFramework.filter.RpcFilter;
import com.jason.rpcFramework.filter.RpcFilterChain;
import com.jason.rpcFramework.network.RpcCallListener;
import com.jason.rpcFramework.network.Service;
import com.jason.rpcFramework.filter.SimpleRpcFilterChain;
import com.jason.rpcFramework.network.RpcSender;
import com.jason.rpcFramework.utils.RpcUtils;

/**
 * Created by bsun on 2016/8/17.
 */
public class RpcServiceProvider implements RpcCallListener, RpcFilter, Service {

    private RemoteExecutor executor;

    private RpcSerializer serializer;

    private int timeout = 200;

    private RpcNetExceptionHandler exceptionHandler;

    private RpcFilterChain filterChain;

    public RpcServiceProvider(){
        serializer = new JdkSerializer();
        exceptionHandler = new SimpleRpcExceptionHandler();
        filterChain = new SimpleRpcFilterChain();
    }

    @Override
    public void onRpcMessage(RpcObject rpc, RpcSender sender){
        RemoteCall call = this.deserializeCall(rpc, sender);
        RcpContext.getContext().putAll(call.getAttachment());
        try{
            if(call != null){
                filterChain.startFilter(rpc, call, sender);
            }
        }
        catch(Exception e){
            this.handleException(rpc, call, sender, e);
        }
    }

    private RemoteCall deserializeCall(RpcObject rpc, RpcSender sender){
        try{
            return (RemoteCall)serializer.deserialize(rpc.getData());
        }
        catch(Exception e){
            this.handleException(rpc, null, sender, e);
            return null;
        }
    }

    private void execute(RemoteCall call, long threadId, int index, RpcSender sender){
        RpcObject rpc = this.createRpcObject(index);
        rpc.setThreadId(threadId);
        Object result = executor.invoke(call);
        rpc.setType(RpcUtils.RpcType.SUC);
        if(result != null){
            byte[] data = serializer.serialize(result);
            rpc.setLength(data.length);
            rpc.setData(new byte[0]);
        }
        sender.sendRpcObject(rpc,timeout);
    }

    private RpcObject createRpcObject(int index){
        return new RpcObject(0,index, 0, null);
    }

    public RemoteExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(RemoteExecutor executor) {
        this.executor = executor;
    }

    public RpcExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(RpcExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public RpcSerializer getSerializer() {
        return serializer;
    }

    public void setSerializer(RpcSerializer serializer) {
        this.serializer = serializer;
    }

    public void addRpcFilter(RpcFilter filter){
        filterChain.addRpcFilter(filter);
    }

    public void setFilterChain(RpcFilterChain filterChain) {
        this.filterChain = filterChain;
    }

    @Override
    public void startService() {
        filterChain.addRpcFilter(this);
    }

    @Override
    public void stopService() {

    }

    private void handleException(RpcObject rpc, RemoteCall call, RpcSender sender,Exception e){
        RpcUtils.handleException(exceptionHandler,rpc,call,e);
        if(rpc.getType()==RpcType.INVOKE){
            //生成异常数据
            RpcObject respRpc = this.createRpcObject(rpc.getIndex());
            respRpc.setThreadId(rpc.getThreadId());
            respRpc.setType(RpcType.FAIL);
            String message = e.getMessage();
            if(message!=null){
                byte[] data = message.getBytes();
                respRpc.setLength(data.length);
                if(data.length>0){
                    respRpc.setData(data);
                }
            }
            //调用失败异常返回
            sender.sendRpcObject(respRpc, timeout);
        }
    }

    @Override
    public void doFilter(RpcObject rpc, RemoteCall call, RpcSender sender,
                         RpcFilterChain chain) {
        int index = rpc.getIndex();
        if (rpc.getType() == RpcType.ONEWAY) {
            executor.oneway(call);
        } else if (rpc.getType() == RpcType.INVOKE) {
            this.execute(call, rpc.getThreadId(), index, sender);
        }
    }
}
