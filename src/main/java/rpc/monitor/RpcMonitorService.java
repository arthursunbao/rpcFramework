package main.java.rpc.monitor;

/**
 * Created by bsun on 2016/8/14.
 */
public interface RpcMonitorService {

    public List<RpcService> getRpcService();

    public String ping();


}
