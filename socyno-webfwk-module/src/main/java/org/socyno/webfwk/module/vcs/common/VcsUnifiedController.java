package org.socyno.webfwk.module.vcs.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.socyno.webfwk.module.application.ApplicationFormAbstract;
import org.socyno.webfwk.module.application.ApplicationFormVcsRefCreate;
import org.socyno.webfwk.module.application.ApplicationFormVcsRefDelete;
import org.socyno.webfwk.module.application.ApplicationService;
import org.socyno.webfwk.module.vcs.change.VcsRefsNameOperation.RefsOpType;
import org.socyno.webfwk.state.module.user.SystemUserFormLogin;
import org.socyno.webfwk.state.module.user.SystemUserService;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.MessageException;
import org.socyno.webfwk.util.exception.NoAuthorityException;
import org.socyno.webfwk.util.remote.R;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

public class VcsUnifiedController {
    @RequestMapping(value = "/key/ssh/get", method = RequestMethod.GET)
    @ResponseBody
    public R listUserSshKey() throws Exception {
        return R.ok().setData(VcsUnifiedService.CommonCloud.listUserSshKey());
    }
    
    @RequestMapping(value = "/key/ssh/add", method = RequestMethod.POST)
    @ResponseBody
    public R addUserSshKey(@RequestBody VcsUserSshKey user, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        String username = SessionContext.getUsername();
        if (user == null) {
            throw new MessageException("请求数据不可识别.");
        }
        VcsUnifiedService.CommonCloud.addUserSshKey(username, user.getTitle(), user.getKeyContent());
        return R.ok();
    }
    
    @RequestMapping(value = "/key/ssh/delete/{sshId}", method = RequestMethod.POST)
    @ResponseBody
    public R deleteUserSshKey(@PathVariable String sshId, HttpServletRequest req) throws Exception {
        VcsUnifiedService.CommonCloud.deleteUserSshKey(SessionContext.getUsername(), sshId);
        return R.ok();
    }
    
    @RequestMapping(value = "/password/reset", method = RequestMethod.POST)
    @ResponseBody
    public R changePassword(@RequestBody VcsResetPasswordForm form) throws Exception {
        SystemUserFormLogin user = new SystemUserFormLogin();
        user.setUsername(form.getUsername());
        user.setPassword(form.getPassword());
        SystemUserService.getInstance().login(user);
        VcsUnifiedService.CommonCloud.resetUserPassword(form.getUsername(),
                CommonUtil.ifBlank(form.getNewPassword(), form.getPassword()));
        return R.ok();
    }
    
    @RequestMapping(value = "/auth/sync/apps/{applicationId}", method = RequestMethod.POST)
    @ResponseBody
    public R resetApplicationGroups(@PathVariable("applicationId") Long applicationId)
            throws Exception {
        VcsUnifiedService.CommonCloud.createOrResetAppRepo(applicationId);
        return R.ok();
    }
    
    @RequestMapping(value = "/auth/sync/subsys/{subystemId}", method = RequestMethod.POST)
    @ResponseBody
    public R resetSubsystemMembers(@PathVariable("subystemId") Long subystemId) throws Exception {
        VcsUnifiedService.CommonCloud.resetGroupMembers(subystemId);
        return R.ok();
    }
    
    @RequestMapping(value = "/file/dirs/get/{applicationId}", method = RequestMethod.GET)
    @ResponseBody
    public R listFileDirs(@PathVariable("applicationId") Long applicationId, @RequestParam("dirPath") String dirPath,
            @RequestParam("revision") String revision) throws Exception {
        return R.ok().setData(VcsUnifiedService.CommonCloud.listDir(applicationId, dirPath, revision));
    }
    
    @RequestMapping(value = "/file/content/get/{applicationId}", method = RequestMethod.GET)
    @ResponseBody
    public R getFileContent(@PathVariable Long applicationId, @RequestParam("filePath") String filePath,
            @RequestParam("revision") String revision) throws Exception {
        if (!new VcsPermissionChecker().check(RefsOpType.Query, null, applicationId)) {
            throw new NoAuthorityException();
        }
        return R.ok().setData(VcsUnifiedService.CommonCloud.getFileContent(applicationId, filePath, revision));
    }
    
    @RequestMapping(value = "/branch/list/{applicationId}", method = RequestMethod.GET)
    @ResponseBody
    public R listBranches(@PathVariable("applicationId") Long applicationId, String keyword, Integer page, Integer limit) throws Exception {
        return R.ok().setData(VcsUnifiedService.CommonCloud.listBranches(applicationId, keyword, page, limit));
    }
    
