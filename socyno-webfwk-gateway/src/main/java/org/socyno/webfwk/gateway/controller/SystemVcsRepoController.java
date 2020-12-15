package org.socyno.webfwk.gateway.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.socyno.webfwk.gateway.util.HttpRedirectUtil;
import org.socyno.webfwk.state.annotation.Authority;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.authority.AuthoriyScopeIdParserFromApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.reinert.jjschema.Attributes;

@RestController
@RequestMapping(value = "/vcs")
public class SystemVcsRepoController {

    @Attributes(title = "变更提交接口")
    @Authority(AuthorityScopeType.Guest)
    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public void submit(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/vcschange/submit", req, res);
    }
    
    @Attributes(title = "查询我的变更集")
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/changes/mine", method = RequestMethod.GET)
    public void queryMyChanges(Long applicationId, String vcsRefsName, String vcsRevision, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/vcschange/query/mine", req, res);
    }
    
    @Attributes(title = "查询应用变更集")
    @Authority(value = AuthorityScopeType.Subsystem, paramIndex = 0, parser = AuthoriyScopeIdParserFromApplication.class)
    @RequestMapping(value = "/changes/application/{applicationId}", method = RequestMethod.GET)
    public void queryApplicationChanges(@PathVariable long applicationId, String vcsRefsName, String vcsRevision,
            Long createdBy, HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/vcschange/query/application/" + applicationId, req, res);
    }
    
    @Attributes(title = "查询用户ssh key")
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/unified/key/ssh/get", method = RequestMethod.GET)
    public void listUserSshKey(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/vcsunified/key/ssh/get", req, res);
    }
    
    @Attributes(title = "新增用户ssh key")
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/unified/key/ssh/add", method = RequestMethod.POST)
    public void addUserSshKey(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/vcsunified/key/ssh/add", req, res);
    }
    
    @Attributes(title = "删除用户ssh key")
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/unified/key/ssh/delete/{sshId}", method = RequestMethod.POST)
    public void deleteUserSshKey(@PathVariable("sshId") String sshId, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/vcsunified/key/ssh/delete/%s", sshId), req, res);
    }
    
    @Attributes(title = "重置用户源码仓密码")
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/unified/password/reset", method = RequestMethod.POST)
    public void changePassword(HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend("/api/vcsunified/password/reset", req, res);
    }
    
    @Attributes(title = "重置应用的授权组信息")
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/auth/sync/apps/{applicationId}", method = RequestMethod.POST)
    public void resetApplicationGroups(@PathVariable("applicationId") Long applicationId, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/vcsunified/auth/sync/apps/%s", applicationId),
                req, res);
    }
    
    @Attributes(title = "重置业务系统的授权组成员")
    @Authority(value = AuthorityScopeType.System)
    @RequestMapping(value = "/auth/sync/subsys/{subystemId}", method = RequestMethod.POST)
    public void resetSubsystemMembers(@PathVariable("subystemId") Long subystemId, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/vcsunified/auth/subsys/apps/%s", subystemId),
                req, res);
    }
    
    @Attributes(title = "查询应用文件目录")
    @Authority(value = AuthorityScopeType.Subsystem, paramIndex = 0, parser = AuthoriyScopeIdParserFromApplication.class)
    @RequestMapping(value = "/unified/file/dirs/get/{applicationId}", method = RequestMethod.GET)
    public void listFileDirs(@PathVariable("applicationId") Long applicationId,
            @RequestParam("dirPath") String dirPath, @RequestParam("revision") String revision, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/vcsunified/file/dirs/get/%s", applicationId),
                req, res);
    }
    
    @Attributes(title = "获取应用指定文件内容")
    @Authority(value = AuthorityScopeType.Subsystem, paramIndex = 0, parser = AuthoriyScopeIdParserFromApplication.class)
    @RequestMapping(value = "/unified/file/content/get/{applicationId}", method = RequestMethod.GET)
    public void getFileContent(@PathVariable("applicationId") Long applicationId,
            @RequestParam("filePath") String filePath, @RequestParam("revision") String revision,
            HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/vcsunified/file/content/get/%s", applicationId),
                req, res);
    }
    
    @Attributes(title = "查询应用所有分支信息")
    @Authority(value = AuthorityScopeType.Subsystem, paramIndex = 0, parser = AuthoriyScopeIdParserFromApplication.class)
    @RequestMapping(value = "/unified/branch/list/{applicationId}", method = RequestMethod.GET)
    public void queryBranches(@PathVariable("applicationId") Long applicationId, String keyword, Integer page,
            Integer limit, HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/vcsunified/branch/list/%s", applicationId), req,
                res);
    }
    
    @Attributes(title = "创建项目分支")
    @Authority(value = AuthorityScopeType.Subsystem, paramIndex = 0, parser = AuthoriyScopeIdParserFromApplication.class)
    @RequestMapping(value = "/unified/branch/create/{applicationId}", method = RequestMethod.POST)
    public void createBranch(@PathVariable("applicationId") Long applicationId, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/vcsunified/branch/create/%s", applicationId),
                req, res);
    }
    
    @Attributes(title = "查询应用所有补丁信息")
    @Authority(value = AuthorityScopeType.Subsystem, paramIndex = 0, parser = AuthoriyScopeIdParserFromApplication.class)
    @RequestMapping(value = "/unified/patch/list/{applicationId}", method = RequestMethod.GET)
    public void queryPatches(@PathVariable("applicationId") Long applicationId, String keyword, Integer page,
            Integer limit, HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/vcsunified/patch/list/%s", applicationId), req,
                res);
    }
    
    @Attributes(title = "创建项目补丁")
    @Authority(value = AuthorityScopeType.Subsystem, paramIndex = 0, parser = AuthoriyScopeIdParserFromApplication.class)
    @RequestMapping(value = "/unified/patch/create/{applicationId}", method = RequestMethod.POST)
    public void createPatch(@PathVariable("applicationId") Long applicationId, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/vcsunified/patch/create/%s", applicationId),
                req, res);
    }
    
    @Attributes(title = "查询应用所有标签信息")
    @Authority(value = AuthorityScopeType.Subsystem, paramIndex = 0, parser = AuthoriyScopeIdParserFromApplication.class)
    @RequestMapping(value = "/unified/tag/list/{applicationId}", method = RequestMethod.GET)
    public void queryTags(@PathVariable("applicationId") Long applicationId, String keyword, Integer page,
            Integer limit, HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/vcsunified/tag/list/%s", applicationId), req,
                res);
    }
    
    @Attributes(title = "创建应用标签")
    @Authority(value = AuthorityScopeType.Subsystem, paramIndex = 0, parser = AuthoriyScopeIdParserFromApplication.class)
    @RequestMapping(value = "/unified/tag/create/{applicationId}", method = RequestMethod.POST)
    public void createTags(@PathVariable("applicationId") Long applicationId, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/vcsunified/tag/create/%s", applicationId), req,
                res);
    }
    
    @Attributes(title = "删除分支、补丁、标签")
    @Authority(value = AuthorityScopeType.Subsystem, paramIndex = 0, parser = AuthoriyScopeIdParserFromApplication.class)
    @RequestMapping(value = "/unified/ref/delete/{applicationId}", method = RequestMethod.POST)
    public void deleteVcsRefsName(@PathVariable("applicationId") Long applicationId, HttpServletRequest req,
            HttpServletResponse res) throws Exception {
        HttpRedirectUtil.redirectToBackend(String.format("/api/vcsunified/ref/delete/%s", applicationId), req,
                res);
    }
}
