package main.java.rpc.monitor;

import main.java.rpc.RpcService

import java.util.List;

/**
 * Created by bsun on 2016/8/14.
 */
public interface RpcMonitorService {

    public List<RpcService> getRpcService();

    public String ping();


}
