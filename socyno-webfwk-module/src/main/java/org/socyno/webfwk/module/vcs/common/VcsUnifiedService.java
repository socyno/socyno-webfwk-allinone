package org.socyno.webfwk.module.vcs.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
//import java.util.Map.Entry;
import java.util.Set;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.socyno.webfwk.module.application.ApplicationService;
import org.socyno.webfwk.module.subsystem.SubsystemService;
import org.socyno.webfwk.state.module.tenant.SystemTenantService;
import org.socyno.webfwk.state.module.user.WindowsAdService;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.exception.NamingFormatInvalidException;
import org.socyno.webfwk.util.model.AbstractUser;
import org.socyno.webfwk.util.remote.HttpUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.StringUtils;
import org.socyno.webfwk.util.vcs.gitlab.GitlabApiService;
import org.socyno.webfwk.util.vcs.gitlab.GitlabApiService.CommonVisibility;
//import org.socyno.webfwk.util.vcs.gitlab.GitlabApiService.FileChangeType;
import org.socyno.webfwk.util.vcs.gitlab.GitlabApiService.ProjectAccessLevel;
import org.socyno.webfwk.util.vcs.gitlab.GitlabBranch;
import org.socyno.webfwk.util.vcs.gitlab.GitlabCommit;
import org.socyno.webfwk.util.vcs.gitlab.GitlabFile;
import org.socyno.webfwk.util.vcs.gitlab.GitlabGroup;
import org.socyno.webfwk.util.vcs.gitlab.GitlabGroupMember;
import org.socyno.webfwk.util.vcs.gitlab.GitlabLogEntry;
import org.socyno.webfwk.util.vcs.gitlab.GitlabProject;
import org.socyno.webfwk.util.vcs.gitlab.GitlabSharedWithGroup;
import org.socyno.webfwk.util.vcs.gitlab.GitlabSshKey;
import org.socyno.webfwk.util.vcs.gitlab.GitlabTag;
import org.socyno.webfwk.util.vcs.gitlab.GitlabUser;
import org.socyno.webfwk.util.vcs.svn.SubversionApiService;
//import org.socyno.webfwk.util.vcs.svn.SubversionMaiaGroupPermission;
import org.socyno.webfwk.util.vcs.svn.SubversionMaiaService;
import org.socyno.webfwk.util.vcs.svn.SubversionProcessor;
import org.socyno.webfwk.util.vcs.svn.SubversionUtil;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.internal.wc.SVNExternal;
import org.tmatesoft.svn.core.io.SVNRepository;

@Slf4j
public abstract class VcsUnifiedService {
    
    private String getContextGitlabUsername() {
        return toGitlabUsername(SessionContext.getUsername());
    }
    
//    private String getContextSubversionUsername() {
//        return toSubversionUsername(SessionContext.getUsername());
//    }
    
    private String toSubversionUsername(@NonNull String username) {
        return username;
    }
    
    private String toGitlabUsername(@NonNull String username) {
        if (StringUtils.endsWith(username, WindowsAdService.getDefaultDomainSuffix())) {
            return StringUtils.removeEnd(username, WindowsAdService.getDefaultDomainSuffix());
        }
        return username.replaceAll("@", "_");
    }
    
    private final static String MESG_AUTO_CREATED = "系统自动创建";
    
    protected String[] getPublicReadGroups() {
        return null;
    }
    
    protected String[] getPublicWriteGroups() {
        return null;
    }
    
    public final static VcsUnifiedService CommonCloud = new VcsUnifiedService() {
        private String getGitlabApiUrl() {
            return ContextUtil.getConfigTrimed("system.gitlab.common.api.url");
        }
        
        private String getGitlabApiToken() {
            return ContextUtil.getConfigTrimed("system.gitlab.common.api.token");
        }
        
        private GitlabApiService gitlabApiService = null;
        
        @Override
        protected String[] getPublicReadGroups() {
            return ContextUtil.getConfigs("system.gitlab.common.groups.read");
        }
        
        @Override
        protected String[] getPublicWriteGroups() {
            return ContextUtil.getConfigs("system.gitlab.common.groups.read");
        }
        
        @Override
        public GitlabApiService getGitlabApiService() {
            String apiUrl, apiToken;
            if (StringUtils.isAnyBlank(apiUrl = getGitlabApiUrl(), apiToken = getGitlabApiToken())) {
                return null;
            }
            if (gitlabApiService == null || !apiUrl.equals(gitlabApiService.getApiUrl())
                    || !gitlabApiService.checkSameApiToken(apiToken)) {
                gitlabApiService = new GitlabApiService(apiUrl, apiToken);
            }
            return gitlabApiService;
        }
        
        private String getSubversionApiUrl() {
            return ContextUtil.getConfigTrimed("system.subversion.common.api.url");
        }
        
        private String getSubversionApiUser() {
            return ContextUtil.getConfigTrimed("system.subversion.common.api.user");
        }
        
        private String getSubversionApiToken() {
            return ContextUtil.getConfigTrimed("system.subversion.common.api.token");
        }
        
        private String getSubversionMaiaApiUrl() {
            return ContextUtil.getConfigTrimed("system.subversion.common.api.maia.url");
        }
        
        @Override
        public SubversionApiService getSubversionApiService() throws Exception {
            return new SubversionApiService() {
                @Override
                public SVNRepository getRepository() throws SVNException {
                    try {
                        return SubversionUtil
                                .getRepository(
                                        HttpUtil.concatUrlPath(getSubversionApiUrl(),
                                                SystemTenantService.getInstance().getSimple(SessionContext.getTenant())
                                                        .getCodeNamespace()),
                                        getSubversionApiUser(), getSubversionApiToken());
                    } catch (SVNException e) {
                        throw (SVNException) e;
                    } catch (Exception e) {
                        throw new SVNException(SVNErrorMessage.create(SVNErrorCode.UNKNOWN, ""), e);
                    }
                }
                
            };
        }
        
        @Override
        public SubversionMaiaService getSubversionMaiaService() throws Exception {
            return new SubversionMaiaService(getSubversionMaiaApiUrl(), getSubversionApiUser(),
                    getSubversionApiToken());
        }
    };

