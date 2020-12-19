package org.socyno.webfwk.module.vcs.change;

import java.util.ArrayList;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.module.application.FieldApplication;
import org.socyno.webfwk.state.abs.AbstractStateFormQuery;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "代码仓变更历史查询")
public class VcsChangeListDefaultQuery extends AbstractStateFormQuery {
    
    /**
     SELECT
         COUNT(1)
     FROM
         %s f
     LEFT JOIN
         application a ON a.id = f.application
    */
   @Multiline
   private static final String SQL_QUERY_COUNT = "X";
   
    /**
     SELECT
         f.*,
         a.name applicationName
     FROM
         %s f
     LEFT JOIN
         application a ON a.id = f.application
    */
    @Multiline
    private static final String SQL_QUERY_ALL = "X";
    
    public VcsChangeListDefaultQuery(Integer limit) {
        super(limit);
    }
    
    public VcsChangeListDefaultQuery(Integer limit, Long page) {
        super(limit, page);
    }
    
    @Attributes(title = "应用", type = FieldApplication.class)
    private Long application;
    
    @Attributes(title = "提交人", type = FieldSystemUser.class)
    private Long createdBy;
    
    @Attributes(title = "分支")
    private String vcsRefsName;
    
    @Attributes(title = "提交版本")
    private String vcsRevision;
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        if (getApplication() != null) {
            sqlargs.add(getApplication());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.application = ?");
        }
        if (getCreatedBy() != null) {
            sqlargs.add(getCreatedBy());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.created_by = ?");
        }
        if (StringUtils.isNotBlank(getVcsRefsName())) {
            sqlargs.add(getVcsRefsName());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.vcs_refs_name LIKE CONCAT('%', ?)");
        }
        if (StringUtils.isNotBlank(getVcsRevision())) {
            sqlargs.add(getVcsRevision());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.vcs_revision LIKE CONCAT(?, '%')");
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues()).setSql(String.format("%s %s",
                       String.format(SQL_QUERY_COUNT, VcsChangeInfoService.getInstance().getFormTable()),
                       whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
               .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                       String.format(SQL_QUERY_ALL, VcsChangeInfoService.getInstance().getFormTable()),
                       whereQuery.getSql(), getOffset(), getLimit()));
    }    
}
