package main.java.rpc.utils;

import main.java.rpc.RemoteCall;

/**
 * Created by bsun on 2016/8/18.
 *
 * This is the proxy interface where user executes the PRC call and forward to service server for real execution
 *
 */

public interface RemoteExecutor {

    /**
     * One way sending and doesn't care about the return value;
     * @param call
     */
    public void oneway(RemoteCall call);

    /**
     * One way sending and return a value after the RPC call.
     * @param call
     * @return
     */
    public Object invoke(RemoteCall call);

    public static final int ONEWAY = RpcUtils.RpcType.ONEWAY.getType();

    public static final int INVOKE = RpcUtils.RpcType.INVOKE.getType();


}