    public abstract GitlabApiService getGitlabApiService() throws Exception;

    public abstract SubversionApiService getSubversionApiService() throws Exception;

    public abstract SubversionMaiaService getSubversionMaiaService() throws Exception;

    /**
     * 获取用户的 Ssh Key 清单。
     * 
     * @param username
     *            用户的名称
     * @return
     * @throws Exception
     */
    public List<VcsUserSshKey> listUserSshKey() {
        String username = getContextGitlabUsername();
        if (StringUtils.isBlank(username)) {
            throw new MessageException("为获取当前登录用户，请重新登录");
        }
        GitlabUser gitUser;
        try {
            gitUser = getGitlabApiService().getUser(username);
        } catch (Exception e) {
            throw new MessageException("gitlab获取用户失败");
        }
        Long userId = gitUser.getId();
        List<GitlabSshKey> gitlabSshKeyList = null;
        List<VcsUserSshKey> vcsUserSshKeyList = null;
        try {
            vcsUserSshKeyList = new ArrayList<VcsUserSshKey>();
            gitlabSshKeyList = getGitlabApiService().listUserSshKey(userId);
            for (GitlabSshKey key : gitlabSshKeyList) {
                vcsUserSshKeyList.add(new VcsUserSshKey().setId(String.valueOf(key.getId())).setTitle(key.getTitle())
                        .setKeyContent(key.getKey()));
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
            throw new MessageException("获取用户sshkeys失败");
        }
        return vcsUserSshKeyList;
    }

    /**
     * 添加用户的 Ssh Key。
     * 
     * @param username
     *            用户的名称
     * @param title
     *            Key 的标题
     * @param keyContent
     *            Key 的内容
     * @throws Exception
     */
    public void addUserSshKey(String username, String title, String keyContent) {
        if (StringUtils.isBlank(username)) {
            throw new MessageException("为获取当前登录用户，请重新登录");
        }
        if (StringUtils.isBlank(title)) {
            throw new MessageException("请求数据title不能为空.");
        }
        if (StringUtils.isBlank(keyContent)) {
            throw new MessageException("请求数据keyContent不能为空.");
        }

        String vcsUsername = toGitlabUsername(username);
        GitlabUser user;
        try {
            user = getGitlabApiService().getUser(vcsUsername);
        } catch (Exception e) {
            log.error(e.toString(), e);
            throw new MessageException("gitlab获取用户失败");
        }
        try {
            getGitlabApiService().addUserSshKey(user.getId(), title, keyContent);
        } catch (Exception e) {
            log.error(e.toString(), e);
            throw new MessageException("gitlab增加ssh key失败");
        }
    }

    /**
     * 删除用户的 Ssh Key。
     * 
     * @param username
     *            用户名称
     * @param keyId
     *            Key 的标识号
     * @throws Exception
     */
    public void deleteUserSshKey(String username, String keyId) {
        if (StringUtils.isBlank(username)) {
            throw new MessageException("为获取当前登录用户，请重新登录");
        }
        String vcsUsername = toGitlabUsername(username);
        GitlabUser user;
        try {
            user = getGitlabApiService().getUser(vcsUsername);
        } catch (Exception e) {
            log.error(e.toString(), e);
            throw new MessageException("用户获取失败");
        }
        try {
            getGitlabApiService().deleteUserSshKey(user.getId(), Long.valueOf(keyId));
        } catch (Exception e) {
            log.error(e.toString(), e);
            throw new MessageException("删除用户ssh key失败");
        }
    }
    
    /**
     * 初始化或重置应用仓库及其标准化的授权配置。
     */
    public VcsUnifiedAppRepoEntity createOrResetAppRepo(Long applicationId) throws VcsFailToRestAppRepoException {
        VcsUnifiedAppInitInfo appInfo;
        try {
            if ((appInfo = ApplicationService.getInstance().getVcsRepositoryInitInfo(applicationId)) == null) {
                throw new MessageException("给定的应用不存在");
            }
            String name = appInfo.getName();
            String namespace = appInfo.getNamespace();
            String nameWithNamespace = appInfo.getPathWithNamespace();

            if (VcsType.Gitlab.equals(appInfo.getVcsTypeEnum())) {
                /**
                 * 获取或创建分组，作为应用的命名空间
                 */
                GitlabGroup namespaceGroup = null;
                try {
                    namespaceGroup = getGitlabApiService().getOrCreateGroup(namespace, null);
                } catch (Exception e) {
                    throw new MessageException("获取应用的名称空间失败", e);
                }
                /**
                 * 创建应用及其代码仓库
                 */
                try {
                    long appNsId = namespaceGroup.getId();
                    if (!nameWithNamespace.startsWith(namespace + "/")) {
                        appNsId = getGitlabApiService().getNamespace(
                                StringUtils.substringBefore(nameWithNamespace, "/")).getId();
                    }
                    getGitlabApiService().createProject(appNsId, name, name, MESG_AUTO_CREATED,
                            CommonVisibility.PRIVATE);
                } catch (Exception e) {
                }
                GitlabProject repo = null;
                try {
                    repo = getGitlabApiService().getProject(nameWithNamespace);
                } catch (Exception ex) {
                    throw new MessageException("获取应用仓库详情失败", ex);
                }
                /**
                 * 清除当前的授权组数据
                 */
                List<GitlabSharedWithGroup> sharedGroups;
                if ((sharedGroups = repo.getSharedWithGroups()) != null) {
                    for (GitlabSharedWithGroup sg : sharedGroups) {
                        getGitlabApiService().removeGroupOfProject(repo.getId(), sg.getGroupId());
                    }
                }
                /**
                 * 创建授权组，并授予对应的权限
                 */
                String[] publicPermissionGroups;
                if ((publicPermissionGroups = getPublicReadGroups()) != null) {
                    for (String pubGroup : publicPermissionGroups) {
                        if (StringUtils.isBlank(pubGroup)) {
                            continue;
                        }
                        try {
                            GitlabGroup typeGroup = getGitlabApiService().getGroup(pubGroup);
                            getGitlabApiService().shareProjectToGroup(repo.getId(), typeGroup.getId(),
                                    ProjectAccessLevel.REPORTER, null, true);
                        } catch (Exception e) {
                            log.error(e.toString(), e);
                            throw new MessageException(String.format("创建授权分组失败 - %s", pubGroup), e);
                        }
                    }
                }
                if ((publicPermissionGroups = getPublicWriteGroups()) != null) {
                    for (String pubGroup : publicPermissionGroups) {
                        if (StringUtils.isBlank(pubGroup)) {
                            continue;
                        }
                        try {
                            GitlabGroup typeGroup = getGitlabApiService().getGroup(pubGroup);
                            getGitlabApiService().shareProjectToGroup(repo.getId(), typeGroup.getId(),
                                    ProjectAccessLevel.DEVELOPER, null, true);
                        } catch (Exception e) {
                            log.error(e.toString(), e);
                            throw new MessageException(String.format("创建授权分组失败 - %s", pubGroup), e);
                        }
                    }
                }
                if (StringUtils.isNotBlank(appInfo.getPermissionGroup())) {
                    try {
                        GitlabGroup typeGroup = getGitlabApiService().getOrCreateGroup(appInfo.getPermissionGroup(),
                                MESG_AUTO_CREATED, namespaceGroup.getId());
                        getGitlabApiService().shareProjectToGroup(repo.getId(), typeGroup.getId(),
                                ProjectAccessLevel.MAINTAINER, null, true);
                    } catch (Exception e) {
                        log.error(e.toString(), e);
                        throw new MessageException(String.format("授于权限权分组失败 - %s", appInfo.getPermissionGroup()), e);
                    }
                }
                return new VcsUnifiedAppRepoEntity()
                                .setNamedId(repo.getPathWithNamespace())
                                .setPathToRepo(repo.getHttpUrlToRepo());
//            } else if (VcsType.Subversion.equals(appInfo.getVcsTypeEnum())) {
//                /**
//                 * 创建仓库的目录
//                 */
//                SubversionApiService svnService;
//                if ((svnService = getSubversionApiService()) == null) {
//                    throw new MessageException("创建 Subversion 接口服务失败");
//                }
//                final VcsUnifiedAppRepoEntity appEntity = new VcsUnifiedAppRepoEntity();
//                svnService.batchProcess(new SubversionProcessor() {
//                    @Override
//                    public void run(SVNRepository repo) throws Exception {
//                        final String repoName = appInfo.getNamespace();
//                        final String tagsPath = appInfo.getSubversionTagsPath();
//                        final String trunkPath = appInfo.getSubversionTrunkPath();
//                        final String patchesPath = appInfo.getSubversionPatchesPath();
//                        final String branchesPath = appInfo.getSubversionBranchesPath();
//                        final String createdUsername = getContextSubversionUsername();
//                        final String permGroupPreffix = appInfo.getPermissionGroupNamespacePrefix(VcsType.Subversion);
//                        final String currentPermGroupName = appInfo.getPermissionGroupWithNamespace(VcsType.Subversion);
//                        /**
//                         * 先创建授权分组
//                         */
//                        SubversionMaiaService maiaService = getSubversionMaiaService();
//                        maiaService.createOrResetGroup(currentPermGroupName);
//
//                        /**
//                         * 创建目录结构,并设置授权分组。为兼容老的授权及结构, 只清理符合新规则的授权,且考虑 external
//                         * 挂载地址的场景
//                         */
//                        List<String> permissionPaths = new ArrayList<>();
//                        permissionPaths.add(nameWithNamespace);
//                        SubversionUtil.createDir(repo, tagsPath, MESG_AUTO_CREATED, createdUsername);
//                        SubversionUtil.createDir(repo, patchesPath, MESG_AUTO_CREATED, createdUsername);
//                        SubversionUtil.createDir(repo, branchesPath, MESG_AUTO_CREATED, createdUsername);
//                        Map<String, SVNExternal> externals = SubversionUtil.getExternalsProperty(repo,
//                                nameWithNamespace);
//                        if (SVNNodeKind.NONE.equals(SubversionUtil.checkPath(repo, trunkPath))) {
//                            if (externals != null && externals.containsKey(trunkPath)) {
//                                permissionPaths.add(getSubversionRealPath(repo, trunkPath));
//                            } else {
//                                SubversionUtil.createDir(repo, trunkPath, MESG_AUTO_CREATED, createdUsername);
//                            }
//                        }
//                        for (String path : permissionPaths) {
//                            List<SubversionMaiaGroupPermission> groupAccesses;
//                            if ((groupAccesses = maiaService.listGroupAccesses(repoName, path)) != null) {
//                                for (SubversionMaiaGroupPermission gas : groupAccesses) {
//                                    if (StringUtils.startsWith(gas.getGroupName(), permGroupPreffix)) {
//                                        maiaService.delGroupAccess(repoName, path, gas.getGroupId());
//                                    }
//                                }
//                            }
//                            maiaService.addGroupAccess(repoName, path, currentPermGroupName, true);
//                        }
//                        if (!skipApplyAuthz) {
//                            maiaService.forceUpdateAuthz();
//                        }
//                        appEntity.setNamedId(nameWithNamespace).setPathToRepo(
//                                HttpUtil.concatUrlPath(SubversionUtil.getRootUrl(repo), nameWithNamespace));
//                    }
//                });
//                return appEntity;
            }
        } catch (MessageException e) {
            throw new VcsFailToRestAppRepoException(e.getMessage(), e);
        } catch (Exception e) {
            throw new VcsFailToRestAppRepoException("系统调用异常", e);
        }
        throw new VcsFailToRestAppRepoException(String.format("不支持的代码仓库类型：%s", appInfo.getVcsTypeEnum()));
    }

    /**
     * 获取变更的明细
     * 
     * @param applicatoinId
     * @param revision
     * @throws Exception
     */
    public VcsRevisionEntry getRevisionInfo(long applicatoinId, String revision) throws Exception {

        VcsUnifiedAppInitInfo appVcsInfo;
        if ((appVcsInfo = ApplicationService.getInstance().getVcsRepositoryInitInfo(applicatoinId)) == null) {
            throw new MessageException("检索应用源码仓库注册信息异常");
        }
        if (VcsType.Gitlab.equals(appVcsInfo.getVcsTypeEnum())) {
            GitlabCommit commit = getGitlabApiService().fetchRefOrCommit(appVcsInfo.getPathWithNamespace(), revision);
            return new VcsRevisionEntry().setRevision(commit.getId()).setMessage(commit.getMessage())
                    .setCreatedAt(StringUtils.parseDate(commit.getCommittedDate()))
                    .setCreatedBy(commit.getCommitterEmail());
        } else if (VcsType.Subversion.equals(appVcsInfo.getVcsTypeEnum())) {
            SVNProperties props = getSubversionApiService().getRevisionProperties(Long.valueOf(revision));
            return new VcsRevisionEntry().setRevision(Long.valueOf(revision).toString())
                    .setMessage(props.getStringValue("svn:log"))
                    .setCreatedAt(StringUtils.parseDate(props.getStringValue("svn:date")))
                    .setCreatedBy(props.getStringValue("svn:author"));
        }
        throw new MessageException(String.format("不支持的代码仓库类型：%s", appVcsInfo.getVcsTypeEnum()));
    }
    
    /**
     * 获取应在中指定目录的内容.
     * 
     * @param applicatoinId 应用的ID
     * @param path 目录的地址：必须以 / 开头， / 作为目录分隔符
     * @Param revision 仓库的版本。值可以是版本号，也可以为分支、标签或补丁全名
     */
    public List<VcsFileEntry> listDir(long applicationId, String path, String revision) throws Exception {
        VcsUnifiedAppInitInfo appVcsInfo;
        if ((appVcsInfo = ApplicationService.getInstance().getVcsRepositoryInitInfo(applicationId)) == null) {
            throw new MessageException(String.format("提供的应用(id=%s)不存在.", applicationId));
        }
        try {
            if (VcsType.Gitlab.equals(appVcsInfo.getVcsTypeEnum())) {
                List<GitlabLogEntry> entries = getGitlabApiService()
                        .listCommitLogEntries(appVcsInfo.getPathWithNamespace(), revision, path);
                if (entries == null || entries.size() <= 0) {
                    return Collections.emptyList();
                }
                List<VcsFileEntry> list = new ArrayList<>();
                for (GitlabLogEntry entry : entries) {
                    list.add(new VcsFileEntry().setIsDir("tree".equalsIgnoreCase(entry.getType()))
                            .setSize("-")
                            .setRevision(entry.getCommit().getId())
                            .setCommiter(entry.getCommit().getCommitterName())
                            .setLastUpdated(entry.getCommit().getCommittedDate())
                            .setMessage(entry.getCommit().getMessage()).setName(entry.getFileName())
                            .setPath(HttpUtil.concatUrlPath(path, entry.getFileName())));
                }
                return list;
            } else if (VcsType.Subversion.equals(appVcsInfo.getVcsTypeEnum())) {
                /**
                 * 如果提供的是一个数字，则视为从主干上的该版本切一条分支。
                 */
                final List<VcsFileEntry> list = new ArrayList<>();
                getSubversionApiService().batchProcess(new SubversionProcessor() {
                    @Override
                    public void run(SVNRepository repo) throws Exception {
                        long svnRevision = -1L;
                        String svnPath = revision;
                        if (isRevisionNumber(revision)) {
                            svnRevision = Long.valueOf(revision);
                            svnPath = appVcsInfo.getSubversionTrunkPath();
                        } else {
                            VcsType.Subversion.getVcsRefsType(svnPath);
                            svnPath = HttpUtil.concatUrlPath(appVcsInfo.getPathWithNamespace(), svnPath);
                        }
                        if (appVcsInfo.getSubversionTrunkPath().equals(svnPath)
                                || svnPath.startsWith(appVcsInfo.getSubversionTrunkPath() + "/")) {
                            svnPath = getSubversionRealPath(repo, svnPath, svnRevision);
                        }
                        Collection<SVNDirEntry> entries = SubversionUtil.getDir(repo,
                                HttpUtil.concatUrlPath(svnPath, path), svnRevision);
                        if (entries == null || entries.size() <= 0) {
                            return;
                        }
                        for (SVNDirEntry entry : entries) {
                            list.add(new VcsFileEntry()
                                    .setIsDir(SVNNodeKind.DIR.equals(entry.getKind()))
                                    .setSize(CommonUtil.ifNull(entry.getSize(), "-").toString())
                                    .setRevision(CommonUtil.ifNull(entry.getRevision(), "-").toString())
                                    .setCommiter(entry.getAuthor()).setLastUpdated(entry.getDate())
                                    .setMessage(entry.getCommitMessage()).setName(entry.getName())
                                    .setPath(HttpUtil.concatUrlPath(path, entry.getName())));
                        }
                    }
                });
                return list;
            }
            return Collections.emptyList();
        } catch (Exception e) {
            throw new MessageException("查询目录失败", e);
        }
    }
    
    /**
     * 获取应在中指定文件的内容.
     * 
     * @param applicatoinId 应用的ID
     * @param path 文件的地址：必须以 / 开头， / 作为目录分隔符
     * @Param revision 仓库的版本。值可以是版本号，也可以为分支、标签或补丁全名
     */
    public byte[] getFileContent(long applicationId, String path, String revision) throws Exception {
        VcsUnifiedAppInitInfo appVcsInfo;
        if ((appVcsInfo = ApplicationService.getInstance().getVcsRepositoryInitInfo(applicationId)) == null) {
            throw new MessageException(String.format("提供的应用(id=%s)不存在.", applicationId));
        }
        try {
            if (VcsType.Gitlab.equals(appVcsInfo.getVcsTypeEnum())) {
                GitlabFile file = getGitlabApiService().fetchFile(appVcsInfo.getPathWithNamespace(), path,
                        revision);
                return file.getContent();
            } else if (VcsType.Subversion.equals(appVcsInfo.getVcsTypeEnum())) {
                /**
                 * 如果提供的是一个数字，则视为从主干上的该版本切一条分支。
                 */
                final List<byte[]> buffer = new ArrayList<>(1);
                getSubversionApiService().batchProcess(new SubversionProcessor() {
                    @Override
                    public void run(SVNRepository repo) throws Exception {
                        long svnRevision = -1L;
                        String svnPath = revision;
                        if (isRevisionNumber(revision)) {
                            svnRevision = Long.valueOf(revision);
                            svnPath = appVcsInfo.getSubversionTrunkPath();
                        } else {
                            VcsType.Subversion.getVcsRefsType(svnPath);
                            svnPath = HttpUtil.concatUrlPath(appVcsInfo.getPathWithNamespace(), svnPath);
                        }
                        if (appVcsInfo.getSubversionTrunkPath().equals(svnPath)
                                || svnPath.startsWith(appVcsInfo.getSubversionTrunkPath() + "/")) {
                            svnPath = getSubversionRealPath(repo, svnPath, svnRevision);
                        }
                        buffer.add(getSubversionApiService().readFromFile(HttpUtil.concatUrlPath(svnPath, path),
                                svnRevision));
                    }
                });
                return buffer.get(0);
            }
            return null;
        } catch (Exception e) {
            throw new MessageException("获取文件内容失败.", e);
        }
    }
    
    private void checkRefsNaming(@NonNull VcsType vcsType, @NonNull VcsRefsType refsType, String name) {
        NamingFormatInvalidException ex = new NamingFormatInvalidException("非法的分支、标签或补丁名称");
        if (StringUtils.isBlank(name) || name.matches("^[0-9]+$")) {
            throw ex;
        }
        if (StringUtils.startsWithIgnoreCase(name, vcsType.getVcsRefsTypePrefix(refsType))) {
            throw ex;
        }
        if (VcsType.Subversion.equals(vcsType) && !name.matches("^[a-zA-Z0-9][a-zA-Z0-9\\_\\-\\.]*[a-zA-Z0-9]$")) {
            throw ex;
        }
        if (VcsType.Gitlab.equals(vcsType)) {
            if (!name.matches("^[a-zA-Z0-9][a-zA-Z0-9\\_\\-\\.\\/]*[a-zA-Z0-9]$")) {
                throw ex;
            }
            if (name.matches("^refs/.*$")) {
                throw ex;
            }
        }
    }
//    
//    private String getSubversionRealPath(@NonNull SVNRepository repo, @NonNull String svnPath)
//            throws SVNException {
//        return getSubversionRealPath(repo, svnPath, -1L);
//    }
    
    /**
     * 为解决兼容性问题，Subversion 仓库的部分 trunk 目录为 external 外挂。
     * 需要通过此方法可获取其实际的地址，但该方法如果在更广泛的场景下使用时，
     * 必须做更多的测试，以确保可得到准确的信息。
     * 
     */
    private String getSubversionRealPath(@NonNull SVNRepository repo, @NonNull String svnPath, long revision)
            throws SVNException {
        SVNNodeKind svnNodeKind;
        svnPath = SubversionUtil.pathCanonicalize(repo, svnPath);
        if ((svnNodeKind = SubversionUtil.checkPath(repo, svnPath, revision)) == null
                || SVNNodeKind.NONE.equals(svnNodeKind)) {
            if (StringUtils.isBlank(svnPath = svnPath.replaceAll("(^/+|/+$)", ""))) {
                return null;
            }
            String[] nodes = CommonUtil.split(svnPath, "/", CommonUtil.STR_NONBLANK);
            if (nodes.length <= 0) {
                return null;
            }
            String leafName = nodes[nodes.length - 1];
            String psvnPath = nodes.length == 1 ? "/" : StringUtils.join(nodes, "/", 0, nodes.length - 1);
            if ((svnNodeKind = SubversionUtil.checkPath(repo, psvnPath, revision)) == null
                    || SVNNodeKind.NONE.equals(svnNodeKind)) {
                String prelPath;
                if ((prelPath = getSubversionRealPath(repo, psvnPath, revision)) != null) {
                    return HttpUtil.concatUrlPath(prelPath, leafName);
                }
            }
            if (SVNNodeKind.DIR.equals(svnNodeKind)) {
                Map<String, SVNExternal> externals = SubversionUtil.getExternalsProperty(repo, psvnPath);
                if (externals != null && externals.containsKey(leafName)) {
                    SVNExternal external = externals.get(leafName);
                    String extRawUrl = external.getUnresolvedUrl();
                    return SubversionUtil.pathCanonicalize(repo, extRawUrl);
                }
                return svnPath;
            }
        }
        return svnPath;
    }
    
    private void createVcsRefsName(@NonNull VcsRefsType vcsRefsType, long applicationId, String refsName,
            String baseRevision, String message) throws Exception {
        VcsUnifiedAppInitInfo appVcsInfo;
        if ((appVcsInfo = ApplicationService.getInstance().getVcsRepositoryInitInfo(applicationId)) == null) {
            throw new MessageException(String.format("提供的应用(id=%s)不存在.", applicationId));
        }
        try {
            VcsType vcsType;
            checkRefsNaming((vcsType = appVcsInfo.getVcsTypeEnum()), vcsRefsType, refsName);
            if (VcsType.Gitlab.equals(vcsType)) {
                if (VcsRefsType.Tag.equals(vcsRefsType)) {
                    getGitlabApiService().createTag(VcsType.getGitRefsSimpleName(vcsRefsType, refsName),
                            appVcsInfo.getPathWithNamespace(), baseRevision,
                            CommonUtil.ifBlank(message, MESG_AUTO_CREATED));
                } else {
                    getGitlabApiService().createBranch(appVcsInfo.getPathWithNamespace(),
                            VcsType.getGitRefsSimpleName(vcsRefsType, refsName), baseRevision);
                }
            } else if (VcsType.Subversion.equals(vcsType)) {
                getSubversionApiService().batchProcess(new SubversionProcessor() {
                    @Override
                    public void run(SVNRepository repo) throws Exception {
                        String destSvnPath = HttpUtil.concatUrlPath(appVcsInfo.getPathWithNamespace(),
                                HttpUtil.concatUrlPath(VcsType.Subversion.getVcsRefsTypePrefix(vcsRefsType), refsName));
                        SVNNodeKind node = SubversionUtil.checkPath(repo, StringUtils.trim(destSvnPath), -1);
                        if (node != null && !SVNNodeKind.NONE.equals(node)) {
                            throw new NamingFormatInvalidException(
                                    String.format("%s（%s）已存在", vcsRefsType.getDisplay(), refsName));
                        }
                        /**
                         * 如果提供的是一个数字，则视为从主干上的该版本切一条分支。
                         */
                        long srcSvnRevision = -1L;
                        String srcSvnPath = baseRevision;
                        if (isRevisionNumber(baseRevision)) {
                            srcSvnRevision = Long.valueOf(baseRevision);
                            srcSvnPath = appVcsInfo.getSubversionTrunkPath();
                        } else {
                            VcsType.Subversion.getVcsRefsType(srcSvnPath);
                            srcSvnPath = HttpUtil.concatUrlPath(appVcsInfo.getPathWithNamespace(), srcSvnPath);
                        }
                        if (appVcsInfo.getSubversionTrunkPath().equals(srcSvnPath)
                                || srcSvnPath.startsWith(appVcsInfo.getSubversionTrunkPath() + "/")) {
                            srcSvnPath = getSubversionRealPath(repo, srcSvnPath, srcSvnRevision);
                        }
                        SVNNodeKind srcSvnNode;
                        if ((srcSvnNode = SubversionUtil.checkPath(repo, srcSvnPath, srcSvnRevision)) == null
                                || SVNNodeKind.NONE.equals(srcSvnNode)) {
                            if (StringUtils.equals(srcSvnPath, appVcsInfo.getSubversionTrunkPath())) {
                                Map<String, SVNExternal> externals = SubversionUtil.getExternalsProperty(repo,
                                        appVcsInfo.getPathWithNamespace());
                                if (externals != null && externals.containsKey(VcsType.Subversion.getMasterName())) {
                                    srcSvnPath = SubversionUtil.pathCanonicalize(repo, externals
                                            .get(VcsType.Subversion.getMasterName()).getResolvedURL().toString());
                                    srcSvnNode = SubversionUtil.checkPath(repo, srcSvnPath, srcSvnRevision);
                                }
                            }
                        }
                        if (!SVNNodeKind.DIR.equals(srcSvnNode)) {
                            throw new NamingFormatInvalidException(
                                    String.format("给定的基线（%s@%s）不存在!", srcSvnPath, srcSvnRevision));
                        }
                        SubversionUtil.copy(SubversionUtil.getClientManager(repo),
                                SubversionUtil.adapterUrl(repo, srcSvnPath),
                                SubversionUtil.adapterUrl(repo, destSvnPath), srcSvnRevision,
                                CommonUtil.ifBlank(message, MESG_AUTO_CREATED));
                    }
                });
            }
        } catch (Exception e) {
            throw new MessageException(
                    String.format("创建应用（%s）的%s(%s)失败.", appVcsInfo.getName(), vcsRefsType.getDisplay(), refsName), e);
        }
    }
    
    private void deleteVcsRefsName(long applicationId, String refsName, String message) throws Exception {
        if (StringUtils.isBlank(refsName)) {
            return;
        }
        VcsUnifiedAppInitInfo appVcsInfo;
        if ((appVcsInfo = ApplicationService.getInstance().getVcsRepositoryInitInfo(applicationId)) == null) {
            throw new MessageException(String.format("提供的应用(id=%s)不存在.", applicationId));
        }
        VcsRefsType vcsRefsType;
        VcsType vcsType = appVcsInfo.getVcsTypeEnum();
        if (VcsRefsType.Master.equals(vcsRefsType = vcsType.getVcsRefsType(refsName))) {
            throw new MessageException("禁止删除主干分支");
        }
        try {
            if (VcsType.Gitlab.equals(appVcsInfo.getVcsTypeEnum())) {
                if (VcsRefsType.Tag.equals(vcsRefsType)) {
                    getGitlabApiService().deleteTag(VcsType.getGitRefsSimpleName(vcsRefsType, refsName),
                            appVcsInfo.getPathWithNamespace());
                } else {
                    getGitlabApiService().deleteBranch(appVcsInfo.getPathWithNamespace(),
                            VcsType.getGitRefsSimpleName(vcsRefsType, refsName));
                }
            } else if (VcsType.Subversion.equals(appVcsInfo.getVcsTypeEnum())) {
                getSubversionApiService().batchProcess(new SubversionProcessor() {
                    @Override
                    public void run(SVNRepository repo) throws Exception {
                        SVNNodeKind svnPathNode;
                        if ((svnPathNode = SubversionUtil.checkPath(repo,
                                HttpUtil.concatUrlPath(appVcsInfo.getPathWithNamespace(), refsName))) == null
                                || !SVNNodeKind.DIR.equals(svnPathNode)) {
                            throw new MessageException(String.format("%s(%s)不存在", vcsRefsType.getDisplay(), refsName));
                        }
                        SubversionUtil.deleteEntry(repo,
                                HttpUtil.concatUrlPath(appVcsInfo.getPathWithNamespace(), refsName),
                                CommonUtil.ifBlank(message, MESG_AUTO_CREATED));
                    }
                });
            }
        } catch (Exception e) {
            throw new MessageException(
                    String.format("删除应用（%s）的%s(%s)失败.", appVcsInfo.getName(), vcsRefsType.getDisplay(), refsName), e);
        }
    }
    
    private List<OptionVcsRefsName> listRefsNames(@NonNull VcsRefsType vcsRefsType, long applicationId, String keyword,
            Integer page, Integer limit) throws Exception {
        VcsUnifiedAppInitInfo appVcsInfo;
        if ((appVcsInfo = ApplicationService.getInstance().getVcsRepositoryInitInfo(applicationId)) == null) {
            throw new MessageException(String.format("提供的应用(id=%s)不存在.", applicationId));
        }
        try {
            page = CommonUtil.parseMaximalInteger(page, 1);
            limit = CommonUtil.parseMaximalInteger(CommonUtil.ifNull(limit, 20), 1);
            VcsType vcsType = appVcsInfo.getVcsTypeEnum();
            String prefix = vcsType.getVcsRefsTypePrefix(vcsRefsType);
            List<OptionVcsRefsName> refsNames = new ArrayList<>();
            if (VcsRefsType.Branch.equals(vcsRefsType) && page <= 1 && (StringUtils.isBlank(keyword)
                    || StringUtils.containsIgnoreCase(vcsType.getMasterName(), keyword))) {
                refsNames.add(new OptionVcsRefsName(vcsRefsType, vcsType.getMasterName()));
            }
            if (VcsType.Gitlab.equals(vcsType)) {
                if (VcsRefsType.Tag.equals(vcsRefsType)) {
                    List<GitlabTag> tags = getGitlabApiService().listTags(appVcsInfo.getPathWithNamespace(), keyword,
                            limit, page);
                    if (tags == null || tags.size() <= 0) {
                        return Collections.emptyList();
                    }
                    for (GitlabTag tag : tags) {
                        refsNames.add(new OptionVcsRefsName(vcsRefsType,
                                vcsType.getVcsRefsFullName(vcsRefsType, tag.getName())));
                    }
                    return refsNames;
                }
                /**
                 * 为支持正则表达式方式的检索,正对原始的 gitlab 接口做了对应的修改
                 */
                prefix = StringUtils.removeStart(prefix, "refs/heads/");
                List<GitlabBranch> branches = getGitlabApiService().listBranches(appVcsInfo.getPathWithNamespace(),
                        HttpUtil.concatUrlPath(prefix, String.format(".*%s", StringUtils.trimToEmpty(keyword))), limit, page);
                if (branches == null || branches.size() <= 0) {
                    return refsNames;
                }
                for (GitlabBranch branch : branches) {
                    String refsFullName = HttpUtil.concatUrlPath("refs/heads/", branch.getName());
                    if (VcsType.Gitlab.getMasterName().equals(refsFullName)) {
                        continue;
                    }
                    refsNames.add(new OptionVcsRefsName(vcsRefsType, refsFullName));
                }
                return refsNames;
            } else if (VcsType.Subversion.equals(vcsType)) {
                final String svnPath = HttpUtil.concatUrlPath(appVcsInfo.getPathWithNamespace(), prefix);
                getSubversionApiService().batchProcess(new SubversionProcessor() {
                    @Override
                    public void run(SVNRepository repo) throws Exception {
                        if (!SVNNodeKind.DIR.equals(SubversionUtil.checkPath(repo, svnPath))) {
                            return;
                        }
                        Collection<SVNDirEntry> entries = SubversionUtil.getDir(repo, svnPath);
                        if (entries == null || entries.size() <= 0) {
                            return;
                        }
                        Collections.sort((List<SVNDirEntry>) entries, new Comparator<SVNDirEntry>() {
                            @Override
                            public int compare(SVNDirEntry left, SVNDirEntry right) {
                                return left.getRevision() > right.getRevision() ? 1
                                        : (left.getRevision() < right.getRevision() ? -1 : 0);
                            }
                        });
                        for (SVNDirEntry patch : entries) {
                            if (StringUtils.isBlank(keyword)
                                    || StringUtils.containsIgnoreCase(patch.getName(), keyword)) {
                                refsNames.add(new OptionVcsRefsName(vcsRefsType,
                                        vcsType.getVcsRefsFullName(vcsRefsType, patch.getName())));
                            }
                        }
                    }
                });
                int offset;
                if ((offset = ((page - 1) * limit)) >= refsNames.size()) {
                    return Collections.emptyList();
                }
                return refsNames.subList(offset, CommonUtil.parseMinimalInteger(offset + limit, refsNames.size()));
            }
            return Collections.emptyList();
        } catch (Exception e) {
            throw new MessageException(String.format("查询应用（%s）的%s失败.", appVcsInfo.getName(), vcsRefsType.getDisplay()),
                    e);
        }
    }

    /**
     * 查询应用代码仓库分支
     */
    public List<OptionVcsRefsName> listBranches(long applicationId, Integer page, Integer limit) throws Exception {
        return listBranches(applicationId, null, page, limit);
    }

    /**
     * 查询应用代码仓库分支
     */
    public List<OptionVcsRefsName> listBranches(long applicationId, String keyword, Integer page, Integer limit) throws Exception {
        return listRefsNames(VcsRefsType.Branch, applicationId, keyword, page, limit);
    }
    
    /**
     * 新建应用代码仓库的分支
     */
    public void createBranch(long applicationId, String newBranch, String baseRevision, String message)
            throws Exception {
        createVcsRefsName(VcsRefsType.Branch, applicationId, newBranch, baseRevision, message);
    }
    
    /**
     * 删除应用代码仓库分支
     * 
     */
    public void deleteBranch(long applicationId, String branch, String message) throws Exception {
        deleteVcsRefsName(applicationId, branch, message);
    }
    
    /**
     * 查询应用代码仓库补丁分支
     */
    public List<OptionVcsRefsName> listPatches(long applicationId, Integer page, Integer limit) throws Exception {
        return listPatches(applicationId, null, page, limit);
    }
    
    /**
     * 查询应用代码仓库补丁分支
     */
    public List<OptionVcsRefsName> listPatches(long applicationId, String keyword, Integer page, Integer limit) throws Exception {
        return listRefsNames(VcsRefsType.Patch, applicationId, keyword, page, limit);
    }
    
    /**
     * 创建应用代码仓库补丁分支
     */
    public void createPatch(long applicationId, String newPatch, String baseRevision, String message) throws Exception {
        createVcsRefsName(VcsRefsType.Patch, applicationId, newPatch, baseRevision, message);
    }
    
    /**
     * 删除应用代码仓库补丁分支
     */
    public void deletePatch(long applicationId, String patch, String message) throws Exception {
        deleteVcsRefsName(applicationId, patch, message);
    }
    
    /**
     * 查询应用代码仓库标签
     */
    public List<OptionVcsRefsName> listTags(long applicationId, Integer page, Integer limit) throws Exception {
        return listTags(applicationId, null, page, limit);
    }
    
    /**
     * 查询应用代码仓库标签
     */
    public List<OptionVcsRefsName> listTags(long applicationId, String keyword, Integer page, Integer limit) throws Exception {
        return listRefsNames(VcsRefsType.Tag, applicationId, keyword, page, limit);
    }
    
    /**
     * 创建应用代码仓库标签
     * 
     */
    public void createTag(long applicationId, String newTag, String baseRevision, String message) throws Exception {
        createVcsRefsName(VcsRefsType.Tag, applicationId, newTag, baseRevision, message);
    }
    
    /**
     * 删除项目标签
     * 
     * @param applicationId
     * @param tagName
     * @return
     * @throws Exception
     */
    public void deleteTag(long applicationId, String tag, String message) throws Exception {
        deleteVcsRefsName(applicationId, tag, message);
    }
    
    /**
     * 重置权业务系统的代码仓权限组的成员(自动创建不存在的用户)
     */
    public void resetGroupMembers(long subsystemId) throws Exception {
        List<? extends AbstractUser> members;
        VcsUnifiedSubsystemInfo vcsSubsystemInfo;
        try {
            members = CommonUtil.ifNull(SubsystemService.getInstance().collectAllCodePermGroupMembers(subsystemId),
                    Collections.emptyList());
            vcsSubsystemInfo = SubsystemService.getInstance().getCodePermGroupNameWithNamespace(subsystemId);
        } catch (Exception ex) {
            throw new VcsFailToRestGroupMembersException("获取权限组或授权人员信息失败", ex);
        }

        /**
         * 在授予组成员时, 无法确认是否使用了哪种代码仓库,因此需要针对每种仓库均需添加
         */
        /* ============================ Gitlab ============================== */
        GitlabGroup namedGroup = null;
        List<GitlabGroupMember> groupMembers;
        Set<String> subversionMembers = new HashSet<>();
        String groupWithNamespace = vcsSubsystemInfo.getPermissionGroupWithNamespace(VcsType.Gitlab);
        try {
            namedGroup = getGitlabApiService().getGroup(groupWithNamespace);
            groupMembers = getGitlabApiService().getMembersOfGroup(namedGroup.getId());
        } catch (Exception ex) {
            throw new VcsFailToRestGroupMembersException(String.format("创建或检索组(%s)失败.", groupWithNamespace));
        }
        Map<String, GitlabGroupMember> groupMembersMap = new HashMap<>();
        for (GitlabGroupMember m : groupMembers) {
            groupMembersMap.put(m.getUsername(), m);
        }

        for (AbstractUser member : members) {
            if (member == null) {
                continue;
            }
            String gitlabUsername;
            subversionMembers.add(toSubversionUsername(member.getUsername()));
            if (groupMembersMap.remove((gitlabUsername = toGitlabUsername(member.getUsername()))) != null) {
                continue;
            }
            try {
                GitlabUser user = getGitlabApiService().getOrCreateUser(gitlabUsername, member.getDisplay(),
                        member.getUsername());
                getGitlabApiService().addGroupMember(namedGroup.getId(), user.getId(), ProjectAccessLevel.MAINTAINER,
                        null, true);
            } catch (Exception ex) {
                throw new VcsFailToRestGroupMembersException(String.format("添加组成员(%s/%s)失败.", namedGroup.getName(),
                        member.getUsername()));
            }
        }

        for (GitlabGroupMember member : groupMembersMap.values()) {
            try {
                getGitlabApiService().removeGroupMember(namedGroup.getId(), member.getId());
            } catch (Exception ex) {
                throw new VcsFailToRestGroupMembersException(String.format("移除组成员(%s/%s)失败.", namedGroup.getName(),
                        member.getUsername()));
            }
        }
//
//        /*
//         * ============================ Subversion ==============================
//         */
//        SubversionMaiaService maiaService = getSubversionMaiaService();
//        maiaService.createOrResetGroupWithMembers(vcsSubsystemInfo.getPermissionGroupWithNamespace(VcsType.Subversion),
//                subversionMembers.toArray(new String[0]));
//        if (!skipApplyAuthz) {
//            maiaService.forceUpdateAuthz();
//        }
    }

    /**
     * 重置用户的密码,当前仅支持 Subversion。 Gitlab 使用 ssh key 的方式访问，不提供重置密码的功能。
     */
    public void resetUserPassword(String username, String password) throws Exception {
        if (StringUtils.isAnyBlank(username, password)) {
            throw new MessageException("用户名或密码不可置空");
        }
        getSubversionMaiaService().changePassword(username, password);
    }
//
//    /**
//     * 上传文件。自动创建对应的目录结构，并视文件是否存在来决定是更新，还是创建。
//     * 
//     * 需要注意的是：该方法强制进行文件的覆盖，不执行任何版本的冲突验证，谨慎使用。
//     * 
//     * @param projectNamedId
//     * @param branch
//     * @param message
//     * @param files
//     * @param skipExists
//     *            存在的文件，是否不进行更新。
//     * @return 返回创建的变更版本号，如果为 null 则意味着无需要上传的文件。
//     */
//    public String uploadFiles(String repoNamedId, String branch, String message, Map<String, byte[]> files,
//            boolean skipExists) throws Exception {
//        if (files == null) {
//            throw new IllegalArgumentException();
//        }
//        Map<String, byte[]> uploads = new HashMap<String, byte[]>();
//        for (Entry<String, byte[]> f : files.entrySet()) {
//            if (f == null) {
//                continue;
//            }
//            /* 确认文件的变更类型 */
//            FileChangeType type = FileChangeType.A;
//            if (f.getValue() == null) {
//                type = FileChangeType.D;
//            } else {
//                try {
//                    getGitlabApiService().fetchFile(repoNamedId, f.getKey(), branch);
//                    type = FileChangeType.M;
//                    if (skipExists) {
//                        continue;
//                    }
//                } catch (Exception e) {
//                    // drop exeption
//                }
//            }
//            uploads.put(String.format("%s:%s", type.name(), f.getKey()), f.getValue());
//        }
//        if (uploads.isEmpty()) {
//            return null;
//        }
//        GitlabCommit commited = getGitlabApiService().uploadFiles(repoNamedId, branch, message, uploads);
//        log.info("Push single committed = {}", commited);
//        return commited.getId();
//    }
//    
    private boolean isRevisionNumber(String revsion) {
        return revsion != null && revsion.matches("^[1-9][0-9]*$");
    }
}
