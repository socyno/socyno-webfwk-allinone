package org.socyno.webfwk.module.subsystem;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.socyno.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import org.socyno.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;
import org.socyno.webfwk.util.tool.ClassUtil;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;

public class SuggerDefinitionSubsystem extends Definition {
    
    @Getter
    private static final SuggerDefinitionSubsystem instance = new SuggerDefinitionSubsystem();
    
    private static final OptionClass<?> optionClass = new OptionClass<SubsystemFormSimple>() {
        @Override
        protected Class<SubsystemFormSimple> getType() {
            return SubsystemFormSimple.class;
        }
        
        @Override
        public Class<?> getAttrType() {
            return null;
        }
        
        @Override
        protected Collection<SubsystemFormSimple> queryOptions(Collection<SubsystemFormSimple> values) throws Exception {
            if (values == null) {
                return null;
            }
            String optionValue;
            Set<String> optionValues = new HashSet<>();
            for (SubsystemFormSimple v : values) {
                if ((optionValue = v.getOptionValue()) != null) {
                    optionValues.add(optionValue);
                }
            }
            return ClassUtil.getSingltonInstance(FieldSubsystemNoAnyLimited.class)
                    .queryDynamicValues(optionValues.toArray());
        }
        
        @Override
        protected void fillOriginOption(Object form, SubsystemFormSimple origin, OptionWrapper wrapper, Field field,
                Attributes fieldAttrs) throws Exception {
            return;
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}
