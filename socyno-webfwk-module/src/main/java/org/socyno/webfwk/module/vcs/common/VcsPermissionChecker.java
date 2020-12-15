package org.socyno.webfwk.module.vcs.common;

import java.util.HashMap;
import java.util.Map;

import org.socyno.webfwk.module.application.ApplicationAbstractForm;
import org.socyno.webfwk.module.application.ApplicationService;
import org.socyno.webfwk.module.vcs.change.VcsRefsNameOperation.RefsOpType;
import org.socyno.webfwk.state.authority.AuthorityScopeType;
import org.socyno.webfwk.state.service.PermissionService;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.tool.StringUtils;

public class VcsPermissionChecker {
    
    public final static VcsPermissionChecker NOCACHED = new VcsPermissionChecker(false);
    
    private final Map<String, Boolean> subsysAuthCached;
    
    public VcsPermissionChecker() {
        this(true);
    }
    
    public VcsPermissionChecker(boolean withCached) {
        subsysAuthCached = withCached ? new HashMap<>() : null;
    }
    
    public boolean check(RefsOpType refsOpType, VcsRefsType vcsRefsType, long applicationId) throws Exception {
        /* 确认用户有代码仓的变更授权 */
        ApplicationAbstractForm app;
        if ((app = ApplicationService.getInstance().getSimple(applicationId)) == null) {
            return false;
        }
        Long subsystemId = app.getSubsystemId();
        
        /* 查询操作 */
        if (RefsOpType.Query.equals(refsOpType)) {
            return check(subsystemId, ApplicationService.getInstance().getCodeAccessFormEventKey());
        }
        
        /* 主干的覆盖更新，要求代码维护授权, 删除被禁止，更新只需普通写权限 */
        if (VcsRefsType.Master.equals(vcsRefsType)) {
            if (RefsOpType.ForceUpdate.equals(refsOpType)) {
                return check(subsystemId, ApplicationService.getInstance().getCodeMaintainerFormEventKey());
            }
            if (RefsOpType.Delete.equals(refsOpType)) {
                return false;
            }
            return check(subsystemId, ApplicationService.getInstance().getCodePushFormEventKey());
        }
        
        /* 创建和删除标签，要求标签管理权限，更新需要维护授权 */
        if (VcsRefsType.Tag.equals(vcsRefsType)) {
            if (RefsOpType.Update.equals(refsOpType) || RefsOpType.ForceUpdate.equals(refsOpType)) {
                return check(subsystemId, ApplicationService.getInstance().getCodeMaintainerFormEventKey());
            }
            return check(subsystemId, ApplicationService.getInstance().getCodeTagFormEventKey());
        }
        
        /* 创建和删除补丁或覆盖更新，要求补丁管理授权, 修改则一般的更新授权即可 */
        if (VcsRefsType.Patch.equals(vcsRefsType)) {
            if (RefsOpType.Update.equals(refsOpType)) {
                return check(subsystemId, ApplicationService.getInstance().getCodePushFormEventKey());
            }
            return check(subsystemId, ApplicationService.getInstance().getCodePatchFormEventKey());
        }
        
        /* 否则,至少拥有普通的代码写权限 */
        return check(subsystemId, ApplicationService.getInstance().getCodePushFormEventKey());
    }
    
    private boolean check(Long subsystemId, String authKey) throws Exception {
        /* 系统管理员，直接开放权限 */
        if (SessionContext.isAdmin()) {
            return true;
        }
        /* 入参不正确的，视为无权限 */
        if (subsystemId == null || StringUtils.isBlank(authKey)) {
            return false;
        }
        Boolean result;
        String cacheKey = String.format("%s/%s", subsystemId, authKey);
        if (subsysAuthCached != null && (result = subsysAuthCached.get(cacheKey)) != null) {
            return result;
        }
        result = PermissionService.hasPermission(authKey, AuthorityScopeType.Subsystem, subsystemId);
        if (subsysAuthCached != null) {
            subsysAuthCached.put(cacheKey, result);
        }
        return result;
    }
}
