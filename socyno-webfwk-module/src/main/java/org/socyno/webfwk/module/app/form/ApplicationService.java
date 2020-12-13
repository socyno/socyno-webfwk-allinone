package org.socyno.webfwk.module.app.form;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;
import org.socyno.webfwk.module.app.form.ApplicationFormDetail.ApplicationType;
import org.socyno.webfwk.module.app.form.FieldApplicationNamespace.OptionApplicationNamespace;
import org.socyno.webfwk.module.deploy.environment.FieldDeployEnvironment;
import org.socyno.webfwk.module.deploy.environment.FieldDeployEnvironment.OptionDeployEnvironment;
import org.socyno.webfwk.module.subsystem.FieldSubsystemAccessors;
import org.socyno.webfwk.module.subsystem.SubsystemFormSimple;
import org.socyno.webfwk.module.systenant.AbstractSystemTenant;
import org.socyno.webfwk.module.systenant.SystemTenantService;
import org.socyno.webfwk.module.vcs.change.VcsRefsNameOperation.RefsOpType;
import org.socyno.webfwk.module.vcs.common.VcsPermissionChecker;
import org.socyno.webfwk.module.vcs.common.VcsRefsType;
import org.socyno.webfwk.module.vcs.common.VcsType;
import org.socyno.webfwk.module.vcs.common.VcsUnifiedAppInitInfo;
import org.socyno.webfwk.module.vcs.common.VcsUnifiedAppRepoEntity;
import org.socyno.webfwk.module.vcs.common.VcsUnifiedService;
import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeIdParser;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialRejecter;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.sugger.DefaultStateFormSugger;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.exception.PageNotFoundException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.model.PagedList;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;

