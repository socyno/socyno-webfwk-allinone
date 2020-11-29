package org.socyno.webfwk.module.vcs.common;

import java.util.Collections;
import java.util.List;

import org.socyno.webfwk.module.app.form.ApplicationService;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.github.reinert.jjschema.v1.FieldType;

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
     * 应用分支、标签及补丁下拉框字段定义s
     * 
     */
    public List<OptionVcsRefsName> queryDynamicOptions(FilterVcsRefsName filter) throws Exception {
        if (filter.getFormId() == null || StringUtils.isBlank(filter.getVcsRefsType())
                || !StringUtils.equalsIgnoreCase(ApplicationService.DEFAULT.getFormName(), filter.getFormName())) {
            return Collections.emptyList();
        }
        if (VcsRefsType.Branch.name().equalsIgnoreCase(filter.getVcsRefsType())) {
            return VcsUnifiedService.CommonCloud.listBranches(filter.getFormId(), filter.getKeyword(), 1, 100);
        }
        
        if (VcsRefsType.Tag.name().equalsIgnoreCase(filter.getVcsRefsType())) {
            return VcsUnifiedService.CommonCloud.listTags(filter.getFormId(), filter.getKeyword(), 1, 100);
        }
        
        if (VcsRefsType.Patch.name().equalsIgnoreCase(filter.getVcsRefsType())) {
            return VcsUnifiedService.CommonCloud.listPatches(filter.getFormId(), filter.getKeyword(), 1, 100);
        }
        
        return Collections.emptyList();
    }
}
