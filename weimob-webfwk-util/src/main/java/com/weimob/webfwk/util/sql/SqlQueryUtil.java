package com.weimob.webfwk.util.sql;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.model.ObjectMap0;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.NonNull;

public class SqlQueryUtil {

    private final static Pattern REGEXP_SQL_NAME =
            Pattern.compile("^\\`?[A-z][0-9A-z\\.\\_\\-\\`]*[0-9A-z]\\`?$");

    private final static Pattern REGEXP_SQL_OPERATOR =
            Pattern.compile("^((?:[\\<\\>]?\\=)|(?:[\\<\\>])|(?:%\\+)|(?:\\+?%))");
    
    private enum SQLTYPE { INSERT, UPDATE, DELETE; }
    
    public static void checkSQLName(String name) {
        checkSQLName(name, false);
    }
    
    public static boolean checkSQLName(String name, boolean noException) {
        if ( name == null || !REGEXP_SQL_NAME.matcher(name).find() ) {
            if ( noException ) {
                return false;
            }
            throw new MessageException( String.format(
            		"无效的数据库表或列名称(%s)", name) );
        }
        return true;
    }
    
    public static String quoteName(String name) {
    	/* TODO: 后续需增加  table.field 样式以及非 mysql 支持 */
    	checkSQLName(name);
    	if (name.indexOf(".") < 1 && !name.startsWith("`")) {
    		name = String.format("`%s`", name);
    	}
    	return name;
    }
    
    /**
     * 根据  Map 拼装 INSERT 语句
     * @param table   表名称
     * @param kvPairs 键值对, 其中第一项的 keys 将被视为数据库字段信息
     * @return
     */
    public static AbstractSqlStatement pairs2InsertQuery(String table, @NonNull List<? extends ObjectMap0<?>> kvPairs) {
        return pairs2InsertQuery(table, kvPairs, null);
    }
    
    /**
     * 根据  Map 拼装 INSERT 语句
     * @param table    表名称
     * @param kvPairs  键值对列表, 其中第一项的 keys 将被视为数据库字段信息
     * @param dupPairs 重复时需要更新的字段数据<pre>
     *    以 # 开头的字段的值直接拼装到 SQL 中，
     *    以 ? 开头的字段的值将被视为从对应的之中取（即 VALUES(<FIELD>)）
     *    以 = 或其它开头的字段的值将被视为参数传入（自动转义）             
     * </pre>
     * @return
     */
    public static AbstractSqlStatement pairs2InsertQuery(String table, @NonNull List<? extends ObjectMap0<?>> kvPairs,
            ObjectMap0<?> dupPairs) {
        String[] keys = null;
        List<List<Object>> values = new ArrayList<List<Object>>();
        for (ObjectMap0<?> ks : kvPairs) {
            Map<String, Object> d = ks.asMap();
            if (keys == null) {
                keys = d.keySet().toArray(new String[0]);
            }
            List<Object> singlvs = new ArrayList<Object>();
            for (int i = 0; i < keys.length; i++) {
                singlvs.add(d.get(keys[i]));
            }
            values.add(singlvs);
        }
        return pairs2InsertQuery(table, Arrays.asList(keys), values, (dupPairs == null) ? null : dupPairs.asMap());
    }
    
    private static AbstractSqlStatement pairs2InsertQuery(final String table, final List<String> keys,
            final List<List<Object>> values, final Map<String, Object> dupPairs) {
        StringBuffer sql = new StringBuffer("INSERT INTO ").append(quoteName(table)).append(" (");
        List<Object> sqlValues = new ArrayList<Object>();
        for (String key : keys) {
            sql.append(" ").append(quoteName(key)).append(",");
        }
        sql.setLength(sql.length() - 1);
        sql.append(") VALUES ");
        
        for (List<Object> vs : values) {
            sql.append("(");
            for (int i = 0; i < keys.size(); i++) {
                sql.append("?,");
                sqlValues.add(vs.get(i));
            }
            sql.setLength(sql.length() - 1);
            sql.append("),");
        }
        sql.setLength(sql.length() - 1);
        if (dupPairs != null && !dupPairs.isEmpty()) {
            sql.append(" ON DUPLICATE KEY UPDATE ");
            for (Entry<String, Object> e : dupPairs.entrySet()) {
                boolean valueAsPlain = false;
                boolean valueAsField = false;
                String key = e.getKey();
                if ((valueAsPlain = StringUtils.startsWith(key, "#"))) {
                    key = key.substring(1);
                } else if ((valueAsField = StringUtils.startsWith(key, "?"))) {
                    key = key.substring(1);
                } else if (StringUtils.startsWith(key, "=")) {
                    key = key.substring(1);
                }
                sql.append(" ").append(quoteName(key)).append("=");
                if (valueAsPlain) {
                    sql.append(e.getValue());
                } else if (valueAsField) {
                    sql.append("VALUES(").append(quoteName(e.getValue().toString())).append(")");
                } else {
                    sql.append("?");
                    sqlValues.add(e.getValue());
                }
                sql.append(",");
            }
            sql.setLength(sql.length() - 1);
        }
        return new BasicSqlStatement().setSql(sql.toString()).setValues(sqlValues.toArray());
    }
    
