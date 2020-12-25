package com.weimob.webfwk.state.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.weimob.webfwk.util.context.ContextUtil;

import lombok.Getter;

public class StateFormDisplayScheduled {
    
    @Getter
    private final static String FormDisplayTable = "system_form_display";
    
    private final static Map<String, String> FROM_DISPLAY_DATA = new ConcurrentHashMap<String, String>();
    
    synchronized public static void reload() throws Exception {
        List<StateFormDisplayEntity> entities = ContextUtil.getBaseDataSource().queryAsList(
                StateFormDisplayEntity.class, String.format("SELECT name, display FROM %s", FormDisplayTable), null);
        if (entities == null || entities.isEmpty()) {
            FROM_DISPLAY_DATA.clear();
            return;
        }
        for (StateFormDisplayEntity e : entities) {
            if (e == null) {
                continue;
            }
            FROM_DISPLAY_DATA.put(e.getName(), e.getDisplay());
        }
    }
    
    public static String getDisplay(String name) {
        return FROM_DISPLAY_DATA == null ? null : FROM_DISPLAY_DATA.get(name);
    }
}
