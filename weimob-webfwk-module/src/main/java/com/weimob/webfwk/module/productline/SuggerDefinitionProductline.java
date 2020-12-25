package com.weimob.webfwk.module.productline;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.productline.FieldProductline.OptionProductline;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;
import com.weimob.webfwk.util.tool.ClassUtil;

import lombok.Getter;

public class SuggerDefinitionProductline extends Definition {
    
    @Getter
    private static final SuggerDefinitionProductline Instance = new SuggerDefinitionProductline();
    
    private static final OptionClass<?> optionClass = new OptionClass<OptionProductline>() {
        @Override
        protected Class<OptionProductline> getType() {
            return OptionProductline.class;
        }
        
        @Override
        public Class<?> getAttrType() {
            return null;
        }
        
        @Override
        protected Collection<OptionProductline> queryOptions(Collection<OptionProductline> values) throws Exception {
            if (values == null) {
                return null;
            }
            String optionValue;
            Set<String> optionValues = new HashSet<>();
            for (OptionProductline v : values) {
                if ((optionValue = v.getOptionValue()) != null) {
                    optionValues.add(optionValue);
                }
            }
            return ClassUtil.getSingltonInstance(FieldProductline.class).queryDynamicValues(optionValues.toArray());
        }
        
        @Override
        protected void fillOriginOption(Object form, OptionProductline origin, OptionWrapper wrapper, Field field,
                Attributes fieldAttrs) throws Exception {
            return;
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}
