package com.weimob.webfwk.module.deploy.cluster;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.adrianwalker.multilinestring.Multiline;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.deploy.cluster.DeployClusterFormSimple.FieldOptionsState;
import com.weimob.webfwk.module.deploy.environment.FieldDeployEnvironment;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.state.field.FieldSystemUser;
import com.weimob.webfwk.util.sql.AbstractSqlStatement;
import com.weimob.webfwk.util.sql.BasicSqlStatement;
import com.weimob.webfwk.util.tool.StringUtils;

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
        StringBuilder sqlwhere = new StringBuilder();
        
        if (StringUtils.isNotBlank(getCode())) {
            sqlArgs.add(getCode());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.code = ?");
        }
        
        if (StringUtils.isNotBlank(getTitle())) {
            sqlArgs.add(getTitle());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.title LIKE CONCAT('%', ?, '%')");
        }
        
        if (StringUtils.isNotBlank(getEnvironment())) {
            sqlArgs.add(getEnvironment());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.environment = ?");
        }
        
        if (getCreatedBy() != null) {
            sqlArgs.add(getCreatedBy());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.created_by = ?");
        }
        
        if (StringUtils.isNotBlank(getState())) {
            sqlArgs.add(getState());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.state_form_status = ?");
        }
        return new BasicSqlStatement().setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString())
                .setValues(sqlArgs.toArray());
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
