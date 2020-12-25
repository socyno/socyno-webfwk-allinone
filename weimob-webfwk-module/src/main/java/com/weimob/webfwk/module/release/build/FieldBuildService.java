package com.weimob.webfwk.module.release.build;

import java.util.List;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.weimob.webfwk.state.field.FilterBasicKeyword;
import com.weimob.webfwk.util.state.field.FieldTableView;

public class FieldBuildService extends FieldTableView {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    /**
     * 构建服务下拉框
     * 
     */
    @Override
    public List<SystemBuildFormOption> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        return SystemBuildService.getInstance().queryDynamicOptions((FilterBasicKeyword)filter);
    }
}
