package org.socyno.webfwk.state.sugger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.socyno.webfwk.state.basic.AbstractStateForm;
import org.socyno.webfwk.state.model.CommonFormAttachement;
import org.socyno.webfwk.state.service.AttachmentService;
import org.socyno.webfwk.state.sugger.AbstractStateFormSugger.Definition;
import org.socyno.webfwk.state.sugger.AbstractStateFormSugger.OptionClass;
import org.socyno.webfwk.util.state.field.FieldFormAttachements;
import org.socyno.webfwk.util.tool.StringUtils;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;

public class SuggerDefinitionFormAttachment extends Definition {
    
    @Getter
    private final static SuggerDefinitionFormAttachment instance = new SuggerDefinitionFormAttachment();
    
    private final static Map<Class<?>, String> FORMNAMES = new ConcurrentHashMap<>();
    
    public static void addFormName(Class<?> clazz, String formName) {
        FORMNAMES.put(clazz, formName);
    }
    
    private static String getFormName(Field field) {
        return FORMNAMES.get(field.getDeclaringClass());
    }
    
    private static final OptionClass<?> optionClass = new OptionClass<CommonFormAttachement>() {
        @Override
        protected Class<CommonFormAttachement> getType() {
            return CommonFormAttachement.class;
        }
        
        @Override
        public Class<?> getAttrType() {
            return FieldFormAttachements.class;
        }
        
        @Override
        protected Collection<CommonFormAttachement> queryOptions(Collection<CommonFormAttachement> values)
                throws Exception {
            if (values == null || values.size() <= 0) {
                return null;
            }
            Long formId;
            String formName;
            String feildName;
            List<Object> fieldArgs;
            Map<String, List<Object>> formsArgs = new HashMap<>();
            for (CommonFormAttachement v : values) {
                ;
                if ((formId = v.getFormId()) == null) {
                    continue;
                }
                if ((formName = v.getFormName()) == null) {
                    continue;
                }
                if ((feildName = v.getField()) == null) {
                    continue;
                }
                String argsKey = String.format("%s:%s", formName, feildName);
                if ((fieldArgs = formsArgs.get(argsKey)) == null) {
                    fieldArgs = new LinkedList<>();
                    fieldArgs.add(formName);
                    fieldArgs.add(feildName);
                    formsArgs.put(argsKey, fieldArgs);
                }
                fieldArgs.add(formId);
            }
            List<CommonFormAttachement> fieldsAtts;
            List<CommonFormAttachement> formsAtts = new LinkedList<>();
            for (List<Object> queryArgs : formsArgs.values()) {
                if ((fieldsAtts = AttachmentService.queryByTargetFormFeild(CommonFormAttachement.class,
                        (String) queryArgs.remove(0), (String) queryArgs.remove(0), queryArgs.toArray())) != null) {
                    formsAtts.addAll(fieldsAtts);
                }
            }
            return formsAtts;
        }
        
        @Override
        protected Object parseOriginValue(Object form, Field field, OptionWrapper wrapper, Attributes fieldAttrs)
                throws Exception {
            String formName;
            if (!(form instanceof AbstractStateForm) || StringUtils.isBlank(formName = getFormName(field))) {
                return null;
            }
            CommonFormAttachement formAtt = new CommonFormAttachement();
            formAtt.setFormId(((AbstractStateForm) form).getId());
            formAtt.setFormName(formName);
            formAtt.setField(field.getName());
            return wrapper.fromFieldFlatValues(CommonFormAttachement.class,
                    Arrays.asList(new CommonFormAttachement[] { formAtt }));
        }
        
        @Override
        protected void fillOriginOption(Object form, CommonFormAttachement origin, OptionWrapper wrapper, Field field,
                Attributes fieldAttrs) throws Exception {
            return;
        }
        
        @Override
        protected void setMatchedValues(Object form, Field field, Object[] flatValues,
                Map<Object, Object> mappedFinalValues, Definition.OptionWrapper wrapper, Attributes fieldAttrs)
                throws Exception {
            if (flatValues == null || mappedFinalValues == null) {
                return;
            }
            CommonFormAttachement origin;
            CommonFormAttachement fetched;
            List<CommonFormAttachement> matched = new ArrayList<>();
            for (Object v : flatValues) {
                origin = (CommonFormAttachement) v;
                for (Object m : mappedFinalValues.values()) {
                    fetched = (CommonFormAttachement) m;
                    if (StringUtils.equals(origin.getFormName(), fetched.getFormName())
                            && origin.getFormId().equals(fetched.getFormId())
                            && StringUtils.equals(origin.getField(), fetched.getField())) {
                        matched.add(fetched);
                    }
                }
                field.setAccessible(true);
                field.set(form, wrapper.fromFieldFlatValues(CommonFormAttachement.class, matched));
                break;
            }
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}