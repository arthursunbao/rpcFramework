package main.java.rpc.server;

import main.java.rpc.RpcServiceBean;

import java.util.List;

/**
 * Created by bsun on 2016/8/14.
 */
public interface RpcServicesHolder {

    public List<RpcServiceBean> getRpcServices();
}
