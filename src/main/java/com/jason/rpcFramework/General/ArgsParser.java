package com.jason.rpcFramework.General;

/**
 * Created by baosun on 7/29/2016.
 */
public interface ArgsParser {

    public Object[] parseArgs(String[] argtype, Object[] args);

    public Object parseResult(Object result);

    public void checkArgs(String[] argtype, Object[] args);



}
