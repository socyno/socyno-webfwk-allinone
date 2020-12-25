package com.weimob.webfwk.util.tool;

import lombok.NonNull;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

import com.weimob.webfwk.util.exception.MessageException;

public class StringUtils extends org.apache.commons.lang3.StringUtils {
    
    public static int stringBufferAppend(StringBuffer bf, byte[] bytes) {
        return stringBufferAppend(bf, bytes, null, 0, bytes.length);
    }
    
    public static int stringBufferAppend(StringBuffer bf, byte[] bytes, CharsetDecoder decoder) {
        return stringBufferAppend(bf, bytes, decoder, 0, bytes.length);
    }
    
    public static int stringBufferAppend(StringBuffer bf, byte[] bytes, int offset, int length) {
        return stringBufferAppend(bf, bytes, null, offset, length);
    }
    
    public static int stringBufferAppend(StringBuffer bf, byte[] bytes, CharsetDecoder decoder, int offset,
            int length) {
        int left = 0;
        if (decoder == null) {
            decoder = Charset.defaultCharset().newDecoder();
        }
        while (left < length) {
            try {
                bf.append(decoder.decode(ByteBuffer.wrap(bytes, offset, length - left)).toString());
                break;
            } catch (CharacterCodingException e) {
                left++;
                continue;
            }
        }
        return left;
    }
    
    /**
     * 仅当 buffer 内的没有数据时追加该数据，否则将被丢弃掉。
     */
    public static StringBuilder appendOnlyEmpty(@NonNull StringBuilder buffer, Object data) {
        return append(buffer, data, APPEND_ONLY_EMPTY);
    }
    
    /**
     * 仅当 buffer 内的有数据时追加该数据，否则将被丢弃掉。
     */
    public static StringBuilder appendIfNotEmpty(@NonNull StringBuilder buffer, Object data) {
        return append(buffer, data, APPEND_IFNOT_EMPTY);
    }
    
    /**
     * 无论 buffer 是否为空，均进行追加(buffer 的默认行为)
     */
    public static StringBuilder append(@NonNull StringBuilder buffer, Object data) {
        return append(buffer, data, APPEND_ALLWAYS);
    }
    
    /**
     * 仅当 buffer 内的有数据时在头部插入该数据，否则将被丢弃掉。
     */
    public static StringBuilder prependIfNotEmpty(@NonNull StringBuilder buffer, Object data) {
        return append(buffer, data, PREPEND_IFNOT_EMPTY);
    }
    
    private final static int APPEND_ALLWAYS = 0;
    
    private final static int APPEND_ONLY_EMPTY = 1;
    
    private final static int APPEND_IFNOT_EMPTY = 2;
    
    private final static int PREPEND_IFNOT_EMPTY = 4;
    
    private static StringBuilder append(@NonNull StringBuilder buffer, Object data, int appendPolicy) {
        
        if (data == null) {
            return buffer;
        }
        boolean prepend = false;
        int length = buffer.length();
        if ((appendPolicy & APPEND_ONLY_EMPTY) != 0 && length > 0) {
            return buffer;
        }
        
        if ((appendPolicy & APPEND_IFNOT_EMPTY) != 0 && length <= 0) {
            return buffer;
        }
        
        if ((prepend = (appendPolicy & PREPEND_IFNOT_EMPTY) != 0) && length <= 0) {
            return buffer;
        }
        
        Class<? extends Object> clazz = data.getClass();
        if (Boolean.class.equals(clazz)) {
            return prepend ? buffer.insert(0, ((Boolean) data).booleanValue())
                    : buffer.append(((Boolean) data).booleanValue());
        }
        if (Integer.class.equals(clazz)) {
            return prepend ? buffer.insert(0, ((Integer) data).intValue()) 
                    : buffer.append(((Integer) data).intValue());
        }
        if (Character.class.equals(clazz)) {
            return prepend ? buffer.insert(0, ((Character) data).charValue())
                    : buffer.append(((Character) data).charValue());
        }
        if (Double.class.equals(clazz)) {
            return prepend ? buffer.insert(0, ((Double) data).doubleValue())
                    : buffer.append(((Double) data).doubleValue());
        }
        if (Float.class.equals(clazz)) {
            return prepend ? buffer.insert(0, ((Float) data).floatValue()) 
                    : buffer.append(((Float) data).floatValue());
        }
        if (char[].class.equals(clazz)) {
            return prepend ? buffer.insert(0, (char[]) data) 
                    : buffer.append((char[]) data);
        }
        if (CharSequence.class.isAssignableFrom(clazz)) {
            return prepend ? buffer.insert(0, (CharSequence) data) 
                    : buffer.append((CharSequence) data);
        }
        return prepend ? buffer.insert(0, data) : buffer.append(data);
    }
    
    /**
     * 检查字串是否包含在自定的数组中，忽略大小写
     */
    public static boolean containsIgnoreCase(String[] names, String name) {
        if (names == null || names.length <= 0) {
            return false;
        }
        for (String n : names) {
            if (StringUtils.equalsIgnoreCase(n, name)) {
                return true;
            }
        }
        return false;
    }
    
    public final static long version2Number(String version) {
        if ((version = StringUtils.trimToEmpty(version)).isEmpty()) {
            return 0;
        }
        List<String> splited = new ArrayList<String>(Arrays.asList(version.split("\\.")));
        
        return version2Number(splited);
    }
    
    public final static long normalVersion2Number(String version) {
        if ((version = StringUtils.trimToEmpty(version)).isEmpty()) {
            return 0;
        }
        List<String> splited = new ArrayList<String>(Arrays.asList(version.split("\\.")));
        
        while (splited.size() < 4) {
            splited.add("0");
        }
        
        return version2Number(splited);
    }
    
    public final static long version2Number(List<String> splited) {
        long number, result = 0;
        int maxIndex = splited.size() - 1;
        for (int i = maxIndex; i >= 0; i--) {
            try {
                number = Long.valueOf(splited.get(i));
            } catch (Exception e) {
                return 0;
            }
            result += i == maxIndex ? number : (number * (long) Math.pow(10L, 4L * (maxIndex - i)));
        }
        return result;
    }
    
    public static Date parseDate(String str) {
        return parseDate(str, null);
    }
    
    public static Date parseDate(String str, String format) {
        if (isBlank(str)) {
            return null;
        }
        if (isNotBlank(format)) {
            try {
                return new SimpleDateFormat(format).parse(str);
            } catch (Exception e) {
            }
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ").parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZ").parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm:ssZZ").parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ").parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm:ssZ").parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z").parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.US).parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US).parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a", Locale.US).parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US).parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.US).parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.US).parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(str);
        } catch (Exception e) {
        }
        try {
            return new SimpleDateFormat("yyyy/MM/dd").parse(str);
        } catch (Exception e) {
        }
        throw new MessageException(String.format("Unknown date string : %s", str));
    }
    
    /**
     * 以驼峰形式转换字串中的下划线
     */
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
}
