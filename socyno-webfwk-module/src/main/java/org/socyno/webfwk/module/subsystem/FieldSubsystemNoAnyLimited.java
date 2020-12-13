package org.socyno.webfwk.module.subsystem;

import java.util.List;

import org.socyno.webfwk.state.field.FilterBasicKeyword;

public class FieldSubsystemNoAnyLimited extends FieldSubsystemAccessors {
    /**
     * 覆盖父类的方法，根据关键字检索业务系统
     */
    public List<SubsystemFormSimple> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return SubsystemService.getInstance()
                .list(SubsystemFormSimple.class, new SubsystemQueryAll(50, 1L).setKeyword(filter.getKeyword()))
                .getList();
    }
}
