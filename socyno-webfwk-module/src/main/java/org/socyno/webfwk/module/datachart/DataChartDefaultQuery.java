package org.socyno.webfwk.module.datachart;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.basic.AbstractStateFormQuery;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class DataChartDefaultQuery extends AbstractStateFormQuery {
    
    @Attributes(title = "状态", type = DataChartDetailForm.FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "申请人", type = FieldSystemUser.class)
    private Long createdBy;
    
    @Attributes(title = "标题")
    private String title;
    
    /**
     * SELECT s.* FROM %s s
     */
    @Multiline
    public final static String SQL_SELECT_FORM = "X";
    
    public AbstractSqlStatement buildWhereSql() {
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder sqlWhere = new StringBuilder("");
        
        if (StringUtils.isNotBlank(getState())) {
            sqlArgs.add(getState());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append(" s.state_form_status = ? ");
        }
        
        if (getCreatedBy() != null) {
            sqlArgs.add(getCreatedBy());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append(" s.created_by = ? ");
        }
        
        if (StringUtils.isNotBlank(getTitle())) {
            sqlArgs.add(getTitle());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append(" s.title like concat('%',?,'%') ");
        }
        
        return new BasicSqlStatement().setValues(sqlArgs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlWhere, " WHERE ").toString());
    }
    
    /**
     * select count(1) from %s s
     */
    @Multiline
    private final static String SQL_SELECT_COUNT = "X";
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(
                String.format(SQL_SELECT_COUNT + " %s", DataChartService.DEFAULT.getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(SQL_SELECT_FORM + " %s ORDER BY s.id DESC LIMIT %s, %s",
                        DataChartService.DEFAULT.getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
