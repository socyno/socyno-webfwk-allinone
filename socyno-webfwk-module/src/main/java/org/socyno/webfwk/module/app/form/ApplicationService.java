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
import org.socyno.webfwk.module.subsystem.SubsystemBasicForm;
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
import org.socyno.webfwk.state.util.StateFormEventBaseEnum;
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

import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;

public class ApplicationService extends AbstractStateFormServiceWithBaseDao<ApplicationFormDetail> {
    
    public static final ApplicationService DEFAULT = new ApplicationService();
    
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
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    protected Map<String, AbstractStateAction<ApplicationFormDetail, ?, ?>> getFormActions() {
        Map<String, AbstractStateAction<ApplicationFormDetail, ?, ?>> actions = new HashMap<>();
        for (EVENTS event : EVENTS.values()) {
            actions.put(event.getName(), event.getAction());
        }
        return actions;
    }
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        CREATED("created", "待上线"), ONLINE("online", "在线中"), OFFLINING("offlining", "待下线"), OFFLINED("offlined", "已下线");
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
        
        public static String[] stringify(STATES... states) {
            if (states == null || states.length <= 0) {
                return new String[0];
            }
            String[] result = new String[states.length];
            for (int i = 0; i < states.length; i++) {
                result[i] = states[i].getCode();
            }
            return result;
        }
        
        public static String[] stringifyEx(STATES... states) {
            if (states == null) {
                states = new STATES[0];
            }
            List<String> result = new ArrayList<>(states.length);
            for (STATES s : STATES.values()) {
                if (!ArrayUtils.contains(states, s)) {
                    result.add(s.getCode());
                }
            }
            return result.toArray(new String[0]);
        }
        
        public static List<? extends FieldOption> getStatesAsOption() {
            List<FieldOption> options = new ArrayList<>();
            for (STATES s : STATES.values()) {
                options.add(new FieldSimpleOption(s.getCode(), s.getName()));
            }
            return options;
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<ApplicationListDefaultForm>("default", ApplicationListDefaultForm.class,
                ApplicationListDefaultQuery.class)),
        /**
         * 默认情况下，为确保授权机制的有效性，请尽量使用 DEFAULT 查询，只显示当前的可见的应用清单。
         * 如在部分场景必须跳过该限制时，可使用该查询设置, 请谨慎使用！！！
         * 
         */
        ALLAPPS(new StateFormNamedQuery<ApplicationListDefaultForm>("allapps", ApplicationListDefaultForm.class,
                ApplicationListAllQuery.class)),
        
        ALLSIMPLES(new StateFormNamedQuery<ApplicationFormSimple>("allsimples", ApplicationFormSimple.class,
                ApplicationListAllQuery.class));
        
        private StateFormNamedQuery<?> namedQuery;

        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
        
