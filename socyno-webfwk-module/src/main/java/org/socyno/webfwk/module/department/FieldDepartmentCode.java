package org.socyno.webfwk.module.department;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.socyno.webfwk.module.department.FieldDepartment.OptionProductline;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.ConvertUtil;
import org.socyno.webfwk.util.tool.StringUtils;

public class FieldDepartmentCode extends FieldTableView {
    
    public static class OptionProductlineCode extends OptionProductline {
        public OptionProductlineCode(Long id, String code, String name) {
            super(id, code, name);
        }
        
        @Override
        public String getOptionValue() {
            return "" + getCode();
        }
        
        @Override
        public void setOptionValue(String value) {
            setCode(value);
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && OptionProductlineCode.class.equals(obj.getClass()) &&
                    this.getOptionValue().equals(((OptionProductlineCode) obj).getOptionValue());
        }

        @Override
        public int hashCode() {
            return getOptionValue().hashCode();
        }
    }
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    /**
     * 获取产品线的基本信息
     */
    public OptionProductlineCode queryDynamicValue(String productlineCode) throws Exception {
        if (StringUtils.isBlank(productlineCode)) {
            return null;
        }
        List<OptionProductlineCode> list;
        if ((list = queryDynamicValues(new Object[] { productlineCode })) == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
    
    /**
     * 获取产品线的编号列表，获取产品线清单
     */
    @Override
    public List<OptionProductlineCode> queryDynamicValues(Object[] productlineCodes) throws Exception {
        if ((productlineCodes = ConvertUtil.asNonBlankUniqueTrimedStringArray(productlineCodes)).length <= 0) {
            return Collections.emptyList();
        }
        List<DepartmentFormSimple> list;
        if ((list = DepartmentService.getInstance().list(DepartmentFormSimple.class, (String[]) productlineCodes)) == null) {
            return Collections.emptyList();
        }
        List<OptionProductlineCode> options = new ArrayList<>();
        for (DepartmentFormSimple form : list) {
            options.add(new OptionProductlineCode(form.getId(), form.getCode(), form.getName()));
        }
        return options;
    }
    
    /**
     * 覆盖父类的方法，根据关键字检索业务系统
     */
    @SuppressWarnings("unchecked")
    public List<OptionProductlineCode> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        List<DepartmentFormSimple> list = (List<DepartmentFormSimple>) DepartmentService.getInstance()
                .listForm(DepartmentService.QUERIES.OPTIONS,
                        new DepartmentQueryOptions(filter.getKeyword()))
                .getList();
        if (list == null) {
            return Collections.emptyList();
        }
        List<OptionProductlineCode> options = new ArrayList<>();
        for (DepartmentFormSimple form : list) {
            options.add(new OptionProductlineCode(form.getId(), form.getCode(), form.getName()));
        }
        return options;
    }
}
