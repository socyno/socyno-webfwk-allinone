package org.socyno.webfwk.module.syslog;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.state.basic.AbstractStateFormQuery;
import org.socyno.webfwk.state.field.*;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class SystemCommonLogDefaultQuery extends AbstractStateFormQuery {


    @Attributes(title = "表单类型")
    private String objectType;

    @Attributes(title = "表单ID")
    private String objectId;

    @Attributes(title = "操作用户", type = FieldSystemUser.class)
    private String operateUserId;


    /**
     * select s.* from %s s
     */
    @Multiline
    public final static String SQL_SELECT_FORM = "X";

    public AbstractSqlStatement buildWhereSql() {
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder sqlWhere = new StringBuilder("");

        if(StringUtils.isNotBlank(getObjectType())){
            sqlArgs.add(getObjectType());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append(" s.object_type = ? ");
        }

        if(StringUtils.isNotBlank(getObjectId())){
            sqlArgs.add(getObjectId());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append(" s.object_id = ? ");
        }

        if(StringUtils.isNotBlank(getOperateUserId())){
            sqlArgs.add(getOperateUserId());
            StringUtils.appendIfNotEmpty(sqlWhere, " and ").append(" s.operate_user_id = ? ");
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
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(SQL_SELECT_COUNT + " %s", SystemCommonLogService.DEFAULT.getFormTable(),
                        query.getSql()));
    }

    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(
                String.format(SQL_SELECT_FORM + " %s ORDER BY s.id DESC LIMIT %s, %s",
                        SystemCommonLogService.DEFAULT.getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
