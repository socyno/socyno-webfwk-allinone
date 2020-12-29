package com.weimob.webfwk.state.module.chart;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateFormatUtils;

import com.weimob.webfwk.state.module.notify.SystemNotifyRecordFormCreation;
import com.weimob.webfwk.state.module.notify.SystemNotifyService;
import com.weimob.webfwk.state.module.notify.SystemNotifyRecordFormSimple.MessageType;
import com.weimob.webfwk.state.module.tenant.SystemTenantDataSource;
import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.model.ObjectMap;
import com.weimob.webfwk.util.tool.StringUtils;

public class SystemDataChartUtil {
    
    private final static Pattern SqlDateRegexp = Pattern.compile("^\\s*([+-]?)(\\d*)([YMd]?)@(Y|M|d|h|m|s)\\s*$");
    
    /**
     * 查询图表原始数据
     * @param querySql 查询语句
     * @param queryArgs 查询参数
     */
    static List<Map<String, Object>> getOriginData(String querySql, String[] queryArgs) throws Exception {
        if (StringUtils.isBlank(querySql)) {
            return Collections.emptyList();
        }
        return SystemTenantDataSource.getMain().queryAsList(querySql, queryArgs);
    }
    
    /**
     * 根据图表类型，将给定的原始数据转为图表所需的数据
     * @param chartType
     * @param chartData
     * @return
     */
    static List<Map<String, Object>> genChartData(String chartType, List<Map<String, Object>> rawData) {
//
//        if (ChartType.PIE.getCode().equalsIgnoreCase(chartType)) {
//            if (rawData == null || rawData.size() <= 0) {
//                return Collections.emptyList();
//            }
//            if (rawData.get(0).size() != 2 ||
//                    !mapList.get(0).containsKey(NAME) ||
//                    !mapList.get(0).containsKey(VALUE)) {
//                throw new MessageException("请确定查询结果只有两列,一列列名为name ,另一列列名为value");
//            }
//        }
//        
        return rawData;
    }
    
    @Getter
    @Setter
    @ToString
    public static class SqlParamsDefinition {
        
        /**
         * SQL 中的传入参数（此处以名称占位）
         */
        private String[] namedPlaceholders;
        
        /**
         * SQL 中的占位入参的默认值
         */
        private Map<String, String> namedValues;
    }
    
    /**
     * 根据SQL参数配置，生成SQL查询的参数数据
     * 
     * @param namedValues
     * @param namedPlaceholders
     * @return
     */
    static String[] parseAndGenSqlArgs(Map<String, String> namedValues, String[] namedPlaceholders) {
        if (namedPlaceholders == null || namedPlaceholders.length <= 0) {
            return new String[0];
        }
        String key, val;
        String[] sqlargs = new String[namedPlaceholders.length];
        for (int i = 0; i < namedPlaceholders.length; i++) {
            key = namedPlaceholders[i];
            if ((val = namedValues.get(key)) == null) {
                throw new MessageException("SQL 参数 %s 没有提供（默认）值");
            }
            namedValues.put(key, sqlargs[i] = parseSqlValue(val));
        }
        return sqlargs;
    }
    
    /**
     * 解析SQL参数值
     * @param value
     * @return
     */
    static String parseSqlValue(String value) {
        Matcher matcher;
        if ((matcher = SqlDateRegexp.matcher(value)) == null || !matcher.find()) {
            return value;
        }
        String oparator = matcher.group(1);
        String deltaStr = matcher.group(2);
        String deltaUnit = matcher.group(3);
        String dateUnit = matcher.group(4);
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(
                "Y".equals(deltaUnit) ? Calendar.YEAR
                        : ("M".equals(deltaUnit) ? Calendar.MONTH
                                : ("d".equals(deltaUnit) ? Calendar.DATE
                                        : ("h".equals(deltaUnit) ? Calendar.HOUR
                                                : ("m".equals(deltaUnit) ? Calendar.MINUTE : Calendar.SECOND)))),
                Integer.valueOf(oparator.concat(StringUtils.isBlank(deltaStr) ? "0" : deltaStr)));
        return DateFormatUtils.format(calendar, 
                "Y".equals(dateUnit) ? "yyyy"
                        : ("M".equals(dateUnit) ? "yyyy-MM"
                                : ("d".equals(dateUnit) ? "yyyy-MM-dd"
                                        : ("h".equals(dateUnit) ? "yyyy-MM-dd HH:00:00"
                                                : ("m".equals(dateUnit)
                                                        ? "yyyy-MM-dd HH:mm:00" : "yyyy-MM-dd HH:mm:ss")))));
    }
    
    /**
     * 根据邮件模板和数据，生成邮件内容
     * @param mailTemplate
     * @param originData
     * @return
     * @throws Exception 
     */
    static String genMailContent(String mailTemplate, List<Map<String, Object>> originData) throws Exception {
        if (StringUtils.isBlank(mailTemplate)) {
            return null;
        }
        Map<String, SystemNotifyRecordFormCreation> notifyContents = SystemNotifyService.getInstance().sendSync(
                mailTemplate,
                new ObjectMap().put("data", originData).asMap(),
                SystemNotifyService.NOEXCEPTION_TMPL_NOTFOUD | SystemNotifyService.NOTIFY_DATA_RETURN_ONLY);
        SystemNotifyRecordFormCreation mailContent;
        if (notifyContents == null || (mailContent = notifyContents.get(MessageType.Email.getValue())) == null) {
            throw new MessageException(String.format("邮件模板(%s)不存在", mailTemplate));
        }
        return mailContent.getContent();
    }
    
    static void sendMailContent(String mailTemplate, List<Map<String, Object>> originData) throws Exception {
        if (StringUtils.isBlank(mailTemplate)) {
            return;
        }
        SystemNotifyService.getInstance().sendSync(mailTemplate, new ObjectMap().put("data", originData).asMap(), 0);
    }
}
