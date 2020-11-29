package org.socyno.webfwk.module.sysjob;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import org.socyno.webfwk.module.sysjob.SystemJobDetailForm.FieldOptionsState;
import org.socyno.webfwk.state.basic.AbstractStateFormQuery;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.StringUtils;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SystemJobDefaultQuery extends AbstractStateFormQuery {
    
    @Attributes(title = "关键字", position = 1100)
    private String keyword;
    
    @Attributes(title = "状态", position = 1200, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "只查询未禁止的任务", position = 1300)
    private boolean onlyNonDisabled = false;
    
    @Attributes(title = "只查询计划执行的任务", position = 1400)
    private boolean onlyScheduled = false;
    
    public SystemJobDefaultQuery() {
        super();
    }
    
    public SystemJobDefaultQuery(Long page, Integer limit) {
        super(limit, page);
    }
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(state)) {
            StringUtils.appendIfNotEmpty(builder, " and ")
                    .append(String.format("%s = ?", SystemJobService.DEFAULT.getFormStateField()));
            sqlArgs.add(state);
        }
        if (StringUtils.isNotBlank(keyword)) {
            sqlArgs.add(keyword);
            sqlArgs.add(keyword);
            StringUtils.appendIfNotEmpty(builder, " and ")
                    .append("(title LIKE CONCAT('%', ?, '%') OR description LIKE CONCAT('%', ?, '%'))");
        }
        if (onlyScheduled) {
            StringUtils.appendIfNotEmpty(builder, " and ")
                    .append("cron_expression IS NOT NULL AND cron_expression != ''");
        }
        if (onlyNonDisabled) {
            StringUtils.appendIfNotEmpty(builder, " and ")
                    .append(String.format("%s != ?", SystemJobService.STATES.DISABLED.getCode()));
        }
        return new BasicSqlStatement().setSql(StringUtils.prependIfNotEmpty(builder, "WHERE ").toString())
                .setValues(sqlArgs.toArray());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format("SELECT COUNT(1) FROM %s %s",
                SystemJobService.DEFAULT.getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(" SELECT * FROM %s %s ORDER BY id DESC LIMIT %s, %s",
                        SystemJobService.DEFAULT.getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
