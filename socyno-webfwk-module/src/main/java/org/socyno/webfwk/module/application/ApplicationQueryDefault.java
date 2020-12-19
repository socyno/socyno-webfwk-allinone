package org.socyno.webfwk.module.application;

import com.github.reinert.jjschema.Attributes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.adrianwalker.multilinestring.Multiline;
import org.socyno.webfwk.module.application.ApplicationFormSimple.FieldOptionsApplicationType;
import org.socyno.webfwk.module.application.ApplicationFormSimple.FieldOptionsCodeLevel;
import org.socyno.webfwk.module.application.ApplicationFormSimple.FieldOptionsState;
import org.socyno.webfwk.module.application.ApplicationFormSimple.FieldOptionsVcsType;
import org.socyno.webfwk.module.release.build.FieldBuildService;
import org.socyno.webfwk.module.subsystem.FieldSubsystemAccessable;
import org.socyno.webfwk.modutil.SubsystemBasicUtil;
import org.socyno.webfwk.state.abs.AbstractStateFormQuery;
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
@Accessors(chain = true)
public class ApplicationQueryDefault extends AbstractStateFormQuery {
    
    @Attributes(title = "我的收藏")
    private boolean bookmarked;
    
    @Attributes(title = "名称")
    private String namelike;
    
    @Attributes(title = "包括已下线")
    private boolean offlineIncluded = false;
    
    @Attributes(title = "业务系统", type = FieldSubsystemAccessable.class)
    private Long subsystemId;
    
    @Attributes(title = "状态", type = FieldOptionsState.class)
    private String[] states;
    
    @Attributes(title = "应用类型", type = FieldOptionsApplicationType.class)
    private String type;
    
    @Attributes(title = "源码仓库类型", type = FieldOptionsVcsType.class)
    private String vcsType;
    
    @Attributes(title = "质量分级", type = FieldOptionsCodeLevel.class)
    private String codeLevel;
    
    @Attributes(title = "构建服务", type = FieldBuildService.class)
    private String buildService;
    
    @Attributes(title = "源码仓库地址")
    private String vcsPathEquals;
    
    @Attributes(title = "应用编号清单")
    private String appIdsIn;

    @Attributes(title = "应用名称清单")
    private String appNamesIn;

    @Attributes(title = "业务系统编号清单")
    private String subsystemIdsIn;

    @Attributes(title = "根据名称正序")
    private boolean sortByNameAsc = false;
    
    public ApplicationQueryDefault() {
        super();
    }
    
    public ApplicationQueryDefault(Integer limit, Long page) {
        super(limit, page);
    }
    
    public boolean onlyMyVisibles() {
        return true;
    }
    
    private long[] getMyAccesableSubsystemIds() throws Exception {
        return SubsystemBasicUtil.subsytemIdFromBusinessId(
                PermissionService.queryMyBusinessByAuthKey(ApplicationService.getInstance().getFormAccessEventKey()),
                true);
    }
    
    /**
        SELECT DISTINCT
            a.*,
            CASE WHEN ISNULL(b.app_id) THEN 0 ELSE 1 END AS bookmarked,
            s.code   AS subsystemCode,
            s.name   AS subsystemName
        FROM
            %s a
        LEFT JOIN subsystem s ON s.id = a.subsystem
        LEFT JOIN application_bookmark b ON b.app_id = a.id AND b.user_id = ?
     **/
    @Multiline
    private final static String SQL_SELECT_FORM = "X";
    
