package org.socyno.webfwk.state.module.chart;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.socyno.webfwk.state.abs.AbstractStateFormQuery;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class SystemDataChartQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "状态", type = SystemDataChartFormDetail.FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "创建人", type = FieldSystemUser.class)
    private Long createdBy;
    
    @Attributes(title = "标题")
    private String title;
    
    public AbstractSqlStatement buildWhereSql() {
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder sqlWhere = new StringBuilder("");
        
        if (StringUtils.isNotBlank(getState())) {
            sqlArgs.add(getState());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append(" s.state_form_status = ? ");
        }
        
        if (getCreatedBy() != null) {
            sqlArgs.add(getCreatedBy());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append(" s.state_form_created_by = ? ");
        }
        
        if (StringUtils.isNotBlank(getTitle())) {
            sqlArgs.add(getTitle());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append(" s.title like concat('%',?,'%') ");
        }
        
        return new BasicSqlStatement().setValues(sqlArgs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlWhere, " WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format("SELECT COUNT(1) FROM %s s %s",
                        SystemDataChartService.getInstance().getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format("SELECT s.* FROM %s s %s ORDER BY s.id DESC LIMIT %s, %s",
                        SystemDataChartService.getInstance().getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
