package org.socyno.webfwk.state.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.model.CommonSimpleLog;
import org.socyno.webfwk.state.model.CommonSimpleLogDetail;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

@Slf4j
public class SimpleLogService {
    
    private static AbstractDao getDao() {
        return SystemTenantDataSource.getMain();
    }
	
	public static boolean createLog(
             String operateType,
	         String objectType,
	         Object objectId,
	         String operateDesc,
	         Object operateBefore,
	         Object operateAfter) throws Exception {
            operateAfter = contentStringfy(operateAfter);
            operateBefore = contentStringfy(operateBefore);
            operateDesc = StringUtils.trimToEmpty(operateDesc);
            AtomicLong changeId = new AtomicLong(0);
            if (StringUtils.isNotBlank((String)operateBefore) || StringUtils.isNotBlank((String)operateAfter)) {
                try {
                    getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                        "system_common_log_detail", new ObjectMap()
                                .put("operate_after", operateAfter)
                                .put("operate_before", operateBefore)
                    ), new ResultSetProcessor() {
                        @Override
                        public void process(ResultSet r, Connection c) throws Exception {
                            r.next();
                            changeId.set(r.getLong(1));
                        }
                    });
                } catch (Exception e) {
                    log.error(e.toString(), e);
                }
            }
            getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                "system_common_log", new ObjectMap()
                        .put("object_type", objectType)
                        .put("object_id", objectId)
                        .put("operate_user_id", SessionContext.getUserId())
                        .put("operate_user_name", SessionContext.getUsername())
                        .put("operate_user_display", SessionContext.getDisplay())
                        .put("operate_type", StringUtils.trimToEmpty(operateType))
                        .put("operate_desc", operateDesc.substring(0, CommonUtil.parseMinimalInteger(operateDesc.length(), 1024)))
                        .put("operate_detail_id", changeId.get())
                        .put("operate_proxy_name", SessionContext.getProxyUsername())
                        .put("operate_proxy_display", SessionContext.getProxyDisplay())
            ));
            return true;
	}
    
    public static boolean createLog(String operateType, String objectType, Object objectId) throws Exception {
        return createLog(operateType, objectType, objectId, null, null, null);
    }
    
    public static boolean createLog(String operateType, String objectType, Object objectId, String operateDesc) throws Exception {
        return createLog(operateType, objectType, objectId, operateDesc, null, null);
    }
    
    /**
     * SELECT
     *     l.*
     * FROM
     *     system_common_log l
     * WHERE
     *     %s
     *     l.object_type = ?
     * AND
     *     l.object_id = ?
     *     %s
     * ORDER BY
     *     l.id %s
     * LIMIT %d;
     */
    @Multiline
    private final static String SQL_QUERY_OPERATION_LOG_BY_OBJECT_TYPE_AND_ID = "X";
    

    public static List<CommonSimpleLog> queryLog(String objectType, Object objectId) throws Exception {
        return queryLog(objectType, objectId, null);
    }
    
    public static List<CommonSimpleLog> queryLog(String objectType, Object objectId, Long lessThen) throws Exception {
        return queryLog(objectType, objectId, null, null, lessThen);
    }
    
    public static List<CommonSimpleLog> queryLogIncludeOperationTypes(String objectType, Object objectId, String[] includedOperationTypes) throws Exception {
        return queryLogIncludeOperationTypes(objectType, objectId, includedOperationTypes, null);
    }
    
    public static List<CommonSimpleLog> queryLogIncludeOperationTypes(String objectType, Object objectId, String[] includedOperationTypes, Long lessThen) throws Exception {
        return queryLog(objectType, objectId, null, includedOperationTypes, lessThen);
    }
    
    public static List<CommonSimpleLog> queryLogExcludeOperationTypes(String objectType, Object objectId, String[] excludedOperationTypes) throws Exception {
        return queryLogExcludeOperationTypes(objectType, objectId, excludedOperationTypes, null);
    }
    
    public static List<CommonSimpleLog> queryLogExcludeOperationTypes(String objectType, Object objectId, String[] excludedOperationTypes, Long lessThen) throws Exception {
        return queryLog(objectType, objectId, excludedOperationTypes, null, lessThen);
    }
    
    public static List<CommonSimpleLog> queryLog(String objectType, Object objectId, String[] excludedOperationTypes, String[] includedOperationTypes) throws Exception {
        return queryLog(objectType, objectId, excludedOperationTypes, includedOperationTypes, null);
    }
    
    public static List<CommonSimpleLog> queryLog(String objectType, Object objectId, String[] excludedOperationTypes, String[] includedOperationTypes, Long lessThen) throws Exception {
        if (StringUtils.isBlank(objectType) || objectId == null || StringUtils.isBlank(objectId.toString())) {
            return Collections.emptyList();
        }
        List<Object> sqlArgs = new ArrayList<>();
        sqlArgs.add(objectType);
        sqlArgs.add(objectId);
        String typeSql = "";
        Set<String> typeSet = new HashSet<>();
        if (excludedOperationTypes != null && excludedOperationTypes.length > 0) {
            typeSet.clear();
            typeSet.addAll(Arrays.asList(excludedOperationTypes));
            sqlArgs.addAll(typeSet);
            typeSql = String.format(" AND operate_type NOT IN (%s)", CommonUtil.join("?", typeSet.size(), ","));
        }
        if (includedOperationTypes != null && includedOperationTypes.length > 0) {
            typeSet.clear();
            typeSet.addAll(Arrays.asList(includedOperationTypes));
            sqlArgs.addAll(typeSet);
            typeSql = String.format(" AND operate_type IN (%s)", CommonUtil.join("?", typeSet.size(), ","));
        }
        String limited = "";
        String orderFlag = "DESC";
        if (lessThen != null && lessThen != 0) {
            if (lessThen > 0) {
                limited = String.format(" l.id < %s AND ", lessThen);
            } else {
                orderFlag = "ASC";
                limited = String.format(" l.id > %s AND ", 0L - lessThen);
            }
        }
        return getDao().queryAsList(CommonSimpleLog.class, String.format(
                SQL_QUERY_OPERATION_LOG_BY_OBJECT_TYPE_AND_ID,
                limited, typeSql, orderFlag, 50), sqlArgs.toArray()).stream().sorted(new Comparator<CommonSimpleLog>() {
                    @Override
                    public int compare(CommonSimpleLog l, CommonSimpleLog r) {
                        return Long.compare(r.getId(), l.getId());
                    }
                }).collect(Collectors.toList());
    }
    
    /**
     * 获取指定记录的首条操作日志
     */
    public static CommonSimpleLog getFirstLog(String objectType, Object objectId) throws Exception {
        if (StringUtils.isBlank(objectType) || objectId == null || StringUtils.isBlank(objectId.toString())) {
            return null;
        }
        return getDao().queryAsObject(CommonSimpleLog.class,
                String.format(SQL_QUERY_OPERATION_LOG_BY_OBJECT_TYPE_AND_ID, "", "", "ASC", 1),
                new Object[] { objectType, objectId });
    }
    
    /**
     * 获取指定记录的最新操作日志
     */
    public static CommonSimpleLog getLatestLog(String objectType, Object objectId) throws Exception {
        if (StringUtils.isBlank(objectType) || objectId == null || StringUtils.isBlank(objectId.toString())) {
            return null;
        }
        return getDao().queryAsObject(CommonSimpleLog.class,
                String.format(SQL_QUERY_OPERATION_LOG_BY_OBJECT_TYPE_AND_ID, "", "", "DESC", 1),
                new Object[] { objectType, objectId });
    }
    
    /**
     * SELECT * FROM system_common_log_detail WHERE id = ?
     */
    @Multiline
    private final static String SQL_QUERY_OPERATION_INFO_LOG_BY_OPERATION_ID = "X";

    public static CommonSimpleLogDetail getLogDetail(Long detailId) throws Exception {
        if (detailId == null) {
            return null;
        }
        return getDao().queryAsObject(CommonSimpleLogDetail.class, SQL_QUERY_OPERATION_INFO_LOG_BY_OPERATION_ID,
                new Object[] { detailId });
    }
    
    private static String contentStringfy(Object object) throws Exception {
        if (object == null) {
            return null;
        }
        if (object instanceof CharSequence || object instanceof Number) {
            return object.toString();
        }
        return CommonUtil.toPrettyJson(object, true);
    }
}
