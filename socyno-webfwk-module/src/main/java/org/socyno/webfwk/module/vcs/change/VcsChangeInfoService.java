package org.socyno.webfwk.module.vcs.change;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;
import org.socyno.webfwk.module.app.form.ApplicationAbstractForm;
import org.socyno.webfwk.module.app.form.ApplicationFormDefault;
import org.socyno.webfwk.module.app.form.ApplicationService;
import org.socyno.webfwk.module.app.form.FieldApplication;
import org.socyno.webfwk.module.app.form.FieldApplication.OptionApplication;
import org.socyno.webfwk.module.systenant.AbstractSystemTenant;
import org.socyno.webfwk.module.systenant.SystemTenantService;
import org.socyno.webfwk.module.vcs.change.VcsRefsNameOperation.RefsOpType;
import org.socyno.webfwk.module.vcs.common.VcsPermissionChecker;
import org.socyno.webfwk.module.vcs.common.VcsRevisionEntry;
import org.socyno.webfwk.module.vcs.common.VcsType;
import org.socyno.webfwk.module.vcs.common.VcsUnifiedService;
import org.socyno.webfwk.state.authority.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeIdParser;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoritySpecialChecker;
import org.socyno.webfwk.state.authority.AuthoriyScopeIdParserFromApplication;
import org.socyno.webfwk.state.basic.*;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.state.module.user.WindowsAdService;
import org.socyno.webfwk.state.util.StateFormEventClassEnum;
import org.socyno.webfwk.state.util.StateFormNamedQuery;
import org.socyno.webfwk.state.util.StateFormQueryBaseEnum;
import org.socyno.webfwk.state.util.StateFormStateBaseEnum;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.RunableWithSessionContext;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.exception.NoAuthorityException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.model.PagedList;
import org.socyno.webfwk.util.model.PagedListWithTotal;
import org.socyno.webfwk.util.remote.HttpUtil;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;
import org.socyno.webfwk.util.vcs.svn.SubversionApiService;
import org.socyno.webfwk.util.vcs.svn.SubversionProcessor;
import org.socyno.webfwk.util.vcs.svn.SubversionUtil;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.io.SVNRepository;

