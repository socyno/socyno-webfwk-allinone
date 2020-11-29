package org.socyno.webfwk.util.remote;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public abstract class R0<T extends R0<?>> {
    private int status = 0;
    private String message = "ok";
    private Object data = null;

    public R0() {
        this(0, "ok");
    }
    
    public R0(String message) {
        this(0, message);
    }
    
    public R0(int status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public int getCode() {
        return getStatus();
    }
    
    public String getMsg() {
        return getMessage();
    }
    
    public T setCode(int status) {
        return setStatus(status);
    }
    
    @SuppressWarnings("unchecked")
    public T setData(Object data) {
        this.data = data;
        return (T)this;
    }
    
    @SuppressWarnings("unchecked")
    public T setMessage(String message) {
        this.message = message;
        return (T)this;
    }
    
    public T setMsg(String message) {
        return setMessage(message);
    }
    
    @SuppressWarnings("unchecked")
    public T setStatus(int status) {
        this.status = status;
        return (T)this;
    }
    
    @SuppressWarnings("unchecked")
    public T put(final String key, final Object value) {
        if (!(data instanceof Map)) {
            data = new HashMap<String , Object>();
        }
        ((Map<String , Object>)data).put(key , value);
        return (T)this;
    }
}
