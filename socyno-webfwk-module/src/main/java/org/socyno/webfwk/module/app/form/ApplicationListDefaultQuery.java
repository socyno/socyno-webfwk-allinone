package org.socyno.webfwk.module.app.form;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsApplicationType;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsCodeLevel;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsState;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsVcsType;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail.FieldOptionsYesOrNo;
import org.socyno.webfwk.module.release.build.FieldSysBuildService;
import org.socyno.webfwk.module.subsystem.FieldSubsystemAccessors;
import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.state.basic.AbstractStateFormQuery;
import org.socyno.webfwk.state.service.PermissionService;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.sql.AbstractSqlStatement;
import org.socyno.webfwk.util.sql.BasicSqlStatement;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.ConvertUtil;
import org.socyno.webfwk.util.tool.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain=true)
public class ApplicationListDefaultQuery extends AbstractStateFormQuery {
    
    @Attributes(title = "我的收藏", position = 1005)
    private boolean bookmarked;
    
    @Attributes(title = "名称", position = 1010)
    private String namelike;
    
    @Attributes(title = "包括已下线", position = 1020)
    private boolean offlineIncluded = false;
    
    @Attributes(title = "业务系统", position = 1025, type = FieldSubsystemAccessors.class)
    private Long subsystemId;
    
    @Attributes(title = "状态", position = 1030, type = FieldOptionsState.class)
    private String[] states;
    
    @Attributes(title = "应用类型", position = 1035, type = FieldOptionsApplicationType.class)
    private String type;
    
    @Attributes(title = "源码仓库类型", position = 1040, type = FieldOptionsVcsType.class)
    private String vcsType;
    
    @Attributes(title = "质量分级", position = 1050, type = FieldOptionsCodeLevel.class)
    private String codeLevel;
    
    @Attributes(title = "是否有状态", position = 1060, type = FieldOptionsYesOrNo.class)
    private String stateless;
    
    @Attributes(title = "构建服务", position = 1040, type = FieldSysBuildService.class)
    private String buildService;
    
    @Attributes(title = "代码仓库")
    private String vcsPathEquals;

    @Attributes(title = "应用编号清单")
    private String appIdsIn;

    @Attributes(title = "应用名称清单")
    private String appNamesIn;

    @Attributes(title = "业务系统编号清单")
    private String subsystemIdsIn;

    @Attributes(title = "根据名称正序")
    private boolean sortByNameAsc = false;
    
    
    public ApplicationListDefaultQuery() {
        super();
    }
    
    public ApplicationListDefaultQuery(Integer limit, Long page) {
        super(limit, page);
    }
    
    public boolean onlyMyVisibles() {
        return true;
    }
    
    /**
        SELECT DISTINCT
            a.*,
            CASE WHEN ISNULL(b.app_id) THEN 0 ELSE 1 END AS bookmarked,
            s.code   AS subsystemCode,
            s.name   AS subsystemName
        FROM
            %s a
        LEFT JOIN subsystem s ON s.id = a.subsystem_id
        LEFT JOIN application_bookmark b ON b.app_id = a.id AND b.user_id = ?
     **/
    @Multiline
    private final static String SQL_SELECT_FORM = "X";
    
