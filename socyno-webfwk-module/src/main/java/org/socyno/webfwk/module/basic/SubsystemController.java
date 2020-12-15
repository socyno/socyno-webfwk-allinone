package org.socyno.webfwk.module.basic;

import javax.servlet.http.HttpServletRequest;

import org.socyno.webfwk.module.application.ApplicationBookmarkService;
import org.socyno.webfwk.module.application.ApplicationQueryAll;
import org.socyno.webfwk.module.application.ApplicationQueryDefault;
import org.socyno.webfwk.module.application.ApplicationService;
import org.socyno.webfwk.module.application.FieldApplicationAll;
import org.socyno.webfwk.module.application.FieldApplicationOfflineIncluded;
import org.socyno.webfwk.module.deploy.environment.FieldDeployEnvironment;
import org.socyno.webfwk.module.productline.ProductlineQueryDefault;
import org.socyno.webfwk.module.productline.ProductlineService;
import org.socyno.webfwk.module.subsystem.FieldSubsystemNoAnyLimited;
import org.socyno.webfwk.module.subsystem.SubsystemFormDefault;
import org.socyno.webfwk.module.subsystem.SubsystemService;
import org.socyno.webfwk.module.vcs.common.FieldVcsRefsName;
import org.socyno.webfwk.module.vcs.common.FilterVcsRefsName;
import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.util.remote.R;
import org.socyno.webfwk.util.tool.ClassUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class SubsystemController {
    
    /**
     * 用户动态选项列表
     */
    @RequestMapping(value = "/options/user", method = RequestMethod.GET)
    public R queryUsersAsOption(String namelike, HttpServletRequest request) throws Exception {
        return R.ok().setData(ClassUtil.getSingltonInstance(FieldSystemUser.class)
                .queryDynamicOptions(new FilterBasicKeyword(namelike, null, null)));
    }
    
    /**
     * 业务系统动态选项列表
     */
    @RequestMapping(value = "/options/subsystem", method = RequestMethod.GET)
    public R querySubsystemsAsOption(String namelike, HttpServletRequest request) throws Exception {
        return R.ok().setData(ClassUtil.getSingltonInstance(FieldSubsystemNoAnyLimited.class)
                .queryDynamicOptions(new FilterBasicKeyword(namelike, null, null)));
    }
    
    /**
     * 应用清单动态选项列表
     */
    @RequestMapping(value = "/options/application", method = RequestMethod.GET)
    public R queryApplicationsAsOption(String namelike, String offlineIncluded, HttpServletRequest request)
            throws Exception {
        if (CommonUtil.parseBoolean(offlineIncluded)) {
            return R.ok().setData(ClassUtil.getSingltonInstance(FieldApplicationOfflineIncluded.class)
                    .queryDynamicOptions(new FilterBasicKeyword(namelike, null, null)));
        }
        return R.ok().setData(ClassUtil.getSingltonInstance(FieldApplicationAll.class)
                .queryDynamicOptions(new FilterBasicKeyword(namelike, null, null)));
    }
    
    /**
     * 应用仓库分支(标签、补丁)动态选项列表
     */
    @RequestMapping(value = "/options/application/refsname/{applicationId}/{vcsType}", method = RequestMethod.GET)
    public R queryVscReferencesAsOption(Long applicationId, String vcsRefsType, String keyword,
            HttpServletRequest request) throws Exception {
        return R.ok().setData(ClassUtil.getSingltonInstance(FieldVcsRefsName.class)
                .queryDynamicOptions(new FilterVcsRefsName(applicationId, vcsRefsType, keyword)));
    }
    
    /**
     * 所有的产品线/组清单
     */
    @RequestMapping(value = "/productlines/list", method = RequestMethod.GET)
    public R queryProductlines(String namelike, Long applicationId, Long subsystemId, Long ownerId, Integer limit,
            Long page) throws Exception {
        return R.ok()
                .setData(ProductlineService.getInstance().listFormWithTotal(ProductlineService.QUERIES.DEFAULT,
                        new ProductlineQueryDefault(namelike, limit, page).setOwnerId(ownerId)
                                .setSubsystemId(subsystemId).setApplicationId(applicationId)
                                .setDisableIncluded(false)));
    }
    
    /**
     * 获取业务系统的基本信息
     */
    @RequestMapping(value = "/subsystems/{subsystemId}/simple", method = RequestMethod.GET)
    public R getSimpleSubsystem(@PathVariable Long subsystemId) throws Exception {
        return R.ok().setData(SubsystemService.getInstance().getForm(SubsystemFormDefault.class, subsystemId));
    }
    
    /**
     * 有查看授权的应用清单
     */
    @RequestMapping(value = "/applications/visible", method = RequestMethod.GET)
    public R queryVisibleApps(Boolean bookmarked, String namelike, String type, Long subsystemId, String codeLevel,
            Integer limit, Long page) throws Exception {
        return R.ok()
                .setData(ApplicationService.getInstance().listFormWithTotal(ApplicationService.QUERIES.DEFAULT,
                        new ApplicationQueryDefault(limit, page).setNamelike(namelike).setType(type)
                                .setBookmarked(CommonUtil.ifNull(bookmarked, false)).setCodeLevel(codeLevel)
                                .setSubsystemId(subsystemId).setOfflineIncluded(false)));
    }
    
    /**
     * 所有的应用清单
     */
    @RequestMapping(value = "/applications/list", method = RequestMethod.GET)
    public R queryAllApps(String namelike, String type, Long subsystemId, String codeLevel, Integer limit,
            Long page) throws Exception {
        return R.ok()
                .setData(ApplicationService.getInstance().listFormWithTotal(ApplicationService.QUERIES.ALLAPPS,
                        new ApplicationQueryAll(limit, page).setNamelike(namelike).setType(type)
                                .setBookmarked(false).setCodeLevel(codeLevel)
                                .setSubsystemId(subsystemId)));
    }
    
    /**
     * 应用的基本信息
     */
    @RequestMapping(value = "/applications/{applicationId}/simple", method = RequestMethod.GET)
    public R getSimpleApp(@PathVariable long applicationId) throws Exception {
        return R.ok().setData(ApplicationService.getInstance().getSimple(applicationId));
    }
    
    /**
     * 添加应用收藏
     */
    @RequestMapping(value = "/applications/{applicationId}/bookmark/add", method = RequestMethod.POST)
    public R addBookmark(@PathVariable long applicationId) throws Exception {
        ApplicationBookmarkService.getInstance().add(applicationId);
        return R.ok();
    }
    
    /**
     * 取消应用收藏
     */
    @RequestMapping(value = "/applications/{applicationId}/bookmark/delete", method = RequestMethod.POST)
    public R cancelBookmark(@PathVariable long applicationId) throws Exception {
        ApplicationBookmarkService.getInstance().delete(applicationId);
        return R.ok();
    }
    
    /**
     * 查询应用已配置的可部署环境选项
     */
    @RequestMapping(value = "/options/appenv/{applicationId}", method = RequestMethod.GET)
    public R queryApplicationEnvOptions(@PathVariable long applicationId) throws Exception {
        return R.ok().setData(ClassUtil.getSingltonInstance(FieldDeployEnvironment.class).queryDynamicOptions(
                new FilterBasicKeyword("" , ApplicationService.getInstance().getFormName() , applicationId)));
    }
    
}
