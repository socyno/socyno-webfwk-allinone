package org.socyno.webfwk.module.application;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.socyno.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import org.socyno.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;
import org.socyno.webfwk.util.tool.ClassUtil;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;

public class SuggerDefinitionApplication extends Definition {
    
    @Getter
    private static final SuggerDefinitionApplication Instance = new SuggerDefinitionApplication();
    
    private static final OptionClass<?> optionClass = new OptionClass<FieldApplication.OptionApplication>() {
        @Override
        protected Class<FieldApplication.OptionApplication> getType() {
            return FieldApplication.OptionApplication.class;
        }
        
        @Override
        public Class<?> getAttrType() {
            return null;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        protected Collection<FieldApplication.OptionApplication> queryOptions(
                Collection<FieldApplication.OptionApplication> values) throws Exception {
            if (values == null) {
                return null;
            }
            String optionValue;
            Set<String> optionValues = new HashSet<>();
            for (FieldApplication.OptionApplication v : values) {
                if ((optionValue = v.getOptionValue()) != null) {
                    optionValues.add(optionValue);
                }
            }
            return (Collection<FieldApplication.OptionApplication>) ClassUtil
                    .getSingltonInstance(FieldApplication.class).queryDynamicValues(optionValues.toArray());
        }
        
        @Override
        protected void fillOriginOption(Object form, FieldApplication.OptionApplication origin, OptionWrapper wrapper,
                Field field, Attributes fieldAttrs) throws Exception {
            return;
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}
