package com.weimob.webfwk.state.sugger;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.field.FieldSystemBusinessAll;
import com.weimob.webfwk.state.field.OptionSystemBusiness;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;
import com.weimob.webfwk.util.tool.ClassUtil;

import lombok.Getter;

public class SuggerDefinitionSystemBusiness extends Definition {
    
    @Getter
    private static final SuggerDefinitionSystemBusiness instance = new SuggerDefinitionSystemBusiness();
    
    private static final OptionClass<?> optionClass = new OptionClass<OptionSystemBusiness>() {
        @Override
        protected Class<OptionSystemBusiness> getType() {
            return OptionSystemBusiness.class;
        }
        
        @Override
        public Class<?> getAttrType() {
            return null;
        }
        
        @Override
        protected Collection<OptionSystemBusiness> queryOptions(Collection<OptionSystemBusiness> values) throws Exception {
            if (values == null) {
                return null;
            }
            String optionValue;
            Set<String> optionValues = new HashSet<>();
            for (OptionSystemBusiness v : values) {
                if ((optionValue = v.getOptionValue()) != null) {
                    optionValues.add(optionValue);
                }
            }
            return ClassUtil.getSingltonInstance(FieldSystemBusinessAll.class).queryDynamicValues(optionValues.toArray());
        }
        
        @Override
        protected void fillOriginOption(Object form, OptionSystemBusiness origin, OptionWrapper wrapper, Field field,
                Attributes fieldAttrs) throws Exception {
            return;
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}