    /**
     * 根据  Map 拼装 INSERT 语句
     * @param table     表名称
     * @param kvPairs 键值对, 如有 key 以 = 或 # 开头, 则将使用  ON DUPLICATE KEY UPDATE 语法.
     *       其中 key 以#开头的值直接拼装到 SQL 语句中, 而以=开头的值被视为参数使用
     * @return
     */
    public static AbstractSqlStatement pairs2InsertQuery(String table, @NonNull ObjectMap0<?> kvPairs) {
        return pairs2InsertQuery(table, kvPairs.asMap());
    }
    
    /**
     * 根据  Map 拼装 INSERT 语句
     * @param table     表名称
     * @param kvPairs 键值对, 如有 key 以 = 或 # 开头, 则将使用  ON DUPLICATE KEY UPDATE 语法.
     *       其中 key 以#开头的值直接拼装到 SQL 语句中, 而以=开头的值被视为参数使用
     * @return
     */
    public static AbstractSqlStatement pairs2InsertQuery(String table, @NonNull Map<String, Object> kvPairs) {
        List<String> keys = new ArrayList<String>();
        List<Object> values = new ArrayList<Object>();
        Map<String, Object> dupPairs = new HashMap<String, Object>();
        for (Map.Entry<String, Object> e : kvPairs.entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();
            if (StringUtils.startsWith(key, "=")) {
                dupPairs.put(key, val);
                key = key.substring(1);
            } else if (StringUtils.startsWith(key, "#")) {
                dupPairs.put(key, val);
                continue;
            }
            keys.add(key);
            values.add(val);
        }
        ArrayList<List<Object>> valuesx = new ArrayList<List<Object>>();
        valuesx.add(values);
        return pairs2InsertQuery(table, keys, valuesx, dupPairs);
    }
    
    /**
     * 根据  Map 拼装 UPDATE 语句
     * @param table     表名称
     * @param kvPairs 键值对, key 以#开头的值直接拼装到  SQL 语句中,其余被视为参数使用.
     *      如 key以操作符开头,则将被视为条件,拼装到 WHERE 语句中, 支持的操作符如下: <pre>
     *      =   等于
     *      >   大于 
     *      >=  大于等于
     *      <   小于
     *      <=  小于等于
     *      %+  前置模糊
     *      +%  后置模糊
     *      %   模糊查询
     *   另外, 在任何操作符前可以加上 ! 标识取非(即 NOT(...))
     *   如 "=field01" 则在 WHERE 条件中生成  filed01=?
     *   如 "!=field01" 则在 WHERE 条件中生成  NOT (filed01=?)
     *   如 "#>field01" 则在 WHERE 条件中生成 field01>$value$
     *   </pre>
     * @param condAsOr 默认拼装的条件使用  AND 连接, 如果该参数为 true, 则使用 OR 连接
     * @return
     */
    public static AbstractSqlStatement pairs2UpdateQuery(String table, @NonNull ObjectMap0<?> kvPairs) {
        return pairs2UpdateQuery(table, kvPairs.asMap(), false);
    }
    
    /**
     * 根据  Map 拼装 UPDATE 语句
     * @param table     表名称
     * @param kvPairs 键值对, key 以#开头的值直接拼装到  SQL 语句中,其余被视为参数使用.
     *      如 key以操作符开头,则将被视为条件,拼装到 WHERE 语句中, 支持的操作符如下: <pre>
     *      =   等于
     *      >   大于 
     *      >=  大于等于
     *      <   小于
     *      <=  小于等于
     *      %+  前置模糊
     *      +%  后置模糊
     *      %   模糊查询
     *   另外, 在任何操作符前可以加上 ! 标识取非(即 NOT(...))
     *   如 "=field01" 则在 WHERE 条件中生成  filed01=?
     *   如 "!=field01" 则在 WHERE 条件中生成  NOT (filed01=?)
     *   如 "#>field01" 则在 WHERE 条件中生成 field01>$value$
     *   </pre>
     * @param condAsOr 默认拼装的条件使用  AND 连接, 如果该参数为 true, 则使用 OR 连接
     * @return
     */
    public static AbstractSqlStatement pairs2UpdateQuery(String table, @NonNull ObjectMap0<?> kvPairs, boolean orConditions) {
        return pairs2UpdateQuery(table, kvPairs.asMap(), orConditions);
    }
    