public class ApplicationService extends
        AbstractStateFormServiceWithBaseDao<ApplicationFormDetail, ApplicationFormDefault, ApplicationFormSimple> {
    
    @Getter
    private static final ApplicationService Instance = new ApplicationService();
    
    private ApplicationService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    static {
        DefaultStateFormSugger.addFieldDefinitions(SuggerDefinitionApplication.getInstance());
        DefaultStateFormSugger.addFieldDefinitions(SuggerDefinitionApplicationName.getInstance());
    }
    
    
    @Override
    public String getFormName() {
        return "application";
    }
    
    @Override
    protected String getFormTable() {
        return "application";
    }
    
    @Override
    public String getFormDisplay() {
        return "应用清单";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        CREATED("created", "待上线"), 
        ONLINE("online", "在线中"), 
        OFFLINING("offlining", "待下线"), 
        OFFLINED("offlined", "已下线");
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    @Getter
    public enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<ApplicationFormDefault>("default", ApplicationFormDefault.class,
                ApplicationQueryDefault.class)),
        /**
         * 默认情况下，为确保授权机制的有效性，请尽量使用 DEFAULT 查询，只显示当前的可见的应用清单。
         * 如在部分场景必须跳过该限制时，可使用该查询设置, 请谨慎使用！！！
         * 
         */
        ALLAPPS(new StateFormNamedQuery<ApplicationFormDefault>("allapps", ApplicationFormDefault.class,
                ApplicationQueryAll.class)),
        
        ALLSIMPLES(new StateFormNamedQuery<ApplicationFormDefault>("allsimples", ApplicationFormDefault.class,
                ApplicationQueryAll.class));
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    public VcsUnifiedAppInitInfo getApplicationInitInfo(Long applicatoinId) throws Exception {
        if (applicatoinId == null) {
            return null;
        }
        ApplicationFormWithSubsystem form = get(ApplicationFormWithSubsystem.class, applicatoinId);
        SubsystemFormSimple subsystem;
        if ((subsystem = form.getSubsystem()) == null) {
            throw new MessageException("应用的业务系统未设置");
        }
        AbstractSystemTenant tenant;
        if ((tenant = SystemTenantService.getInstance().getSimple(SessionContext.getTenant())) == null
                || StringUtils.isBlank(tenant.getCodeNamespace())) {
            throw new MessageException("获取租户代码空间未设置");
        }
        return new VcsUnifiedAppInitInfo(tenant.getCodeNamespace(), subsystem.getCode()).setName(form.getName())
                .setDescription(form.getDescription()).setVcsPath(form.getVcsPath()).setVcsType(form.getVcsType());
    }
    
    public class EventCreate extends AbstractStateSubmitAction<ApplicationFormDetail, ApplicationFormCreation> {
        
        public EventCreate() {
            super("添加", STATES.CREATED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class)
        public void check(String event, ApplicationFormDetail form, String sourceState) {

        }
        
        @Override
        public Long handle(String event, ApplicationFormDetail originForm, final ApplicationFormCreation form,
                final String message) throws Exception {
            ApplicationBasicUtil.ensuerNameFormatValid(form.getName());
            final AtomicLong id = new AtomicLong(0);
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet result, Connection conn) throws Exception {
                    getFormBaseDao().executeUpdate(
                            SqlQueryUtil.prepareInsertQuery(
                                    getFormTable(),
                                    new ObjectMap().put("name", form.getName()).put("type", form.getType())
                                            .put("vcs_type", form.getVcsType()).put("vcs_path", "")
                                            .put("stateless", form.getStateless())
                                            .put("description", form.getDescription())
                                            .put("code_level", form.getCodeLevel())
                                            .put("subsystem_id", form.getSubsystemId())
                                            .put("created_user_code", SessionContext.getUsername())
                                            .put("created_user_name", SessionContext.getDisplay())),
                            new ResultSetProcessor() {
                                @Override
                                public void process(ResultSet result, Connection conn) throws Exception {
                                    result.next();
                                    id.set(result.getLong(1));
                                }
                            });
                    VcsUnifiedAppRepoEntity repo = VcsUnifiedService.CommonCloud.createOrResetAppRepo(id.get());
                    getFormBaseDao().executeUpdate(
                            SqlQueryUtil.prepareUpdateQuery(
                                    getFormTable(),
                                    new ObjectMap()
                                            .put("=id", id.get())
                                            .put("vcs_path", repo.getPathToRepo())
                                            .put("release_branch",
                                                    VcsType.forName(form.getVcsType()).getMasterName())));
                }
            });
            return id.get();
        }
    }
    
    public class EventEdit extends AbstractStateAction<ApplicationFormDetail, ApplicationFormForEdit, Void> {
        
        public EventEdit() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class)
        public void check(String event, ApplicationFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, ApplicationFormDetail originForm, final ApplicationFormForEdit form,
                final String message) throws Exception {
            getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                @Override
                public void process(ResultSet resultSet, Connection connection) throws Exception {
                    getFormBaseDao().executeUpdate(
                            SqlQueryUtil.prepareUpdateQuery(
                                    getFormTable(),
                                    new ObjectMap().put("=id", form.getId()).put("stateless", form.getStateless())
                                            .put("description", form.getDescription())
                                            .put("code_level", form.getCodeLevel())
                                            .put("build_service", form.getBuildService())));
                }
            });
            return null;
        }
    }
    
    public class EventUpgradeVersion
            extends AbstractStateAction<ApplicationFormDetail, ApplicationFormUpgradeVersion, Void> {
        
        public EventUpgradeVersion() {
            super("版本升级", getStateCodes(STATES.CREATED, STATES.ONLINE), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class)
        public void check(String event, ApplicationFormDetail form, String sourceState) {
        }

        @Override
        public Void handle(String event, ApplicationFormDetail originForm, ApplicationFormUpgradeVersion form,
                String message) throws Exception {
            long version = checkVersion(originForm.getBuildMainVersion(), form.getBuildVersion());
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareUpdateQuery(getFormTable(), new ObjectMap()
                            .put("=id", form.getId()).put("build_main_version", version)));
            return null;
        }
    }
    
    public class EventToNormalRelease
            extends AbstractStateAction<ApplicationFormDetail, ApplicationFormMasterRelease, Void> {
        
        public EventToNormalRelease() {
            super("切换为常规发布", getStateCodes(STATES.ONLINE), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, rejecter = ToMasterReleaseRejecter.class, parser = AppSubsystemParser.class)
        public void check(String event, ApplicationFormDetail originForm, String sourceState) {
        }

        @Override
        public Void handle(String event, ApplicationFormDetail originForm, ApplicationFormMasterRelease form,
                String message) throws Exception {
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareUpdateQuery(
                            getFormTable(),
                            new ObjectMap().put("=id", form.getId()).put("release_branch",
                                    VcsType.forName(originForm.getVcsType()).getMasterName())));
            return null;
        }
    }
    
    public class EventToBranchRelease
            extends AbstractStateAction<ApplicationFormDetail, ApplicationFormBranchRelease, Void> {
        
        public EventToBranchRelease() {
            super("切换为分支发布", getStateCodes(STATES.ONLINE), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, rejecter = ToBranchReleaseRejecter.class, parser = AppSubsystemParser.class)
        public void check(String event, ApplicationFormDetail originForm, String sourceState) {
        
        }
        
        @Override
        public Void handle(String event, ApplicationFormDetail originForm, ApplicationFormBranchRelease form,
                String message) throws Exception {
            if (!StringUtils.startsWith(form.getReleaseBranch(), VcsType.forName(originForm.getVcsType())
                    .getPatchesPrefix())) {
                throw new MessageException("代码分支路径不符合规范，请修改路径后重新提交！");
            }
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareUpdateQuery(getFormTable(), new ObjectMap()
                            .put("=id", form.getId()).put("release_branch", form.getReleaseBranch())));
            return null;
        }
    }
    
    public class EventResetVcsPermission extends AbstractStateAction<ApplicationFormDetail, BasicStateForm, Void> {
        
        public EventResetVcsPermission() {
            super("重置仓库授权组", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class)
        public void check(String event, ApplicationFormDetail originForm, String sourceState) {
        }

        @Override
        public Void handle(String event, ApplicationFormDetail originForm, BasicStateForm form, String message)
                throws Exception {
            VcsUnifiedService.CommonCloud.createOrResetAppRepo(originForm.getId());
            return null;
        }
    }
    
    public class EventVcsBranchCreate
            extends AbstractStateAction<ApplicationFormDetail, ApplicationFormVcsRefCreate, Void> {
        
        public EventVcsBranchCreate() {
            super("新增分支", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppBranchCreateRejector.class)
        public void check(String event, ApplicationFormDetail originForm, String sourceState) {
        }
        
        @Override
        public Void handle(String event, ApplicationFormDetail originForm, ApplicationFormVcsRefCreate form,
                String message) throws Exception {
            VcsUnifiedService.CommonCloud.createBranch(form.getId(), form.getVcsRefsName(), form.getRefOrCommit(), message);
            return null;
        }
        
        @Override
        public boolean getStateRevisionChangeIgnored() {
            return true;
        }
    }
    
    public class EventVcsPatchCreate
            extends AbstractStateAction<ApplicationFormDetail, ApplicationFormVcsRefCreate, Void> {
        
        public EventVcsPatchCreate() {
            super("新增补丁", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppPatchCreateRejector.class)
        public void check(String event, ApplicationFormDetail originForm, String sourceState) {
        }
        
        @Override
        public Void handle(String event, ApplicationFormDetail originForm, ApplicationFormVcsRefCreate form,
                String message) throws Exception {
            VcsUnifiedService.CommonCloud.createPatch(form.getId(), form.getVcsRefsName(), form.getRefOrCommit(),
                    message);
            return null;
        }
        
        @Override
        public boolean getStateRevisionChangeIgnored() {
            return true;
        }
    }
    
    public class EventVcsTagCreate
            extends AbstractStateAction<ApplicationFormDetail, ApplicationFormVcsRefCreate, Void> {
        
        public EventVcsTagCreate() {
            super("新增标签", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppTagCreateRejector.class)
        public void check(String event, ApplicationFormDetail originForm, String sourceState) {
        }
        
        @Override
        public Void handle(String event, ApplicationFormDetail originForm, ApplicationFormVcsRefCreate form,
                String message) throws Exception {
            VcsUnifiedService.CommonCloud.createTag(form.getId(), form.getVcsRefsName(), form.getRefOrCommit(),
                    message);
            return null;
        }
        
        @Override
        public boolean getStateRevisionChangeIgnored() {
            return true;
        }
    }
    
    public class EventVcsBranchDelete extends AbstractStateAction<ApplicationFormDetail, ApplicationFormVcsRefDelete, Void> {
        
        public EventVcsBranchDelete() {
            super("删除分支", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppBranchDeleteRejector.class)
        public void check(String event, ApplicationFormDetail originForm, String sourceState) {
        }
        
        @Override
        public Void handle(String event, ApplicationFormDetail originForm, ApplicationFormVcsRefDelete form,
                String message) throws Exception {
            VcsUnifiedService.CommonCloud.deleteBranch(form.getId(), form.getVcsRefsName(), message);
            return null;
        }
        
        @Override
        public boolean getStateRevisionChangeIgnored() {
            return true;
        }
    }
    
    public class EventPatchDelete extends AbstractStateAction<ApplicationFormDetail, ApplicationFormVcsRefDelete, Void> {
        
        public EventPatchDelete() {
            super("删除补丁", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppPatchDeleteRejector.class)
        public void check(String event, ApplicationFormDetail originForm, String sourceState) {
        }
        
        @Override
        public Void handle(String event, ApplicationFormDetail originForm, ApplicationFormVcsRefDelete form,
                String message) throws Exception {
            VcsUnifiedService.CommonCloud.deletePatch(form.getId(), form.getVcsRefsName(), message);
            return null;
        }
        
        @Override
        public boolean getStateRevisionChangeIgnored() {
            return true;
        }
    }
    
    public class EventTagDelete extends AbstractStateAction<ApplicationFormDetail, ApplicationFormVcsRefDelete, Void> {
        
        public EventTagDelete() {
            super("删除标签", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppTagDeleteRejector.class)
        public void check(String event, ApplicationFormDetail originForm, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, ApplicationFormDetail originForm, ApplicationFormVcsRefDelete form,
                String message) throws Exception {
            VcsUnifiedService.CommonCloud.deleteTag(form.getId(), form.getVcsRefsName(), message);
            return null;
        }
        
        @Override
        public boolean getStateRevisionChangeIgnored() {
            return true;
        }
    }
    
    public class EventListDeployNamespaces extends AbstractStateAction<ApplicationFormDetail, BasicStateForm, ApplicationDeployNamespacesView> {
        
        public EventListDeployNamespaces() {
            super("部署机组清单", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppNonApplicationRejector.class)
        public void check(String s, ApplicationFormDetail s1, String s2) {
            
        }
        
        @Override
        public ApplicationDeployNamespacesView handle(String event, ApplicationFormDetail originForm,
                BasicStateForm form, String message) throws Exception {
            ApplicationDeployNamespacesView view = new ApplicationDeployNamespacesView();
            view.setDeployNamespaces(get(ApplicationFormDeployNamespaces.class, form.getId()).getDeployNamespaces());
            return view;
        }
        
        @Override
        public boolean getStateRevisionChangeIgnored() {
            return true;
        }
        
        @Override
        public Boolean messageRequired() {
            return null;
        }
        
        @Override
        public boolean confirmRequired() {
            return false;
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class),
        Edit(EventEdit.class),
        UpgradeVersion(EventUpgradeVersion.class),
        ToNormalRelease(EventToNormalRelease.class),

        ToBranchRelease(EventToBranchRelease.class),

        ResetVcsPermGroups(EventResetVcsPermission.class),
        /**
         * 创建新的分支
         */
        VcsBranchCreate(EventVcsBranchCreate.class),
        /**
         * 创建新的补丁
         */
        VcsPatchCreate(EventVcsPatchCreate.class),
        /**
         * 创建新的标签
         */
        VcsTagCreate(EventVcsTagCreate.class),
        /**
         * 删除已有的分支
         */
        VcsBranchDelete(EventVcsBranchDelete.class),
        /**
         * 删除已有的补丁
         */
        VcsPatchDelete(EventPatchDelete.class),
        /**
         * 删除已有的标签
         */
        VcsTagDelete(EventTagDelete.class),
        /**
         * 查看部署机组信息
         */
        ListDeployNamespaces(EventListDeployNamespaces.class)
        ;
        
        private final Class<? extends AbstractStateAction<ApplicationFormDetail, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<ApplicationFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    public ApplicationFormDefault getByVcsPath(String vcsPath) throws Exception {
        if (StringUtils.isBlank(vcsPath)) {
            return null;
        }
        ApplicationQueryDefault applicationQuery = new ApplicationQueryDefault();
        applicationQuery.setVcsPathEquals(vcsPath);
        applicationQuery.setLimit(1);
        PagedList<?> apps = listForm(QUERIES.DEFAULT, applicationQuery);
        if (apps == null || apps.getList() == null || apps.getList().size() != 1) {
            return null;
        }
        return (ApplicationFormDefault) apps.getList().get(0);
    }
    
    private List<ApplicationRuntimeStatusNodeItem> getRuntimeStatus(String environment, final Long appId,
            final String appName) throws Exception {
        List<ApplicationRuntimeStatusNodeItem> result = new ArrayList<>();
        
        return result;
    }
    
    public class AppSubsystemParser implements AuthorityScopeIdParser {
        @Override
        public Long getAuthorityScopeId(Object originForm) {
            ApplicationAbstractForm manageForm = (ApplicationAbstractForm) originForm;
            if (manageForm == null) {
                return null;
            }
            return manageForm.getSubsystemId();
        }
    }
    
    public class AppBranchCreateRejector implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !VcsPermissionChecker.NOCACHED.check(RefsOpType.Create, VcsRefsType.Branch,
                    ((AbstractStateForm) originForm).getId());
        }
        
    }
    
    public class AppBranchDeleteRejector implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !VcsPermissionChecker.NOCACHED.check(RefsOpType.Delete, VcsRefsType.Branch,
                    ((AbstractStateForm) originForm).getId());
        }
    }
    
    public class AppPatchCreateRejector implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !VcsPermissionChecker.NOCACHED.check(RefsOpType.Create, VcsRefsType.Patch,
                    ((AbstractStateForm) originForm).getId());
        }
        
    }
    
    public class AppPatchDeleteRejector implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !VcsPermissionChecker.NOCACHED.check(RefsOpType.Delete, VcsRefsType.Patch,
                    ((AbstractStateForm) originForm).getId());
        }
    }
    
    public class AppTagCreateRejector implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !VcsPermissionChecker.NOCACHED.check(RefsOpType.Create, VcsRefsType.Tag,
                    ((AbstractStateForm) originForm).getId());
        }
        
    }
    
    public class AppTagDeleteRejector implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !VcsPermissionChecker.NOCACHED.check(RefsOpType.Delete, VcsRefsType.Tag,
                    ((AbstractStateForm) originForm).getId());
        }
    }
    
    public class AppNonApplicationRejector implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !ApplicationType.APPLICATION.getValue().equals(((ApplicationAbstractForm) originForm).getType());
        }
        
    }
    
    /**
     * 当正好为主干发布模式时，不再允许执行切换主干模式的操作
     *
     */
    public class ToMasterReleaseRejecter implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) {
            return originForm == null || ((ApplicationFormDetail) originForm).isMasterReleaseMode();
        }
        
    }

    /**
     * 当正好为分支发布模式时，不再允许执行切换分支模式的操作
     *
     */
    public class ToBranchReleaseRejecter implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) {
            return originForm == null || ((ApplicationFormDetail) originForm).isPatchReleaseMode();
        }
        
    }

    private long checkVersion(String oldBuildVersion, String newBuildVersoin) throws Exception {
        Pattern reg = Pattern.compile("^([1-9][0-9]*)\\.([0-9]+)\\.([0-9]+)$");
        Matcher matched;
        if (StringUtils.isBlank(newBuildVersoin) || (matched = reg.matcher(newBuildVersoin)) == null || !matched.find()
                || Long.valueOf(matched.group(1)) > 9999
                || (matched.group(2).length() > 1 && "0".equals(matched.group(2).substring(0, 1)))
                || Long.valueOf(matched.group(2)) > 9999
                || (matched.group(3).length() > 1 && "0".equals(matched.group(3).substring(0, 1)))
                || Long.valueOf(matched.group(3)) > 9999) {
            throw new MessageException(String.format("非法的版本号字符串，版本参考格式（2.1.0），三位且纯数字): %s",
                    CommonUtil.ifNull(newBuildVersoin, "")));
        }
        long newVersion = StringUtils.version2Number(newBuildVersoin);
        if (CommonUtil.parseLong(oldBuildVersion, 0L) > newVersion) {
            throw new MessageException("应用要升级的版本必须比当前的版本更大");
        }
        return newVersion;
    }
    
    /**
     * SELECT
     *      env_name env,
     *      env_display display,
     *      GROUP_CONCAT(DISTINCT version) AS versions
     * FROM
     *      application_runtime_status
     * WHERE
     *      app_id = ?
     * GROUP BY env_name
     */
    @Multiline
    private final static String SQL_QUERY_APP_RUNTIME_VERSIONS = "X";
    
    /**
     * SELECT
     *     env_name,
     *     MIN(last_updated) AS last_updated
     * FROM
     *     application_runtime_status
     * WHERE
     *     app_id = ?
     * GROUP by env_name
     */
    @Multiline
    private final static String SQL_QUERY_APP_LAST_UPDATED_ENVSTATUS = "X";
    
    /**
     * DELETE FROM
     *     application_runtime_status
     * WHERE
     *     app_id = ?
     */
    @Multiline
    private final static String SQL_CLEAN_APP_LAST_UPDATED_ENVSTATUS = "X";
    
    @Data
    public static class AppEnvStatusLastUpdated {
        
        private String envName;
        
        private Date lastUpdated;
        
    }
    
    private String[] getCachedUnExpiredAppStatusEnvs(List<AppEnvStatusLastUpdated> items) {
        if (items == null || items.size() <= 0) {
            return new String[0];
        }
        List<String> exclussions = new ArrayList<>();
        long expiredMS = CommonUtil.parseLong(ContextUtil.getConfigTrimed("system.appenv.runtime.status.expired.ms"),
                6000000L);
        for (AppEnvStatusLastUpdated last : items) {
            long lastUpdated = last.getLastUpdated().getTime();
            if ((lastUpdated + expiredMS) > new Date().getTime()) {
                exclussions.add(last.getEnvName());
            }
        }
        return exclussions.toArray(new String[0]);
    }
    
    /**
     * 获取应用的环境版本信息，为提升效率，该数据做了一定时间的本地缓存
     */
    public List<ApplicationRuntimeEvnVersions> getCachedApplicationVersion(long applicationId) throws Exception {
        
        List<AppEnvStatusLastUpdated> envsLastUpdated = getFormBaseDao().queryAsList(
                AppEnvStatusLastUpdated.class, SQL_QUERY_APP_LAST_UPDATED_ENVSTATUS, new Object[] { applicationId });
        String[] unexpired = getCachedUnExpiredAppStatusEnvs(envsLastUpdated);
        
        List<Object> cleanArgs = new ArrayList<>();
        List<OptionDeployEnvironment> appEnvOptions = ClassUtil.getSingltonInstance(FieldDeployEnvironment.class)
                .queryDynamicOptionsByAppId(applicationId);
        for (OptionDeployEnvironment openv : appEnvOptions) {
            cleanArgs.add(openv.getName());
            if (ArrayUtils.contains(unexpired, openv.getName())) {
                continue;
            }
            getRuntimeStatus(openv.getOptionValue(), applicationId, "");
        }
        String cleanSql = SQL_CLEAN_APP_LAST_UPDATED_ENVSTATUS;
        if (cleanArgs.size() > 0) {
            cleanSql = String.format("%s AND env_name NOT IN (%s)", cleanSql,
                    CommonUtil.join("?", cleanArgs.size(), ","));
        }
        cleanArgs.add(0, applicationId);
        getFormBaseDao().executeUpdate(cleanSql, cleanArgs.toArray());
        
        return getFormBaseDao().queryAsList(ApplicationRuntimeEvnVersions.class, SQL_QUERY_APP_RUNTIME_VERSIONS,
                new Object[] { applicationId });
    }
    
    public <T extends ApplicationFormSimple> PagedList<T> list(@NonNull Class<T> clazz,
            @NonNull ApplicationQueryDefault query) throws Exception {
        return listForm(clazz, query);
    }
    
    public <T extends ApplicationFormSimple> T get(Class<T> clazz, long id) throws Exception {
        List<T> list;
        if ((list = list(clazz, new ApplicationQueryAll(2, 1L).setAppIdsIn(id + "")).getList()) == null
                || list.size() != 1) {
            throw new PageNotFoundException();
        }
        return list.get(0);
    }

    public <T extends ApplicationFormSimple> T get(Class<T> clazz, String appName) throws Exception {
        List<T> list;
        if ((list = list(clazz, new ApplicationQueryAll().setAppNamesIn(appName + "")).getList()) == null
                || list.size() != 1) {
            throw new PageNotFoundException();
        }
        return list.get(0);
    }
    
    public ApplicationAbstractForm getSimple(long id) throws Exception {
        return get(ApplicationFormSimple.class, id);
    }
    
    @Override
    public ApplicationFormDetail getForm(long id) throws Exception {
        return get(ApplicationFormDetail.class, id);
    }
    
    <T extends AbstractStateForm> void fillFormDetails(@NonNull Class<T> itemClazz, List<T> resultSet) throws Exception {
        if (resultSet == null || resultSet.isEmpty()) {
            return;
        }
        Long subsystemId;
        Long applicationId;
        Map<Long, List<ApplicationAbstractForm>> idApps = new HashMap<>();
        Map<Long, List<ApplicationAbstractForm>> subApps = new HashMap<>();
        for (T r : resultSet) {
            if (r == null) {
                continue;
            }
            if ((subsystemId = ((ApplicationAbstractForm) r).getSubsystemId()) != null) {
                if (!subApps.containsKey(subsystemId)) {
                    subApps.put(subsystemId, new ArrayList<>());
                }
                subApps.get(subsystemId).add((ApplicationAbstractForm) r);
            }
            if ((applicationId = ((ApplicationAbstractForm) r).getId()) != null) {
                if (!idApps.containsKey(applicationId)) {
                    idApps.put(applicationId, new ArrayList<>());
                }
                idApps.get(applicationId).add((ApplicationAbstractForm) r);
            }
        }
        /**
         * 补全业务系统实体
         */
        if (ApplicationWithSubsystemEntity.class.isAssignableFrom(itemClazz) && subApps.size() > 0) {
            for (SubsystemFormSimple subsystem : ClassUtil.getSingltonInstance(FieldSubsystemAccessors.class)
                    .queryDynamicValues(subApps.keySet().toArray(new Long[0]))) {
                for (ApplicationAbstractForm app : subApps.get(subsystem.getId())) {
                    ((ApplicationWithSubsystemEntity) app).setSubsystem(subsystem);
                }
            }
        }
        /**
         * 补全部署命名空间清单
         */
        if (ApplicationWithNamespaces.class.isAssignableFrom(itemClazz) && idApps.size() > 0) {
            
            List<OptionApplicationNamespace> namespaceList = FieldApplicationNamespace
                    .queryByApplications(idApps.keySet().toArray(new Long[0]));
            for (OptionApplicationNamespace oan : namespaceList) {
                List<OptionApplicationNamespace> deployNamespaces;
                for (ApplicationAbstractForm app : idApps.get(oan.getApplicationId())) {
                    if ((deployNamespaces = ((ApplicationWithNamespaces) app).getDeployNamespaces()) == null) {
                        ((ApplicationWithNamespaces) app).setDeployNamespaces(deployNamespaces = new ArrayList<>());
                    }
                    deployNamespaces.add(oan);
                }
            }
        }
        
        if (ApplicationWithNamespaceSummaries.class.isAssignableFrom(itemClazz)) {
            List<OptionApplicationNamespace> optionNamespaces = FieldApplicationNamespace
                    .queryByApplications(idApps.keySet().toArray(new Long[0]));
            Map<Long, List<OptionApplicationNamespace>> applicationNamespaces = optionNamespaces.stream()
                    .collect(Collectors.groupingBy(OptionApplicationNamespace::getApplicationId));
            for (T t : resultSet) {
                ((ApplicationWithNamespaceSummaries) t).setDeployNamespaceSummaries(DeployEnvNamespaceSummaryDetail
                        .toSimple(genApplicationNamespaceSummary(applicationNamespaces.get(t.getId()))));
            }
        }
        
    }
    
    public String getCodeAccessFormEventKey() {
        return getFormEventKey(getCodeAccessEventKey());
    }
    
    public String getCodeTagFormEventKey() {
        return getFormEventKey(getCodeTagEventKey());
    }
    
    public String getCodePatchFormEventKey() {
        return getFormEventKey(getCodePatchEventKey());
    }
    
    public String getCodePushFormEventKey() {
        return getFormEventKey(getCodePushEventKey());
    }
    
    public String getCodeMaintainerFormEventKey() {
        return getFormEventKey(getCodeMaintainerEventKey());
    }
    
    private String getCodeAccessEventKey() {
        return "_code_repo_access";
    }
    
    private String getCodePushEventKey() {
        return "_code_repo_push";
    }
    
    private String getCodeTagEventKey() {
        return "_code_repo_tag";
    }
    
    private String getCodePatchEventKey() {
        return "_code_repo_patch";
    }
    
    private String getCodeMaintainerEventKey() {
        return "_code_repo_maintainer";
    }
    
    @SuppressWarnings("serial")
    @Override
    public Map<String, String> getFormPredefinedExtraEvents() {
        Map<String, String> events = super.getFormPredefinedExtraEvents();
        events.putAll(new HashMap<String, String>() {
            {
                put(getCodeAccessEventKey(), "代码查看及下载");
                put(getCodePushEventKey(), "代码变更推送");
                put(getCodeMaintainerEventKey(), "代码仓库的维护");
                put(getCodePatchEventKey(), "代码仓库补丁管理");
                put(getCodeTagEventKey(), "代码仓库管理标签");
            }
        });
        return events;
    }
    
    public static List<DeployEnvNamespaceSummaryDetail> genApplicationNamespaceSummary(
            List<OptionApplicationNamespace> namespaces) throws Exception {
        if (namespaces == null || namespaces.isEmpty()) {
            return null;
        }
        /**
         * 按照应用到环境进行分组
         */
        Map<Long, Map<String, List<OptionApplicationNamespace>>> mappedAppEnvNamespaces = new HashMap<>();
        for (OptionApplicationNamespace ns : namespaces) {
            long appId = ns.getApplicationId();
            String envName = ns.getEnvironment();
            
            Map<String, List<OptionApplicationNamespace>> appAllEnvNamespaces;
            if ((appAllEnvNamespaces = mappedAppEnvNamespaces.get(appId)) == null) {
                mappedAppEnvNamespaces.put(appId, appAllEnvNamespaces = new HashMap<>());
            }
            List<OptionApplicationNamespace> appOneEnvNamespaces;
            if ((appOneEnvNamespaces = appAllEnvNamespaces.get(envName)) == null) {
                appAllEnvNamespaces.put(envName, appOneEnvNamespaces = new ArrayList<>());
            }
            appOneEnvNamespaces.add(ns);
        }
        
        /**
         * 将归组的结果转换成部署机组的概要信息
         */
        List<DeployEnvNamespaceSummaryDetail> deployNamespaceSummary = new ArrayList<>();
        for (Map<String, List<OptionApplicationNamespace>> appEntry : mappedAppEnvNamespaces.values()) {
            for (List<OptionApplicationNamespace> evnNs : appEntry.values()) {
                if (evnNs == null || evnNs.isEmpty()) {
                    continue;
                }
                DeployEnvNamespaceSummaryDetail appOneEnvSummary = new DeployEnvNamespaceSummaryDetail();
                for (OptionApplicationNamespace ns : evnNs) {
                    appOneEnvSummary.setTargetScopeId(ns.getApplicationId());
                    appOneEnvSummary.setEnvName(ns.getEnvironment());
                    appOneEnvSummary.setEnvDisplay(ns.getEnvDisplay());
                    appOneEnvSummary.addReplicas(ns.getReplicas());
                    appOneEnvSummary.addAppId(ns.getApplicationId());
                    appOneEnvSummary.addClusterInfo(ns.getClusterType(), ns.getClusterName());
                    appOneEnvSummary.addNamespaceInfo(ns.getNamespaceId(), ns.getNamespaceName());
                }
                deployNamespaceSummary.add(appOneEnvSummary);
            }
        }
        return deployNamespaceSummary;
    }

    @Override
    protected void fillExtraFormFields(Collection<? extends ApplicationFormSimple> forms) throws Exception {
        // TODO Auto-generated method stub
        
    }
}
