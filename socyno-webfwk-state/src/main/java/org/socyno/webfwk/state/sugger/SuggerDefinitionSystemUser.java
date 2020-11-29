package org.socyno.webfwk.state.sugger;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.socyno.webfwk.state.field.FieldSystemUser;
import org.socyno.webfwk.state.field.OptionSystemUser;
import org.socyno.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import org.socyno.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;
import org.socyno.webfwk.util.tool.ClassUtil;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;

public class SuggerDefinitionSystemUser extends Definition {
    
    @Getter
    private static final SuggerDefinitionSystemUser instance = new SuggerDefinitionSystemUser();
    
    private static final OptionClass<?> optionClass = new OptionClass<OptionSystemUser>() {
        @Override
        protected Class<OptionSystemUser> getType() {
            return OptionSystemUser.class;
        }
        
        @Override
        public Class<?> getAttrType() {
            return null;
        }
        
        @Override
        protected Collection<OptionSystemUser> queryOptions(Collection<OptionSystemUser> values) throws Exception {
            if (values == null) {
                return null;
            }
            String optionValue;
            Set<String> optionValues = new HashSet<>();
            for (OptionSystemUser v : values) {
                if ((optionValue = v.getOptionValue()) != null) {
                    optionValues.add(optionValue);
                }
            }
            return ClassUtil.getSingltonInstance(FieldSystemUser.class).queryDynamicValues(optionValues.toArray());
        }
        
        @Override
        protected void fillOriginOption(Object form, OptionSystemUser origin, OptionWrapper wrapper, Field field,
                Attributes fieldAttrs) throws Exception {
            return;
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}
