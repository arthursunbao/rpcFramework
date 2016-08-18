package com.jason.rpcFramework.General;

/**
 * Created by baosun on 7/29/2016.
 */
public interface GeneralService {

    public Object invoke(String service, String version, String method, String[] argtype, Object[] args);

    public void oneway(String service, String version, String method, String[] argtype, Object[] args);

}
