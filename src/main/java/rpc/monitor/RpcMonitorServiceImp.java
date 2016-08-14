package main.java.rpc.monitor;

import main.java.rpc.RpcServiceBean;

import java.util.*;

/**
 * Created by bsun on 2016/8/14.
 */
public class RpcMonitorServiceImp implements RpcMonitorService {

    private RpcServicesHolder rpcServiceHolder;

    private StatMonitor statMonitor;

    private long time = 0;

    public RpcMonitorServiceImp(RpcServicesHolder rpcServiceHolder, StatMonitor statMonitor){
        this.rpcServiceHolder = rpcServiceHolder;
        time = System.currentTimeMillis();
    }

    @Override
    public List<RpcService> getRpcService(){
        if(rpcServicesHolder != null){
            List<RpcService> list = new ArrayList<RpcService>();
            for(RpcServiceBean service:services){
                RpcService rpcService = new RpcService(service.getInterf().getName(),service.getVersion(),service.getBean().getClass().getName());
                rpcService.setTime(time);
                list.add(rpcService);
            }
            return list;
        }
        return Collections.emptyList();

    }

    @Override
    public String ping(){
        return "ping" + new Date();
    }

    @Override
    public Map<Long, Long> getRpcStat(){
        if(statMonitor != null){
            return statMonitor.getRpcStat();
        }
        return new HashMap<Long, Long>();


    }

}