        public static List<StateFormNamedQuery<?>> getQueries() {
            List<StateFormNamedQuery<?>> queries = new ArrayList<>();
            for (QUERIES item : QUERIES.values()) {
                queries.add(item.getNamedQuery());
            }
            return queries;
        }
    }
    
    public VcsUnifiedAppInitInfo getApplicationInitInfo(Long applicatoinId) throws Exception {
        if (applicatoinId == null) {
            return null;
        }
        ApplicationFormWithSubsystem form = get(ApplicationFormWithSubsystem.class, applicatoinId);
        SubsystemBasicForm subsystem;
        if ((subsystem = form.getSubsystem()) == null) {
            throw new MessageException("应用的业务系统未设置");
        }
        AbstractSystemTenant tenant;
        if ((tenant = SystemTenantService.getSimple(SessionContext.getTenant())) == null
                || StringUtils.isBlank(tenant.getCodeNamespace())) {
            throw new MessageException("获取租户代码空间未设置");
        }
        return new VcsUnifiedAppInitInfo(tenant.getCodeNamespace(), subsystem.getCode()).setName(form.getName())
                .setDescription(form.getDescription()).setVcsPath(form.getVcsPath()).setVcsType(form.getVcsType());
    }
    
    public static enum EVENTS implements StateFormEventBaseEnum {
        Create(new AbstractStateSubmitAction<ApplicationFormDetail, ApplicationFormForCreation>("添加",
                STATES.CREATED.getCode()) {

            @Override
            @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class)
            public void check(String event, ApplicationFormDetail form, String sourceState) {

            }

            @Override
            public Long handle(String event, ApplicationFormDetail originForm, final ApplicationFormForCreation form,
                    final String message) throws Exception {
                ApplicationBasicUtil.ensuerNameFormatValid(form.getName());
                final AtomicLong id = new AtomicLong(0);
                DEFAULT.getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                    @Override
                    public void process(ResultSet result, Connection conn) throws Exception {
                        DEFAULT.getFormBaseDao().executeUpdate(
                                SqlQueryUtil.prepareInsertQuery(
                                        DEFAULT.getFormTable(),
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
                        DEFAULT.getFormBaseDao().executeUpdate(
                                SqlQueryUtil.prepareUpdateQuery(
                                        DEFAULT.getFormTable(),
                                        new ObjectMap()
                                                .put("=id", id.get())
                                                .put("vcs_path", repo.getPathToRepo())
                                                .put("release_branch",
                                                        VcsType.forName(form.getVcsType()).getMasterName())));
                    }
                });
                return id.get();
            }
        }),
        Edit(new AbstractStateAction<ApplicationFormDetail, ApplicationFormForEdit, Void>("编辑",
                STATES.stringifyEx(), "") {

            @Override
            @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class)
            public void check(String event, ApplicationFormDetail form, String sourceState) {

            }
            
            @Override
            public Void handle(String event, ApplicationFormDetail originForm, final ApplicationFormForEdit form,
                    final String message) throws Exception {
                DEFAULT.getFormBaseDao().executeTransaction(new ResultSetProcessor() {
                    @Override
                    public void process(ResultSet resultSet, Connection connection) throws Exception {
                        DEFAULT.getFormBaseDao().executeUpdate(
                                SqlQueryUtil.prepareUpdateQuery(
                                        DEFAULT.getFormTable(),
                                        new ObjectMap().put("=id", form.getId()).put("stateless", form.getStateless())
                                                .put("description", form.getDescription())
                                                .put("code_level", form.getCodeLevel())
                                                .put("build_service", form.getBuildService())));
                    }
                });
                return null;
            }
        }),
        UpgradeVersion(new AbstractStateAction<ApplicationFormDetail, ApplicationFormForUpgradeVersion, Void>(
                "版本升级", STATES.stringify(STATES.CREATED, STATES.ONLINE), "") {
            @Override
            @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class)
            public void check(String event, ApplicationFormDetail form, String sourceState) {
            }

            @Override
            public Void handle(String event, ApplicationFormDetail originForm, ApplicationFormForUpgradeVersion form,
                    String message) throws Exception {
                long version = checkVersion(originForm.getBuildMainVersion(), form.getBuildVersion());
                DEFAULT.getFormBaseDao().executeUpdate(
                        SqlQueryUtil.prepareUpdateQuery(DEFAULT.getFormTable(), new ObjectMap()
                                .put("=id", form.getId()).put("build_main_version", version)));
                return null;
            }
        }),
        ToNormalRelease(new AbstractStateAction<ApplicationFormDetail, ApplicationFormFromNormalRelease, Void>(
                "切换为常规发布", STATES.stringify(STATES.ONLINE), "") {
            @Override
            @Authority(value = AuthorityScopeType.Subsystem, rejecter = ToMasterReleaseRejecter.class, parser = AppSubsystemParser.class)
            public void check(String event, ApplicationFormDetail originForm, String sourceState) {
            }

            @Override
            public Void handle(String event, ApplicationFormDetail originForm, ApplicationFormFromNormalRelease form,
                    String message) throws Exception {
                DEFAULT.getFormBaseDao().executeUpdate(
                        SqlQueryUtil.prepareUpdateQuery(
                                DEFAULT.getFormTable(),
                                new ObjectMap().put("=id", form.getId()).put("release_branch",
                                        VcsType.forName(originForm.getVcsType()).getMasterName())));
                return null;
            }
        }),

        ToBranchRelease(new AbstractStateAction<ApplicationFormDetail, ApplicationFormFromBranchRelease, Void>(
                "切换为分支发布", STATES.stringify(STATES.ONLINE), "") {

            @Override
            @Authority(value = AuthorityScopeType.Subsystem, rejecter = ToBranchReleaseRejecter.class, parser = AppSubsystemParser.class)
            public void check(String event, ApplicationFormDetail originForm, String sourceState) {
            }

            @Override
            public Void handle(String event, ApplicationFormDetail originForm, ApplicationFormFromBranchRelease form,
                    String message) throws Exception {
                if (!StringUtils.startsWith(form.getReleaseBranch(), VcsType.forName(originForm.getVcsType())
                        .getPatchesPrefix())) {
                    throw new MessageException("代码分支路径不符合规范，请修改路径后重新提交！");
                }
                DEFAULT.getFormBaseDao().executeUpdate(
                        SqlQueryUtil.prepareUpdateQuery(DEFAULT.getFormTable(), new ObjectMap()
                                .put("=id", form.getId()).put("release_branch", form.getReleaseBranch())));
                return null;
            }
        }),

        ResetRepoPermGroups(new AbstractStateAction<ApplicationFormDetail, BasicStateForm, Void>("重置仓库授权组",
                STATES.stringify(STATES.CREATED, STATES.ONLINE), "") {

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
        }),
        /**
         * 创建新的分支
         */
        VcsRefBranchCreate(new AbstractStateAction<ApplicationFormDetail, ApplicationVcsRefsCreate, Void>("新增分支",
                STATES.stringifyEx(), "") {
            
            @Override
            @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppBranchCreateRejector.class)
            public void check(String event, ApplicationFormDetail originForm, String sourceState) {
            }
            
            @Override
            public Void handle(String event, ApplicationFormDetail originForm, ApplicationVcsRefsCreate form,
                    String message) throws Exception {
                VcsUnifiedService.CommonCloud.createBranch(form.getId(), form.getVcsRefsName(), form.getRefOrCommit(), message);
                return null;
            }
            
            @Override
            public boolean getStateRevisionChangeIgnored() {
                return true;
            }
        }),
        /**
         * 创建新的补丁
         */
        VcsRefPatchCreate(new AbstractStateAction<ApplicationFormDetail, ApplicationVcsRefsCreate, Void>("新增补丁",
                STATES.stringifyEx(), "") {
            
            @Override
            @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppPatchCreateRejector.class)
            public void check(String event, ApplicationFormDetail originForm, String sourceState) {
            }
            
            @Override
            public Void handle(String event, ApplicationFormDetail originForm, ApplicationVcsRefsCreate form,
                    String message) throws Exception {
                VcsUnifiedService.CommonCloud.createPatch(form.getId(), form.getVcsRefsName(), form.getRefOrCommit(), message);
                return null;
            }
            
            @Override
            public boolean getStateRevisionChangeIgnored() {
                return true;
            }
            
        }),
        /**
         * 创建新的标签
         */
        VcsRefTagCreate(new AbstractStateAction<ApplicationFormDetail, ApplicationVcsRefsCreate, Void>("新增标签",
                STATES.stringifyEx(), "") {
            
            @Override
            @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppTagCreateRejector.class)
            public void check(String event, ApplicationFormDetail originForm, String sourceState) {
            }
            
            @Override
            public Void handle(String event, ApplicationFormDetail originForm, ApplicationVcsRefsCreate form,
                    String message) throws Exception {
                VcsUnifiedService.CommonCloud.createTag(form.getId(), form.getVcsRefsName(), form.getRefOrCommit(),
                        message);
                return null;
            }
            
            @Override
            public boolean getStateRevisionChangeIgnored() {
                return true;
            }
        }),
        /**
         * 删除已有的分支
         */
        VcsRefBranchDelete(new AbstractStateAction<ApplicationFormDetail, ApplicationVcsRefsDelete, Void>("删除分支",
                STATES.stringifyEx(), "") {
            
            @Override
            @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppBranchDeleteRejector.class)
            public void check(String event, ApplicationFormDetail originForm, String sourceState) {
            }
            
            @Override
            public Void handle(String event, ApplicationFormDetail originForm, ApplicationVcsRefsDelete form,
                    String message) throws Exception {
                VcsUnifiedService.CommonCloud.deleteBranch(form.getId(), form.getVcsRefsName(), message);
                return null;
            }
            
            @Override
            public boolean getStateRevisionChangeIgnored() {
                return true;
            }
        }),
        /**
         * 删除已有的补丁
         */
        VcsRefPatchDelete(new AbstractStateAction<ApplicationFormDetail, ApplicationVcsRefsDelete, Void>("删除补丁",
                STATES.stringifyEx(), "") {
            
            @Override
            @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppPatchDeleteRejector.class)
            public void check(String event, ApplicationFormDetail originForm, String sourceState) {
            }
            
            @Override
            public Void handle(String event, ApplicationFormDetail originForm, ApplicationVcsRefsDelete form,
                    String message) throws Exception {
                VcsUnifiedService.CommonCloud.deletePatch(form.getId(), form.getVcsRefsName(), message);
                return null;
            }
            
            @Override
            public boolean getStateRevisionChangeIgnored() {
                return true;
            }
        }),
        /**
         * 删除已有的标签
         */
        VcsRefTagDelete(new AbstractStateAction<ApplicationFormDetail, ApplicationVcsRefsDelete, Void>("删除标签",
                STATES.stringifyEx(), "") {
            
            @Override
            @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppTagDeleteRejector.class)
            public void check(String event, ApplicationFormDetail originForm, String sourceState) {
            }
            
            @Override
            public Void handle(String event, ApplicationFormDetail originForm, ApplicationVcsRefsDelete form,
                    String message) throws Exception {
                VcsUnifiedService.CommonCloud.deleteTag(form.getId(), form.getVcsRefsName(), message);
                return null;
            }
            
            @Override
            public boolean getStateRevisionChangeIgnored() {
                return true;
            }
        }),
        
        ViewRuntimeStatus(
                new AbstractStateAction<ApplicationFormDetail, ApplicationFromForQueryStatus, ApplicationRuntimeStatusView>(
                        "查看部署信息", STATES.stringifyEx(STATES.OFFLINED), "") {
                    @Override
                    @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppNonApplicationRejector.class)
                    public void check(String s, ApplicationFormDetail s1, String s2) {
                        
                    }
                    
                    @Override
                    public ApplicationRuntimeStatusView handle(String event, ApplicationFormDetail originForm,
                            ApplicationFromForQueryStatus form, String message) throws Exception {
                        ApplicationRuntimeStatusView view = new ApplicationRuntimeStatusView();
                        view.setNodeItems(getRuntimeStatus(form.getEnvironment(), originForm.getId(), originForm.getName()));
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
                }),
        ViewDeployNamespaces(
                new AbstractStateAction<ApplicationFormDetail, BasicStateForm, ApplicationDeployNamespacesView>(
                        "显示部署机组配置", STATES.stringifyEx(STATES.OFFLINED), "") {
                    
                    @Override
                    @Authority(value = AuthorityScopeType.Subsystem, parser = AppSubsystemParser.class, rejecter = AppNonApplicationRejector.class)
                    public void check(String s, ApplicationFormDetail s1, String s2) {
                        
                    }
                    
                    @Override
                    public ApplicationDeployNamespacesView handle(String event, ApplicationFormDetail originForm,
                            BasicStateForm form, String message) throws Exception {
                        ApplicationDeployNamespacesView view = new ApplicationDeployNamespacesView();
                        view.setDeployNamespaces(DEFAULT.get(ApplicationFormDeployNamespaces.class, form.getId()).getDeployNamespaces());
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
                })
        ;
        private final AbstractStateAction<ApplicationFormDetail, ?, ?> action;

        EVENTS(AbstractStateAction<ApplicationFormDetail, ?, ?> action) {
            this.action = action;
        }

        public AbstractStateAction<ApplicationFormDetail, ?, ?> getAction() {
            return action;
        }
    }
    
    public static ApplicationListDefaultForm getByVcsPath(String vcsPath) throws Exception {
        if (StringUtils.isBlank(vcsPath)) {
            return null;
        }
        ApplicationListDefaultQuery applicationQuery = new ApplicationListDefaultQuery();
        applicationQuery.setVcsPathEquals(vcsPath);
        applicationQuery.setLimit(1);
        PagedList<?> apps = DEFAULT.listForm(QUERIES.DEFAULT, applicationQuery);
        if (apps == null || apps.getList() == null || apps.getList().size() != 1) {
            return null;
        }
        return (ApplicationListDefaultForm) apps.getList().get(0);
    }
    
    private static List<ApplicationRuntimeStatusNodeItem> getRuntimeStatus(String environment, final Long appId,
            final String appName) throws Exception {
        List<ApplicationRuntimeStatusNodeItem> result = new ArrayList<>();
        
        return result;
    }

    @Override
    public List<StateFormNamedQuery<?>> getFormNamedQueries() {
        return QUERIES.getQueries();
    }

    public static class AppSubsystemParser implements AuthorityScopeIdParser {
        @Override
        public Long getAuthorityScopeId(Object originForm) {
            ApplicationAbstractForm manageForm = (ApplicationAbstractForm) originForm;
            if (manageForm == null) {
                return null;
            }
            return manageForm.getSubsystemId();
        }
    }
    
    public static class AppBranchCreateRejector implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !VcsPermissionChecker.NOCACHED.check(RefsOpType.Create, VcsRefsType.Branch,
                    ((AbstractStateForm) originForm).getId());
        }
        
    }
    
    public static class AppBranchDeleteRejector implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !VcsPermissionChecker.NOCACHED.check(RefsOpType.Delete, VcsRefsType.Branch,
                    ((AbstractStateForm) originForm).getId());
        }
    }
    
    public static class AppPatchCreateRejector implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !VcsPermissionChecker.NOCACHED.check(RefsOpType.Create, VcsRefsType.Patch,
                    ((AbstractStateForm) originForm).getId());
        }
        
    }
    
    public static class AppPatchDeleteRejector implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !VcsPermissionChecker.NOCACHED.check(RefsOpType.Delete, VcsRefsType.Patch,
                    ((AbstractStateForm) originForm).getId());
        }
    }
    
    public static class AppTagCreateRejector implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !VcsPermissionChecker.NOCACHED.check(RefsOpType.Create, VcsRefsType.Tag,
                    ((AbstractStateForm) originForm).getId());
        }
        
    }
    
    public static class AppTagDeleteRejector implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !VcsPermissionChecker.NOCACHED.check(RefsOpType.Delete, VcsRefsType.Tag,
                    ((AbstractStateForm) originForm).getId());
        }
    }
    
    public static class AppNonApplicationRejector implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !ApplicationType.APPLICATION.getValue().equals(((ApplicationAbstractForm) originForm).getType());
        }
        
    }
    
    /**
     * 当正好为主干发布模式时，不再允许执行切换主干模式的操作
     *
     */
    public static class ToMasterReleaseRejecter implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) {
            return originForm == null || ((ApplicationFormDetail) originForm).isMasterReleaseMode();
        }
        
    }

    /**
     * 当正好为分支发布模式时，不再允许执行切换分支模式的操作
     *
     */
    public static class ToBranchReleaseRejecter implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) {
            return originForm == null || ((ApplicationFormDetail) originForm).isPatchReleaseMode();
        }
        
    }

    private static long checkVersion(String oldBuildVersion, String newBuildVersoin) throws Exception {
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
    
    private static String[] getCachedUnExpiredAppStatusEnvs(List<AppEnvStatusLastUpdated> items) {
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
    public static List<ApplicationRuntimeEvnVersions> getCachedApplicationVersion(long applicationId) throws Exception {
        
        List<AppEnvStatusLastUpdated> envsLastUpdated = DEFAULT.getFormBaseDao().queryAsList(
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
        DEFAULT.getFormBaseDao().executeUpdate(cleanSql, cleanArgs.toArray());
        
        return DEFAULT.getFormBaseDao().queryAsList(ApplicationRuntimeEvnVersions.class, SQL_QUERY_APP_RUNTIME_VERSIONS,
                new Object[] { applicationId });
    }
    
    @Override
    public List<? extends FieldOption> getStates() {
        return STATES.getStatesAsOption();
    }
    
    public <T extends ApplicationAbstractForm> PagedList<T> list(@NonNull Class<T> clazz,
            @NonNull ApplicationListDefaultQuery query) throws Exception {
        return listFormX(clazz, query);
    }
    
    public <T extends ApplicationAbstractForm> T get(Class<T> clazz, long id) throws Exception {
        List<T> list;
        if ((list = list(clazz, new ApplicationListAllQuery(2, 1L).setAppIdsIn(id + "")).getList()) == null
                || list.size() != 1) {
            throw new PageNotFoundException();
        }
        return list.get(0);
    }

    public <T extends ApplicationAbstractForm> T get(Class<T> clazz, String appName) throws Exception {
        List<T> list;
        if ((list = list(clazz, new ApplicationListAllQuery().setAppNamesIn(appName + "")).getList()) == null
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
            for (SubsystemBasicForm subsystem : ClassUtil.getSingltonInstance(FieldSubsystemAccessors.class)
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
}
