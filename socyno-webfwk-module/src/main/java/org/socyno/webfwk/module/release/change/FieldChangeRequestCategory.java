package org.socyno.webfwk.module.release.change;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.socyno.webfwk.module.release.change.ChangeRequestFormSimple.Category;
import org.socyno.webfwk.module.release.change.ChangeRequestFormSimple.ChangeType;
import org.socyno.webfwk.state.field.FilterBasicKeyword;
import org.socyno.webfwk.util.context.HttpMessageConverter;
import org.socyno.webfwk.util.state.field.FieldTableView;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldOptionsFilter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class FieldChangeRequestCategory extends FieldTableView {
    
    @Getter
    @Setter
    @ToString
    public static class OptionChangeCategory implements FieldOption {
        private String optionValue;
        private String optionDisplay;
        
        @Override
        public int hashCode() {
            return getOptionValue() == null ? -1 : getOptionValue().hashCode();
        }
        
        @Override
        public boolean equals(Object anthor) {
            if (anthor instanceof OptionChangeCategory) {
                return StringUtils.equals(getOptionValue(), ((OptionChangeCategory) anthor).getOptionValue());
            }
            return false;
        }
    }
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    @Override
    public List<? extends FieldOption> queryDynamicOptions(FieldOptionsFilter filter) throws Exception {
        return queryDynamicOptions((FilterBasicKeyword)filter, false);
    }
    
    protected List<? extends FieldOption> queryDynamicOptions(FilterBasicKeyword filter, boolean allAllowed) throws Exception {
        List<Category> categories = null;
        if (filter != null && StringUtils.isNotBlank(filter.getFormJson())) {
            ChangeRequestFormSimple form = HttpMessageConverter.toInstance(ChangeRequestFormSimple.class, filter.getFormJson());
            ChangeType changeType;
            if (StringUtils.isNotBlank(form.getChangeType())
                    && (changeType = ChangeType.get(form.getChangeType())) != null) {
                categories = Category.get(changeType);
            }
        }
        if (categories == null || categories.isEmpty()) {
            if (!allAllowed) {
                return Collections.emptyList();
            }
            categories = Arrays.asList(Category.values());
        }
        OptionChangeCategory category;
        List<OptionChangeCategory> options = new LinkedList<>();
        for (Category c : categories) {
            category = new OptionChangeCategory();
            category.setOptionValue(c.getCode());
            category.setOptionDisplay(c.getDisplay());
            options.add(category);
        }
        return options;
    }
}
