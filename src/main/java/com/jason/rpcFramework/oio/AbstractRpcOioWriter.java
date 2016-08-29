package com.jason.rpcFramework.oio;

import com.jason.rpcFramework.RpcObject;
import com.jason.rpcFramework.network.AbstractRpcConnector;
import com.jason.rpcFramework.network.AbstractRpcWriter;
import com.jason.rpcFramework.utils.RpcUtils;

import java.io.DataOutputStream;

/**
 * Created by bsun on 2016/8/29.
 */
public abstract class AbstractRpcOioWriter extends AbstractRpcWriter {

    public AbstractRpcOioWriter(){
        super();
    }

    public boolean exeSend(AbstractRpcConnector con){
        boolean hasSend = false;
        RpcOioConnector connector = (RpcOioConnector) con;
        DataOutputStream dos = connector.getOutputStream();
        while(connector.isNeedToSend()){
            RpcObject rpc = connector.getToSend();
            RpcUtils.writeDataRpc(rpc, dos, connector);
            hasSend = true;
        }
        return hasSend;

    }



}
