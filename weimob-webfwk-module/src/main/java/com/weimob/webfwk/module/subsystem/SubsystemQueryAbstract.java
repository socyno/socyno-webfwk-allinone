package com.weimob.webfwk.module.subsystem;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.modutil.SubsystemBasicUtil;
import com.weimob.webfwk.state.abs.AbstractStateFormQuery;
import com.weimob.webfwk.state.service.PermissionService;
import com.weimob.webfwk.util.sql.AbstractSqlStatement;
import com.weimob.webfwk.util.sql.BasicSqlStatement;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "可访问的业务系统清单查询")
public abstract class SubsystemQueryAbstract extends AbstractStateFormQuery {
    
    @Attributes(title = "关键字", position = 10)
    private String keyword;
    
    @Attributes(title = "包括已禁用", position = 20)
    private boolean disableIncluded;
    
    @Attributes(title = "主键列表（逗号分隔）")
    private String idsIn;
    
    @Attributes(title = "代码清单", description = "业务系统代码清单，多个使用逗号、分号或空格分隔")
    private String codesIn;
    
    protected abstract String requiredAccessEventKey();
    
    public SubsystemQueryAbstract(String keyword, Integer limit, Long page) {
        super(limit, page);
        setKeyword(keyword);
    }
    
    public SubsystemQueryAbstract(Integer limit, Long page) {
        super(limit, page);
    }
    
    public SubsystemQueryAbstract(String keyword) {
        this(keyword, null, null);
    }
    
    public SubsystemQueryAbstract() {
        super();
    }
    
    private long[] getMyAccesableSubsystemIds() throws Exception {
        String authKey;
        if (StringUtils.isBlank(authKey = requiredAccessEventKey())) {
            return null;
        }
        return SubsystemBasicUtil.subsytemIdFromBusinessId(PermissionService.queryMyBusinessByAuthKey(authKey), true);
    }
    
    private AbstractSqlStatement buildWhereSql() throws Exception {
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        long[] subsystemIds;
        if ((subsystemIds = getMyAccesableSubsystemIds()) != null) {
            if (subsystemIds.length <= 0) {
                return new BasicSqlStatement().setSql("WHERE 1 = 0");
            }
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append(String.format(" s.id IN (%s)", StringUtils.join(subsystemIds, ',')));
        }
        if (StringUtils.isNotBlank(getIdsIn())) {
            String[] subsysIds = CommonUtil.split(getIdsIn(), "[,;\\s]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED);
            if (subsysIds != null && subsysIds.length > 0) {
                sqlArgs.addAll(Arrays.asList(subsysIds));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(" s.id IN ")
                        .append(CommonUtil.join("?", subsysIds.length, ",", "(", ")"));
            } else {
                return new BasicSqlStatement().setSql("WHERE 1 = 0");
            }
        }
        if (StringUtils.isNotBlank(getCodesIn())) {
            String[] sysCodes = CommonUtil.split(getCodesIn(), "[,;\\s]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED);
            if (sysCodes != null && sysCodes.length > 0) {
                sqlArgs.addAll(Arrays.asList(sysCodes));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("s.code IN ")
                        .append(CommonUtil.join("?", sysCodes.length, ",", "(", ")"));
            } else {
                return new BasicSqlStatement().setSql("WHERE 1 = 0");
            }
        }
        if (StringUtils.isNotBlank(getKeyword())) {
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append("(s.code LIKE CONCAT('%', ?, '%') OR s.name LIKE CONCAT('%', ?, '%')"
                            + " OR s.description LIKE CONCAT('%', ?, '%'))");
            sqlArgs.add(getKeyword());
            sqlArgs.add(getKeyword());
            sqlArgs.add(getKeyword());
        }
        if (!isDisableIncluded()) {
            sqlArgs.add(SubsystemService.STATES.DISABLED.getCode());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append(String.format("s.%s != ?", SubsystemService.getInstance().getFormStateField()));
        }
        return new BasicSqlStatement().setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString())
                .setValues(sqlArgs.toArray());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(
                String.format("SELECT COUNT(1) FROM %s s %s", SubsystemService.getInstance().getFormTable(), query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues())
                .setSql(String.format("SELECT s.* FROM %s s %s ORDER BY s.code ASC LIMIT %s, %s",
                        SubsystemService.getInstance().getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
