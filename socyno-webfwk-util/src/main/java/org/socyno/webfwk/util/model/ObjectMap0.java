package org.socyno.webfwk.util.model;

import java.util.HashMap;
import java.util.Map;

import lombok.ToString;

@ToString
public class ObjectMap0<T extends ObjectMap0<?>> {
    
    private final Map<String, Object> data = new HashMap<String, Object>();
    
    public T add(String key, Object value) {
        return put(key, value);
    }
    
    public T addAll(ObjectMap0<T> map) {
        return putAll(map);
    }
    
    public T addAll(Map<String, Object> map) {
        return putAll(map);
    }
    
    public Map<String, Object> asMap() {
        return data;
    }
    
    @SuppressWarnings("unchecked")
    public T clear() {
        data.clear();
        return (T)this;
    }
    
    public Object get(String key) {
        return data.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public T put(String key, Object value) {
        data.put(key, value);
        return (T)this;
    }
    
    @SuppressWarnings("unchecked")
    public T put(String key, Object value, boolean ignoreNull) {
        if (!ignoreNull || value != null) {
            data.put(key, value);
        }
        return (T)this;
    }
    
    @SuppressWarnings("unchecked")
    public T putAll(ObjectMap0<?> map) {
        if (map != null) {
            data.putAll(map.asMap());
        }
        return (T)this;
    }
    
    @SuppressWarnings("unchecked")
    public T putAll(Map<String, Object> map) {
        if (map != null) {
            data.putAll(map);
        }
        return (T)this;
    }
    
    @SuppressWarnings("unchecked")
    public T remove(String key) {
        data.remove(key);
        return (T)this;
    }
    
    public boolean isEmpty() {
        return data.isEmpty();
    }
}
