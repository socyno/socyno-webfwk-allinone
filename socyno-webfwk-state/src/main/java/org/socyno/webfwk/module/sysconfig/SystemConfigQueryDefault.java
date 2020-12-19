package org.socyno.webfwk.module.sysconfig;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.abs.AbstractStateFormQuery;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "系统参数配置列表查询")
public class SystemConfigQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "键", position = 1002)
    private String name;
    
    @Attributes(title = "值", position = 1003)
    private String value;
    
    @Attributes(title = "备注", position = 1005)
    private String comment;
    
    /**
     * SELECT a.* FROM %s a
     **/
    @Multiline
    public final static String SQL_SELECT_FORM = "X";
    
    /**
     * SELECT COUNT(1) FROM %s a
     **/
    @Multiline
    public final static String SQL_SELECT_COUNT = "X";
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        
        if (StringUtils.isNotBlank(getName())) {
            sqlargs.add(getName());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.name like concat('%',?,'%') ");
        }
        
        if (StringUtils.isNotBlank(getValue())) {
            sqlargs.add(getValue());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.value like concat('%',?,'%') ");
        }
        
        if (StringUtils.isNotBlank(getComment())) {
            sqlargs.add(getComment());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.comment like concat('%',?,'%') ");
        }
        
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(SQL_SELECT_COUNT.concat(" %s"),
                SystemConfigService.getInstance().getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(SQL_SELECT_FORM.concat(" %s ORDER BY a.id DESC LIMIT %s, %s"),
                        SystemConfigService.getInstance().getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
