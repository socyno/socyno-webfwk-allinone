package com.weimob.webfwk.module.application;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;
import com.weimob.webfwk.util.tool.ClassUtil;

import lombok.Getter;

public class SuggerDefinitionApplicationName extends Definition {
    
    @Getter
    private static final SuggerDefinitionApplicationName Instance = new SuggerDefinitionApplicationName();
    
    private static final OptionClass<?> optionClass = new OptionClass<FieldApplicationName.OptionApplication>() {
        @Override
        protected Class<FieldApplicationName.OptionApplication> getType() {
            return FieldApplicationName.OptionApplication.class;
        }
        
        @Override
        public Class<?> getAttrType() {
            return null;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        protected Collection<FieldApplicationName.OptionApplication> queryOptions(
                Collection<FieldApplicationName.OptionApplication> values) throws Exception {
            if (values == null) {
                return null;
            }
            String optionValue;
            Set<String> optionValues = new HashSet<>();
            for (FieldApplicationName.OptionApplication v : values) {
                if ((optionValue = v.getOptionValue()) != null) {
                    optionValues.add(optionValue);
                }
            }
            return (Collection<FieldApplicationName.OptionApplication>) ClassUtil
                    .getSingltonInstance(FieldApplicationName.class).queryDynamicValues(optionValues.toArray());
        }
        
        @Override
        protected void fillOriginOption(Object form, FieldApplicationName.OptionApplication origin,
                OptionWrapper wrapper, Field field, Attributes fieldAttrs) throws Exception {
            return;
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}
