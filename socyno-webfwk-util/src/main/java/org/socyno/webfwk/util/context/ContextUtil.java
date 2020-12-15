package org.socyno.webfwk.util.context;

import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.util.conf.ConfigFlags;
import org.socyno.webfwk.util.conf.ConfigService;
import org.socyno.webfwk.util.conf.PropertyPlaceholderLoader;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.tool.CommonUtil;

@Slf4j
public class ContextUtil {
    
    public static boolean configAccessable() {
        ConfigService srv = null;
        try {
            srv = getConfigService();
        } catch (Exception e) {
            log.trace(e.toString(), e);
        }
        return srv != null;
    }
    
    private static ConfigService getConfigService() {
        return PropertyPlaceholderLoader.getConfigService();
    }
    
    public static AbstractDao getBaseDataSource() {
        return PropertyPlaceholderLoader.getBaseDataSource();
    }
    
    public static void deleteConfigs(String... names) throws Exception {
        getConfigService().deleteConfigs(names);
    }
    
    public static void setConfig(String name, String value) throws Exception {
        getConfigService().setConfig(name, value);
    }
    
    public static void setConfig(String name, String value, String comment) throws Exception {
        getConfigService().setConfig(name, value, comment);
    }
    
    public static String getConfig(String key) {
        return getConfigService().getValue(key);
    }
    
    public static String getConfig(String key, int flags) {
        return getConfigService().getValue(key, flags);
    }
    
    public static String getConfigTrimed(String key) {
        return getConfigService().getValue(key, ConfigFlags.TRIMED);
    }
    
    public static String[] getConfigs(String key) {
        return getConfigService().getValues(key, "[,;]+");
    }
    
    public static String[] getConfigsNonBlankTrimed(String key) {
        return getConfigService().getValues(key, "[,;]+",
                ConfigFlags.TRIMED | ConfigFlags.NONBLANK | ConfigFlags.UNIQUE);
    }
    
    public static String[] getConfigs(String key, String regexp) {
        return getConfigService().getValues(key, regexp);
    }
    
    public static String[] getConfig(String key, String regexp, int flags) {
        return getConfigService().getValues(key, regexp, flags);
    }
    
    public static List<String> getConfigKeys() {
        return getConfigService().getConfigKeys();
    }
    
    public static boolean inDebugMode() {
        return configAccessable() && "yes".equals(getConfigTrimed("system.basic.debug.enabled"));
    }
    
    public static int getSlowSqlMillisecond() {
        final int dfltMs = 1000;
        if (configAccessable()) {
            final int slowMs = CommonUtil.parseInteger(getConfigTrimed("system.base.dao.sql.slow.millisecond"), dfltMs);
            return slowMs > 0 ? slowMs : dfltMs;
        }
        return dfltMs;
    }
    
    public static Map<String, String> getConfigAsStringMap(String key) {
        String config = getConfig(key);
        if (StringUtils.isBlank(config)) {
            return Collections.emptyMap();
        }
        return CommonUtil.fromJson(config, new TypeToken<Map<String, String>>() {}.getType());
    }
    
    public static String getCurrentEnvironment() {
        
        String currentEnv = ContextUtil.getConfigTrimed("system.current.environment.name");
        if (StringUtils.isBlank(currentEnv)) {
            throw new MessageException("请配置添加数据源的环境");
        }
        return currentEnv;
    }
}