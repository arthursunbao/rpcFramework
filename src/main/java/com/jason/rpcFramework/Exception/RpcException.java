package com.jason.rpcFramework.Exception;

/**
 * Created by bsun on 2016/8/18.
 */
public class RpcException extends RuntimeException {

    private static final long serialVersionUID = 6238589897120159526L;

    public RpcException(){
        super();
    }

    public RpcException(String message){
        super(message);
    }

    public RpcException(Throwable throwable){
        super(throwable);
    }


}
