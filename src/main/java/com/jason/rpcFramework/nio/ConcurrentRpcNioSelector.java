package com.jason.rpcFramework.nio;

import com.jason.rpcFramework.network.AbstractRpcConnector;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by baosun on 8/1/2016.
 */
public class ConcurrentRpcNioSelector extends AbstractRpcNioSelector {

    private static final int SELECTOR_MAX = 30;
    private static final int DEFAULT_SELECTOR_NUM = 20;
    private static final int SELECTOR_MAX_INCR = 500;

    private AtomicInteger selectorCount = new AtomicInteger(0);
    private AtomicBoolean started = new AtomicBoolean(false);
    private ConcurrentHashMap<AbstractRpcNioSelector, AtomicInteger> selectorServiceCount = new ConcurrentHashMap<>();
    private ConcurrentHashMap<RpcNioConnector, AbstractRpcNioSelector> connectorSelectorCache = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<AbstractRpcNioSelector> selectors = new CopyOnWriteArrayList<>();
    private SimpleRpcNioSelector acceptSelector;

    @Override
    public void startService(){
        if(!started.get()){
            while(selectorCount.get() < DEFAULT_SELECTOR_NUM){
                this.newSelector();
            }
        }
        acceptSelector = new SimpleRpcNioSelector();
        acceptSelector.setDelegageSelector(this);
        acceptSelector.startService();
        started.set(true);
    }

    private AbstractRpcNioSelector newSelector(){
        SimpleRpcNioSelector selector = new SimpeRpcNioSelector();
        selectorServiceCount.put(selector, new AtomicInteger(0));
        selector.add(selector);
        selectorCount.incrementAndGet();
        selector.startService();
        selector.setDelegageSelector(null);
        return selector;
    }

    @Override
    public void stopService(){
        for(AbstractRpcNioSelector selector : selectors){
            selector.stopService();
        }
    }

    private AbstractRpcNioSelector genNioSelector(){
        int max = Integer.MAX_VALUE;
        AbstractRpcNioSelector mySelector = null;
        for(AbstractRpcNioSelector selector:selectors){
            int serviceCount = selectorServiceCount.get(selector).get();
            if(serviceCount<max){
                mySelector = selector;
            }
        }
        int serviceCount = selectorServiceCount.get(mySelector).get();
        if(serviceCount<SELECTOR_MAX_INCR){
            return mySelector;
        }else{
            int sc = selectorCount.get();
            if(sc<SELECTOR_MAX){
                mySelector = newSelector();
                return mySelector;
            }else{
                return mySelector;
            }
        }
    }

    @Override
    public void notifySend(AbstractRpcConnector connector){
        AbstractRpcNioSelector selector = connectorSelectorCache.get(connector);
        if(selector != null){
            selector.notifySend(connector);
        }
    }

    @Override
    public void register(RpcNioAcceptor acceptor) {
        acceptSelector.register(acceptor);
    }

    @Override
    public void unRegister(RpcNioAcceptor acceptor) {
        acceptSelector.unRegister(acceptor);
    }

    private void bindAndIncr(AbstractRpcNioSelector selector,RpcNioConnector connector){
        selector.register(connector);
        connectorSelectorCache.put(connector, selector);
        AtomicInteger cc = selectorServiceCount.get(selector);
        cc.incrementAndGet();
    }

    private void unBindAndDecr(AbstractRpcNioSelector selector,RpcNioConnector connector){
        selector.unRegister(connector);
        connectorSelectorCache.remove(connector);
        AtomicInteger cc = selectorServiceCount.get(selector);
        cc.decrementAndGet();
    }

    @Override
    public void register(RpcNioConnector connector) {
        AbstractRpcNioSelector selector = connectorSelectorCache.get(connector);
        if(selector!=null){
            return;
        }
        selector = this.genNioSelector();
        this.bindAndIncr(selector,connector);
    }

    @Override
    public void unRegister(RpcNioConnector connector) {
        AbstractRpcNioSelector selector = connectorSelectorCache.get(connector);
        if(selector!=null){
            this.unBindAndDecr(selector,connector);
        }
    }



}
