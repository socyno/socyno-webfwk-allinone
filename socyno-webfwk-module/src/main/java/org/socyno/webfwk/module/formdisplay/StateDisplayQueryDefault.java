package org.socyno.webfwk.module.formdisplay;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.basic.AbstractStateFormQuery;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "表单显示配置列表查询")
public class StateDisplayQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "路径", position = 1001)
    private String name;
    
    @Attributes(title = "显示", position = 1002)
    private String display;
    
    @Attributes(title = "只显示我的配置", position = 1003)
    private boolean createdByMe;
    
    @Attributes(title = "备注", position = 1004)
    private String remark;
    
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
        if (createdByMe == true) {
            sqlargs.add(SessionContext.getUsername());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.created_code_by = ? ");
        }
        
        if (StringUtils.isNotBlank(getName())) {
            sqlargs.add(getName());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.name like concat('%',?,'%') ");
        }
        
        if (StringUtils.isNotBlank(getDisplay())) {
            sqlargs.add(getDisplay());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.display like concat('%',?,'%') ");
        }
        
        if (StringUtils.isNotBlank(getRemark())) {
            sqlargs.add(getRemark());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.remark like concat('%',?,'%') ");
        }
        
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(SQL_SELECT_COUNT.concat(" %s"),
                StateDisplayService.getInstance().getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(SQL_SELECT_FORM.concat(" %s ORDER BY a.id DESC LIMIT %s, %s"),
                        StateDisplayService.getInstance().getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
