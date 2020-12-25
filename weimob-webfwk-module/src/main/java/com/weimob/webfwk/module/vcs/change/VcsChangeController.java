package com.weimob.webfwk.module.vcs.change;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.weimob.webfwk.module.vcs.common.VcsType;
import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.remote.R;
import com.weimob.webfwk.util.tool.CommonUtil;
import com.weimob.webfwk.util.tool.StringUtils;

public class VcsChangeController {
    
    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public R submit(String type, HttpServletRequest req) throws Exception {
        VcsType vcsType;
        if (StringUtils.isBlank(type) || (vcsType = VcsType.forName(type)) == null) {
            throw new MessageException(String.format("The vcs type not supported: %s", type));
        }
        
        VcsChangeInfoFormCreation vcsChangeInfo = null;
        if (VcsType.Gitlab.equals(vcsType)) {
            JsonObject eventData = CommonUtil.fromJson(IOUtils.toString(req.getInputStream(), "UTF-8"),
                            JsonObject.class);
            if (eventData == null || !"push".equals(CommonUtil.getJstring(eventData, "event_name"))) {
                return R.ok("Not commits push, ignored");
            }
            String targetBranch = CommonUtil.getJstring(eventData, "ref");
            String newRevision = CommonUtil.getJstring(eventData, "after");
            String oldRevision = CommonUtil.getJstring(eventData, "before");
            String gitlabUsername = CommonUtil.getJstring(eventData, "user_username");
            JsonElement gitlabRepository = eventData.get("repository");
            String gitlabHttpUrl = null;
            if (gitlabRepository != null && gitlabRepository.isJsonObject() && StringUtils
                    .isBlank(gitlabHttpUrl = CommonUtil.getJstring((JsonObject) gitlabRepository, "http_url"))) {
                gitlabHttpUrl = CommonUtil.getJstring((JsonObject) gitlabRepository, "git_http_url");
            }
            if (StringUtils.isBlank(targetBranch) || StringUtils.isBlank(newRevision)
                    || StringUtils.isBlank(oldRevision) || StringUtils.isBlank(gitlabUsername)
                    || StringUtils.isBlank(gitlabHttpUrl)) {
                throw new MessageException("The request body is invald");
            }
            vcsChangeInfo = new VcsChangeInfoFormCreation()
                                .setVcsType(vcsType.name())
                                .setVcsPath(gitlabHttpUrl)
                                .setVcsRevision(newRevision)
                                .setVcsOldRevision(oldRevision)
                                .setVcsRefsName(targetBranch)
                                .setVcsCommiter(gitlabUsername);
        }
        if (vcsChangeInfo == null) {
            throw new MessageException("The vcs push not parsed");
        }
        VcsChangeInfoService.getInstance().submit(vcsChangeInfo);
        return R.ok();
    }
    
    @RequestMapping(value = "/query/mine", method = RequestMethod.GET)
    public R queryByContextUser(Long applicationId, String vcsRefsName, String vcsRevision, Integer limit, Long page)
            throws Exception {
        VcsChangeListContextCond cond = new VcsChangeListContextCond();
        cond.setVcsRevision(vcsRevision);
        cond.setVcsRefsName(vcsRefsName);
        cond.setApplicationId(applicationId);
        return R.ok().setData(VcsChangeInfoService.getInstance().queryByContextUser(cond, limit, page));
    }
    
    @RequestMapping(value = "/query/application/{applicationId}", method = RequestMethod.GET)
    public R queryByApplicationId(@PathVariable long applicationId, String vcsRefsName, String vcsRevision,
            Long createdBy, Integer limit, Long page) throws Exception {
        VcsChangeListApplicationCond cond = new VcsChangeListApplicationCond();
        cond.setCreatedBy(createdBy);
        cond.setVcsRevision(vcsRevision);
        cond.setVcsRefsName(vcsRefsName);
        return R.ok().setData(VcsChangeInfoService.getInstance().queryByApplication(applicationId, cond, limit, page));
    }
    
}
