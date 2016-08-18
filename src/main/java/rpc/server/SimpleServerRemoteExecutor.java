package main.java.rpc.server;

import main.java.rpc.Exception.RpcNetExceptionHandler;
import main.java.rpc.RemoteCall;
import main.java.rpc.RpcServiceBean;
import main.java.rpc.utils.RpcUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bsun on 2016/8/17.
 */
public class SimpleServerRemoteExecutor implements RemoteExecutor, RpcServiceHolder{

    protected ConcurrentHashMap<String, RpcServiceBean> exeCache = new ConcurrentHashMap<>();

    private RpcNetExceptionHandler exceptionHandler;

    public SimpleServerRemoteExecutor(){
        exceptionHandler = new SimpleRpcExceptionHandler();
    }

    @Override
    public void oneway(RemoteCall call){
        RpcUtils.invokeMethod(this.findService(call), call.getMethod(), call.getArgs(), exceptionHandler);
    }

    @Override
    public Object invoke(RemoteCall call) {
        return RpcUtils.invokeMethod(this.findService(call), call.getMethod(), call.getArgs(),exceptionHandler);
    }

    public void registerRemote(Class<?> clazz,Object ifaceImpl){
        this.registerRemote(clazz, ifaceImpl,null);
    }


}
