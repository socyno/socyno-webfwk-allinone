package com.weimob.webfwk.module.subsystem;

import java.util.List;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.weimob.webfwk.state.field.FilterBasicKeyword;

public class FieldSubsystemManagement extends FieldSubsystemAccessable {
    /**
     * 覆盖父类的方法，根据关键字检索业务系统
     */
    @Override
    public List<SubsystemFormSimple> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        return SubsystemService.getInstance()
                .listForm(SubsystemFormSimple.class,
                        new SubsystemQueryManagement(50, 1L).setKeyword(((FilterBasicKeyword) filter).getKeyword()))
                .getList();
    }
}
