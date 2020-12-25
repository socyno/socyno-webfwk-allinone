package com.weimob.webfwk.module.release.change;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.util.sql.AbstractSqlStatement;
import com.weimob.webfwk.util.sql.BasicSqlStatement;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.ConvertUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.adrianwalker.multilinestring.Multiline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ChangeRequestQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "变更单号")
    private Long id;
    
    @Attributes(title = "上线项目", type = FieldChangeRequestReleaseId.class)
    private String releaseId;
    
    @Attributes(title = "上线窗口")
    private String releaseCycle;
    
    @Attributes(title = "状态", type = ChangeRequestFormSimple.FieldOptionsState.class)
    private String[] states;
    
    @Attributes(title = "部署项名称")
    private String deployItemName;
    
    @Attributes(title = "变更类型", type = ChangeRequestFormSimple.FieldOptionsChangeType.class)
    private String[] changeTypes;
    
    @Attributes(title = "适用范围", type = ChangeRequestFormSimple.FieldOptionsScopeType.class)
    private String scopeType;
    
    @Attributes(title = "变更分类", type = FieldChangeRequestCategoryAllAllowed.class)
    private String category;
    
    public ChangeRequestQueryDefault() {
        super();
    }
    
    public ChangeRequestQueryDefault(long page, int limit) {
        super();
        setPage(page);
        setLimit(limit);
    }
    
    /**
     * SELECT
     *      a.*
     * FROM
     *      %s a
     *  %s
     **/
    @Multiline
    private final static String SQL_SELECT_FORM = "X";
    
    /**
     * SELECT COUNT(1) FROM %s a %s
     **/
    @Multiline
    private final static String SQL_SELECT_COUNT = "X";
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlstmt = new StringBuilder();
        
        if (id != null) {
            sqlargs.add(id);
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("a.id = ?");
        }
        
        if (StringUtils.isNotBlank(releaseId)) {
            sqlargs.add(releaseId);
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("a.release_id LIKE CONCAT('%', ?, '%')");
        }
        
        if (StringUtils.isNotBlank(releaseCycle)) {
            sqlargs.add(releaseCycle);
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append(
                    "EXISTS (SELECT r.release_id FROM release_change_requirement r WHERE r.release_id = a.release_id AND r.release_cycle = ?)");
        }
        
        if (StringUtils.isNotBlank(deployItemName)) {
            sqlargs.add(deployItemName);
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("a.deploy_item_name = ?");
        }
        
        if (changeTypes != null
                && (changeTypes = ConvertUtil.asNonBlankUniqueTrimedStringArray((Object[]) changeTypes)).length > 0) {
            sqlargs.addAll(Arrays.asList(changeTypes));
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("a.change_type IN (")
                    .append(CommonUtil.join("?", changeTypes.length, ",")).append(")");
        }
        
        if (states != null && (states = ConvertUtil.asNonBlankUniqueTrimedStringArray((Object[]) states)).length > 0) {
            sqlargs.addAll(Arrays.asList(states));
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("a.state_form_status IN (")
                    .append(CommonUtil.join("?", states.length, ",")).append(")");
        }
        
        if (StringUtils.isNotBlank(scopeType)) {
            sqlargs.add(scopeType);
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("a.scope_type = ?");
        }
        
        if (StringUtils.isNotBlank(category)) {
            sqlargs.add(category);
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append("a.category = ?");
        }
        
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlstmt, "WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(SQL_SELECT_COUNT, ChangeRequestService.getInstance().getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format(SQL_SELECT_FORM.concat(" ORDER BY a.id DESC LIMIT %s, %s"),
                        ChangeRequestService.getInstance().getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
    
}
