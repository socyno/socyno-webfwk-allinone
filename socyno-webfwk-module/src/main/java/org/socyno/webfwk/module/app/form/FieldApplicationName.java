package org.socyno.webfwk.module.app.form;

import com.github.reinert.jjschema.v1.FieldOption;
import java.util.List;

import org.socyno.webfwk.state.field.FilterBasicKeyword;

public class FieldApplicationName extends FieldApplication {
    
    public static class OptionApplication extends FieldApplication.OptionApplication implements FieldOption {
        @Override
        public String getOptionValue() {
            return "" + getName();
        }
        
        @Override
        public String getOptionDisplay() {
            return getName();
        }
        
        @Override
        public String getOptionGroup() {
            return getSubsystemName();
        }
        
        @Override
        public void setOptionValue(String value) {
            setName(value);
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && OptionApplication.class.equals(obj.getClass()) &&
                    this.getOptionValue().equals(((OptionApplication) obj).getOptionValue());
        }

        @Override
        public int hashCode() {
            return getOptionValue().hashCode();
        }
    }

    public List<FieldApplicationName.OptionApplication> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return queryOnlyVisibleOptions(FieldApplicationName.OptionApplication.class, filter, false);
    }

    public List<? extends FieldApplicationName.OptionApplication> queryDynamicValues(Object[] values) throws Exception {
        return queryDynamicValues(FieldApplicationName.OptionApplication.class, values, true);
    }

    public FieldApplicationName.OptionApplication queryDynamicValue(String name) throws Exception {
        if (name == null) {
            return null;
        }
        List<FieldApplicationName.OptionApplication> list;
        if ((list = queryDynamicValues(FieldApplicationName.OptionApplication.class, new Object[] { name }, true)) == null
                || list.size() != 1) {
            return null;
        }
        return list.get(0);
    }
}
