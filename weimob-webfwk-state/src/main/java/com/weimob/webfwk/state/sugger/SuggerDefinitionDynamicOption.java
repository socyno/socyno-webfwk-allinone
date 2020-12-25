package com.weimob.webfwk.state.sugger;

import java.lang.reflect.Field;
import java.util.Collection;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.AbstractFieldDynamicStandard;
import com.weimob.webfwk.state.field.OptionDynamicStandard;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;
import com.weimob.webfwk.util.tool.ClassUtil;
import com.weimob.webfwk.util.tool.StringUtils;

import lombok.Getter;

public class SuggerDefinitionDynamicOption extends Definition {
    
    @Getter
    private static final SuggerDefinitionDynamicOption instance = new SuggerDefinitionDynamicOption();
    
    private final OptionClass<?> optionClass = new OptionClass<OptionDynamicStandard>() {
        @Override
        protected Class<OptionDynamicStandard> getType() {
            return OptionDynamicStandard.class;
        }
        
        @Override
        public Class<?> getAttrType() {
            return AbstractFieldDynamicStandard.class;
        }
        
        @Override
        protected Collection<OptionDynamicStandard> queryOptions(Collection<OptionDynamicStandard> values) throws Exception {
            return AbstractFieldDynamicStandard.queryDynamicValues(values);
        }
        
        @Override
        protected void fillOriginOption(Object form, OptionDynamicStandard fieldValue, OptionWrapper wrapper, Field field,
                Attributes fieldAttrs) throws Exception {
            if (fieldValue == null || fieldAttrs == null || !(fieldValue instanceof OptionDynamicStandard)) {
                return;
            }
            String category = "";
            AbstractFieldDynamicStandard fieldTypeInstance = (AbstractFieldDynamicStandard) ClassUtil
                    .getSingltonInstance(fieldAttrs.type());
            if (fieldTypeInstance.categoryRequired()
                    && StringUtils.isBlank(category = fieldTypeInstance.getCategoryFieldValue(form))) {
                return;
            }
            ((OptionDynamicStandard) fieldValue).setCategory(category).setClassPath(fieldAttrs.type().getName());
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}
