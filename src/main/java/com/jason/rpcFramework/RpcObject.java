package com.jason.rpcFramework;

import com.jason.rpcFramework.utils.RpcUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic Defining the data protocal of the RPC Communication
 * Created by baosun on 7/29/2016.
 */
public class RpcObject {

    public void setRpcContext(ConcurrentHashMap<String, Object> rpcContext) {
        this.rpcContext = rpcContext;
    }

    private RpcUtils.RpcType type;

    private long threadId;

    private int index;

    private int length;

    private byte[] data = new byte[10];

    private String host;

    private int port;

    //Context for previous parameters
    private ConcurrentHashMap<String, Object> rpcContext;

    public RpcObject(){

    }

    public RpcObject(RpcUtils.RpcType type, long threadId, int index, int length, byte[] data) {
        this.type = RpcUtils.RpcType.getByType(type);
        this.threadId = Thread.currentThread().getId();
        this.index = index;
        this.length = length;
        this.data = data;
    }

    public RpcUtils.RpcType getType() {
        return type;
    }

    public void setType(RpcUtils.RpcType type) {
        this.type = type;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ConcurrentHashMap<String, Object> getRpcContext() {
        return rpcContext;
    }

    @Override
    public String toString() {
        return "RpcObject [type=" + type + ", threadId=" + threadId
                + ", index=" + index + ", length=" + length + ", host=" + host
                + ", port=" + port + "]";
    }
}
