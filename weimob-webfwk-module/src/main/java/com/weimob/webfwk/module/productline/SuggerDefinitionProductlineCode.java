package com.weimob.webfwk.module.productline;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.productline.FieldProductlineCode.OptionProductlineCode;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;
import com.weimob.webfwk.util.tool.ClassUtil;

import lombok.Getter;

public class SuggerDefinitionProductlineCode extends Definition {
    
    @Getter
    private static final SuggerDefinitionProductlineCode Instance = new SuggerDefinitionProductlineCode();
    
    private static final OptionClass<?> optionClass = new OptionClass<OptionProductlineCode>() {
        @Override
        protected Class<OptionProductlineCode> getType() {
            return OptionProductlineCode.class;
        }
        
        @Override
        public Class<?> getAttrType() {
            return null;
        }
        
        @Override
        protected Collection<OptionProductlineCode> queryOptions(Collection<OptionProductlineCode> values)
                throws Exception {
            if (values == null) {
                return null;
            }
            String optionValue;
            Set<String> optionValues = new HashSet<>();
            for (OptionProductlineCode v : values) {
                if ((optionValue = v.getOptionValue()) != null) {
                    optionValues.add(optionValue);
                }
            }
            return ClassUtil.getSingltonInstance(FieldProductlineCode.class).queryDynamicValues(optionValues.toArray());
        }
        
        @Override
        protected void fillOriginOption(Object form, OptionProductlineCode origin, OptionWrapper wrapper, Field field,
                Attributes fieldAttrs) throws Exception {
            return;
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}
