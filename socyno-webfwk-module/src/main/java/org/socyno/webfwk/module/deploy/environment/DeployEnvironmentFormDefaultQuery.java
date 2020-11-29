package org.socyno.webfwk.module.deploy.environment;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.basic.AbstractStateFormQuery;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;

import com.github.reinert.jjschema.Attributes;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class DeployEnvironmentFormDefaultQuery extends AbstractStateFormQuery {
    
    @Attributes(title = "关键字", position = 10)
    private String namelike;
    
    /**
     * SELECT a.* FROM %s a
     */
    @Multiline
    public final static String SQL_SELECT_FORM = "X";
    
    public AbstractSqlStatement buildWhereSql() {
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder sqlWhere = new StringBuilder("WHERE 1 = 1");
        
        if (StringUtils.isNotBlank(getNamelike())) {
            sqlArgs.add(getNamelike());
            sqlArgs.add(getNamelike());
            sqlWhere.append("and (").append("a.name LIKE CONCAT('%', ?, '%')")
                    .append(" OR a.display LIKE CONCAT('%', ?, '%')").append(")");
            
        }
        return new BasicSqlStatement().setSql(sqlWhere.toString()).setValues(sqlArgs.toArray());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format("SELECT COUNT(1) FROM %s a %s",
                DeployEnvironmentService.DEFAULT.getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format(SQL_SELECT_FORM + " %s ORDER BY a.name DESC LIMIT %s, %s",
                        DeployEnvironmentService.DEFAULT.getFormTable(), whereQuery.getSql(), getOffset(), getLimit()));
    }
    
}
