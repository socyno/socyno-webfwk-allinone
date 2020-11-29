package org.socyno.webfwk.module.department;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.socyno.webfwk.module.department.FieldDepartmentCode.OptionProductlineCode;
import org.socyno.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import org.socyno.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;
import org.socyno.webfwk.util.tool.ClassUtil;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;

public class SuggerDefinitionDepartmentCode extends Definition {
    
    @Getter
    private static final SuggerDefinitionDepartmentCode instance = new SuggerDefinitionDepartmentCode();
    
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
            return ClassUtil.getSingltonInstance(FieldDepartmentCode.class).queryDynamicValues(optionValues.toArray());
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