    /**
     * 根据  Map 拼装 UPDATE 语句
     * @param table     表名称
     * @param kvPairs 键值对, key 以#开头的值直接拼装到  SQL 语句中,其余被视为参数使用.
     *      如 key以操作符开头,则将被视为条件,拼装到 WHERE 语句中, 支持的操作符如下: <pre>
     *      =   等于
     *      >   大于 
     *      >=  大于等于
     *      <   小于
     *      <=  小于等于
     *      %+  前置模糊
     *      +%  后置模糊
     *      %   模糊查询
     *   另外, 在任何操作符前可以加上 ! 标识取非(即 NOT(...))
     *   如 "=field01" 则在 WHERE 条件中生成  filed01=?
     *   如 "!=field01" 则在 WHERE 条件中生成  NOT (filed01=?)
     *   如 "#>field01" 则在 WHERE 条件中生成 field01>$value$
     *   </pre>
     * @param condAsOr 默认拼装的条件使用  AND 连接, 如果该参数为 true, 则使用 OR 连接
     * @return
     */
    public static AbstractSqlStatement pairs2UpdateQuery(String table, Map<String, Object> kvPairs ) {
        return pairs2UpdateQuery(table, kvPairs, false);
    }
    
    /**
     * 根据  Map 拼装 UPDATE 语句
     * @param table     表名称
     * @param kvPairs 键值对, key 以#开头的值直接拼装到  SQL 语句中,其余被视为参数使用.
     *      如 key以操作符开头,则将被视为条件,拼装到 WHERE 语句中, 支持的操作符如下: <pre>
     *      =   等于
     *      >   大于 
     *      >=  大于等于
     *      <   小于
     *      <=  小于等于
     *      %+  前置模糊
     *      +%  后置模糊
     *      %   模糊查询
     *   另外, 在任何操作符前可以加上 ! 标识取非(即 NOT(...))
     *   如 "=field01" 则在 WHERE 条件中生成  filed01=?
     *   如 "!=field01" 则在 WHERE 条件中生成  NOT (filed01=?)
     *   如 "#>field01" 则在 WHERE 条件中生成 field01>$value$
     *   </pre>
     * @param condAsOr 默认拼装的条件使用  AND 连接, 如果该参数为 true, 则使用 OR 连接
     * @return
     */
    public static AbstractSqlStatement pairs2UpdateQuery(String table, Map<String, Object> kvPairs, boolean condAsOr) {
        return prepareQueryWithCondition(SQLTYPE.UPDATE, table, kvPairs, condAsOr);
    }
    
    /**
     * 根据  Map 拼装  DELETE 语句
     * @param table     表名称
     * @param kvPairs 键值对, key 以#开头的值直接拼装到  SQL 语句中,其余被视为参数使用.
     *      如 key 以操作符开头, 则将被视为条件,拼装到 WHERE 语句中, 支持的操作符如下: <pre>
     *      =   等于
     *      >   大于
     *      >=  大于等于
     *      <   小于
     *      <=  小于等于
     *      %+  前置模糊
     *      +%  后置模糊
     *      %   模糊查询
     *   另外, 在任何操作符前可以加上 ! 标识取非(即 NOT(...))
     *   如 "=field01" 则在 WHERE 条件中生成  filed01=?
     *   如 "!=field01" 则在 WHERE 条件中生成  NOT (filed01=?)
     *   如 "#>field01" 则在 WHERE 条件中生成 field01>$value$
     *   </pre>
     * @param orConditions 默认拼装的条件使用  AND 连接, 如果该参数为 true, 则使用 OR 连接
     * @return
     */
    public static AbstractSqlStatement prepareDeleteQuery(String table, ObjectMap0<?> kvPairs) {
        return prepareQueryWithCondition(SQLTYPE.DELETE, table, kvPairs.asMap(), false);
    }

