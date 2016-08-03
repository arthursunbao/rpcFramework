package main.java.rpc;

import main.java.rpc.network.Service;
import main.java.rpc.utils.RpcUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bsun on 2016/8/3.
 */
public class SimpleClientRemoteProxy implements InvocationHandler, Service {

    private RemoteExecutor remoteExecutor;

    private ConcurrentHashMap<Class, String> verionCache  = new ConcurrentHashMap<Class, String>();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
        Class<?> service = method.getDeclaringClass()
        String name = method.getName();
        RemoteCall call = new RemoteCall(service.getName(), name);
        call.setArgs(args);
        String version = verionCache.get(service);
        if(version != null){
            call.setVersion(version);
        }
        else{
            call.setVersion(RpcUtils.DEFAULT_VERSION);
        }
        Map<String, Object> attachment = RpcContext.getContext().getAttachment();
        call.setAttachment(attachment);
        if(method.getReturnType() == void.class){
            remoteExecutor.oneway(call);
            return null;
        }
        return remoteExecutor.invoke(call);
    }

    public RemoteExecutor getRemoteExecutor() {
        return remoteExecutor;
    }

    public void setRemoteExecutor(RemoteExecutor remoteExecutor) {
        this.remoteExecutor = remoteExecutor;
    }

    public <Iface> Iface registerRemote(Class<Iface> remote, String version){
        Iface result = (Iface) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{remote}, this);
        if(version == null){
            version = RpcUtils.DEFAULT_VERSION;
        }
        versionCache.put(remote, version);
        return result;
    }

    @Override
    public void startService(){
        remoteExecutor.startService();
    }

    @Override
    public void stopService(){
        remoteExecutor.stopService();
    }

}
