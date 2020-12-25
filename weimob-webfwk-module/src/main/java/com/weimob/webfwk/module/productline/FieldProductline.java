package com.weimob.webfwk.module.productline;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.weimob.webfwk.state.field.FilterBasicKeyword;
import com.weimob.webfwk.util.state.field.FieldTableView;
import com.weimob.webfwk.util.tool.ConvertUtil;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FieldProductline extends FieldTableView {
    
    @Data
    public static class OptionProductline implements FieldOption {
        
        @Attributes(title = "编号")
        private Long id;
        
        @Attributes(title = "代码")
        private String code;
        
        @Attributes(title = "名称")
        private String name;
        
        @Override
        public String getOptionValue() {
            return "" + getId();
        }
        
        @Override
        public String getOptionDisplay() {
            return String.format("%s:%s", getCode(), getName());
        }
        
        @Override
        public void setOptionValue(String value) {
            setId(new Long(value));
        }
        
        public OptionProductline(Long id, String code, String name) {
            setId(id);
            setCode(code);
            setName(name);
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && OptionProductline.class.equals(obj.getClass()) &&
                    this.getOptionValue().equals(((OptionProductline) obj).getOptionValue());
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
    public OptionProductline queryDynamicValue(Long productlineId) throws Exception {
        if (productlineId == null) {
            return null;
        }
        List<OptionProductline> list;
        if ((list = queryDynamicValues(new Object[] { productlineId })) == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
    
    /**
     * 获取产品线的编号列表，获取产品线清单
     */
    @Override
    public List<OptionProductline> queryDynamicValues(Object[] productlineIds) throws Exception {
        List<ProductlineFormSimple> list = ProductlineService.getInstance().listByIds(
                ProductlineFormSimple.class,
                ConvertUtil.asNonNullUniqueLongArray((Object[]) productlineIds)
            );
        if (list == null) {
            return Collections.emptyList();
        }
        List<OptionProductline> options = new ArrayList<>();
        for (ProductlineFormSimple form : list) {
            options.add(new OptionProductline(form.getId(), form.getCode(), form.getName()));
        }
        return options;
    }
    
    /**
     * 覆盖父类的方法，根据关键字检索业务系统
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<OptionProductline> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        FilterBasicKeyword keyword = (FilterBasicKeyword) filter;
        List<ProductlineFormSimple> list = (List<ProductlineFormSimple>) ProductlineService.getInstance()
                .listForm(ProductlineService.QUERIES.OPTIONS,
                        new ProductlineQueryOptions(keyword.getKeyword()))
                .getList();
        if (list == null) {
            return Collections.emptyList();
        }
        List<OptionProductline> options = new ArrayList<>();
        for (ProductlineFormSimple form : list) {
            options.add(new OptionProductline(form.getId(), form.getCode(), form.getName()));
        }
        return options;
    }
}