    /**
     * 根据  Map 拼装  DELETE 语句
     * @param table     表名称
     * @param kvPairs 键值对, key 以#开头的值直接拼装到  SQL 语句中,其余被视为参数使用.
     *      如 key 以操作符开头, 则将被视为条件,拼装到 WHERE 语句中, 支持的操作符如下: <pre>
     *      =   等于
     *      >   大于
     *      >=  大于等于
     *      <   小于
     *      <=  小于等于
     *      %+  前置模糊
     *      +%  后置模糊
     *      %   模糊查询
     *   另外, 在任何操作符前可以加上 ! 标识取非(即 NOT(...))
     *   如 "=field01" 则在 WHERE 条件中生成  filed01=?
     *   如 "!=field01" 则在 WHERE 条件中生成  NOT (filed01=?)
     *   如 "#>field01" 则在 WHERE 条件中生成 field01>$value$
     *   </pre>
     * @param orConditions 默认拼装的条件使用  AND 连接, 如果该参数为 true, 则使用 OR 连接
     * @return
     */
    public static AbstractSqlStatement prepareDeleteQuery(String table, @NonNull ObjectMap0<?> kvPairs, boolean orConditions ) {
        return prepareQueryWithCondition(SQLTYPE.DELETE, table, kvPairs.asMap(), orConditions);
    }
    
    private static AbstractSqlStatement prepareQueryWithCondition(@NonNull SQLTYPE type, @NonNull String table,
            @NonNull Map<String, Object> kvPairs, boolean orConditions) {
        StringBuffer sqlWhere = new StringBuffer();
        StringBuffer sqlUpdate = SQLTYPE.UPDATE.equals(type)
                ? new StringBuffer("UPDATE ").append(quoteName(table)).append(" SET ")
                : new StringBuffer("DELETE FROM ").append(quoteName(table)).append(" ");
        List<Object> valWhere = new ArrayList<Object>();
        List<Object> valUpdate = new ArrayList<Object>();
        for (Map.Entry<String, Object> e : kvPairs.entrySet()) {
            boolean valueAsPlain;
            Matcher fieldMatcher;
            String operator = "=";
            boolean operatorIsNot = false;
            String field = e.getKey();
            Object value = e.getValue();
            boolean fieldAsWhere = false;
            if ((valueAsPlain = StringUtils.startsWith(field, "#"))) {
                field = field.substring(1);
            }
            if ((operatorIsNot = StringUtils.startsWith(field, "!"))) {
                fieldAsWhere = true;
                field = field.substring(1);
            }
            if ((fieldMatcher = REGEXP_SQL_OPERATOR.matcher(field)) != null && fieldMatcher.find()) {
                fieldAsWhere = true;
                operator = fieldMatcher.group(1);
                field = field.substring(operator.length());
            }
            if (fieldAsWhere || SQLTYPE.DELETE.equals(type)) {
                if (sqlWhere.length() > 0) {
                    sqlWhere.append(orConditions ? " OR " : " AND ");
                }
                if (operatorIsNot) {
                    sqlWhere.append(" NOT (");
                }
                sqlWhere.append(quoteName(field));
                String condition = operator.equals("%") ? " LIKE CONCAT('%%', %s, '%%')"
                        : (operator.equals("+%") ? " LIKE CONCAT(%s, '%%')"
                                : (operator.equals("%+") ? " LIKE CONCAT('%%', '%s')"
                                        : String.format(" %s %%s", operator)));
                if (valueAsPlain) {
                    sqlWhere.append(String.format(condition, value));
                } else if (value == null && "=".equals(operator)) {
                    sqlWhere.append(" IS NULL");
                } else if ("=".equals(operator) && value instanceof String[]) {
                    sqlWhere.append(CommonUtil.join("?", ((String[]) value).length, ",", " IN (", ")"));
                    valWhere.addAll(Arrays.asList((String[]) value));
                } else {
                    valWhere.add(value);
                    sqlWhere.append(String.format(condition, "?"));
                }
                if (operatorIsNot) {
                    sqlWhere.append(")");
                }
            } else if (SQLTYPE.UPDATE.equals(type)) {
                sqlUpdate.append(quoteName(field));
                if (valueAsPlain) {
                    sqlUpdate.append(String.format("=%s,", value));
                } else {
                    valUpdate.add(value);
                    sqlUpdate.append("=?,");
                }
            }
        }
        sqlUpdate.setLength(sqlUpdate.length() - 1);
        if (sqlWhere.length() > 0) {
            sqlUpdate.append(" WHERE ").append(sqlWhere);
            valUpdate.addAll(valWhere);
        }
        return new BasicSqlStatement().setSql(sqlUpdate.toString()).setValues(valUpdate.toArray());
    }
    
