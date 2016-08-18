package com.jason.rpcFramework.server;

import com.jason.rpcFramework.monitor.RpcMonitorService;
import com.jason.rpcFramework.nio.AbstractRpcNioSelector;
import com.jason.rpcFramework.filter.RpcFilter;
import com.jason.rpcFramework.filter.RpcStatFilter;
import com.jason.rpcFramework.monitor.RpcMonitorServiceImp;
import com.jason.rpcFramework.network.AbstractRpcNetworkBase;
import com.jason.rpcFramework.nio.RpcNioAcceptor;
import com.jason.rpcFramework.utils.RpcUtils;

import java.util.List;

/**
 * Created by bsun on 2016/8/17.
 */
public abstract class AbstractRpcServer extends AbstractRpcNetworkBase {

    private AbstractRpcAcceptor acceptor;

    private RpcServiceProvider provider  = new RpcServiceProvider();

    private SimpleServerRemoteExecutor proxy = new SimpleServerRemoteExecutor();

    private RpcStatFilter statFilter = new RpcStatFilter();

    private int executorThreadCount = 20;

    public void setAcceptor(AbstractRpcAcceptor acceptor){
        this.acceptor = acceptor;
    }

    public void addRpcFiler(RpcFilter filter){
        provider.addRpcFilter(filter);
    }

    public void register(Class<?> clazz, Object ifaceImpl){
        proxy.registerRemote(clazz, ifaceImpl, null);
    }

    public void register(Class<?> clazz, Object ifaceImpl, String version){
        proxy.registerRemote(clazz, ifaceImpl, version);
    }

    @Override
    public String getHost(){
        String host = super.getHost();
        if(host == null || host.equals("0.0.0.0")){
            List<String> iPs = RpcUtils.getLocalV4IPs();
            String chooseIP = RpcUtils.chooseIP(iPs);
            super.setHost(chooseIP);
        }
        return super.getHost();
    }

    @Override
    public void setHost(String host){
        super.setHost(host);
    }

    @Override
    public void startService(){
        checkAcceptor();
        statFilter.startService();
        this.addRpcFiler(statFilter);
        this.addMonitor();
        this.addGenericSupport();
        acceptor.setHost(this.getHost());
        acceptor.setPort(this.getPort());
        provider.setExecutor(proxy);
        acceptor.addRpcCallListener(provider);
        acceptor.setExecutorThreadCount(executorThreadCount);
        acceptor.setExecutorSharable(false);
        acceptor.startService();
    }

    @Override
    public void stopService(){
        acceptor.stopService();
        proxy.stopService();
        provider.stopService();
        if(statFilter != null) {
            statFilter.stopService();
        }
    }

    public abstract AbstractRpcNioSelector getNioSelector();

    private void checkAcceptor(){
        if(acceptor == null){
            this.setAcceptor(new RpcNioAcceptor(getNioSelector()));
        }
    }

    private void addMonitor(){
        this.register(RpcMonitorService.class, new RpcMonitorServiceImp(proxy, statFilter));
    }

    private void addGenericSupport(){
        this.register(GenericService.class, new SimpleGenericSerivce(proxy));
    }

    public void setExecutorThreadCount(int executorThreadCount) {
        this.executorThreadCount = executorThreadCount;
    }

    public StatMonitor getStatMonitor(){
        return this.statFilter;
    }
}
