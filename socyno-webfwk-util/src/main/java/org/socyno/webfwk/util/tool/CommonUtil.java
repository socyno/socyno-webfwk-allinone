package org.socyno.webfwk.util.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.socyno.webfwk.util.exception.MessageException;

import com.github.reinert.jjschema.v1.FieldOption;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

@Slf4j
public class CommonUtil {
    private static Gson gsonDefault = new GsonBuilder().disableHtmlEscaping().create();
    private static Gson gsonSeNulls = new GsonBuilder().serializeNulls()
            .create();
    private static Gson gsonPretty = new GsonBuilder().setPrettyPrinting()
            .create();
    private static Gson gsonPrettyNulls = new GsonBuilder().serializeNulls()
            .setPrettyPrinting().create();

    private static Gson gsonFieldNamingPolicy = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private static String[] regexpKeyworlds = { "\\", "$", "(", ")", "*", "+",
            ".", "[", "]", "?", "^", "{", "}", "|" };
    
    private static final Pattern regexpSensitive = Pattern.compile(
            "(token|password)(\\\"?\\s*[\\=\\:]\\s*\\[?\\\"?)([^,;\\\"\\'&}]+)",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern REGEXP_BEAN_STRING_ARRAY = Pattern.compile("^\\[([^\\]]+)\\](.+)$");
    
    public final static int STR_TRIMED = 1;
    public final static int STR_LOWER = 2;
    public final static int STR_UPPER = 4;
    public final static int STR_NONBLANK = 8;
    public final static int STR_UNIQUE = 16;
    public final static int STR_PATHEND = 32;

    /**
     * 判断对象是否为 NULL，是则返回默认值，否则返回对象自身
     * */
    public static <T> T ifNull(T obj, T e) {
        return obj == null ? e : obj;
    }
    
    /**
     * 判断对象是否为 NULL，是则返回前值，否则返回后值
     */
    public static <T> T ifNull(T obj, T yes, T no) {
        return obj == null ? yes : no;
    }
    
    /**
     * 检查字符串是否为空。 是 ： 返回默认值； 否 ：返回当前值。
     */
    public static String ifBlank(String str, String ifBlank) {
        return StringUtils.isBlank(str) ? ifBlank : str;
    }
    
    /**
     * 检查字符串是否为空。 是 ： 返回 yesValue； 否 ：返回 noValue。
     */
    public static String ifBlank(String str, String yesValue, String noValue) {
        return StringUtils.isBlank(str) ? yesValue : noValue;
    }
    
    /* 如果字串为 null， 返回空字串，否则返回字串本身 */
    public static String nullToEmpty(String str) {
        return ifNull(str, "");
    }

    /* 整数解析 */
    public static Integer parseInteger(Object intstr) {
        if (intstr == null) {
            return null;
        }
        if (intstr instanceof Boolean || boolean.class.equals(intstr.getClass())) {
            return ((boolean)intstr) ? 1 : 0;
        }
        try {
            Double dbl = Double.valueOf(intstr.toString());
            return dbl.intValue();
        } catch (NumberFormatException e) {

        }
        return null;
    }

    public static int parseInteger(Object str, int defaultValue) {
        Integer parsed;
        if ((parsed = parseInteger(str)) == null) {
            return defaultValue;
        }
        return parsed.intValue();
    }
    
    /**
     * 获取两者中较小的值
     **/
    public static int parseMinimalInteger(Object value, int minimal) {
        int parsed = parseInteger(value, minimal);
        return parsed > minimal ? minimal : parsed;
    }
    
    /**
     * 获取两者中较大的值
     **/
    public static int parseMaximalInteger(Object value, int maximal) {
        int parsed = parseInteger(value, maximal);
        return parsed < maximal ? maximal : parsed;
    }
    
    /**
     * 正整数解析(>0)，如不是正整数则返回默认值
     */
    public static int parsePositiveInteger(Object value, int defaultValue) {
        int parsed = parseInteger(value, 0);
        return parsed > 0 ? parsed : defaultValue;
    }
    
    /* 长整数解析 */
    public static Long parseLong(Object value) {
        return parseLong(value, null);
    }
    
    /* 长整数解析 */
    public static Long parseLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean || boolean.class.equals(value.getClass())) {
            return ((boolean)value) ?1L : 0L;
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            Double parsed;
            if ((parsed = parseDouble(value)) != null) {
                defaultValue = parsed.longValue();
            }
        }
        return defaultValue;
    }

    /**
     * 数字转换， 返回 Double 类型
     */
    public static Double parseDouble(Object value, Double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Boolean || boolean.class.equals(value.getClass())) {
            return ((boolean)value) ? 1.0 : 0.0;
        }
        try {
            return Double.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 数字转换， 返回 Double 类型 如果无法转换， 返回 null值
     */
    public static Double parseDouble(Object value) {
        return parseDouble(value, null);
    }

    /* 长整数解析,获取两者中较小的值 */
    public static long parseMinimalLong(Object value, long minimal) {
        long parsed = parseLong(value, minimal);
        return parsed > minimal ? minimal : parsed;
    }

    /* 长整数解析,获取两者中较大的值 */
    public static long parseMaximalLong(Object value, long minimal) {
        long parsed = parseLong(value, minimal);
        return parsed < minimal ? minimal : parsed;
    }

    /* 长整数解析，如不是正整数（>0）则返回默认值 */
    public static long parsePositiveLong(Object value, long defaultValue) {
        long parsed = parseLong(value, 0L);
        return parsed > 0 ? parsed : defaultValue;
    }

    /* JSON 序列化对象 */
    public static String toJson(Object obj) {
        return gsonDefault.toJson(obj);
    }
    /* JSON 序列化对象(自动驼峰命名) */
    public static <T> T toJsonByFieldNamingPolicy(String json, Type classOfT){
        return gsonFieldNamingPolicy.fromJson(json, classOfT);
    }
    
    /* JSON 序列化对象(自动驼峰命名) */
    public static <T> T fromJsonByFieldNamingPolicy(JsonElement json, Type classOfT){
        return gsonFieldNamingPolicy.fromJson(json, classOfT);
    }

    /* JSON 序列化对象(自动驼峰命名) */
    public static String toJson(Object obj, boolean serializeNulls) {
        return serializeNulls ? gsonSeNulls.toJson(obj) : gsonDefault
                .toJson(obj);
    }

    /* JSON 序列化对象 */
    public static JsonElement toJsonElement(Object obj) {
        return gsonDefault.toJsonTree(obj);
    }

    /* JSON 序列化对象 */
    public static String toPrettyJson(Object obj) {
        return gsonPretty.toJson(obj);
    }

    /* JSON 序列化对象 */
    public static String toPrettyJson(Object obj, boolean serializeNulls) {
        return serializeNulls ? gsonPrettyNulls.toJson(obj) : gsonPretty
                .toJson(obj);
    }
    
    /**
     * 将Json数据中的属性名称按照Java属性命名规范转换
     */
    public static JsonElement convertByFieldNaming(JsonElement json) {
        if (json == null) {
            return null;
        }
        if (json.isJsonObject()) {
            HashMap<String, Object> data = new HashMap<String, Object>();
            for (Map.Entry<String, JsonElement> e : ((JsonObject)json).entrySet()) {
                data.put(applyFieldNamingPolicy(e.getKey()), convertByFieldNaming(e.getValue()));
            }
            return fromObject(data, JsonElement.class);
        }
        else if (json.isJsonArray()) {
            JsonArray array = new JsonArray();
            for (JsonElement e : (JsonArray)json) {
                array.add(convertByFieldNaming(e));
            }
            return array;
        } else {
            return json;
        }
    }
    
    /**
     * 转换成数据库可识别的时间格式 即： yyy-MM-dd HH:mm:ss
     */
    public static String toSqlString(Date datetime) {
        if (datetime == null) {
            return null;
        }
        return DateFormatUtils.format(datetime, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 强制字符转转换(尝试使用多种字符集 UTF-8, GBK, ISO8859-1)
     * 
     * @param bytes
     * @param emptyIfFail
     *            如果字串无法转换，是返空字串还是null
     * @param nullToEmpty
     *            如果字节数据为 null， 是返回空字串还是null
     * @return 如果 bytes 为 null值，则返回 null； 如果在尝试以上编码后仍无法转换, 返回 null 或
     *         空(emptyIfFail=true)
     */
    public static String toString(byte[] bytes, boolean emptyIfFail,
            boolean nullToEmpty) {
        if (bytes == null) {
            return nullToEmpty ? "" : null;
        }
        try {
            return new String(bytes, "UTF-8");
        } catch (IOException e) {
        }
        try {
            return new String(bytes, "GBK");
        } catch (IOException e) {
        }
        try {
            return new String(bytes, "ISO8859-1");
        } catch (IOException e) {
        }
        return emptyIfFail ? "" : null;
    }

    public static String toString(byte[] bytes) {
        return toString(bytes, false, false);
    }

    public static String toEmptyString(byte[] bytes, boolean emptyIfFail) {
        return toString(bytes, emptyIfFail, true);
    }

    public static String toNonNullString(byte[] bytes, boolean emptyIfFail) {
        return toString(bytes, true, true);
    }

    /* JSON 反序列化对象 */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gsonDefault.fromJson(json, clazz);
    }

    /* JSON 反序列化对象 */
    public static <T> T fromJson(String json, Type typeOfT) {
        return gsonDefault.fromJson(json, typeOfT);
    }

    /* JSON 反序列化对象 */
    public static <T> T fromObject(Object json, Class<T> clazz) {
        if (json instanceof JsonElement) {
            return gsonDefault.fromJson((JsonElement) json, clazz);
        }
        return fromJson(gsonDefault.toJson(json), clazz);
    }

    /* JSON 反序列化对象 */
    public static <T> T fromObject(Object json, Type clazz) {
        if (json instanceof JsonElement) {
            return gsonDefault.fromJson((JsonElement) json, clazz);
        }
        return fromJson(gsonDefault.toJson(json), clazz);
    }

    /* 解析ISO08601 格式时间，否则返回默认值 */
    public static long timeMSFromISO8601(String dateTime, long defaultMS) {
        if (dateTime != null) {
            try {
                DateFormat df = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                return df.parse(
                        dateTime.replaceAll("\\+0([0-9]){1}\\:00", "+0$100"))
                        .getTime();
            } catch (ParseException e) {
            }
        }
        return defaultMS;
    }
    
    /**
     * 检查给定的字串是否为空白，如果是则抛出异常，否则返回首位去空白后的值
     * 
     * @param str
     *            要检查的字符串
     * @param errmsg
     *            空白时的异常信息
     * @return 移除首位空白后的字符串
     */
    public static String ensureNonBlank(String str, String errmsg)
            throws MessageException {
        if (StringUtils.isBlank(str)) {
            throw new MessageException(errmsg);
        }
        return str.trim();
    }
    
    /**
     * 检查给定的对象是否为Null值，如果是则抛出异常
     * 
     * @param object
     *            要检查的对象
     * @param errmsg
     *            空时的异常信息
     */
    public static void ensureNonNull(Object obj, String errmsg)
            throws MessageException {
        if (obj == null) {
            throw new MessageException(errmsg);
        }
        return;
    }
    
    /* 正则表达式逃逸 */
    public static String escapeRegexp(String str) {
        if (StringUtils.isNotBlank(str)) {
            for (String key : regexpKeyworlds) {
                if (str.contains(key)) {
                    str = str.replace(key, "\\" + key);
                }
            }
        }
        return str;
    }

    /* 数组去重 */
    public static <T> T[] uniqueArray(@NonNull T[] array) {
        return uniqueArray(array, false);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] uniqueArray(T[] array, boolean clearNull) {
        if (array == null || array.length < 2) {
            return array;
        }
        Collection<T> uniqued = uniqueCollection(Arrays.asList(array));
        return uniqued.toArray((T[]) Array.newInstance(array.getClass()
                .getComponentType(), uniqued.size()));
    }

    /* 集合去重 */
    public static <T> Collection<T> uniqueCollection(Collection<T> collection) {
        if (collection == null || collection.size() < 2) {
            return collection;
        }
        return new LinkedHashSet<T>(collection);
    }

    /* 替换下划线 */
    public static String applyFieldNamingPolicy(@NonNull String str) {
        StringBuffer dest = new StringBuffer();
        for (String sub : str.split("_")) {
            if (sub.length() == 0) {
                continue;
            }
            if (dest.length() == 0) {
                dest.append(sub);
                continue;
            }
            dest.append(Character.toUpperCase(sub.charAt(0))).append(
                    sub.substring(1));
        }
        return dest.toString();
    }
    
    /* 将 MAP 转换为指定对象 */
    public static <T> T toBean(Map<String, Object> data, Class<T> clazz) throws Exception {
        return toBean(data, clazz, null);
    }
    
    /**
     * 将 MAP 封装为指定类对象, 可以自定义属性映射表。注意：此函数仅通过遍历并调用 public 的 set 方法封装对象。
     * 
     * @param data
     *            - 对象数据
     * @param clazz
     *            - 封装类对象
     * @param mapper
     *            - 属性映射表，键是数据的名称，值为属性名称。
     * 
     **/

    public static <T> T toBean(Map<String, Object> data, Class<T> clazz, Map<String, String> mapper) throws Exception {
        return toBean(data, clazz, mapper, false);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T toBean(Map<String, Object> data, @NonNull Class<T> clazz, Map<String, String> mapper, boolean primitiveSupported) throws Exception {
        if (data == null) {
            return null;
        }
        
        /* 简单类型处理 */
        if (primitiveSupported && (clazz.isPrimitive() 
                    || String.class.isAssignableFrom(clazz)
                    || Number.class.isAssignableFrom(clazz))) {
            if (data.isEmpty()) {
                return null;
            }
            if (data.size() != 1) {
                throw new RuntimeException("More then one column for primitive result.");
            }
            for (Object value : data.values()) {
                if (Long.class.equals(clazz) || long.class.equals(clazz)) {
                    return (T) CommonUtil.parseLong(value);
                }
                if (Integer.class.equals(clazz) || int.class.equals(clazz)) {
                    return (T) CommonUtil.parseInteger(value);
                }
                if (Double.class.equals(clazz) || double.class.equals(clazz)) {
                    return (T) CommonUtil.parseDouble(value);
                }
                if (String.class.equals(clazz)) {
                    return (T)(value == null ? null : value.toString());
                }
                return (T)value;
            }
        }
        
        /* 字段映射处理 */
        if (mapper != null) {
            for (Map.Entry<String, String> m : mapper.entrySet()) {
                String k1 = m.getKey();
                String dk = m.getValue();
                boolean replaced = false;
                String arraySeparater = null;
                if (k1 != null && k1.length() > 1 && k1.substring(0,1).equals("-")) {
                    replaced = true;
                    k1 = k1.substring(1);
                } else if (k1 != null && k1.length() > 1 && k1.substring(0,1).equals("[")) {
                    Matcher matched;
                    if ((matched = REGEXP_BEAN_STRING_ARRAY.matcher(k1)) != null && matched.find()) {
                        k1 = matched.group(2);
                        arraySeparater = matched.group(1);
                    }
                }
                for (String key : new String[] { k1, applyFieldNamingPolicy(k1) }) {
                    if (data.containsKey(key)) {
                        if (replaced) {
                            data.put(key, dk);
                        } else {
                            Object val = data.get(key);
                            if (arraySeparater != null && val != null && CharSequence.class.isAssignableFrom(val.getClass())) {
                                val = val.toString().split(arraySeparater);
                                if (StringUtils.isBlank(dk)) {
                                    data.put(key, val);
                                }
                            }
                            if (StringUtils.isNotBlank(dk)) {
                                data.put(dk, val);
                            }
                        }
                        break;
                    }
                }
            }
        }
        
        T obj = clazz.newInstance();
        for (Method method : clazz.getMethods()) {
            int mod = method.getModifiers();
            String name = method.getName();
            Class<?>[] ptypes = method.getParameterTypes();
            if (Modifier.isStatic(mod) || ptypes.length != 1
                    || name.length() < 4 || !name.startsWith("set")) {
                continue;
            }
            name = String.format("%s%s", Character.toLowerCase(name.charAt(3)),
                    name.substring(4));
            if (data.containsKey(name)) {
                Object val = data.get(name);
                /* boolean 转换 */
                if (boolean.class.equals(ptypes[0])) {
                    val = parseBoolean(val);
                } else if (Boolean.class.equals(ptypes[0])) {
                    val = parseBooleanAllownNull(val);
                }
                /* char 转换 */
                else if (char.class.equals(ptypes[0]) || Character.class.equals(ptypes[0])) {
                    val = parseCharacter(val);
                }
                /* long 转换 */
                else if ((Long.class.equals(ptypes[0]) || long.class.equals(ptypes[0]))) {
                    val = parseDateMS(val);
                }
                /* int 转换 */
                else if (Integer.class.equals(ptypes[0]) || int.class.equals(ptypes[0])) {
                    val = parseInteger(val);
                }
                /* Date 转换 */
                else if (Date.class.equals(ptypes[0])) {
                    val = parseDate(val);
                }
                /* String 转换 */
                else if (String.class.equals(ptypes[0])) {
                    val = val == null ? (String) null : val.toString();
                }
                /* FieldOption 转换 */
                else if (val != null && FieldOption.class.isAssignableFrom(ptypes[0])) {
                    Object newVal = ptypes[0].getConstructor().newInstance();
                    ((FieldOption)newVal).setOptionValue(val.toString());
                    val = newVal;
                }
                /* FieldOption 数组与常用集合转换 */
                else if (val != null && String[].class.equals(val.getClass())) {
                    Class<?> parameterizedType;
                    /* Array */
                    if ((parameterizedType = ptypes[0].getComponentType()) != null
                            && FieldOption.class.isAssignableFrom(parameterizedType)) {
                        String[] vals = (String[]) val;
                        Object newVal = Array.newInstance(parameterizedType, vals.length);
                        if (vals.length > 0) {
                            FieldOption arritem;
                            Constructor<?> constructor = parameterizedType.getConstructor();
                            for (int i = 0; i < vals.length; i++) {
                                arritem = (FieldOption) constructor.newInstance();
                                Array.set(newVal, i, arritem);
                                arritem.setOptionValue(vals[i]);
                            }
                        }
                        val = newVal;
                    }
                    /* Collection */
                    else if (List.class.equals(ptypes[0]) || Set.class.equals(ptypes[0])
                            || Collection.class.equals(ptypes[0])) {
                        parameterizedType = (Class<?>) ((ParameterizedType)method.getGenericParameterTypes()[0])
                                .getActualTypeArguments()[0];
                        if (FieldOption.class.isAssignableFrom(parameterizedType)) {
                            String[] vals = (String[]) val;
                            @SuppressWarnings("rawtypes")
                            Collection newVal = Set.class.equals(ptypes[0]) ? new HashSet(vals.length)
                                    : new ArrayList(vals.length);
                            if (vals.length > 0) {
                                FieldOption arritem;
                                Constructor<?> constructor = parameterizedType.getConstructor();
                                for (int i = 0; i < vals.length; i++) {
                                    arritem = (FieldOption) constructor.newInstance();
                                    arritem.setOptionValue(vals[i]);
                                    newVal.add(arritem);
                                }
                            }
                            val = newVal;
                        } else if (String.class.equals(parameterizedType)) {
                            String[] vals = (String[]) val;
                            @SuppressWarnings("rawtypes")
                            Collection newVal = Set.class.equals(ptypes[0]) ? new HashSet<String>(vals.length)
                                    : new ArrayList(vals.length);
                            if (vals.length > 0) {
                                for (int i = 0; i < vals.length; i++) {
                                    newVal.add(vals[i]);
                                }
                            }
                            val = newVal;
                        }
                    }
                }
                try {
                    method.invoke(obj, val);
                } catch (Exception ex) {
                    log.error("Cann't set property {} of {} to {}", method.getName(), clazz.getName(),
                            ifNull(val, "NULL"));
                    throw ex;
                }
                data.remove(name);
            }
        }
        return obj;
    }
    
    /*
     * 解析 boolean 值: null/[empty]/0/false/no/off => false, * => true
     */
    public static boolean parseBoolean(Object bool) {
        if (bool == null) {
            return false;
        }
        if (bool instanceof Boolean || boolean.class.equals(bool)) {
            return (boolean)bool;
        }
        String s = StringUtils.trimToEmpty(bool.toString()).toLowerCase();
        if (s.equals("") || s.equals("0") || s.equals("false")
                || s.equals("no") || s.equals("off")) {
            return false;
        }
        return true;
    }
    
    /*
     * 解析 boolean 值: null => null , [empty]/0/false/no/off => false, * => true
     */
    public static Boolean parseBooleanAllownNull(Object bool) {
        if (bool == null) {
            return null;
        }
        return parseBoolean(bool);
    }
    
    /**
     * 解析 Character : null/[empty] => null, * => toString().charAt(0)
     */
    public static Character parseCharacter(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Character || char.class.equals(val.getClass())) {
            return (char)val;
        }
        if (StringUtils.isEmpty((String)(val = val.toString()))) {
            return null;
        }
        return ((String)val).charAt(0);
    }

    /* 时间转换 */
    public static Date parseDate(Object date) {
        Long result;
        if ((result = parseDateMS(date)) == null) {
            return null;
        }
        return new Date(result);
    }

    public static Long parseDateMS(Object date) {
        if (date == null) {
            return null;
        }
        if (long.class.equals(date.getClass())) {
            return (long) date;
        } else if (date instanceof Long) {
            return (Long) date;
        } else if (date instanceof java.util.Date) {
            return ((java.util.Date) date).getTime();
        } else if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).getTime();
        } else if (date instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) date).getTime();
        }
        return parseLong(date.toString());
    }

    /* 重复字串连接 */
    public static String join(CharSequence str, int length) {
        return join(str, length, ",", null, null);
    }

    public static String join(CharSequence str, int length,
            CharSequence seperator) {
        return join(str, length, seperator, null, null);
    }

    public static String join(CharSequence str, int length,
            CharSequence seperator, CharSequence open, CharSequence close) {
        if (length <= 0) {
            return "";
        }
        StringBuffer joined = new StringBuffer();
        if (open != null) {
            joined.append(open);
        }
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                joined.append(seperator);
            }
            joined.append(str);
        }
        if (close != null) {
            joined.append(close);
        }
        return joined.toString();
    }

    /**
     * 字串分割：按照给定正则对字符串进行分割，可定义一些分割后的处理操作
     * 
     * @param str
     *            要分给的字串
     * @param regex
     *            分割正则表达式
     * @param flags
     *            分割后的处理操作， 可选常量定义如下： STR_TRIMED 去掉前后空白； STR_LOWER 转小写；
     *            STR_UPPER 转大写； STR_NONBLANK 忽略空白项； STR_UNIQUE 过滤重复项；
     *            STR_PATHEND 确保已 / 结尾；
     * 
     * @return 如果 str 为 null 或 empty，返回空数组
     * */
    public static String[] split(String str, String regex, int flags) {
        str = ifNull(str, "");
        if (str.isEmpty()) {
            return new String[0];
        }
        String[] values = regex == null ? new String[] { str } : str
                .split(regex);
        if (flags == 0) {
            return values;
        }
        List<String> processed = new ArrayList<String>();
        for (String v : values) {
            if ((flags & STR_TRIMED) != 0) {
                v = v.trim();
            }
            if ((flags & STR_LOWER) != 0) {
                v = v.toLowerCase();
            } else if ((flags & STR_UPPER) != 0) {
                v = v.toUpperCase();
            }
            if ((flags & STR_PATHEND) != 0) {
                if (!v.endsWith("/")) {
                    v = String.format("%s/", v);
                }
            }
            if ((flags & STR_NONBLANK) != 0 && StringUtils.isBlank(v)) {
                continue;
            }
            if ((flags & STR_UNIQUE) != 0 && processed.contains(v)) {
                continue;
            }
            processed.add(v);
        }
        return processed.toArray(new String[processed.size()]);
    }

    public static String stringifyStackTrace(Throwable e) {
        if (e == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    public static boolean getJsBoolean(JsonObject data, String name) {
        JsonElement val = null;
        if (data == null || (val = data.get(name)) == null || val.isJsonNull()) {
            return false;
        }
        if (val.isJsonPrimitive() && ((JsonPrimitive) val).isBoolean()) {
            return ((JsonPrimitive) val).getAsBoolean();
        }
        return parseBoolean(getJstring(data, name));
    }
    
    public static String getJstring(JsonObject data, String name) {
        JsonElement val = null;
        if (data == null || (val = data.get(name)) == null || val.isJsonNull()) {
            return null;
        }
        if (!val.isJsonPrimitive() || !((JsonPrimitive) val).isString()) {
            return val.toString();
        }
        return ((JsonPrimitive) val).getAsString();
    }
    
    public static Long getJsLong(JsonObject data, String name) {
        JsonElement val = null;
        if (data == null || (val = data.get(name)) == null || val.isJsonNull()) {
            return null;
        }
        if (val.isJsonPrimitive() && ((JsonPrimitive) val).isNumber()) {
            return ((JsonPrimitive) val).getAsLong();
        }
        return parseLong(getJstring(data, name));
    }
    
    /**
     * 尝试多种字符集对字节流进行字串转换，如果转换失败返回字串"<无法转换的数据流>"。 此函数仅用于日志打印时，业务逻辑请勿使用该函数。
     * 
     * @return
     */
    public static String bytesToDisplay(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException eu) {
            try {
                return new String(bytes, "GBK");
            } catch (UnsupportedEncodingException eg) {
                try {
                    return new String(bytes, "ISO-8859-1");
                } catch (UnsupportedEncodingException ei) {
                    return "<无法转换的数据流>";
                }
            }
        }
    }
    
    public final static long toDigitalVersion(String version) {
        if ((version = StringUtils.trimToEmpty(version)).isEmpty()) {
            return 0;
        }
        List<String> splited = new ArrayList<String>(Arrays.asList(version
                .split("\\.")));
        while (splited.size() < 4) {
            splited.add("0");
        }
        long number, result = 0;
        int maxIndex = splited.size() - 1;
        for (int i = maxIndex; i >= 0; i--) {
            try {
                number = Long.valueOf(splited.get(i));
            } catch (Exception e) {
                return 0;
            }
            result += i == maxIndex ? number : (number * (long) Math.pow(10L,
                    4L * (maxIndex - i)));
        }
        return result;
    }
    
    public static String replaceSensitive(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        
        return regexpSensitive.matcher(str).replaceAll("$1$2######");
    }
}
