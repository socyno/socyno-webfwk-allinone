package org.socyno.webfwk.module.subsystem;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;
import org.socyno.webfwk.state.basic.AbstractStateFormQuery;
import org.socyno.webfwk.state.service.PermissionService;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.CommonUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain=true)
@Attributes(title="可访问的业务系统清单查询")
public class SubsystemQueryAccessable extends AbstractStateFormQuery {
    
    @Attributes(title = "关键字", position = 10)
    private String keyword;
    
    @Attributes(title = "包括已禁用", position = 20)
    private boolean disableIncluded;
    
    @Attributes(title = "主键列表（逗号分隔）")
    private String idsIn;
    
    @Attributes(title = "代码清单", description = "业务系统代码清单，多个使用逗号、分号或空格分隔")
    private String codesIn;
    
    protected String requiredAccessEventKey() {
        return SubsystemService.getInstance().getFormAccessEventKey();
    }
    
    public SubsystemQueryAccessable(String keyword, Integer limit, Long page) {
        super(limit, page);
        setKeyword(keyword);
    }
    
    public SubsystemQueryAccessable(Integer limit, Long page) {
        super(limit, page);
    }
    
    public SubsystemQueryAccessable(String keyword) {
        this(keyword, null, null);
    }
    
    public SubsystemQueryAccessable() {
        super();
    }
    
    /**
     *  SELECT DISTINCT
     *       s.*
     *  FROM %s s
     */
    @Multiline
    public final static String SQL_SELECT_FORM = "X";
    
    public AbstractSqlStatement buildWhereSql() throws Exception {
        String authKey;
        Long[] authSubsys;
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder builder = new StringBuilder("WHERE");
        if (StringUtils.isNotBlank(authKey = requiredAccessEventKey())
                && (authSubsys = PermissionService.queryMySubsystemByAuthKey(authKey)) != null) {
            if (authSubsys.length <= 0) {
                return new BasicSqlStatement().setSql(" 1 = 0").setValues(sqlArgs.toArray());
            }
            builder.append(String.format(" s.id IN (%s)", StringUtils.join(authSubsys, ',')));
        } else {
            builder.append(" 1 = 1");
        }
        if (StringUtils.isNotBlank(getIdsIn())) {
            String[] subsysIds = CommonUtil.split(getIdsIn(), "[,;\\s]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED);
            if (subsysIds != null) {
                sqlArgs.addAll(Arrays.asList(subsysIds));
                builder.append(" AND s.id IN ").append(CommonUtil.join("?", subsysIds.length, ",", "(", ")"));
            } else {
                builder.append(" AND 1 = 0 ");
            }
        }
        if (StringUtils.isNotBlank(getCodesIn())) {
            String[] sysCodes = CommonUtil.split(getCodesIn(), "[,;\\s]+",
                    CommonUtil.STR_NONBLANK | CommonUtil.STR_UNIQUE | CommonUtil.STR_TRIMED);
            if (sysCodes != null) {
                sqlArgs.addAll(Arrays.asList(sysCodes));
                builder.append(" AND s.code IN ").append(CommonUtil.join("?", sysCodes.length, ",", "(", ")"));
            } else {
                builder.append(" AND 1 = 0 ");
            }
        }
        if (StringUtils.isNotBlank(getKeyword())) {
            builder.append(" AND ( s.code LIKE CONCAT('%', ?, '%') OR s.name LIKE CONCAT('%', ?, '%')"
                                + " OR s.description LIKE CONCAT('%', ?, '%'))");
            sqlArgs.add(getKeyword());
            sqlArgs.add(getKeyword());
            sqlArgs.add(getKeyword());
        }
        if (!isDisableIncluded()) {
            sqlArgs.add(SubsystemService.STATES.DISABLED.getCode());
            builder.append(String.format(" and s.%s != ?", SubsystemService.getInstance().getFormStateField()));
        }
        return new BasicSqlStatement().setSql(builder.toString()).setValues(sqlArgs.toArray());
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
                .setSql(String.format(SQL_SELECT_FORM.concat(" %s ORDER BY s.code ASC LIMIT %s, %s"),
                        SubsystemService.getInstance().getFormTable(), query.getSql(), getOffset(), getLimit()));
    }
}
