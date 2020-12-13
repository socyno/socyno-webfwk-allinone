package org.socyno.webfwk.module.release.build;

import java.util.List;

import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.util.state.field.FieldTableView;

public class FieldBuildService extends FieldTableView {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    /**
     * 构建服务下拉框
     * 
     */
    public static List<SystemBuildFormOption> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return SystemBuildService.getInstance().queryDynamicOptions(filter);
    }
}
