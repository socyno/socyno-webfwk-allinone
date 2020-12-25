package com.weimob.webfwk.state.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.weimob.webfwk.state.module.role.SystemRoleFormSimple;
import com.weimob.webfwk.state.module.role.SystemRoleService;
import com.weimob.webfwk.util.model.PagedList;
import com.weimob.webfwk.util.state.field.FieldTableView;
import com.weimob.webfwk.util.tool.ConvertUtil;

public class FieldSystemRole extends FieldTableView {
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    /**
     * 覆盖父类的方法，根据关键字检索系统角色
     * @param keyword
     * @return
     * @throws Exception
     */
    @Override
    public List<OptionSystemRole> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        PagedList<SystemRoleFormSimple> list;
        FilterBasicKeyword keyword = (FilterBasicKeyword)filter;
        if ((list = SystemRoleService.getInstance().query(keyword.getKeyword(), 1, 100)) == null
                || list.getList() == null) {
            return null;
        }
        List<OptionSystemRole> options = new ArrayList<>();
        for (SystemRoleFormSimple l : list.getList()) {
            if (l == null) {
                continue;
            }
            OptionSystemRole option = new OptionSystemRole();
            option.setId(l.getId());
            option.setCode(l.getCode());
            option.setName(l.getName());
            options.add(option);
        }
        return options.stream().sorted(Comparator.comparing(OptionSystemRole::getOptionDisplay))
                        .collect(Collectors.toList());
    }
    
    /**
     * 覆盖父类的方法，根据选项值检索系统角色
     * @param values
     * @return
     * @throws Exception
     */
    @Override
    public List<OptionSystemRole> queryDynamicValues(Object[] values) throws Exception {
        Long[] ids;
        List<SystemRoleFormSimple> list = null;
        if (values == null || values.length <= 0 || (ids = ConvertUtil.asNonNullUniqueLongArray(values)).length <= 0
                || (list = SystemRoleService.getInstance().queryByIds(SystemRoleFormSimple.class, ids)) == null
                || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<OptionSystemRole> options = new ArrayList<>();
        for (SystemRoleFormSimple l : list) {
            options.add(new OptionSystemRole().setId(l.getId())
                            .setName(l.getName()).setCode(l.getCode()));
        }
        return options.stream().sorted(Comparator.comparing(OptionSystemRole::getOptionDisplay))
                        .collect(Collectors.toList());
    }
}
