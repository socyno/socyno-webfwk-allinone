package com.weimob.webfwk.module.release.change;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.reinert.jjschema.Attributes;
import com.weimob.webfwk.module.application.FieldApplication;
import com.weimob.webfwk.module.application.FieldApplication.OptionApplication;
import com.weimob.webfwk.state.abs.AbstractStateFormInput;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import com.weimob.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;
import com.weimob.webfwk.util.tool.CommonUtil;

import lombok.Getter;
import lombok.Setter;

public class SuggerDefinitionChangeApplication extends Definition {
    
    @Getter
    private final static SuggerDefinitionChangeApplication Instance = new SuggerDefinitionChangeApplication();
    
    @Getter
    @Setter
    public static class ChangeApplication {
        
        private long changeRequestId;
        
        private long applicationId;
        
    }
    
    private static final OptionClass<?> optionClass = new OptionClass<OptionApplication>() {
        @Override
        protected Class<OptionApplication> getType() {
            return OptionApplication.class;
        }
        
        @Override
        public Class<?> getAttrType() {
            return FieldChangeRequestApplication.class;
        }
        
        private final ThreadLocal<List<ChangeApplication>> threadChangeApps = new ThreadLocal<List<ChangeApplication>>();
        
        @Override
        protected Collection<OptionApplication> queryOptions(Collection<OptionApplication> values)
                throws Exception {
            threadChangeApps.set(null);
            if (values == null || values.size() <= 0) {
                return null;
            }
            Set<Object> queryArgs = new HashSet<>();
            for (OptionApplication v : values) {
                queryArgs.add(v.getId());
            }
            if (queryArgs.size() <= 0 ) {
                return Collections.emptyList();
            }
            List<ChangeApplication> changeApps = ChangeRequestService.getInstance().getFormBaseDao()
                    .queryAsList(ChangeApplication.class, String.format(
                            "SELECT * FROM release_change_application WHERE change_request_id IN (%s)",
                            CommonUtil.join("?", queryArgs.size(), ",")), queryArgs.toArray());
            if (changeApps == null || changeApps.size() <= 0) {
                return Collections.emptyList();
            }
            queryArgs.clear(); 
            for (ChangeApplication v : changeApps) {
                queryArgs.add(v.getApplicationId());
            }
            threadChangeApps.set(changeApps);
            return FieldApplication.queryDynamicValues(OptionApplication.class, queryArgs.toArray(), false);
        }
        
        @Override
        protected Object parseOriginValue(Object form, Field field, OptionWrapper wrapper, Attributes fieldAttrs)
                throws Exception {
            Long formId;
            if (!(form instanceof AbstractStateFormInput) || (formId = ((AbstractStateFormInput) form).getId()) == null) {
                return null;
            }
            OptionApplication formApp = new OptionApplication();
            formApp.setId(formId);
            return wrapper.fromFieldFlatValues(OptionApplication.class,
                    Arrays.asList(new OptionApplication[] { formApp }));
        }
        
        @Override
        protected void fillOriginOption(Object form, OptionApplication origin, OptionWrapper wrapper, Field field,
                Attributes fieldAttrs) throws Exception {
            return;
        }
        
        @Override
        protected void setMatchedValues(Object form, Field field, Object[] flatValues,
                Map<Object, Object> mappedFinalValues, OptionWrapper wrapper, Attributes fieldAttrs)
                throws Exception {
            Long formId;
            List<ChangeApplication> changeApps;
            if ((changeApps = threadChangeApps.get()) == null || (formId = ((AbstractStateFormInput) form).getId()) == null
                        || mappedFinalValues == null) {
                return;
            }
            OptionApplication option;
            List<OptionApplication> values = new LinkedList<>();
            Map<Long, OptionApplication> options = new HashMap<>();;
            for (ChangeApplication app : changeApps) {
                if (formId != app.getChangeRequestId()) {
                    continue;
                }
                long appId = app.getApplicationId();
                if ((option = options.get(appId)) == null) {
                    options.put(appId, option = new OptionApplication());
                    option.setId(appId);
                }
                if ((option = (OptionApplication) mappedFinalValues.get(option)) != null ) {
                    values.add(option);
                }
            }
            field.setAccessible(true);
            field.set(form, wrapper.fromFieldFlatValues(OptionApplication.class, values));
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}