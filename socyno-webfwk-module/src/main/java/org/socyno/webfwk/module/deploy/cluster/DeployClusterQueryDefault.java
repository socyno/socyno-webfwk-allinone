package org.socyno.webfwk.module.deploy.cluster;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.module.deploy.cluster.DeployClusterFormSimple.FieldOptionsState;
import org.socyno.webfwk.module.deploy.environment.FieldDeployEnvironment;
import org.socyno.webfwk.state.basic.AbstractStateFormQuery;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;

import com.github.reinert.jjschema.Attributes;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class DeployClusterQueryDefault extends AbstractStateFormQuery {

    @Attributes(title = "代码", position = 5)
    private String code;

    @Attributes(title = "名称", position = 10)
    private String title;

    @Attributes(title = "环境", position = 20, type = FieldDeployEnvironment.class)
    private String environment;

    @Attributes(title = "状态", position = 30, type = FieldOptionsState.class)
    private String state;

    @Attributes(title = "创建人", position = 40, type = FieldSystemUser.class)
    private Long createdBy;
    
    /**
     * SELECT a.* FROM %s a
     */
    @Multiline
    public final static String SQL_SELECT_FORM = "X";

    public AbstractSqlStatement buildWhereSql() {
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder sqlWhere = new StringBuilder("WHERE 1 = 1");
        
        if (StringUtils.isNotBlank(getCode())) {
            sqlArgs.add(getCode());
            sqlWhere.append(" and a.code = ?");
        }
        
        if (StringUtils.isNotBlank(getTitle())) {
            sqlArgs.add(getTitle());
            sqlWhere.append(" and a.title LIKE CONCAT('%', ?, '%')");
        }
        
        if (StringUtils.isNotBlank(getEnvironment())) {
            sqlArgs.add(getEnvironment());
            sqlWhere.append(" and a.environment = ?");
        }
        
        if (getCreatedBy() != null) {
            sqlArgs.add(getCreatedBy());
            sqlWhere.append(" and a.created_by = ?");
        }
        
        if (StringUtils.isNotBlank(getState())) {
            sqlArgs.add(getState());
            sqlWhere.append(" and a.state_form_status = ?");
        }
        return new BasicSqlStatement().setSql(sqlWhere.toString()).setValues(sqlArgs.toArray());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format("SELECT COUNT(1) FROM %s a %s",
                DeployClusterService.getInstance().getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format(SQL_SELECT_FORM.concat(" %s ORDER BY a.code DESC LIMIT %s, %s"),
                        DeployClusterService.getInstance().getFormTable(), whereQuery.getSql(), getOffset(),
                        getLimit()));
    }
    
}