    private void fillApplicationVcsRefsCreate(Long applicationId, ApplicationFormVcsRefCreate refsCreate) throws Exception {
        if (applicationId != null && refsCreate != null) {
            ApplicationFormAbstract app = null;
            if ((app = ApplicationService.getInstance().getSimple(applicationId)) == null) {
                return;
            }
            refsCreate.setId(app.getId());
            refsCreate.setState(app.getState());
            refsCreate.setRevision(app.getRevision());
            ClassUtil.checkFormRequiredAndOpValue(refsCreate);
        }
    }
    
    private ApplicationFormAbstract fillApplicationVcsRefsDelete(Long applicationId, ApplicationFormVcsRefDelete refsDelete)
            throws Exception {
        ApplicationFormAbstract app = null;
        if (applicationId != null && refsDelete != null) {
            if ((app = ApplicationService.getInstance().getSimple(applicationId)) != null) {
                refsDelete.setId(app.getId());
                refsDelete.setState(app.getState());
                refsDelete.setRevision(app.getRevision());
                ClassUtil.checkFormRequiredAndOpValue(refsDelete);
            }
        }
        return app;
    }
    
    @RequestMapping(value = "/branch/create/{applicationId}", method = RequestMethod.POST)
    @ResponseBody
    public R createBranch(@PathVariable("applicationId") Long applicationId,
            @RequestBody ApplicationFormVcsRefCreate vcsCreate) throws Exception {
        fillApplicationVcsRefsCreate(applicationId, vcsCreate);
        ApplicationService.getInstance().triggerAction(ApplicationService.EVENTS.VcsBranchCreate.getName(), vcsCreate);
        return R.ok();
    }
    
    @RequestMapping(value = "/patch/list/{applicationId}", method = RequestMethod.GET)
    @ResponseBody
    public R listPatches(@PathVariable("applicationId") Long applicationId, String keyword, Integer page, Integer limit)
            throws Exception {
        return R.ok().setData(VcsUnifiedService.CommonCloud.listPatches(applicationId, keyword, page, limit));
    }
    
    @RequestMapping(value = "/patch/create/{applicationId}", method = RequestMethod.POST)
    @ResponseBody
    public R createPatch(@PathVariable("applicationId") Long applicationId,
            @RequestBody ApplicationFormVcsRefCreate vcsCreate) throws Exception {
        fillApplicationVcsRefsCreate(applicationId, vcsCreate);
        ApplicationService.getInstance().triggerAction(ApplicationService.EVENTS.VcsPatchCreate.getName(), vcsCreate);
        return R.ok();
    }
    
    @RequestMapping(value = "/tag/list/{applicationId}", method = RequestMethod.GET)
    @ResponseBody
    public R listTags(@PathVariable("applicationId") Long applicationId, String keyword, Integer page, Integer limit)
            throws Exception {
        return R.ok().setData(VcsUnifiedService.CommonCloud.listTags(applicationId, keyword, page, limit));
    }
    
    @RequestMapping(value = "/tag/create/{applicationId}", method = RequestMethod.POST)
    @ResponseBody
    public R createTag(@PathVariable("applicationId") Long applicationId,
            @RequestBody ApplicationFormVcsRefCreate vcsCreate) throws Exception {
        fillApplicationVcsRefsCreate(applicationId, vcsCreate);
        ApplicationService.getInstance().triggerAction(ApplicationService.EVENTS.VcsTagCreate.getName(), vcsCreate);
        return R.ok();
    }
    
    @RequestMapping(value = "/ref/delete/{applicationId}", method = RequestMethod.POST)
    @ResponseBody
    public R deleteRefName(@PathVariable("applicationId") Long applicationId,
            @RequestBody ApplicationFormVcsRefDelete refsDelete) throws Exception {
        ApplicationFormAbstract app = fillApplicationVcsRefsDelete(applicationId, refsDelete);
        VcsRefsType vcsRefsType = app.getVcsTypeEnum().getVcsRefsType(refsDelete.getVcsRefsName());
        if (VcsRefsType.Patch.equals(vcsRefsType)) {
            ApplicationService.getInstance().triggerAction(ApplicationService.EVENTS.VcsPatchDelete.getName(),
                    refsDelete);
        } else if (VcsRefsType.Tag.equals(vcsRefsType)) {
            ApplicationService.getInstance().triggerAction(ApplicationService.EVENTS.VcsTagDelete.getName(),
                    refsDelete);
        } else if (VcsRefsType.Branch.equals(vcsRefsType)) {
            ApplicationService.getInstance().triggerAction(ApplicationService.EVENTS.VcsBranchDelete.getName(),
                    refsDelete);
        } else if (VcsRefsType.Master.equals(vcsRefsType)) {
            throw new MessageException("禁止删除主干分支");
        }
        return R.ok();
    }
}
