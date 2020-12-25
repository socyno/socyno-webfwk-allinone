package com.weimob.webfwk.module.release.change;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.release.change.ChangeRequestFormSimple.Category;
import com.weimob.webfwk.module.release.change.FieldChangeRequestCategory.OptionChangeCategory;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;

import lombok.Getter;

public class SuggerDefinitionChangeCategory extends Definition {
    
    @Getter
    private static final SuggerDefinitionChangeCategory Instance = new SuggerDefinitionChangeCategory();
    
    private static final OptionClass<?> optionClass = new OptionClass<OptionChangeCategory>() {
        @Override
        protected Class<OptionChangeCategory> getType() {
            return OptionChangeCategory.class;
        }
        
        @Override
        public Class<?> getAttrType() {
            return null;
        }
        
        @Override
        protected Collection<OptionChangeCategory> queryOptions(Collection<OptionChangeCategory> values) throws Exception {
            if (values == null) {
                return null;
            }
            String optionValue;
            Category singleCategory;
            OptionChangeCategory singleOption;
            Map<String, OptionChangeCategory> options = new HashMap<>();
            for (OptionChangeCategory v : values) {
                if (options.containsKey(optionValue = v.getOptionValue())) {
                    continue;
                }
                if ((singleCategory = Category.get(optionValue)) != null) {
                    singleOption = new OptionChangeCategory();
                    singleOption.setOptionValue(singleCategory.getCode());
                    singleOption.setOptionDisplay(singleCategory.getDisplay());
                    options.put(optionValue, singleOption);
                }
            }
            return options.values();
        }
        
        @Override
        protected void fillOriginOption(Object form, OptionChangeCategory origin, OptionWrapper wrapper,
                Field field, Attributes fieldAttrs) throws Exception {
            return;
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}
