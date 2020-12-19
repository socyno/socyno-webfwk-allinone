package org.socyno.webfwk.module.deploy.environment;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.abs.AbstractStateFormQuery;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.Attributes;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class DeployEnvironmentQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "关键字", position = 10)
    private String namelike;
    
    /**
     * SELECT a.* FROM %s a
     */
    @Multiline
    public final static String SQL_SELECT_FORM = "X";
    
    public AbstractSqlStatement buildWhereSql() {
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        
        if (StringUtils.isNotBlank(getNamelike())) {
            sqlArgs.add(getNamelike());
            sqlArgs.add(getNamelike());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("(").append("a.name LIKE CONCAT('%', ?, '%')")
                    .append(" OR a.display LIKE CONCAT('%', ?, '%')").append(")");
            
        }
        return new BasicSqlStatement().setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString())
                .setValues(sqlArgs.toArray());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(
                "SELECT COUNT(1) FROM %s a %s",
                DeployEnvironmentService.getInstance().getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format(SQL_SELECT_FORM.concat(" %s ORDER BY a.name DESC LIMIT %s, %s"),
                        DeployEnvironmentService.getInstance().getFormTable(), whereQuery.getSql(), getOffset(), getLimit()));
    }
    
}
