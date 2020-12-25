package com.weimob.webfwk.util.tool;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ConvertUtil<T> {
    
    /**
     * 将对象数组强制转换为 Long[]。非数字将被丢弃，结果将被去重。
     * @param values
     * @return
     */
    public static Long[] asNonNullUniqueLongArray(Object ...values) {
        if (values == null || values.length < 0) {
            return new Long[0];
        }
        Long id;
        Set<Long> result = new HashSet<>();
        for (Object value : values) {
            if ((id = CommonUtil.parseLong(value)) == null) {
               continue;
            }
            result.add(id);
        }
        return result.toArray(new Long[0]);
    }
    
    public static Long[] asNonNullUniqueLongArray(Long ...values) {
        return asNonNullUniqueLongArray((Object[]) values);
    }
    
    /**
     * 将对象数组强制转换为 long[]。非数字将被丢弃，结果将被去重。
     */
    public static long[] asNonNullUniquePrimitiveLongArray(Object ...values){
        Long[] longArray = asNonNullUniqueLongArray(values);
        long[] result = new long[values.length];
        for (int i = 0; i < longArray.length; i++) {
            result[i] = longArray[i].longValue() ;
        }
        return result ;
    }
    
    /**
     * 将对象数组强制转换为 long[]。非数字将被丢弃，结果将被去重。
     */
    public static long[] asNonNullUniquePrimitiveLongArray(Collection<?> values) {
        if (values == null || values.size() < 0) {
            return new long[0];
        }
        return asNonNullUniquePrimitiveLongArray(values.toArray());
    }
    
    
    /**
     * 将对象数组强制转换为 long[]。非数字将被丢弃，结果将被去重。
     */
    public static long[] asNonNullUniquePrimitiveLongArray(Long ...values) {
        return asNonNullUniquePrimitiveLongArray((Object[]) values);
    }
    
    /**
     * 将对象数组强制转换为 String[]。空白字串被丢弃，结果将去除首尾的空白且去重。
     */
    public static String[] asNonBlankUniqueTrimedStringArray(Object ...values) {
        if (values == null || values.length < 0) {
            return new String[0];
        }
        String str;
        Set<String> result = new HashSet<>();
        for (Object value : values) {
            if (value == null || StringUtils.isBlank(str = value.toString().trim())) {
               continue;
            }
            result.add(str);
        }
        return result.toArray(new String[0]);
    }
    
    /**
     * 将对象数组强制转换为 String[]。空白字串被丢弃，结果将去除首尾的空白且去重。
     */
    public static String[] asNonBlankUniqueTrimedStringArray(Collection<?> values) {
        if (values == null || values.size() < 0) {
            return new String[0];
        }
        return asNonBlankUniqueTrimedStringArray(values.toArray());
    }
}
