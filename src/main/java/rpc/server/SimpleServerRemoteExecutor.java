package main.java.rpc.server;

import main.java.rpc.Exception.RpcException;
import main.java.rpc.Exception.RpcNetExceptionHandler;
import main.java.rpc.RemoteCall;
import main.java.rpc.RpcServiceBean;
import main.java.rpc.network.Service;
import main.java.rpc.utils.RemoteExecutor;
import main.java.rpc.utils.RpcUtils;
import main.java.rpc.server.RpcServicesHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bsun on 2016/8/17.
 */
public class SimpleServerRemoteExecutor implements RemoteExecutor, RpcServicesHolder, Service{

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

    public void registerRemote(Class<?> clazz, Object ifaceImpl,String version){
        Object service = exeCache.get(clazz.getName());
        if(service != null && service != ifaceImpl){
            throw new RpcException("Cannot register for this service" + clazz.getName() + " ");
        }
        if(ifaceImpl == service || ifaceImpl == null){
            return;
        }
        exeCache.put(this.genExeKey(clazz.getName(), version), new RpcServiceBean(clazz, ifaceImpl, version));
    }

    private String genExeKey(String service, String version){
        if(version != null){
            return service + "_" + version;
        }
        return service;
    }

    private Object findService(RemoteCall call){
        String exeKey = this.genExeKey(call.getService(),call.getVersion());
        RpcServiceBean object = exeCache.get(exeKey);
        if(object == null || object.getBean() == null){
            throw new RpcException("Service " + call.getService() + "Version" + call.getVersion() + "Not Found");
        }
        return object.getBean();
    }

    @Override
    public void startService(){

    }

    @Override
    public void stopService(){

    }

    public RpcNetExceptionHandler getExceptionHandler(){
        return exceptionHandler;
    }

    public void setExceptionHandler(RpcNetExceptionHandler exceptionHandler) {this.exceptionHandler = exceptionHandler;}

    public List<RpcServiceBean> getRpcServices(){
        ArrayList<RpcServiceBean> serviceBeanList = new ArrayList<>();
        serviceBeanList.addAll(exeCache.values());
        return serviceBeanList;
    }



}
