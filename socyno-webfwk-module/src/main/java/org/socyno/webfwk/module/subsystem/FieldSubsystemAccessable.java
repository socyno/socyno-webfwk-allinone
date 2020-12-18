package org.socyno.webfwk.module.subsystem;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import java.util.Collections;
import java.util.List;

import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.StringUtils;

public class FieldSubsystemAccessable extends FieldTableView {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    /**
     * 获取业务系统基本信息
     */
    public SubsystemFormSimple queryDynamicValue(Long subsystemId) throws Exception {
        return SubsystemService.getInstance().getForm(SubsystemFormSimple.class, subsystemId);
    }
    
    /**
     * 获取业务系统基本信息
     */
    @Override
    public List<SubsystemFormSimple> queryDynamicValues(Object[] subsystemIds) throws Exception {
        if (subsystemIds == null || subsystemIds.length <= 0) {
            return Collections.emptyList();
        }
        return SubsystemService.getInstance().listForm(SubsystemFormSimple.class,
                new SubsystemQueryAll(50, 1L).setIdsIn(StringUtils.join(subsystemIds, ','))).getList();
    }
    
    /**
     * 覆盖父类的方法，根据关键字检索业务系统
     */
    @Override
    public List<SubsystemFormSimple> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        FilterBasicKeyword keyword = (FilterBasicKeyword) filter;
        return SubsystemService.getInstance()
                .listForm(SubsystemFormSimple.class, new SubsystemQueryAccessable(keyword.getKeyword(), 50, 1L))
                .getList();
    }
}
