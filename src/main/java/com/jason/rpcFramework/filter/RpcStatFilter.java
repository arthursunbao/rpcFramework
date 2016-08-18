package com.jason.rpcFramework.filter;

import com.jason.rpcFramework.RemoteCall;
import com.jason.rpcFramework.RpcObject;
import com.jason.rpcFramework.network.Service;
import com.jason.rpcFramework.network.RpcSender;
import com.jason.rpcFramework.utils.RpcUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by bsun on 2016/8/8.
 *
 * Filter Monitor, which monitors the filter by minutes
 */
public class RpcStatFilter implements RpcFilter, Service, StatMonitor {

    private ConcurrentHashMap<Long, AtomicLong> staCache = new ConcurrentHashMap<>();

    private AtomicBoolean running = new AtomicBoolean(false);

    private StatThread thread = new StatThread();

    /**
     *  Callback function of Filter
     */

    /**
     * Filter For Once
     * @param rpc
     * @param call
     * @param sender
     * @param filterChain
     */
    @Override
    public void doFilter(RpcObject rpc, RemoteCall call, RpcSender sender, RpcFilterChain filterChain){
        long now = RpcUtils.getNowMinutes();
        AtomicLong cc = staCache.get(now);
        cc.incrementAndGet();
        filterChain.nextFilter(rpc, call, sender);
    }

    /**
     * Start the filter
     */
    @Override
    public void startService(){
        List<Long> keys = getStatTime();
        ensureAtomicValue(keys);
        running.set(true);
        thread.start();
    }


    /**
     * Stop Service
     */
    @Override
    public void stopService(){
        running.set(false);
        thread.interrupt();
    }

    private void ensureAtomicValue(List<Long> keys){
        Long max = null;
        if(keys.size()> 0){
            max = Collections.max(keys);
        }
        if(max == null){
            max = RpcUtils.getNowMinute() - RpcUtils.MINUTE;
        }
        long key = max + RpcUtils.MINUTE;
        AtomicLong cc = staCache.get(key);
        if(cc == null){
            staCache.put(key, new AtomicLong(0));
        }
        key = max + RpcUtils.MINUTE * 1;
        cc = staCache.get(key);
        if(cc == null){
            staCache.put(key, new AtomicLong(0));
        }
        key = max + RpcUtils.MINUTE * 2;
        cc = staCache.get(key);
        if(cc == null){
            staCache.put(key, new AtomicLong(0));
        }
    }

    private void moveStatKeys(List<Long> keys, long now){
        for(Long k: keys){
            if(k < now - RpcUtils.MINUTE * 30){
                staCache.remove(k);
            }
        }
    }

    private List<Long> getStatTime(){
        Set<Long> set = staCache.keySet();
        ArrayList<Long> keys = new ArrayList();
        keys.addAll(set);
        return keys
    }

    private class StatThread extends Thread(){
        @Override
        public void run(){
            while(running.get()){
                long now = RpcUtils.getNowMinutes();
                List<Long> keys = getStatTime();
                ensureAtomicValue(keys);
                moveStatKeys(keys, now);
                try{
                    Thread.currentThread().sleep(RpcUtils.MINUTE);
                }
                catch(InterruptedException e){
                    break;
                }
            }
        }
    }

    @Override
    public Map<Long, Long> getRpcStat(){
        HashMap<Long, Long> result = new HashMap<>();
        List<Long> times = this.getStatTime();
        for(Long time : times){
            AtomicLong ato = staCache.get(time);
            if(ato != null){
                result.put(time, ato.get());
            }
        }
        return result;
    }



}