    /**
     * 根据  Map 拼装 INSERT 语句
     * @param table     表名称
     * @param kvPairs 键值对, 如有 key 以 = 或 # 开头, 则将使用  ON DUPLICATE KEY UPDATE 语法.
     *       其中 key 以#开头的值直接拼装到 SQL 语句中, 而以=开头的值被视为参数使用
     * @return
     */
    public static AbstractSqlStatement prepareInsertQuery(String table, ObjectMap0<?> kvPairs) {
        return pairs2InsertQuery(table, kvPairs);
    }
    
    /**
     * 根据  Map 拼装 UPDATE 语句
     * @param table     表名称
     * @param kvPairs 键值对, key 以#开头的值直接拼装到  SQL 语句中,其余被视为参数使用.
     *      如 key以操作符开头,则将被视为条件,拼装到 WHERE 语句中, 支持的操作符如下: <pre>
     *      =   等于
     *      >   大于 
     *      >=  大于等于
     *      <   小于
     *      <=  小于等于
     *      %+  前置模糊
     *      +%  后置模糊
     *      %   模糊查询
     *   如 "=field01" 则在 WHERE 条件中生成  filed01=?
     *   如 "#>field01" 则在 WHERE 条件中生成 field01>$value$
     *   </pre>
     * @param orConditions 默认拼装的条件使用  AND 连接, 如果该参数为 true, 则使用 OR 连接
     * @return
     */
    public static AbstractSqlStatement prepareUpdateQuery(String table, @NonNull ObjectMap0<?> kvPairs) {
        return pairs2UpdateQuery(table, kvPairs.asMap(), false);
    }

    /**
     * 根据  Map 拼装 UPDATE 语句
     * @param table     表名称
     * @param kvPairs 键值对, key 以#开头的值直接拼装到  SQL 语句中,其余被视为参数使用.
     *      如 key以操作符开头,则将被视为条件,拼装到 WHERE 语句中, 支持的操作符如下: <pre>
     *      =   等于
     *      >   大于 
     *      >=  大于等于
     *      <   小于
     *      <=  小于等于
     *      %+  前置模糊
     *      +%  后置模糊
     *      %   模糊查询
     *   如 "=field01" 则在 WHERE 条件中生成  filed01=?
     *   如 "#>field01" 则在 WHERE 条件中生成 field01>$value$
     *   </pre>
     * @param orConditions 默认拼装的条件使用  AND 连接, 如果该参数为 true, 则使用 OR 连接
     * @return
     */
    public static AbstractSqlStatement prepareUpdateQuery(String table, @NonNull ObjectMap0<?> kvPairs, boolean orConditions) {
        return pairs2UpdateQuery(table, kvPairs.asMap(), orConditions);
    }
    
    public static Timestamp convert2Timestamp(Object datetime) {
        if ( datetime == null ) {
            return null;
        }
        if ( datetime instanceof Timestamp ) {
            return (Timestamp)datetime;
        }
        if ( datetime instanceof java.util.Date ) {
            return new Timestamp(((java.util.Date)datetime).getTime());
        }
        if ( StringUtils.isBlank(datetime.toString()) ) {
            return null;
        }
        try {
            return Timestamp.valueOf(datetime.toString());
        } catch(Exception e) { }
        for ( String fmt : new String[] {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH-mm-ss",
                "yyyy-MM-dd_HH:mm:ss",
                "yyyy-MM-dd_HH-mm-ss",
                "yyyy/MM/dd HH:mm:ss",
                "yyyy/MM/dd HH-mm-ss",
                "yyyy/MM/dd_HH:mm:ss",
                "yyyy-MM/dd_HH-mm-ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd HH-mm",
                "yyyy-MM-dd_HH:mm",
                "yyyy-MM-dd_HH-mm",
                "yyyy-MM/dd HH:mm",
                "yyyy-MM/dd HH-mm",
                "yyyy-MM/dd_HH:mm",
                "yyyy-MM/dd_HH-mm",
                "yyyy-MM-dd",
                "yyyy/MM/dd"
        } ) {
            try {
                return new Timestamp(new SimpleDateFormat(fmt).parse(
                            datetime.toString()).getTime());
            } catch(Exception e) { }
        }
        return null;
    }
}
