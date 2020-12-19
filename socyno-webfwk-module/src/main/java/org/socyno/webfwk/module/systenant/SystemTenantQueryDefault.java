package org.socyno.webfwk.module.systenant;

import java.util.ArrayList;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.abs.AbstractStateFormQuery;
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
@Attributes(title = "租户信息查询")
public class SystemTenantQueryDefault extends AbstractStateFormQuery {
    
    /**
     SELECT
         COUNT(1)
     FROM
         %s f
    */
   @Multiline
   private static final String SQL_QUERY_COUNT = "X";
   
    /**
     SELECT
         f.*
     FROM
         %s f
    */
    @Multiline
    private static final String SQL_QUERY_ALL = "X";
   
    /**
     (
         f.code LIKE CONCAT(?, '%')
       OR
         f.name LIKE CONCAT(?, '%')
     )
    */
   @Multiline
   private static final String SQL_QUERY_NAMELIKE_TMPL = "X";
   
   @Attributes(title = "关键字", position = 1010)
   private String nameLike;
   
   @Attributes(title = "是否包括已禁用", position = 1020)
   private boolean disableIncluded = false;
   
   public SystemTenantQueryDefault(Long page, Integer limit) {
       this(null, page, limit);
   }
   
   public SystemTenantQueryDefault(String nameLike, Long page, Integer limit) {
       super(limit, page);
       this.nameLike = nameLike;
    }
    
    private AbstractSqlStatement buildWhereSql() {
        StringBuilder sqlcond = new StringBuilder();
        List<Object> sqlargs = new ArrayList<>();
        if (StringUtils.isNotBlank(getNameLike())) {
            sqlargs.add(nameLike);
            sqlargs.add(nameLike);
            StringUtils.appendIfNotEmpty(sqlcond, " and ").append(SQL_QUERY_NAMELIKE_TMPL);
        }
        if (!isDisableIncluded()) {
            sqlargs.add(SystemTenantService.STATES.DISABLED.getCode());
            StringUtils.appendIfNotEmpty(sqlcond, " and ").append(SystemTenantService.getInstance().getFormStateField())
                    .append(" != ?");
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlcond, "WHERE ").toString());
    }
   
   @Override
   public AbstractSqlStatement prepareSqlTotal() {
       AbstractSqlStatement whereQuery = buildWhereSql();
       return new BasicSqlStatement().setValues(whereQuery.getValues()).setSql(String.format("%s %s",
                       String.format(SQL_QUERY_COUNT, SystemTenantService.getInstance().getFormTable()),
                       whereQuery.getSql()));
   }
   
   @Override
   public AbstractSqlStatement prepareSqlQuery() {
       AbstractSqlStatement whereQuery = buildWhereSql();
       return new BasicSqlStatement().setValues(whereQuery.getValues())
               .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                       String.format(SQL_QUERY_ALL, SystemTenantService.getInstance().getFormTable()),
                       whereQuery.getSql(), getOffset(), getLimit()));
   }
}
