package com.jason.rpcFramework.monitor;

import java.util.Map;

/**
 * Created by bsun on 2016/8/14.
 */
public interface StatMonitor {

    public Map<Long, Long> getRpcStat();

}
