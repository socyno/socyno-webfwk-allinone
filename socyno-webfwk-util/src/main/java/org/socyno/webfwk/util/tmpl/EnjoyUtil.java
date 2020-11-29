package org.socyno.webfwk.util.tmpl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import com.jfinal.kit.HashKit;
import com.jfinal.template.Engine;
import com.jfinal.template.Template;

@Slf4j
public class EnjoyUtil {
    
    private static int CACHE_INDEX = 0;

    static{
        Engine.addExtensionMethod(String.class, EnjoyStringExpand.class);
        Engine.addExtensionMethod(Date.class, EnjoyDateExpand.class);
    }

    /* 缓存数据的数量， 默认为 10K 条 */
    public final static int CACHE_LIMITS = 10240;
    
    private final static Object[] CACHE_DATA = new Object[] {
        new ConcurrentHashMap<String, Template>(),
        new ConcurrentHashMap<String, Template>()
    };
    
    public static int getCacheIndex() {
        return CACHE_INDEX;
    }
    
    public static int getCacheSize() {
        return CACHE_LIMITS;
    }
    
    /**
     * 模板转换，使用 jFinal Enjoy 模板引擎(数据的 key 为 "data")。
     * @param template
     * @param data
     * @return
     */
    public static String format(String template, Object data) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", data);
        return format(template, map);
    }
    
    /**
     * 模板转换，使用 jFinal Enjoy 模板引擎。
     * @param template
     * @param data
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String format(String template, Map<String, Object> data) {

        Template tmpl;
        String key = HashKit.md5(template);
        if (CACHE_INDEX > 1 || CACHE_INDEX < 0) {
            CACHE_INDEX = 0;
        }
        int firstCacheIdx = CACHE_INDEX;
        int secondCacheIdx = firstCacheIdx == 0 ? 1 : 0;
        Map<String, Template> cacheMaster =
                (Map<String, Template>)CACHE_DATA[firstCacheIdx];
        Map<String, Template> cacheSlave  = 
                (Map<String, Template>) CACHE_DATA[secondCacheIdx];
        if ((tmpl = (Template)cacheMaster.get(key)) == null
                && (tmpl = (Template)cacheSlave.get(key)) == null) {
            if (cacheMaster.size() >= CACHE_LIMITS) {
                cacheSlave.clear();
                cacheMaster = cacheSlave;
                CACHE_INDEX = secondCacheIdx;
                log.info("Templates cache index switch to {}",
                                    CACHE_INDEX);
            }

            cacheMaster.put(key, tmpl = Engine.use()
                    .getTemplateByString(template, false));
        }
        return tmpl.renderToString(data);
    }
}