    public AbstractSqlStatement buildWhereSql() throws Exception {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder builder = new StringBuilder("WHERE 1 = 1 ");
        /**
         * 默认情况下只显示当前的可见的应用清单。
         * 如在部分场景必须跳过该限制时，可使用 ApplicationListAllQuery, 请谨慎使用！！！
         * 
         */
        if (onlyMyVisibles()) {
            Long[] mySubsystems;
            if ((mySubsystems = PermissionService
                    .queryMySubsystemByAuthKey(ApplicationService.DEFAULT.getFormAccessEventKey())) != null) {
                builder.append(mySubsystems.length <= 0 ? " and 1 = 0"
                        : String.format(" and a.subsystem_id IN (%s)", StringUtils.join(mySubsystems, ',')));
            }
        }
        sqlargs.add(SessionContext.getUserId());
        if (getSubsystemId() != null) {
            builder.append(" and a.subsystem_id = ? ");
            sqlargs.add(getSubsystemId());
        }
        if (getStates() != null && getStates().length > 0) {
            sqlargs.addAll(Arrays.asList(getStates()));
            builder.append(String.format(" and a.%s IN (%s)", ApplicationService.DEFAULT.getFormStateField(),
                    CommonUtil.join("?", getStates().length, ",")));
        }
        if (!isOfflineIncluded()) {
            sqlargs.add(ApplicationService.STATES.OFFLINED.getCode());
            builder.append(String.format(" and a.%s != ?", ApplicationService.DEFAULT.getFormStateField()));
        }
        if (StringUtils.isNotBlank(getType())) {
            sqlargs.add(getType());
            builder.append(" and a.type = ?");
        }
        if (StringUtils.isNotBlank(getCodeLevel())) {
            sqlargs.add(getCodeLevel());
            builder.append(" and a.code_level = ?");
        }
        if (StringUtils.isNotBlank(getStateless())) {
            sqlargs.add(getStateless());
            builder.append(" and a.stateless = ?");
        }
        if (StringUtils.isNotBlank(getBuildService())) {
            sqlargs.add(getBuildService());
            builder.append(" and a.buildService = ?");
        }
        if (StringUtils.isNotBlank(getNamelike())) {
            sqlargs.add(getNamelike());
            builder.append(" and a.name LIKE CONCAT('%' , ?, '%') ");
        }
        if (StringUtils.isNotBlank(getVcsType())) {
            sqlargs.add(getVcsType());
            builder.append(" and a.vcs_type = ?");
        }
        if (isBookmarked()) {
            builder.append(" and b.app_id IS NOT NULL ");
        }
        if (StringUtils.isNotBlank(getVcsPathEquals())) {
            sqlargs.add(getVcsPathEquals());
            builder.append(" and a.vcs_path = ?");
        }
        if (StringUtils.isNotBlank(getAppIdsIn())) {
            Long[] appIds = ConvertUtil.asNonNullUniqueLongArray((Object[]) CommonUtil.split(getAppIdsIn(), "[,;\\s+]",
                    CommonUtil.STR_UNIQUE | CommonUtil.STR_NONBLANK | CommonUtil.STR_TRIMED));
            if (appIds == null || appIds.length <= 0) {
                builder.append(" and 1 = 0");
            } else {
                sqlargs.addAll(Arrays.asList(appIds));
                builder.append(" and a.id IN ").append(CommonUtil.join("?", appIds.length, ",", " (", ")"));
            }
        }
        if (StringUtils.isNotBlank(getAppNamesIn())) {
            String[] appNames = ConvertUtil.asNonBlankUniqueTrimedStringArray((Object[]) CommonUtil.split(getAppNamesIn(),
                    "[,;\\s+]", CommonUtil.STR_UNIQUE | CommonUtil.STR_NONBLANK | CommonUtil.STR_TRIMED));
            if (appNames == null || appNames.length <= 0) {
                builder.append(" and 1 = 0");
            } else {
                sqlargs.addAll(Arrays.asList(appNames));
                builder.append(" and a.name IN ").append(CommonUtil.join("?", appNames.length, ",", " (", ")"));
            }
        }
        if (StringUtils.isNotBlank(getSubsystemIdsIn())) {
            Long[] subIds = ConvertUtil.asNonNullUniqueLongArray((Object[]) CommonUtil.split(getSubsystemIdsIn(), "[,;\\s+]",
                    CommonUtil.STR_UNIQUE | CommonUtil.STR_NONBLANK | CommonUtil.STR_TRIMED));
            if (subIds == null || subIds.length <= 0) {
                builder.append(" and 1 = 0");
            } else {
                sqlargs.addAll(Arrays.asList(subIds));
                builder.append(" and a.subsystem_id IN ").append(CommonUtil.join("?", subIds.length, ",", " (", ")"));
            }
        }
        return new BasicSqlStatement().setSql(builder.toString()).setValues(sqlargs.toArray());
    }
    
    /**
        SELECT
            COUNT(1)
        FROM
            %s a
        LEFT JOIN subsystem s ON s.id = a.subsystem_id
        LEFT JOIN application_bookmark b ON b.app_id = a.id AND b.user_id = ?
     */
    @Multiline
    private static final String SQL_SELECT_COUNT = "X";
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()) .setSql(String.format(
                SQL_SELECT_COUNT + " %s" , ApplicationService.DEFAULT.getFormTable() , query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(
                SQL_SELECT_FORM + " %s ORDER BY %s LIMIT %s, %s",
                ApplicationService.DEFAULT.getFormTable(),
                query.getSql(),
                isSortByNameAsc() ? "a.name ASC" : "a.id DESC",
                 getOffset(), getLimit()));
    }
    
    @Override
    public <T extends AbstractStateForm> List<T> processResultSet(Class<T> itemClazz, List<T> resultSet) throws Exception {
        ApplicationService.DEFAULT.fillFormDetails(itemClazz, resultSet);
        return resultSet;
    }
}