import com.github.reinert.jjschema.Attributes;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Attributes(title = "应用的变更集的管理")
public class VcsChangeInfoService extends
        AbstractStateFormServiceWithBaseDao<VcsChangeInfoFormDetail, VcsChangeInfoFormDefault, VcsChangeInfoFormSimple> {
    
    @Getter
    private static final VcsChangeInfoService Instance = new VcsChangeInfoService();
    
    private VcsChangeInfoService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<VcsChangeInfoFormDefault>(
            "默认查询",
            VcsChangeInfoFormDefault.class,
            VcsChangeListDefaultQuery.class
        ));
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
          DRAFT     ("draft",       "待确认")
        , CREATED   ("created",  "已确认")
        ;
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    public static class VcsChangeInfoSubsystemParser implements AuthorityScopeIdParser {
        
        @Override
        public Long getAuthorityScopeId(Object form) {
            return new AuthoriyScopeIdParserFromApplication()
                    .getAuthorityScopeId(((VcsChangeInfoFormSimple) form).getApplicationId());
        }
    }
    
    public static class VcsChangeInfoSubsystemChecker implements AuthoritySpecialChecker {
        
        @Override
        public boolean check(Object form) throws Exception {
            return SessionContext.getUserId().equals(((VcsChangeInfoFormSimple) form).getCreatedBy());
        }
    }
    
    public class EventCreate extends AbstractStateSubmitAction<VcsChangeInfoFormDetail, VcsChangeInfoFormCreation> {
        
        public EventCreate() {
            super("创建", STATES.DRAFT.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Guest)
        public void check(String event, VcsChangeInfoFormDetail form, String sourceState) {
            
        }
        
        @Override
        public boolean getStateRevisionChangeIgnored() throws Exception {
            return true;
        }
        
        @Override
        public boolean allowHandleReturnNull() {
            return true;
        }
        
        @Override
        public Long handle(String event, VcsChangeInfoFormDetail originForm, VcsChangeInfoFormCreation form,
                String message) throws Exception {
            create(form);
            return null;
        }
    }
    
    public class EventFixRevision extends AbstractStateAction<VcsChangeInfoFormDetail, BasicStateForm, Void> {
        
        public EventFixRevision() {
            super("确认", STATES.DRAFT.getCode(), STATES.CREATED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.Subsystem, parser = VcsChangeInfoSubsystemParser.class, checker = VcsChangeInfoSubsystemChecker.class)
        public void check(String event, VcsChangeInfoFormDetail form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, VcsChangeInfoFormDetail originForm, final BasicStateForm form,
                final String message) throws Exception {
            if (!VcsType.Subversion.equals(VcsType.forName(originForm.getVcsType()))) {
                return null;
            }
            final AbstractSystemTenant tenantInfo = SystemTenantService.getInstance().getSimple(SessionContext.getTenant());
            VcsUnifiedService.CommonCloud.getSubversionApiService().batchProcess(new SubversionProcessor() {
                @Override
                public void run(SVNRepository repo) throws Exception {
                    long endRevision = -1L;
                    int logsInterval = 20;
                    List<SVNLogEntry> logEntreis;
                    int maxLogsSearch = CommonUtil.parseInteger(
                            ContextUtil.getConfigTrimed("system.subversion.fix.revision.maxlogs"), 0);
                    maxLogsSearch = CommonUtil.ifNull(maxLogsSearch <= 0 ? null : maxLogsSearch, logsInterval);
                    while (maxLogsSearch > 0) {
                        maxLogsSearch -= logsInterval;
                        if ((logEntreis = SubversionUtil.getLogs(repo, "/", endRevision, logsInterval)) == null
                                || logEntreis.isEmpty()) {
                            break;
                        }
                        for (SVNLogEntry logEntry : logEntreis) {
                            if (StringUtils.equals(originForm.getVcsRevision(), logEntry.getRevision() + "")
                                    || StringUtils.equals(originForm.getVcsRevision(),
                                            getSubversionPropertyAsString(logEntry.getRevisionProperties(),
                                                    "socyno-change:txn"))) {
                                String vcsMessage = StringUtils.trimToEmpty(logEntry.getMessage());
                                getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                                        new ObjectMap().put("=id", originForm.getId())
                                                .put("vcs_revision", logEntry.getRevision())
                                                .put("=vcs_summary", parseVcsCommitSummary(tenantInfo, VcsType.Subversion, vcsMessage))
                                                .put("=vcs_message", StringUtils.truncate(vcsMessage, 500))
                                                .put("=created_at", logEntry.getDate())));
                                return;
                            }
                            if ((endRevision = logEntry.getRevision()) <= 1) {
                                break;
                            }
                        }
                    }
                    throw new MessageException(
                            String.format("未检索到对应的 Subversion 版本（%s）记录", originForm.getVcsRevision()));
                }
            });
            
            return null;
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class),
        
        FixRevision(EventFixRevision.class);
        ;
        private final Class<? extends AbstractStateAction<VcsChangeInfoFormDetail, ?, ?>> eventClass;

        EVENTS(Class<? extends AbstractStateAction<VcsChangeInfoFormDetail, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Data
    public static class RefsCommitAppsInfo {
        
        private String message;
        
        private final Set<VcsRefsNameOperation> refsNameOperations = new HashSet<>();
    }
    
    /**
     * 校验变更提交描述信息的基本规则
     */
    private static String checkVcsCommitMessage(@NonNull AbstractSystemTenant tenantInfo, @NonNull VcsType vcsType,
            String vcsCommitMessage) {
        if ((vcsCommitMessage = StringUtils.truncate(StringUtils.trimToEmpty(vcsCommitMessage), 500)).length() < 1) {
            throw new MessageException("The commit message is required.");
        }
        return vcsCommitMessage;
    }
    
    /**
     * 提取变更提交的标题
     */
    private static String parseVcsCommitSummary(@NonNull AbstractSystemTenant tenantInfo, @NonNull VcsType vcsType,
            String vcsCommitMessage) {
        if (StringUtils.isBlank(vcsCommitMessage)) {
            return "";
        }
        return StringUtils.truncate(CommonUtil.split(vcsCommitMessage, "[\\r\\n]+", CommonUtil.STR_TRIMED)[0], 80);
    }
    
    /**
     * 确认向紧急分支下变更时，必须当前应用正处于紧急发布模式下，且分支名称严格匹配
     */
    private static void checkReleaseModeMatched(VcsRefsNameOperation refsop, ApplicationAbstractForm application) {
        String vcsRefsName = refsop.getVcsRefsName();
        if (vcsRefsName.startsWith(application.getVcsTypeEnum().getPatchesPrefix())) {
            /**
             * 对补丁（紧急）分支的更新，必须确保当前在紧急发布模式，且匹配要更新的分支名
             */
            if (RefsOpType.Update.equals(refsop.getRefsOpType())) {
                if (!application.isPatchReleaseMode()) {
                    log.error("应用当前并非紧急发布模式，禁止向紧急分支命名空间下提交变更: {}", vcsRefsName);
                    throw new MessageException(
                            String.format("Application(%s) is on master release mode, patches change is forbidden",
                                    application.getName()));
                }
                if (!StringUtils.equals(vcsRefsName, application.getReleaseBranch())) {
                    log.error("应用当前定义的紧急发布分支为 {}，当变更的紧急分支却是 {}", application.getReleaseBranch(), vcsRefsName);
                    throw new MessageException(
                            String.format("Application(%s) patches branch is %s , but you committed is %s.",
                                    application.getName(), application.getReleaseBranch(), vcsRefsName));
                }
            }
            /**
             * 删除补丁分支时，必须确保删除的不是当前正在使用的
             */
            else if (RefsOpType.Delete.equals(refsop.getRefsOpType())) {
                if (StringUtils.equals(vcsRefsName, application.getReleaseBranch())) {
                    log.error("禁止删除当前正在使用的紧急分支 ：{}", application.getReleaseBranch());
                    throw new MessageException(
                            String.format("Application(%s) patches branch ( %s) in using, forbidden to delete.",
                                    application.getName(), application.getReleaseBranch()));
                }
            }
            /**
             * TODO : 创建补丁分支时，必须确保基于生产运行版本
             */
            else if (RefsOpType.Create.equals(refsop.getRefsOpType())) {
                
            }
        }
    }
    
    /**
     SELECT
         p.project_name,
         p.branch_svn_path
     FROM
         ctmp.ctmp_project p
     WHERE
         p.branch_svn_path LIKE CONCAT('%/', ?)
     OR
         p.svn_path LIKE CONCAT('%/', ?) 
     */
    @Multiline
    private final static String SQL_QUERY_CTMP_PROJECT_BYPATH = "X";
    
    private static String getSubversionPropertyAsString(@NonNull SVNProperties props, @NonNull String key)
            throws UnsupportedEncodingException {
        String val;
        byte[] bin;
        if ((val = props.getStringValue(key)) == null && (bin = props.getBinaryValue(key)) != null) {
            val = new String(bin, "UTF-8");
        }
        return val;
    }
    
    /**
     * 针对 Subversion 源码仓库，在 pre-commit hook 中只有 TXN 信息（惊险本地使用），
     * 无法通过直接接口获的变更数据。
     * 
     * 因此，为适配变更管理规则，要求在 pre-commit 中调用该接口前，必须将当前的 TXN 
     * 值、变更描述以及变更文件清单信息提取并存储于 vcsOldRevision 的属性中。 对应的
     * 属性名称分别为：
     *      socyno-change:next:txn      变更的 TXN 值
     *      socyno-change:next:log      变更描述文本
     *      socyno-change:next:files    变更文件清单
     * 
     * 同时，Subversion 采用大仓库模式，在同一次变更中可包含多个应用，必须分析其变更
     * 文件的清单，来判断具体涉及哪些应用，并针对每个应用做授权的校验。
     * 
     * 根据仓库的目录结构规则，第一级目录必须为租户的代码仓库命名空间，第二级必须为应用
     * 名称， 第三级 必须为 trunk（主干）、branches（分支）、 patches（紧急补丁，仅当前
     * 应用设置紧急 发布状态时允许）以及 tags（标签，仅支持创建和删除，禁止变更）。
     */
    private void parseSubversinRefsCommitAppsInfo(final VcsChangeInfoFormCreation info,
            final AbstractSystemTenant tenantInfo, final RefsCommitAppsInfo refsCommitAppsInfo) throws Exception {
        SubversionApiService svnService = VcsUnifiedService.CommonCloud.getSubversionApiService();
        
        String changedFiles = null;
        Long vcsOldRevision = null;
        /* 视为提交后的版本添加 */
        if ((vcsOldRevision = CommonUtil.parseLong(info.getVcsOldRevision(), null)) == null) {
            if (StringUtils.isNotBlank(info.getVcsRevision()) && info.getVcsRevision().matches("^\\d+$")) {
                Collection<SVNLogEntry> logEntries;
                long vcsRevision = Long.valueOf(info.getVcsRevision());
                if ((logEntries = svnService.getLogs("/", vcsRevision, 2)) != null && !logEntries.isEmpty()) {
                    SVNLogEntry vcsRevisionInfo = ((List<SVNLogEntry>) logEntries).get(0);
                    info.setVcsMessage(vcsRevisionInfo.getMessage());
                    info.setVcsCommiter(vcsRevisionInfo.getAuthor());
                    vcsOldRevision = logEntries.size() > 1 ? ((List<SVNLogEntry>) logEntries).get(1).getRevision() : 0;
                    info.setVcsOldRevision("" + vcsOldRevision);
                    
                    StringBuffer changedBuffer = new StringBuffer();
                    Map<String, SVNLogEntryPath> changedPaths = vcsRevisionInfo.getChangedPaths();
                    for (Map.Entry<String, SVNLogEntryPath> path : changedPaths.entrySet()) {
                        changedBuffer.append(path.getValue().getType()).append(" ")
                                .append(String.format("%s%s",
                                        StringUtils.removeEnd(StringUtils.removePattern(path.getKey(), "^/+"), "/"),
                                        SVNNodeKind.DIR.equals(path.getValue().getKind()) ? "/" : ""))
                                .append("\n");
                    }
                    changedFiles = changedBuffer.toString();
                    refsCommitAppsInfo.setMessage(StringUtils.trimToEmpty(info.getVcsMessage()));
                }
            }
            if (vcsOldRevision == null) {
                throw new MessageException(String.format("No such revision found : %s", info.getVcsRevision()));
            }
        }
        /* 视为版本前的 pre-commit hook 方式创建 */
        else {
            if (vcsOldRevision < 1 || vcsOldRevision > svnService.getMaxRevision()) {
                throw new MessageException(String.format("No such revision found : %s", vcsOldRevision));
            }
            /* 确认正确设置了 TXN 属性 */
            SVNProperties revProps = svnService.getRevisionProperties(vcsOldRevision);
            if (!StringUtils.equals(
                    StringUtils.trimToEmpty(getSubversionPropertyAsString(revProps, "socyno-change:next:txn")),
                    info.getVcsRevision())) {
                throw new MessageException("Invalid svn transaction txn provided.");
            }
            /* 确认按照规范填写了变更描述信息 */
            info.setVcsMessage(checkVcsCommitMessage(tenantInfo, VcsType.Subversion,
                    getSubversionPropertyAsString(revProps, "socyno-change:next:log")));
            refsCommitAppsInfo.setMessage(info.getVcsMessage());
            /* 确认hook中正确设置了变更文件清单 */
            if (StringUtils.isBlank(changedFiles = getSubversionPropertyAsString(revProps, "socyno-change:next:files"))) {
                throw new MessageException("No changes files found.");
            }
        }
        for (final String changed : CommonUtil.split(changedFiles, "[\\r\\n]+",
                CommonUtil.STR_NONBLANK | CommonUtil.STR_TRIMED)) {
            
            /* 根据SVN的规范，前两个字符代表变更类型 A|D|U|_U|UU */
            int firstBlankIndex = 1;
            if (!StringUtils.isBlank(changed.substring(firstBlankIndex, 2))
                    && !StringUtils.isBlank(changed.substring(++firstBlankIndex, 3))) {
                throw new MessageException(String.format("Unknown changes item : %s", changed));
            }
            String changeType = changed.substring(0, firstBlankIndex);
            String changePath = changed.substring(firstBlankIndex).trim();
            RefsOpType refsOpType = "A".equals(changeType) ? RefsOpType.Create
                    : ("D".equals(changeType) ? RefsOpType.Delete
                            : ("R".equals(changeType) || "U".equals(changeType) || "_U".equals(changeType)
                                    || "UU".equals(changeType) ? RefsOpType.Update : null));
            if (refsOpType == null) {
                throw new MessageException(String.format("Unknown changes item : %s", changed));
            }
            
            /* 确认符合基本 <租户>/<应用>/ 的命名规范 */
            Matcher matched;
            final Pattern pathRegexp = Pattern.compile("^([^/]+)/([^/]+/)?(.*)?$");
            if ((matched = pathRegexp.matcher(changePath)) == null || !matched.find()) {
                throw new MessageException(String.format("The changed file is out of tenant code namespace : %s", changed));
            }
            String changeAppName = matched.group(2);
            String changeRefsName = matched.group(3);
            
            /* 提取应用信息，无则意味着初始化租户目录，忽略 */
            if (StringUtils.isBlank(changeAppName)) {
                /* 正则中的 changeRefsName 也未匹配到, 禁止删除租户空间 */
                if (StringUtils.isEmpty(changeRefsName)) {
                    if (RefsOpType.Delete.equals(refsOpType)) {
                        throw new MessageException(
                                String.format("Trying to remove tenant's code namespace : %s ? Rejected!!", changed));
                    }
                }
                /* 此时，说明在租户空间根目录下变更文件，禁止创建或修改，但可允许删除 */
                else if (!RefsOpType.Delete.equals(refsOpType)) {
                    throw new MessageException(
                            String.format("The changed file is not in any application : %s", changed));
                }
                continue;
            }
            changeAppName = changeAppName.substring(0, changeAppName.length() - 1);
            
            /* 提取变更分支，无则意味着初始化应用目录，忽略 */
            if (StringUtils.isBlank(changeRefsName)) {
                if (RefsOpType.Delete.equals(refsOpType)) {
                    throw new MessageException(String
                            .format("Trying to remove application's namespace : %s ? Rejected!!", changed));
                }
                continue;
            }
            
            /* 主干 */
            String scopePrefix;
            boolean isRefsNameRoot = false;
            if (changeRefsName.startsWith(VcsType.Subversion.getMasterName() + "/")) {
                scopePrefix = VcsType.Subversion.getMasterName();
                if (StringUtils.isBlank(changeRefsName.substring(scopePrefix.length() + 1))) {
                    isRefsNameRoot = true;
                    if (RefsOpType.Delete.equals(refsOpType)) {
                        throw new MessageException(String
                                .format("Trying to remove application's trunk : %s ? Rejected!!", changed));
                    }
                    continue;
                }
                changeRefsName = VcsType.Subversion.getMasterName();
            }
            /* 分支或标签 */
            else if (changeRefsName.startsWith(scopePrefix = VcsType.Subversion.getPatchesPrefix())
                    || changeRefsName.startsWith(scopePrefix = VcsType.Subversion.getBranchesPrefix())
                    || changeRefsName.startsWith(scopePrefix = VcsType.Subversion.getTagsPrefix())) {
                if (StringUtils.isBlank(changeRefsName = changeRefsName.substring(scopePrefix.length()))) {
                    if (RefsOpType.Delete.equals(refsOpType)) {
                        throw new MessageException(String.format(
                                "Trying to remove application's branch/tag/patch namespace : %s ? Rejected!!",
                                changed));
                    }
                    continue;
                }
                if (!changeRefsName.contains("/")) {
                    /* 允许删除非标的结构文件，但禁止创建或变更 */
                    if (!RefsOpType.Delete.equals(refsOpType)) {
                        throw new MessageException(String.format(
                                "Trying to create non-standard application's branch/tag/patch : %s ? Rejected!!",
                                changed));
                    }
                    continue;
                }
                if (StringUtils.isBlank(StringUtils.substringAfter(changeRefsName, "/"))) {
                    isRefsNameRoot = true;
                }
                if (scopePrefix.equals(changeRefsName = HttpUtil
                        .concatUrlPath(scopePrefix, StringUtils.substringBefore(changeRefsName, "/")).trim())) {
                    throw new MessageException(String.format(
                            "Internal server error, no name of branch/tag/patch parsed: %s ? Rejected!!", changed));
                }
            } else {
                /* 允许删除非标的结构文件或目录，禁止创建或变更 */
                if (!RefsOpType.Delete.equals(refsOpType)) {
                    throw new MessageException(String.format(
                            "Trying to change or create non-standard application's branch/tag/patch : %s ? Rejected!!",
                            changed));
                }
                continue;
            }
            VcsRefsNameOperation refsOperation = new VcsRefsNameOperation(changeAppName,
                    HttpUtil.concatUrlPath(HttpUtil.concatUrlPath(info.getVcsPath(), tenantInfo.getCodeNamespace()),
                            changeAppName),
                    changeRefsName, isRefsNameRoot ? refsOpType : RefsOpType.Update);
            /* 当前为分支根目录时，必须确保存储值的 RefsOpType 的准确性, 执行删除重新添加 */
            if (isRefsNameRoot && refsCommitAppsInfo.getRefsNameOperations().remove(refsOperation)) {
                refsCommitAppsInfo.getRefsNameOperations().add(refsOperation);
            }
            
            /* 确认是否出现过，否则添加该应用 */
            if (refsCommitAppsInfo.getRefsNameOperations().contains(refsOperation)) {
                continue;
            }
            refsCommitAppsInfo.getRefsNameOperations().add(refsOperation);
        }
    }
    
    private void parseGitlabRefsCommitAppsInfo(final VcsChangeInfoFormCreation info, final AbstractSystemTenant tenantInfo,
            final RefsCommitAppsInfo refsCommitAppsInfo) throws Exception {
        if (StringUtils.isBlank(info.getVcsRefsName())) {
            throw new MessageException("Gitlab 仓库创建变更集，仓库变更分支必须提供");
        }
        ApplicationFormDefault application;
        if ((application = ApplicationService.getInstance().getByVcsPath(info.getVcsPath())) == null) {
            throw new MessageException("应用仓库未在DevOps系统中注册，拒绝接收变更");
        }
        /**
         * TODO : VcsOldRevision 为空则用0补全，并视为添加已存在的版本
         */
        boolean createAfterPushed = false;
        if (StringUtils.isBlank(info.getVcsOldRevision())) {
            createAfterPushed = true;
            info.setVcsOldRevision("0000000000000000000000000000000000000000");
        }
        if (StringUtils.isBlank(info.getVcsRevision())) {
            info.setVcsRevision("0000000000000000000000000000000000000000");
        }
        VcsRefsNameOperation refsOperation = new VcsRefsNameOperation(application.getName(), info.getVcsPath(),
                info.getVcsRefsName(), info.getVcsOldRevision().matches("^0+$") ? RefsOpType.Create
                        : (info.getVcsRevision().matches("^0+$") ? RefsOpType.Delete : RefsOpType.Update),
                application);
        VcsRevisionEntry commmitInfo = null;
        /* 非创建操作，需要检查分支或标签其是否存在 */
        if (!RefsOpType.Create.equals(refsOperation.getRefsOpType())) {
            try {
                commmitInfo = VcsUnifiedService.CommonCloud.getRevisionInfo(application.getId(), info.getVcsRefsName());
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
            if (commmitInfo == null || StringUtils.isBlank(commmitInfo.getRevision())) {
                throw new MessageException(String.format("输入的分支或标签(%s)不存在，拒绝接收变更", info.getVcsRefsName()));
            }
        }
        /* 非删除操作，需要检查变更版本是否存在 */
        if (!RefsOpType.Delete.equals(refsOperation.getRefsOpType())) {
            try {
                commmitInfo = VcsUnifiedService.CommonCloud.getRevisionInfo(application.getId(), info.getVcsRevision());
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
            if (commmitInfo == null || !StringUtils.equals(commmitInfo.getRevision(), info.getVcsRevision())) {
                throw new MessageException(String.format("输入的变更版本(%s)不存在，拒绝接收变更", info.getVcsRevision()));
            }
            info.setVcsMessage(commmitInfo.getMessage());
            refsCommitAppsInfo.setMessage(commmitInfo.getMessage());
            
            /* 通过后创建方式触发，则无需校验提交描述信息是否符合规范 */
            if (!createAfterPushed) {
                checkVcsCommitMessage(tenantInfo, VcsType.Gitlab, info.getVcsMessage());
            }
            /* 需要确认是否为 force update， 后续从 gitlab 开新的接口来获取更准确的信息 */
            else if (RefsOpType.Update.equals(refsOperation.getRefsOpType())) {
                if (CommonUtil.parseBoolean(info.getForceUpdate())) {
                    refsOperation.setRefsOpType(RefsOpType.ForceUpdate);
                }
            }
        }
        refsCommitAppsInfo.getRefsNameOperations().add(refsOperation);
    }
    
    private static String formatToSystemUsername(@NonNull String vcsCommiter) {
        if (!vcsCommiter.contains("@")) {
            String replaced = vcsCommiter.replaceFirst("\\_([^\\_]+)$", "@$1");
            /* 为兼容公司内部老的 gitlab 用户登录，做的特殊处理 */
            if (vcsCommiter.equals(replaced)) {
                replaced = String.format("%s%s", replaced, WindowsAdService.getDefaultDomainSuffix());
            }
            return replaced;
        }
        return vcsCommiter;
    }
    
    void submit(@NonNull VcsChangeInfoFormCreation info) throws Exception {
        
        final String systemUserCode = formatToSystemUsername(info.getVcsCommiter());
        SystemUserService.DEFAULT.forceSuToUser(systemUserCode);
        info.setVcsCommiter(systemUserCode);
        triggerAction(EVENTS.Create.getName(), info);
    }
    
    private void create(VcsChangeInfoFormCreation info) throws Exception {
        
        AbstractSystemTenant tenantInfo;
        if ((tenantInfo = SystemTenantService.getInstance().getSimple(SessionContext.getTenant())) == null
                || tenantInfo.isDisabled() || StringUtils.isBlank(tenantInfo.getCodeNamespace())) {
            log.info("租户（{}）不存在、或代码空间未配置、或者已被禁用", SessionContext.getTenant());
            throw new MessageException("Invalid vcs account, no such user or disabled.");
        }
        
        /* 查找仓库对应的应用 */
        final VcsType vcsType = VcsType.forName(info.getVcsType());
        final RefsCommitAppsInfo refsCommitAppsInfo = new RefsCommitAppsInfo();
        if (VcsType.Gitlab.equals(vcsType)) {
            parseGitlabRefsCommitAppsInfo(info, tenantInfo, refsCommitAppsInfo);
        } 
        else if (VcsType.Subversion.equals(vcsType)) {
            parseSubversinRefsCommitAppsInfo(info, tenantInfo, refsCommitAppsInfo);
        } else {
            throw new MessageException("Unsupported version control system, reject changes.");
        }
        if (refsCommitAppsInfo.getRefsNameOperations() == null || refsCommitAppsInfo.getRefsNameOperations().isEmpty()) {
            return;
        }
        
        /* 确认用户有代码仓的变更授权 */
        VcsPermissionChecker vcsPermChecker = new VcsPermissionChecker();
        String[] skippedTenants = ContextUtil
                .getConfigsNonBlankTrimed("system.vcs.permission.submit.check.skipped.tenants");
        for (VcsRefsNameOperation refsop : refsCommitAppsInfo.getRefsNameOperations()) {
            ApplicationAbstractForm application;
            if ((application = refsop.getApplicationForm()) == null) {
                if ((application = ApplicationService.getInstance().getByVcsPath(refsop.getVcsPath())) == null) {
                    throw new MessageException(
                            String.format("Application not found for vcs path : %s", refsop.getVcsPath()));
                }
                refsop.setApplicationForm(application);
            }
            checkReleaseModeMatched(refsop, application);
            if (!application.getVcsTypeEnum().equals(vcsType)) {
                throw new MessageException(
                        String.format("Current version control system is %s, but matched application(%s) is %s",
                                vcsType, application.getName(), application.getVcsTypeEnum()));
            }
            /* 针对特定的租户，可以通过全局配置绕过授权的检查 */
            if (!ArrayUtils.contains(skippedTenants, SessionContext.getTenant())
                    && !vcsPermChecker.check(refsop.getRefsOpType(), vcsType.getVcsRefsType(refsop.getVcsRefsName()),
                            application.getId())) {
                throw new NoAuthorityException();
            }
        }
        
        getFormBaseDao().executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet result, Connection conn) throws Exception {
                for (VcsRefsNameOperation refsop : refsCommitAppsInfo.getRefsNameOperations()) {
                    getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(getFormTable(), new ObjectMap()
                        .put("application_id", refsop.getApplicationForm().getId())
                        .put("vcs_revision", info.getVcsRevision())
                        .put("vcs_refs_name", refsop.getVcsRefsName())
                        .put("=vcs_type", vcsType.name())
                        .put("=vcs_path", refsop.getVcsPath())
                        .put("=vcs_summary", parseVcsCommitSummary(tenantInfo, vcsType, refsCommitAppsInfo.getMessage()))
                        .put("=vcs_message", refsCommitAppsInfo.getMessage())
                        .put("=vcs_old_revision", info.getVcsOldRevision())
                        .put("=created_by", SessionContext.getUserId())
                        .put("=created_code_by", SessionContext.getUsername())
                        .put("=created_name_by", SessionContext.getDisplay())
                    ));
                }
                
                /**
                 * 针对Subversion, 在 hook 中只能知道其对应的 txn, 在验证完成后必须通过检索仓库记录
                 * 才能知道具体的变更版本号, 这里通过异步任务来完成该操作。
                 */
                for (VcsRefsNameOperation refsop : refsCommitAppsInfo.getRefsNameOperations()) {
                    new Thread(new VcsCommittedRevisionFixRunner(refsop.getApplicationForm().getId(),
                            info.getVcsRevision(), refsop.getVcsRefsName())).start();
                }
            }
        });
    }
    
    private class VcsCommittedRevisionFixRunner extends RunableWithSessionContext {
        
        private final long applicationId;
        private final String vcsRevision;
        private final String vcsRefsName;
        
        public VcsCommittedRevisionFixRunner (long applicationId, String vcsRevision, String vcsRefsName) {
            this.vcsRefsName = vcsRefsName;
            this.vcsRevision = vcsRevision;
            this.applicationId = applicationId;
        }
        
        @Override
        public void exec() {
            try {
                int fixDelaySeconds = CommonUtil
                        .parseInteger(ContextUtil.getConfigTrimed("system.subversion.fix.revision.delay"), 0);
                fixDelaySeconds = CommonUtil.ifNull(fixDelaySeconds <= 0 ? null : fixDelaySeconds, 5);
                Thread.sleep(fixDelaySeconds * 1000L);
                PagedList<?> changeInfos = listForm(QUERIES.DEFAULT, new VcsChangeListDefaultQuery(1, 1)
                        .setVcsRevision(vcsRevision).setVcsRefsName(vcsRefsName).setApplicationId(applicationId));
                if (changeInfos == null || changeInfos.getList() == null || changeInfos.getList().isEmpty()) {
                    return;
                }
                for (Object entry : changeInfos.getList()) {
                    if (!StringUtils.equals(((VcsChangeInfoFormSimple)entry).getVcsRevision(), vcsRevision)
                            || !StringUtils.equals(((VcsChangeInfoFormSimple)entry).getVcsRefsName(), vcsRefsName)) {
                        continue;
                    }
                    BasicStateForm eventForm = new BasicStateForm();
                    eventForm.setId(((VcsChangeInfoFormSimple)entry).getId());
                    eventForm.setRevision(((VcsChangeInfoFormSimple)entry).getRevision());
                    triggerAction(EVENTS.FixRevision.getName(), eventForm);
                }
            } catch (Exception e) {
                log.error(e.toString(), e);
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException(e);
            }
        }
    }
    
    public PagedListWithTotal<VcsChangeInfoFormSimple> queryByContextUser(Integer limit, Integer page)
            throws Exception {
        return queryByContextUser(null, limit, page);
    }
    
    @SuppressWarnings("unchecked")
    public PagedListWithTotal<VcsChangeInfoFormSimple> queryByContextUser(VcsChangeListContextCond cond, Integer limit,
            Integer page) throws Exception {
        if (!SessionContext.hasUserSession()) {
            return null;
        }
        VcsChangeListDefaultQuery query = new VcsChangeListDefaultQuery(limit, page);
        query.setCreatedBy(SessionContext.getUserId());
        if (cond != null) {
            query.setVcsRefsName(cond.getVcsRefsName());
            query.setApplicationId(cond.getApplicationId());
            query.setVcsRevision(cond.getVcsRevision());
        }
        return (PagedListWithTotal<VcsChangeInfoFormSimple>) listFormWithTotal(QUERIES.DEFAULT, query);
    }
    
    public PagedListWithTotal<VcsChangeInfoFormSimple> queryByApplication(long applicationId, Integer limit,
            Integer page) throws Exception {
        return queryByApplication(applicationId, null, limit, page);
    }
    
    @SuppressWarnings("unchecked")
    public PagedListWithTotal<VcsChangeInfoFormSimple> queryByApplication(long applicationId,
            VcsChangeListApplicationCond cond, Integer limit, Integer page) throws Exception {
        VcsChangeListDefaultQuery query = new VcsChangeListDefaultQuery(limit, page);
        query.setApplicationId(applicationId);
        if (cond != null) {
            query.setCreatedBy(cond.getCreatedBy());
            query.setVcsRefsName(cond.getVcsRefsName());
            query.setVcsRevision(cond.getVcsRevision());
        }
        return (PagedListWithTotal<VcsChangeInfoFormSimple>) listFormWithTotal(QUERIES.DEFAULT, query);
    }
    
    @Override
    public VcsChangeInfoFormDetail getForm(long formId) throws Exception {
        VcsChangeInfoFormDetail form;
        if ((form = super.getForm(formId)) != null) {
            fillFormDetails(VcsChangeInfoFormDetail.class, Arrays.asList(new VcsChangeInfoFormDetail[] { form }));
        }
        return form;
    }
    
    /**
     * 补全表单的必要详情数据
     */
    <T extends AbstractStateForm> void fillFormDetails(Class<T> itemClazz, List<T> resultSet) throws Exception {
        if (resultSet == null || resultSet.size() <= 0) {
            return;
        }
        /* 补全应用数据 */
        if (VcsChangeInfoWithApplication.class.isAssignableFrom(itemClazz)) {
            Set<Long> applicationIds = new HashSet<>();
            for (T r : resultSet) {
                applicationIds.add(((VcsChangeInfoAbstractForm) r).getApplicationId());
            }
            List<? extends OptionApplication> changeApplications;
            Map<Long, OptionApplication> mapedChangeApplications = null;
            if ((changeApplications = ClassUtil.getSingltonInstance(FieldApplication.class)
                    .queryDynamicValues(applicationIds.toArray())) != null && !changeApplications.isEmpty()) {
                mapedChangeApplications = new HashMap<>();
                for (OptionApplication e : changeApplications) {
                    mapedChangeApplications.put(e.getId(), e);
                }
                for (T r : resultSet) {
                    ((VcsChangeInfoWithApplication) r).setApplication(
                            mapedChangeApplications.get(((VcsChangeInfoWithApplication) r).getApplicationId()));
                }
            }
        }
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Override
    public String getFormName() {
        return "vcs_change_info";
    }
    
    @Override
    protected String getFormTable() {
        return "vsc_change_info";
    }
    
    @Override
    public String getFormDisplay() {
        return "代码变更";
    }

    @Override
    protected void fillExtraFormFields(Collection<? extends VcsChangeInfoFormSimple> forms) throws Exception {
        // TODO Auto-generated method stub
        
    }
}