    public AbstractSqlStatement buildWhereSql() throws Exception {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        /**
         * 默认情况下只显示当前的可见的应用清单。
         * 如在部分场景必须跳过该限制时，可使用 ApplicationListAllQuery, 请谨慎使用！！！
         * 
         */
        long[] mySubsystems;
        if (onlyMyVisibles() && (mySubsystems = getMyAccesableSubsystemIds()) != null) {
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(
                    mySubsystems.length <= 0 ? "1 = 0"
                            : String.format("a.subsystem IN (%s)", StringUtils.join(mySubsystems, ','))
                        );
        }
        sqlargs.add(SessionContext.getUserId());
        if (getSubsystemId() != null) {
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.subsystem = ? ");
            sqlargs.add(getSubsystemId());
        }
        if (getStates() != null && getStates().length > 0) {
            sqlargs.addAll(Arrays.asList(getStates()));
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append(String.format("a.%s IN (%s)", ApplicationService.getInstance().getFormStateField(),
                            CommonUtil.join("?", getStates().length, ",")));
        }
        if (!isOfflineIncluded()) {
            sqlargs.add(ApplicationService.STATES.OFFLINED.getCode());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append(String.format("a.%s != ?", ApplicationService.getInstance().getFormStateField()));
        }
        if (StringUtils.isNotBlank(getType())) {
            sqlargs.add(getType());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.type = ?");
        }
        if (StringUtils.isNotBlank(getCodeLevel())) {
            sqlargs.add(getCodeLevel());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.code_level = ?");
        }
        if (StringUtils.isNotBlank(getBuildService())) {
            sqlargs.add(getBuildService());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.buildService = ?");
        }
        if (StringUtils.isNotBlank(getNamelike())) {
            sqlargs.add(getNamelike());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.name LIKE CONCAT('%' , ?, '%') ");
        }
        if (StringUtils.isNotBlank(getVcsType())) {
            sqlargs.add(getVcsType());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.vcs_type = ?");
        }
        if (isBookmarked()) {
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("b.app_id IS NOT NULL ");
        }
        if (StringUtils.isNotBlank(getVcsPathEquals())) {
            sqlargs.add(getVcsPathEquals());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.vcs_path = ?");
        }
        if (StringUtils.isNotBlank(getAppIdsIn())) {
            Long[] appIds = ConvertUtil.asNonNullUniqueLongArray((Object[]) CommonUtil.split(getAppIdsIn(), "[,;\\s+]",
                    CommonUtil.STR_UNIQUE | CommonUtil.STR_NONBLANK | CommonUtil.STR_TRIMED));
            if (appIds == null || appIds.length <= 0) {
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("1 = 0");
            } else {
                sqlargs.addAll(Arrays.asList(appIds));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.id IN ")
                        .append(CommonUtil.join("?", appIds.length, ",", " (", ")"));
            }
        }
        if (StringUtils.isNotBlank(getAppNamesIn())) {
            String[] appNames = ConvertUtil
                    .asNonBlankUniqueTrimedStringArray((Object[]) CommonUtil.split(getAppNamesIn(), "[,;\\s+]",
                            CommonUtil.STR_UNIQUE | CommonUtil.STR_NONBLANK | CommonUtil.STR_TRIMED));
            if (appNames == null || appNames.length <= 0) {
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("1 = 0");
            } else {
                sqlargs.addAll(Arrays.asList(appNames));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.name IN ")
                        .append(CommonUtil.join("?", appNames.length, ",", " (", ")"));
            }
        }
        if (StringUtils.isNotBlank(getSubsystemIdsIn())) {
            Long[] subIds = ConvertUtil.asNonNullUniqueLongArray((Object[]) CommonUtil.split(getSubsystemIdsIn(),
                    "[,;\\s+]", CommonUtil.STR_UNIQUE | CommonUtil.STR_NONBLANK | CommonUtil.STR_TRIMED));
            if (subIds == null || subIds.length <= 0) {
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("1 = 0");
            } else {
                sqlargs.addAll(Arrays.asList(subIds));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("a.subsystem IN ")
                        .append(CommonUtil.join("?", subIds.length, ",", " (", ")"));
            }
        }
        return new BasicSqlStatement().setSql(StringUtils.prependIfNotEmpty(sqlwhere, " WHERE ").toString())
                .setValues(sqlargs.toArray());
    }
    
    /**
        SELECT
            COUNT(1)
        FROM
            %s a
        LEFT JOIN subsystem s ON s.id = a.subsystem
        LEFT JOIN application_bookmark b ON b.app_id = a.id AND b.user_id = ?
     */
    @Multiline
    private static final String SQL_SELECT_COUNT = "X";
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()) .setSql(String.format(
                SQL_SELECT_COUNT.concat(" %s") , ApplicationService.getInstance().getFormTable() , query.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() throws Exception {
        AbstractSqlStatement query = buildWhereSql();
        return new BasicSqlStatement().setValues(query.getValues()).setSql(String.format(
                SQL_SELECT_FORM.concat(" %s ORDER BY %s LIMIT %s, %s"),
                ApplicationService.getInstance().getFormTable(),
                query.getSql(),
                isSortByNameAsc() ? "a.name ASC" : "a.id DESC",
                 getOffset(), getLimit()));
    }
}
