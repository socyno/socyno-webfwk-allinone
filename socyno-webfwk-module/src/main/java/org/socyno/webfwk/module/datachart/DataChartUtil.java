package org.socyno.webfwk.module.datachart;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.tool.StringUtils;

public class DataChartUtil {

    private static final String NAME = "name";
    private static final String VALUE = "value";

    public static List<Map<String, Object>> getPieChartData(String querySql, String[] replaceParams) {

        if (StringUtils.isBlank(querySql)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> mapList;
        try {
            mapList = getFormBaseDao().queryAsList(querySql, replaceParams);
            if (mapList == null || mapList.size() <= 0) {
                return Collections.emptyList();
            }
            if (mapList.get(0).size() != 2 ||
                    !mapList.get(0).containsKey(NAME) ||
                    !mapList.get(0).containsKey(VALUE)) {
                throw new MessageException("请确定查询结果只有两列,一列列名为name ,另一列列名为value");
            }
        } catch (Exception e) {
            throw new MessageException(String.format("请检查SQL是否正确: %s", e.getMessage()));
        }
        String rule = "[0-9]+.?[0-9]+";
        for (Map<String, Object> objectMap : mapList) {
            if (!Pattern.matches(rule, objectMap.get(VALUE).toString())) {
                throw new MessageException("请确保value列全为数字(含小数,不含负数)");
            }
        }
        return mapList;
    }

    public static List<Map<String, Object>> getOriginData(String querySql, String[] sqlParams) throws Exception{

        if (StringUtils.isBlank(querySql)) {
            return Collections.emptyList();
        }
        return getFormBaseDao().queryAsList(querySql, sqlParams);
    }
    
    protected static AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }

    @Getter
    @Setter
    @ToString
    public static class SqlMapParam {

        private Map<String, String> sqlMap;

        private String[] sqlParams;

    }

    /**
     * @param sqlMap    sql参数key-value(未解析)   {"key1":"111","key2":"222"}
     * @param sqlParams 参数数组,可重复的map的key ["key1","key2","key1"]
     * @return 替换后的sql参数数组,同时sqlMap 将会替换为 key-value(解析过)
     */
    public static String[] replaceParams(Map<String, String> sqlMap, String[] sqlParams) {

        if (sqlParams == null || sqlParams.length <= 0
                || sqlMap == null || sqlMap.size() <= 0) {
            return new String[]{};
        }
        String[] sqlArr=new String[sqlParams.length];
        for (int i = 0; i < sqlParams.length; i++) {
            sqlArr[i] = parseDate(sqlMap.get(sqlParams[i]));
            sqlMap.put(sqlParams[i], sqlArr[i]);
        }
        return sqlArr;
    }


    private static String parseDate(String dateStr) {

        String onlyReg = "^\\s*(@Y|@M|@d)\\s*$";
        if (Pattern.matches(onlyReg, dateStr)) {
            switch (dateStr) {
                case "@Y":
                    return DateFormatUtils.format(new Date(), "yyyy");
                case "@M":
                    return DateFormatUtils.format(new Date(), "yyyy-MM");
                case "@d":
                    return DateFormatUtils.format(new Date(), "yyyy-MM-dd");
                default:
                    break;
            }
        }
        
        String reg = "^\\s*([+-]?)(\\d*)([YMd]?)(@Y|@M|@d)?\\s*";
        Pattern p = Pattern.compile(reg);
        Matcher matcher = p.matcher(dateStr);
        if (matcher.find()) {
            Calendar instance = Calendar.getInstance();
            Integer num = null;
            if (StringUtils.isBlank(matcher.group(2))) {
                num = Integer.valueOf(matcher.group(1) + "0");
            } else {
                num = Integer.valueOf(matcher.group(1) + matcher.group(2));
            }
            if(StringUtils.isNotBlank(matcher.group(3))) {
                String yy = matcher.group(3);
                switch (yy) {
                    case "Y":
                        instance.add(Calendar.YEAR, num);
                        break;
                    case "M":
                        instance.add(Calendar.MONTH, num);
                        break;
                    case "d":
                        instance.add(Calendar.DAY_OF_MONTH, num);
                        break;
                    default:
                        break;
                }
                if (StringUtils.isNotBlank(matcher.group(4))) {
                    String detail = matcher.group(4);
                    switch (detail) {
                        case "@Y":
                            return DateFormatUtils.format(instance.getTime(), "yyyy");
                        case "@M":
                            return DateFormatUtils.format(instance.getTime(), "yyyy-MM");
                        case "@d":
                            return DateFormatUtils.format(instance.getTime(), "yyyy-MM-dd");
                        default:
                            break;
                    }
                }
                return DateFormatUtils.format(instance.getTime(), "yyyy-MM-dd");
            }
            return dateStr;
        }
        return dateStr;
    }
    
}
