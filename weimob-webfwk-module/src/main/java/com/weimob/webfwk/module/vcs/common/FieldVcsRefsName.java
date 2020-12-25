package com.weimob.webfwk.module.vcs.common;

import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.github.reinert.jjschema.v1.FieldType;
import com.weimob.webfwk.module.application.ApplicationService;
import com.weimob.webfwk.util.tool.StringUtils;

public class FieldVcsRefsName extends FieldType {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldType.FieldOptionsType.DYNAMIC;
    }
    
    /**
     * 声明查询的入参类型
     */
    @Override
    public Class<? extends FieldOptionsFilter> getDynamicFilterFormClass() {
        return FilterVcsRefsName.class;
    }
    
    /**
     * 应用分支、标签及补丁下拉框字段定义
     * 
     */
    @Override
    public List<OptionVcsRefsName> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        FilterVcsRefsName cond = (FilterVcsRefsName) filter;
        if (cond.getFormId() == null || StringUtils.isBlank(cond.getVcsRefsType()) || !StringUtils
                .equalsIgnoreCase(ApplicationService.getInstance().getFormName(), cond.getFormName())) {
            return Collections.emptyList();
        }
        
        if (VcsRefsType.Branch.name().equalsIgnoreCase(cond.getVcsRefsType())) {
            return VcsUnifiedService.CommonCloud.listBranches(cond.getFormId(), cond.getKeyword(), 1, 100);
        }
        
        if (VcsRefsType.Tag.name().equalsIgnoreCase(cond.getVcsRefsType())) {
            return VcsUnifiedService.CommonCloud.listTags(cond.getFormId(), cond.getKeyword(), 1, 100);
        }
        
        if (VcsRefsType.Patch.name().equalsIgnoreCase(cond.getVcsRefsType())) {
            return VcsUnifiedService.CommonCloud.listPatches(cond.getFormId(), cond.getKeyword(), 1, 100);
        }
        
        return Collections.emptyList();
    }
}
