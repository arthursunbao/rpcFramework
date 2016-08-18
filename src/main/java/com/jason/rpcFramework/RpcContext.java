package com.jason.rpcFramework;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by baosun on 7/29/2016.
 */
public class RpcContext {
    private static ThreadLocal<RpcContext> context = new ThreadLocal<RpcContext>(){
        @Override
        protected RpcContext initialValue(){
            return new RpcContext();
        }
    };

    public static RpcContext getContext(){
        return context.get();
    }

    public void clear(){
        context.remove();
    }

    private Map<String, Object> attachment = new HashMap<>();

    public void putAll(Map<String, Object> attachment){
        if(attachment != null){
            attachment.putAll(attachment);
        }
    }

    public Map<String, Object> getAttachment(){
        return this.attachment;
    }

    public int size(){
        return this.attachment.size();
    }

    public Object getAttachment(String key){
        return attachment.get(key);
    }

    public void setAttachment(String key, Object Value){
        this.attachment.put(key , Value);
    }
}

