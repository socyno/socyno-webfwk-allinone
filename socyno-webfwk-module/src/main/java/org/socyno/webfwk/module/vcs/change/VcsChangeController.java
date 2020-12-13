package org.socyno.webfwk.module.vcs.change;

import org.socyno.webfwk.util.remote.R;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class VcsChangeController {
    
    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public R submit(@RequestBody VcsChangeInfoFormCreation info) throws Exception {
        VcsChangeInfoService.getInstance().submit(info);
        return R.ok();
    }
    
    @RequestMapping(value = "/query/mine", method = RequestMethod.GET)
    public R queryByContextUser(Long applicationId, String vcsRefsName, String vcsRevision, Integer limit, Integer page)
            throws Exception {
        VcsChangeListContextCond cond = new VcsChangeListContextCond();
        cond.setVcsRevision(vcsRevision);
        cond.setVcsRefsName(vcsRefsName);
        cond.setApplicationId(applicationId);
        return R.ok().setData(VcsChangeInfoService.getInstance().queryByContextUser(cond, limit, page));
    }
    
    @RequestMapping(value = "/query/application/{applicationId}", method = RequestMethod.GET)
    public R queryByApplicationId(@PathVariable long applicationId, String vcsRefsName, String vcsRevision,
            Long createdBy, Integer limit, Integer page) throws Exception {
        VcsChangeListApplicationCond cond = new VcsChangeListApplicationCond();
        cond.setCreatedBy(createdBy);
        cond.setVcsRevision(vcsRevision);
        cond.setVcsRefsName(vcsRefsName);
        return R.ok().setData(VcsChangeInfoService.getInstance().queryByApplication(applicationId, cond, limit, page));
    }
    
}
