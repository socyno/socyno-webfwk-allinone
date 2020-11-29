package org.socyno.webfwk.module.dynamicoption;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.basic.AbstractStateFormQuery;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class SystemFieldOptionDefaultQuery extends AbstractStateFormQuery {
    
    @Attributes(title = "路径/名称", type = FieldSystemFieldClassPath.class)
    private String classPath;
    
    /**
     * SELECT 
     *      s.*
     *  FROM 
     *     %s s
     */
    @Multiline
    public final static String SQL_SELECT_FORM = "X";
    
    public AbstractSqlStatement buildWhereSql() {
        
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder sqlWhere = new StringBuilder("");
        
        if (StringUtils.isNotBlank(getClassPath())) {
            sqlArgs.add(getClassPath());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append(" s.class_path = ? ");
        }
        
        return new BasicSqlStatement().setValues(sqlArgs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlWhere, " WHERE ").toString());
    }
    
    /**
     * SELECT
     *      COUNT(DISTINCT class_path)
     * FROM
     *      %s s
     */
    @Multiline
    private final static String SQL_SELECT_COUNT = "X";

    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(SQL_SELECT_COUNT.concat(" %s"), SystemFieldOptionService.DEFAULT.getFormTable(),
                        query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(
                String.format(SQL_SELECT_FORM + " %s GROUP BY s.class_path ORDER BY s.id DESC LIMIT %s, %s",
                        SystemFieldOptionService.DEFAULT.getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
