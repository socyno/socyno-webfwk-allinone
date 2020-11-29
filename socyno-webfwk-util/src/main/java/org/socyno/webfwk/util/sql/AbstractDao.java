package org.socyno.webfwk.util.sql;

import com.github.reinert.jjschema.v1.FieldOption;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.apache.commons.io.IOUtils;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class AbstractDao {
    
    private final DataSource dao;
    private int transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
    
    private final ThreadLocal<Connection> currentConnection
                = new ThreadLocal<Connection>();
    
    private final static Pattern RE_SQL_DML = Pattern.compile(
            "^\\s*(SELECT|UPDATE|DELETE|INSERT)\\s+",
            Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
    private static final Pattern REGEXP_BEAN_STRING_ARRAY = Pattern.compile(
            "^\\[([^\\]]+)\\](.+)$");
    
    public AbstractDao(String propertiesFile) throws Exception {
        this.dao = initDataSource(propertiesFile);
    }
    
    /**
     * 默认大小写
     */
    public static final int SQL_COLUMN_MAPPER_CASE_NONE = 0;
    
    /**
     * 强制转小写
     */
    public static final  int SQL_COLUMN_MAPPER_CASE_LOWER = 1;

    /**
     * 强制转大写
     */
    public static final  int SQL_COLUMN_MAPPER_CASE_UPPER = 2;
    
    public int getColumnMapperCase() {
        return SQL_COLUMN_MAPPER_CASE_NONE;
    }
    
    protected DataSource initDataSource(String propertiesFile) throws Exception {
        if (StringUtils.isBlank(propertiesFile)) {
            return null;
        }
        InputStream propertiesStream;
        String copiedFileName = propertiesFile.replaceFirst("^classpath\\s*:\\s*", "");
        if (copiedFileName.length() != propertiesFile.length()) {
            log.info("loading database properties form classpath:{}", copiedFileName);
            propertiesStream = getClass().getClassLoader().getResourceAsStream(copiedFileName);
        } else {
            propertiesStream = new FileInputStream(new File(copiedFileName));
        }
        Properties properties = new Properties();
        properties.load(propertiesStream);
        return BasicDataSourceFactory.createDataSource(properties);
    }
    
    public AbstractDao (DataSource dataSource) {
        this.dao = dataSource;
    }
    
    public AbstractDao () {
        this.dao = null;
    }
    
    public DataSource getDataSource() {
        return dao;
    }
    
    protected int getTransactionIsolation() {
        return transactionIsolation;
    }
    
    public void setTransactionIsolation (int isolation) {
        this.transactionIsolation = isolation;
    }
    
    private void close() {
        Connection conn; 
        if ( (conn=get()) != null ) {
            currentConnection.set(null);
            try {conn.setAutoCommit(true); }
            catch( Exception e ) {}
            try { conn.close(); }
            catch ( Exception e ) {
                log.error("Failed to close database connection.", e);
            }
        }
    }
    
    private void close(Statement stat) {
        if ( stat != null ) {
            try { stat.close(); }
            catch ( Exception e ) {
                log.error("Failed to close db statement.", e);
            }
        }
    }
    
    private void close(ResultSet resulstset) {
        if ( resulstset != null ) {
            try { resulstset.close(); }
            catch ( Exception e ) {
                log.error("Failed to close db resulstset.", e);
            }
        }
    }
    
    private void commit() throws SQLException {
        Connection conn;
        if ( (conn=get()) != null ) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }
    
    private boolean rollback(Savepoint sp) throws SQLException {
        Connection conn;
        /* 仅连接存在并开启了事务的情况下，才执行回滚操作 */
        if ((conn=get()) != null && !conn.getAutoCommit()) {
            if ( sp == null ) {
                conn.rollback();
            } else {
                conn.rollback(sp);
            }
            return true;
        }
        return false;
    }
    
    private boolean reset() throws Exception {
        if (currentConnection.get() == null) {
            Connection conn = getDataSource().getConnection();
            conn.setAutoCommit(true);
            currentConnection.set(conn);
            return true;
        }
        return false;
    }
    
    private Connection get()  {
        return currentConnection.get();
    }
    
    public int executeUpdate(String sql)
            throws Exception {
        return executeUpdate(sql, null, null);
    }
    
    public int executeUpdate(AbstractSqlStatement sqlQuery)
            throws Exception {
        return executeUpdate(sqlQuery.getSql(), sqlQuery.getValues(), null);
    }

    public int executeUpdate(AbstractSqlStatement lockQuery, ResultSetProcessor processor)
            throws Exception {
        return executeUpdate(lockQuery.getSql(), lockQuery.getValues(), processor);
    }
    
    public int executeUpdate(String sql, Object[] args)
            throws Exception {
        return executeUpdate(sql, args, null);
    }
    
    public int executeUpdate(String sql, ResultSetProcessor processor)
            throws Exception {
        return executeUpdate(sql, null, processor);
    }
    
    public int executeUpdate(String sql, Object[] args, ResultSetProcessor processor)
            throws Exception {
        if (StringUtils.isBlank(sql)) {
            throw new SQLException("No sql statement provided.");
        }
        ResultSet resultset = null;
        PreparedStatement stmt = null;
        boolean connectionReset = false;
        int sqlAffectRows = 0;
        Throwable exception = null;
        String databaseUrl = null;
        final long startedMs = System.currentTimeMillis();
        try {
            connectionReset = reset();
            Connection conn = get();
            databaseUrl = conn.getMetaData().getURL();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    Object argv = args[i];
                    if (argv == null) {
                        stmt.setString(i + 1, null);
                    } else if (argv instanceof java.sql.Date) {
                        stmt.setDate(i + 1, (java.sql.Date) argv);
                    } else if (argv instanceof Date) {
                        stmt.setTimestamp(i + 1, new Timestamp(((Date) argv).getTime()));
                    } else if (argv instanceof Timestamp) {
                        stmt.setTimestamp(i + 1, (Timestamp) argv);
                    } else if (argv instanceof Integer || int.class.equals(argv.getClass())) {
                        stmt.setInt(i + 1, (int) argv);
                    } else if (argv instanceof Long || long.class.equals(argv.getClass())) {
                        stmt.setLong(i + 1, (long) argv);
                    } else if (argv instanceof Boolean || boolean.class.equals(argv.getClass())) {
                        stmt.setInt(i + 1, ((boolean) argv) ? 1 : 0);
                    } else if (argv instanceof FieldOption) { 
                        stmt.setString(i + 1, ((FieldOption)argv).getOptionValue());
                    } else {
                        stmt.setString(i + 1, argv.toString());
                    }
                }
            }
            Matcher reMatched = null;
            if ((reMatched = RE_SQL_DML.matcher(sql)) != null && reMatched.find()) {
                if (reMatched.group(1).toUpperCase().contains("SELECT")) {
                    resultset = stmt.executeQuery();
                } else {
                    sqlAffectRows = stmt.executeUpdate();
                    resultset = stmt.getGeneratedKeys();
                }
            } else {
                stmt.execute();
                resultset = stmt.getResultSet();
                sqlAffectRows = stmt.getUpdateCount();
            }
            if (processor != null) {
                processor.setConnection(conn);
                processor.setAffectRows(sqlAffectRows);
                processor.process(resultset, conn);
            }
            if (stmt != null) {
                close(stmt);
                stmt = null;
            }
            return sqlAffectRows;
        } finally {
            close(resultset);
            close(stmt);
            if (connectionReset) {
                close();
            }
            final long excutedMs = System.currentTimeMillis() - startedMs;
            final boolean isSlowSql = excutedMs > getSlowSqlMillisecond();
			if (isSlowSql || inDebugMode()) {
                log.info("{} ：{} ms, effected rows = {}, sql = {}, args = {}, url = {}",
                        isSlowSql ? "Slow SQL" : "Debug SQL", excutedMs, exception != null ? "error" : sqlAffectRows,
                        sql, args, databaseUrl);
            }
        }
    }
    
    public boolean inDebugMode() {
    	return log.isDebugEnabled();
    }
    
    public int getSlowSqlMillisecond() {
    	return 1000;
    }

    public void executeQuery(String sql, ResultSetProcessor processor)
            throws Exception {
        executeQuery(sql, null, processor);
    }
    
    public void executeQuery(String sql, Object[] args, ResultSetProcessor processor)
            throws Exception {
        executeUpdate(sql, args, processor);
    }
    
    private Savepoint begin() throws SQLException {
        Connection conn;
        if ( (conn=get()) != null && conn.getAutoCommit() ) {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(getTransactionIsolation());
            return null;
        } else if (conn != null) {
            return conn.setSavepoint();
        }
        throw new SQLException("No connection initialized.");
    }
    
    public static void executeTransaction(ResultSetProcessor processor, @NonNull AbstractDao... daos) throws Exception {
        if (processor == null) {
            return;
        }
        final List<AbstractDao> connections = new ArrayList<AbstractDao>();
        Map<AbstractDao, Savepoint> savepoints = new HashMap<AbstractDao, Savepoint>();
        try {
            for (AbstractDao dao : daos) {
                if (dao.reset()) {
                    connections.add(dao);
                }
                savepoints.put(dao, dao.begin());
            }
            processor.process(null, null);
            /* 当前开启的事物，需要提交并关闭 */
            for (Entry<AbstractDao, Savepoint> sp : savepoints.entrySet()) {
                if (sp.getValue() == null) {
                    sp.getKey().commit();
                }
            }
        } catch (Throwable e) {
            /* 回滚已执行的操作 */
            for (Entry<AbstractDao, Savepoint> sp : savepoints.entrySet()) {
                try {
                    sp.getKey().rollback(sp.getValue());
                } catch (Exception ex) {
                    log.error("Rollback failure : ", ex);
                }
            }
            throw e;
        } finally {
            /* 当前创建的连接需要关闭 */
            for (AbstractDao dao : connections) {
                dao.close();
            }
        }
    }
    
    public void executeTransaction(ResultSetProcessor processor)
            throws Exception {
        executeTransaction(processor, this);
    }
    
    public List<Map<String, Object>> queryAsList(String sql) throws Exception {
        return queryAsList(sql, (Object[])null);
    }

    public List<Map<String, Object>> queryAsList( String sql, Object[] args)
            throws Exception {
        return queryAsList(sql, args, false);
    }
    
    private Object parseDbValue(Object v) throws Exception {
        if (v == null) {
            return v;
        }
        if ("org.h2.jdbc.JdbcClob".equals(v.getClass().getName())) {
            Reader stream = ((org.h2.jdbc.JdbcClob) v).getCharacterStream();
            try {
                v = new String(IOUtils.toByteArray(stream, "UTF-8"), "UTF-8");
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        return v;
    }
    
    protected String fieldNamingColumnLabel(@NonNull String label, int columnMapperCase, boolean withFieldNaming) {
        if ((columnMapperCase & SQL_COLUMN_MAPPER_CASE_UPPER) != 0) {
            label = label.toUpperCase();
        } else if ((columnMapperCase & SQL_COLUMN_MAPPER_CASE_LOWER) != 0) {
            label = label.toLowerCase();
        }
        return withFieldNaming ? StringUtils.applyFieldNamingPolicy(label) : label;
    }
    
    public List<Map<String, Object>> queryAsList(String sql, Object[] args, final boolean fieldNaming)
                throws Exception {
        final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        executeQuery(sql, args, new ResultSetProcessor () {
            @Override
            public void process(ResultSet r, Connection c) throws Exception {
                int mapperCase = getColumnMapperCase();
                ResultSetMetaData meta = r.getMetaData();
                int columnCount = meta.getColumnCount();
                String[] columnLabels = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    columnLabels[i] = fieldNamingColumnLabel(meta.getColumnLabel(i+1), mapperCase, fieldNaming);
                }
                while (r.next()) {
                    Map<String, Object> rowdata = new HashMap<String, Object>();
                    for (int i = 0; i < columnCount; i++) {
                        rowdata.put(columnLabels[i], parseDbValue(r.getObject(i + 1)));
                    }
                    result.add(rowdata);
                }
            }
        });
        return result;
    }
    
    /* 检索对象列表 */
    public <T> List<T> queryAsList(Class<T> clazz, String sql) throws Exception {
        return queryAsList(clazz, sql, (Object[])null, null);
    }
    
    /* 检索对象列表 */
    public <T> List<T> queryAsList(Class<T> clazz, String sql, Object[] args) throws Exception {
        return queryAsList(clazz, sql, args, null);
    }
    
    /* 检索对象列表 */
    public <T> List<T> queryAsList(Class<T> clazz, String sql, Object[] args, Map<String, String> mapper )
            throws Exception {
        final List<T> result= new ArrayList<T>();   
        for ( Map<String, Object> row : queryAsList(sql, args, true) ) {
            result.add(toBean(row, clazz, mapper, true));
        }
        return result;
    }

    public Map<String, Object> queryAsMap(String sql) throws Exception {
        return queryAsMap(sql, null);
    }
    
    public Map<String, Object> queryAsMap( String sql, Object[] args)
            throws Exception {
        return queryAsMap(sql, args, false); 
    }
    
    private Map<String, Object> queryAsMap(String sql, Object[] args, final boolean fieldNaming) throws Exception {
        final Map<String, Object> result = new HashMap<String, Object>();
        final AtomicInteger counter = new AtomicInteger(0);
        executeQuery(sql, args, new ResultSetProcessor () {
            @Override
            public void process(ResultSet r, Connection c)
                    throws Exception {
                if (!r.next()) {
                    return;
                }
                if (counter.getAndIncrement() > 0) {
                    throw new SQLException("Single result required, but more queried.");
                }
                int mapperCase = getColumnMapperCase();
                ResultSetMetaData meta = r.getMetaData();
                int columnCount = meta.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    result.put(fieldNamingColumnLabel(meta.getColumnLabel(i), mapperCase, fieldNaming),
                                        parseDbValue(r.getObject(i)));
                }
            }
        });
        if (counter.get() <= 0) {
            return null;
        }
        return result;
    }
    
    public Map<String, Object> queryObject(String sql, Object[] args) throws Exception {
        return queryAsMap(sql, args, false);
    }
    
    public Map<String, Object> queryObject(String sql, Object[] args, final boolean fieldNaming) throws Exception {
        return queryAsMap(sql, args, fieldNaming);
    }
    
    public <T> T queryAsObject(Class<T> clazz, String sql, Object[] args) throws Exception {
        return queryAsObject(clazz, sql, args, null);
    }
    
    public <T> T queryAsObject(Class<T> clazz, String sql, Object[] args, Map<String, String> mapper) throws Exception {
        return toBean(queryAsMap(sql, args, true), clazz, mapper, true);
    }
    
    @SuppressWarnings("unchecked")
    protected <T> T toBean(Map<String, Object> data, @NonNull Class<T> clazz, Map<String, String> mapper, boolean primitiveSupported) throws Exception {
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
                for (String key : new String[] { k1, StringUtils.applyFieldNamingPolicy(k1) }) {
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
                    val = CommonUtil.parseBoolean(val);
                } else if (Boolean.class.equals(ptypes[0])) {
                    val = CommonUtil.parseBooleanAllownNull(val);
                }
                /* char 转换 */
                else if (char.class.equals(ptypes[0]) || Character.class.equals(ptypes[0])) {
                    val = CommonUtil.parseCharacter(val);
                }
                /* long 转换 */
                else if ((Long.class.equals(ptypes[0]) || long.class.equals(ptypes[0]))) {
                    val = CommonUtil.parseDateMS(val);
                }
                /* int 转换 */
                else if (Integer.class.equals(ptypes[0]) || int.class.equals(ptypes[0])) {
                    val = CommonUtil.parseInteger(val);
                }
                /* Date 转换 */
                else if (Date.class.equals(ptypes[0])) {
                    val = CommonUtil.parseDate(val);
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
                            CommonUtil.ifNull(val, "NULL"));
                    throw ex;
                }
                data.remove(name);
            }
        }
        return obj;
    }
    
    
    @Setter
    @Getter
    public static abstract class ResultSetProcessor {
        private int affectRows = -1;
        private Connection connection = null;

        public abstract void process(ResultSet result, Connection conn)
                throws Exception;
    }
}