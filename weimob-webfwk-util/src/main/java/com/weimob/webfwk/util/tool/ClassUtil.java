package com.weimob.webfwk.util.tool;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.JJSchemaUtil;
import com.github.reinert.jjschema.JJSchemaUtil.NamedTypeEntity;
import com.github.reinert.jjschema.v1.CustomAttributesProccessor;
import com.github.reinert.jjschema.v1.CustomSchemaWrapper;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;
import com.github.reinert.jjschema.v1.FieldType.FieldOptionsType;
import com.google.gson.reflect.TypeToken;
import com.weimob.webfwk.util.exception.FormValidationException;
import com.weimob.webfwk.util.exception.MessageException;
import com.weimob.webfwk.util.model.InternalFieldsModifiable;
import com.github.reinert.jjschema.v1.JsonSchemaFactory;
import com.github.reinert.jjschema.v1.JsonSchemaV4Factory;
import com.github.reinert.jjschema.v1.PropertyWrapper;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClassUtil {
    
    public static interface DynamicSelectedEditable {
        
    }

    @Data
    @Accessors(chain = true)
    private static class FieldCustomizedProperty {
        private String      field;
        private String      title;
        private String      editable;
        private String      required;
        private String      pattern;
        private String      fieldType;
        private String      description;
        private Map<String, String> attributes;

        private Attributes originAttrs;
        private Method originMethod;
        private Class<?> originClass;

        FieldCustomizedProperty() {

        }
    }

    @Data
    @Accessors(chain = true)
    public static class FieldAttribute {

        private final Field originField;
        private final Method originMethod;
        private final Class<?> originClass;

        private final String field;
        private final boolean custom;
        private String title;
        private boolean editable;
        private boolean required;
        private String pattern;
        private String fieldType;
        private String description;
        private List<? extends FieldOption> options;

        FieldAttribute(Class<?> clazz, Method method, Field field, String name, boolean custom) {
            this.field = name;
            this.custom = custom;
            this.originField = field;
            this.originMethod = method;
            this.originClass = clazz;
        }
    }

    @Getter
    public static abstract class FieldAttributeConverter {
        private final String attribute;

        FieldAttributeConverter(String attribute) {
            this.attribute = attribute;
        }

        public abstract void setValue(ObjectNode node, FieldCustomizedProperty property, boolean customField, boolean modifiableForm);

        public abstract void setValue(FieldAttribute attribute, FieldCustomizedProperty property, boolean customField, boolean modifiableForm);
    }

    private final static FieldAttributeConverter[] customizedProperties = new FieldAttributeConverter[] {
        new FieldAttributeConverter("title") {
            @Override
            public void setValue(FieldAttribute attribute, FieldCustomizedProperty property, boolean customField,
                    boolean modifiableForm) {
                String title;
                if (property != null && StringUtils.isNotBlank(title = property.getTitle())) {
                    attribute.setTitle(title);
                }
            }
            
            @Override
            public void setValue(ObjectNode node, FieldCustomizedProperty property, boolean customField,
                    boolean modifiableForm) {
                String title;
                if (property != null && StringUtils.isNotBlank(title = property.getTitle())) {
                    node.put(getAttribute(), title);
                }
            }
        },
        new FieldAttributeConverter("pattern") {
            @Override
            public void setValue(FieldAttribute attribute, FieldCustomizedProperty property, boolean customField,
                    boolean modifiableForm) {
                String value;
                if (property != null && StringUtils.isNotBlank(value = property.getPattern())) {
                    attribute.setPattern(value);
                }
            }
            
            @Override
            public void setValue(ObjectNode node, FieldCustomizedProperty property, boolean customField,
                    boolean modifiableForm) {
                String value;
                if (property != null && StringUtils.isNotBlank(value = property.getPattern())) {
                    node.put(getAttribute(), value);
                }
            }
        },
        new FieldAttributeConverter("description") {
            @Override
            public void setValue(FieldAttribute attribute, FieldCustomizedProperty property, boolean customField,
                    boolean modifiableForm) {
                String value;
                if (property != null && StringUtils.isNotBlank(value = property.getDescription())) {
                    attribute.setDescription(value);
                }
            }
            
            @Override
            public void setValue(ObjectNode node, FieldCustomizedProperty property, boolean customField,
                    boolean modifiableForm) {
                String value;
                if (property != null && StringUtils.isNotBlank(value = property.getDescription())) {
                    node.put(getAttribute(), value);
                }
            }
        },
        new FieldAttributeConverter("fieldType") {
            @Override
            public void setValue(FieldAttribute attribute, FieldCustomizedProperty property, boolean customField,
                    boolean modifiableForm) {
                String value;
                if (customField && property != null && StringUtils.isNotBlank(value = property.getFieldType())) {
                    attribute.setFieldType(value);
                }
            }
            
            @Override
            public void setValue(ObjectNode node, FieldCustomizedProperty property, boolean customField,
                    boolean modifiableForm) {
                String value;
                if (customField && property != null && StringUtils.isNotBlank(value = property.getFieldType())) {
                    node.put(getAttribute(), value);
                }
            }
        },
        new FieldAttributeConverter("editable") {
            @Override
            public void setValue(FieldAttribute attribute, FieldCustomizedProperty property, boolean customField,
                    boolean modifiableForm) {
                if (property == null || property.getOriginMethod() == null) {
                    return;
                }
                String value;
                if ((customField || modifiableForm)
                        && StringUtils.isNotBlank(value = property.getEditable())) {
                    attribute.setEditable(CommonUtil.parseBoolean(value));
                }
                Class<?> clazz;
                if ((clazz = property.getOriginClass()) != null
                        && AttributesProccessor.hasEditableSelector(clazz)) {
                    attribute.setEditable(AttributesProccessor.isEditableField(clazz, property.getField()));
                }
            }
            
            @Override
            public void setValue(ObjectNode node, FieldCustomizedProperty property, boolean customField,
                    boolean modifiableForm) {
                if (property == null || property.getOriginMethod() == null) {
                    return;
                }
                String value;
                if ((customField || modifiableForm)
                        && StringUtils.isNotBlank(value = property.getEditable())) {
                    node.put("readonly", !CommonUtil.parseBoolean(value));
                }
                Class<?> clazz;
                if ((clazz = property.getOriginClass()) != null
                        && AttributesProccessor.hasEditableSelector(clazz)) {
                    node.put("readonly", !AttributesProccessor.isEditableField(clazz, property.getField()));
                }
            }
        },
        new FieldAttributeConverter("required") {
            @Override
            public void setValue(FieldAttribute attribute, FieldCustomizedProperty property, boolean customField,
                    boolean modifiableForm) {
                if (property == null || property.getOriginMethod() == null) {
                    return;
                }
                String value;
                if ((customField || modifiableForm)
                        && StringUtils.isNotBlank(value = property.getRequired())) {
                    attribute.setRequired(CommonUtil.parseBoolean(value));
                }
                Class<?> clazz;
                if ((clazz = property.getOriginClass()) != null
                        && AttributesProccessor.hasRequiredSelector(clazz)
                        && AttributesProccessor.isRequiredField(clazz, property.getField())) {
                    attribute.setRequired(true);
                }
            }
            
            @Override
            public void setValue(ObjectNode node, FieldCustomizedProperty property, boolean customField,
                    boolean modifiableForm) {
                if (property == null || property.getOriginMethod() == null) {
                    return;
                }
                String value;
                if ((customField || modifiableForm)
                        && StringUtils.isNotBlank(value = property.getRequired())) {
                    node.put(getAttribute(), CommonUtil.parseBoolean(value));
                }
                Class<?> clazz;
                if ((clazz = property.getOriginClass()) != null
                        && AttributesProccessor.hasRequiredSelector(clazz)) {
                    node.put(getAttribute(), AttributesProccessor.isRequiredField(clazz, property.getField()));
                }
            }
        }
    };

    @Getter
    public static class AttributeException extends MessageException {

        private static final long serialVersionUID = 1L;

        private final String clazz;

        public AttributeException(String clazz, Exception e) {
            super("", e);
            this.clazz = clazz;
        }
    }

    public static class DynamicMethodUtil {

        @Attributes()
        public String get$$templated$$() {
            return null;
        }

        public static Object getFieldValue(@NonNull Object object, String field)
                throws IllegalAccessException, NoSuchFieldException {
            Field feildDef = object.getClass().getDeclaredField(field);
            feildDef.setAccessible(true);
            return feildDef.get(object);
        }

        public static boolean isDynamicMethod(@NonNull Method method) {
            return DynamicMethodUtil.class.isAssignableFrom(method.getDeclaringClass());
        }

        @SuppressWarnings("unchecked")
        public static void setAnnotationAttr(Annotation annotation, String attr, Object value)
                throws NoSuchFieldException, IllegalAccessException {
            InvocationHandler hanlder = Proxy.getInvocationHandler(annotation);
            Field valuesFeld = hanlder.getClass().getDeclaredField("memberValues");
            valuesFeld.setAccessible(true);
            ((Map<String, Object>) valuesFeld.get(hanlder)).put(attr, value);
        }

        public static Method createAttributesMethod(@NonNull String method, @NonNull Class<?> returnType, Map<String, Object> attributes)
                throws Exception {
            if (!method.startsWith("get") && !method.startsWith("is")) {
                method = String.format("get%s", method);
            }
            Method tmpl = DynamicMethodUtil.class.getDeclaredMethod("get$$templated$$");
            Constructor<Method> copier = Method.class.getDeclaredConstructor(Class.class, String.class, Class[].class,
                    Class.class, Class[].class, int.class, int.class, String.class, byte[].class, byte[].class,
                    byte[].class);
            copier.setAccessible(true);
            Method copied = copier.newInstance(
                    getFieldValue(tmpl, "clazz"),
                    method,
                    getFieldValue(tmpl, "parameterTypes"),
                    returnType,
                    getFieldValue(tmpl, "exceptionTypes"),
                    getFieldValue(tmpl, "modifiers"),
                    getFieldValue(tmpl, "slot"),
                    getFieldValue(tmpl, "signature"),
                    getFieldValue(tmpl, "annotations"),
                    getFieldValue(tmpl, "parameterAnnotations"),
                    getFieldValue(tmpl, "annotationDefault"));
            Attributes annotation = copied.getAnnotation(Attributes.class);
            if (attributes != null && attributes.size() > 0) {
                for (Map.Entry<String,Object> attr : attributes.entrySet()) {
                    setAnnotationAttr(annotation, attr.getKey(), attr.getValue());
                }
            }
            return copied;
        }
    }

    @Getter
    @Setter
    private static class MatchTags {
        private boolean hasRequiredSelector = false;
        private boolean hasEditableSelector = false;
        private final Set<String> requireds = new HashSet<>();
        private final Set<String> editables = new HashSet<>();
    }

    public static class AttributesProccessor extends CustomAttributesProccessor {

        private static final String FORM_SPECIAL_FIELD_FORM = ":form";

        private static final Type TYPE_FIELD_CUSTOM_DEFINITION
                = new TypeToken<List<Map<String, String>>>() {}.getType();

        private static final Map<String, List<FieldCustomizedProperty>> CACHED_FORMS_ATTRIBUTES
                = new ConcurrentHashMap<>();

        private static final ThreadLocal<String> CUSTOM_PREVIES_ATTRIBUTES = new ThreadLocal<>();

        private static final ThreadLocal<Map<Class<?>, MatchTags>> TAG_MATCHED_RESULT = new ThreadLocal<>();
        
        private static MatchTags getMatchTags(Class<?> clazz) {
            Map<Class<?>, MatchTags> cached;
            if ((cached = TAG_MATCHED_RESULT.get()) == null) {
                return null;
            }
            return cached.get(clazz);
        }
        
        private static MatchTags getOrNewMatchTags(Class<?> clazz) {
            Map<Class<?>, MatchTags> cached;
            if ((cached = TAG_MATCHED_RESULT.get()) == null) {
                TAG_MATCHED_RESULT.set(cached = new HashMap<>());
            }
            MatchTags matchTags;
            if ((matchTags = cached.get(clazz)) == null) {
                cached.put(clazz, matchTags = new MatchTags());
            }
            return matchTags;
        }
        
        public static boolean hasRequiredSelector(Class<?> clazz) {
            MatchTags matchTags;
            if ((matchTags = getMatchTags(clazz)) != null) {
                return matchTags.isHasRequiredSelector();
            }
            return false;
        }
        
        public static boolean hasEditableSelector(Class<?> clazz) {
            MatchTags matchTags;
            if ((matchTags = getMatchTags(clazz)) != null) {
                return matchTags.isHasEditableSelector();
            }
            return false;
        }
        
        public static boolean isRequiredField(Class<?> clazz, String field) {
            MatchTags matchTags;
            if ((matchTags = getMatchTags(clazz)) != null) {
                return matchTags.getRequireds().contains(field);
            }
            return false;
        }
        
        public static boolean isEditableField(Class<?> clazz, String field) {
            MatchTags matchTags;
            if ((matchTags = getMatchTags(clazz)) != null) {
                return matchTags.getEditables().contains(field);
            }
            return false;
        }
        
        public static String setContextPreviewAttributes(String form, String attrsForPreview) {
            String origin = CUSTOM_PREVIES_ATTRIBUTES.get();
            CUSTOM_PREVIES_ATTRIBUTES.set(String.format("%s/%s", form, attrsForPreview));
            return origin;
        }

        public static void resetContextPreviewAttributes(String attrsForPreview) {
            CUSTOM_PREVIES_ATTRIBUTES.set(attrsForPreview);
        }

        public static void setCustomFormAttributes(String form, List<FieldCustomizedProperty> attributes) {
            CACHED_FORMS_ATTRIBUTES.put(form, attributes);
        }

        public static void removeCustomFormAttributes(String form) {
            CACHED_FORMS_ATTRIBUTES.remove(form);
        }

        public static List<FieldCustomizedProperty> parseFormCustomizedProperties(String clazz, String formAttrs) {
            if (StringUtils.isBlank(formAttrs)) {
                return Collections.emptyList();
            }
            List<Map<String, String>> attrs = null; try {
                attrs = CommonUtil.fromJson(formAttrs, TYPE_FIELD_CUSTOM_DEFINITION);
            } catch (Exception e) {
                throw new AttributeException(clazz, e);
            }
            if (attrs == null) {
                throw new AttributeException(clazz, new NullPointerException());
            }
            List<FieldCustomizedProperty> result = new ArrayList<>();
            for (Map<String, String> attr : attrs) {
                if (StringUtils.isBlank(attr.get("field"))) {
                    continue;
                }
                result.add(new FieldCustomizedProperty()
                        .setField(attr.remove("field"))
                        .setTitle(attr.remove("title"))
                        .setPattern(attr.remove("pattern"))
                        .setRequired(attr.remove("required"))
                        .setEditable(attr.remove("editable"))
                        .setFieldType(attr.remove("fieldType"))
                        .setDescription(attr.remove("description"))
                        .setAttributes(attr));
            }
            return result;
        }
        
        private static Map<String, FieldCustomizedProperty> getFormCustomizeProperties(String form) {
            Map<String, List<FieldCustomizedProperty>> clonedAttrs = new HashMap<>(CACHED_FORMS_ATTRIBUTES);
            String previewAsCurrentForm;
            if (StringUtils.isNotBlank(previewAsCurrentForm = CUSTOM_PREVIES_ATTRIBUTES.get())
                    && previewAsCurrentForm.startsWith(String.format("%s/", form))) {
                List<FieldCustomizedProperty> previewAttrs = null;
                if ((previewAttrs = parseFormCustomizedProperties(form, previewAsCurrentForm.substring(form.length() + 1))) != null) {
                    clonedAttrs.put(form, previewAttrs);
                }
            }
            List<FieldCustomizedProperty> formAttrs;
            if ((formAttrs = clonedAttrs.get(form)) == null) {
                return Collections.emptyMap();
            }
            Map<String, FieldCustomizedProperty> result = new HashMap<>();
            for (FieldCustomizedProperty item : formAttrs) {
                String name;
                if (item == null || StringUtils.isBlank(name = item.getField())) {
                    continue;
                }
                result.put(name, item);
            }
            return result;
        }

        private static FieldCustomizedProperty getFieldCustomizedProperty(String form, String field) {
            if (StringUtils.isBlank(field)) {
                field = FORM_SPECIAL_FIELD_FORM;
            }
            FieldCustomizedProperty result;
            Map<String, FieldCustomizedProperty> mappedFields;
            if ((result = (mappedFields = getFormCustomizeProperties(form)).get(field)) != null) {
                return result;
            }
            for (String name : mappedFields.keySet()) {
                if (!name.endsWith("*") || !field.startsWith(name.substring(0, name.length() - 1))) {
                    continue;
                }
                if (result == null || name.length() > result.getField().length()) {
                    result = mappedFields.get(name);
                }
            }
            return result;
        }

        public static void processCommonAttributes(ObjectNode jsonNode, Attributes attrs, Class<?> clazz, String field, Method method) {
            if (attrs != null && attrs.type() != null && !Modifier.isAbstract(attrs.type().getModifiers())) {
                FieldType attrTypeInstance = ClassUtil.getSingltonInstance(attrs.type());
                /* 定制化筛选条件表单解析 */
                Class<?> formClass;
                if ((formClass = attrTypeInstance.getDynamicFilterFormClass()) != null) {
                    jsonNode.put("dynamicFilterFormClass", classToJson(formClass).toString());
                    if (DynamicSelectedEditable.class.isAssignableFrom(formClass)) {
                        jsonNode.put("dynamicSelectedEditable", true);
                    }
                }
                /* 定制化表格行创建表单解析 */
                if ((formClass = attrTypeInstance.getListItemCreationFormClass()) != null) {
                    jsonNode.put("listItemCreationFormClass", classToJson(formClass).toString());
                }
            }
            boolean custom;
            boolean modifiable;
            jsonNode.put("custom", (custom = method != null && DynamicMethodUtil.isDynamicMethod(method)));
            jsonNode.put("modifiable", (modifiable = InternalFieldsModifiable.class.isAssignableFrom(clazz)));

            if (jsonNode == null || clazz == null) {
                return;
            }
            FieldCustomizedProperty fieldAttr;
            if ((fieldAttr = getFieldCustomizedProperty(clazz.getName(), field)) == null) {
                fieldAttr = new FieldCustomizedProperty().setField(field);
            }
            fieldAttr.setOriginClass(clazz).setOriginMethod(method).setOriginAttrs(attrs);
            for (FieldAttributeConverter converter : customizedProperties) {
                converter.setValue(jsonNode, fieldAttr, custom, modifiable);
            }
            Map<String, String> customAttributes;
            if ((customAttributes = fieldAttr.getAttributes()) != null) {
                for (Map.Entry<String, String> c : customAttributes.entrySet()) {
                    jsonNode.put(String.format("custom%s", StringUtils.capitalize(c.getKey())), c.getValue());
                }
            }
        }

        public static Method[] getDynamicMethods(Class<?> clazz) {
            Map<String, FieldCustomizedProperty> fields = getFormCustomizeProperties(clazz.getName());
            try {
                List<Method> methods = new ArrayList<>();
                for (FieldCustomizedProperty attr : fields.values()) {
                    if (attr == null || FORM_SPECIAL_FIELD_FORM.equals(attr.getField())
                            || StringUtils.endsWith(attr.getField(), "*")) {
                        continue;
                    }
                    methods.add(DynamicMethodUtil.createAttributesMethod(attr.getField(), String.class, null));
                }
                return methods.toArray(new Method[0]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static void filterDynamicMethods(Class<?> clazz, CustomSchemaWrapper.MethodField methodField) {
            MatchTags matchTags = getOrNewMatchTags(clazz);
            matchTags.getRequireds().clear();
            matchTags.getEditables().clear();
            matchTags.setHasEditableSelector(false);
            matchTags.setHasRequiredSelector(false);
            if(clazz == null || methodField == null || methodField.size() <= 0){
                return;
            }
            Attributes classAttributes = clazz.getAnnotation(Attributes.class);
            if(classAttributes == null){
                return;
            }
            String [] visibleSelectors = classAttributes.visibleSelector();
            String [] requiredSelectors = classAttributes.requiredSelector();
            String [] editableSelectors = classAttributes.editableSelector();
            Attributes propAttributes = null;
            if (visibleSelectors.length > 0) {
                for (Iterator<Map.Entry<Method, Field>> iter = methodField.entrySet().iterator(); iter.hasNext();){
                    Map.Entry<Method,Field> item = iter.next();
                    Field f = item.getValue();
                    Method m = item.getKey();
                    if (f == null && m == null) {
                        iter.remove();
                        continue;
                    }
                    /**
                     * 表单自定义字段将始终被保留
                     */
                    if (DynamicMethodUtil.isDynamicMethod(m)) {
                        continue;
                    }
                    if(f != null && f.getAnnotation(Attributes.class) != null){
                        propAttributes =  f.getAnnotation(Attributes.class);
                    }else {
                        propAttributes = m.getAnnotation(Attributes.class);
                    }
                    if (propAttributes == null) {
                        iter.remove();
                        continue;
                    }
                    String [] visibleTags =  propAttributes.visibleTags();
                    if (!getIntersection(visibleSelectors, visibleTags)) {
                        iter.remove();
                    }
                }
            }
            if (requiredSelectors != null && requiredSelectors.length > 0) {
                matchTags.setHasRequiredSelector(true);
            }
            if (editableSelectors != null && editableSelectors.length > 0) {
                matchTags.setHasEditableSelector(true);
            }
            if (!matchTags.isHasRequiredSelector() && !matchTags.isHasEditableSelector()) {
                return;
            }
            String field;
            for (Map.Entry<Method, Field> entry : methodField.entrySet()) {
                Field f = entry.getValue();
                Method m = entry.getKey();
                if (f == null && m == null) {
                    continue;
                }
                if (StringUtils.isBlank(field = PropertyWrapper.parsePropertyName(CommonUtil.ifNull(f, m)))) {
                    continue;
                }
                if (f == null || (propAttributes = f.getAnnotation(Attributes.class)) == null) {
                    propAttributes = m.getAnnotation(Attributes.class);
                }
                if (propAttributes == null) {
                    continue;
                }
                String[] requiredTags = propAttributes.requiredTags();
                String[] editableTags = propAttributes.editableTags();
                if (getIntersection(requiredSelectors, requiredTags)) {
                    matchTags.getRequireds().add(field);
                }
                if (getIntersection(editableSelectors, editableTags)) {
                    matchTags.getEditables().add(field);
                }
            }
        }

        /** 获取两个数组的交集 **/
        private static boolean getIntersection(String [] firstArray, String [] secondArray){
            boolean res = false;
            if(firstArray == null || firstArray.length == 0 || secondArray == null || secondArray.length == 0) {
                return false;
            }
            Map<String, Integer> map = new HashMap<String, Integer>();
            for(int i = 0;i < firstArray.length;i ++) {
                String key = firstArray[i];
                if(!map.containsKey(key)){
                    map.put(key, 1);
                }
            }
            for(int j = 0;j < secondArray.length;j ++) {
                String key = secondArray[j];
                if(map.containsKey(key)) {
                    res = true;
                    break;
                }
            }
            return res;
        }
    }

    public static Type[] getActualParameterizedTypes(Class<?> sourceClazz, Class<?> targetClass) {
        if (sourceClazz == null) {
            return null;
        }
        Type superGenericType;
        Type[] nextActualTypes;
        Type[] currActualTypes = new Type[0];
        ParameterizedType superParameterizedType;
        targetClass = CommonUtil.ifNull(targetClass, Object.class);
        while (sourceClazz != null && !sourceClazz.equals(targetClass)) {
            superGenericType = sourceClazz.getGenericSuperclass();
//            System.out.println(clazz);
            if (superGenericType instanceof ParameterizedType) {
                superParameterizedType = (ParameterizedType)superGenericType;
//                System.out.println("\tSuperRawType = " + superRawType);
//                System.out.println("\tSuperGenericType = " + superGenericType);
                Type[] parameterTypes = superParameterizedType.getActualTypeArguments();
                nextActualTypes = new Type[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    Type currentType = parameterTypes[i];
//                    System.out.println("\tSuperParameterType[" + i + "] = " + currentType);
                    nextActualTypes[i] = currentType;
                }
                int startedIndex = 0;
                if (currActualTypes != null && currActualTypes.length  > 0) {
                    for (Type prevType : currActualTypes) {
                        for (int i = startedIndex; i < nextActualTypes.length; i++) {
//                            System.out.println("\tPrevoius = " + prevType);
//                            System.out.println("\tPosition = " + nextActualTypes[i]);
                            if (!nextActualTypes[i].toString().contains(".")) {
                                startedIndex = i + 1;
                                nextActualTypes[i] = prevType;
//                                System.out.println("\tReplaced[" + i + "] = " + prevType);
                                break;
                            }
                        }
                    }
                }
                currActualTypes = nextActualTypes;
//                for (Type xx : currActualTypes) {
//                    System.out.println("\tRESULT:" + xx);
//                }
            }
            sourceClazz = sourceClazz.getSuperclass();
        }
        return currActualTypes;
    }

    public static Type[] getActualParameterizedTypes(Class<?> sourceClazz) {
        return getActualParameterizedTypes(sourceClazz, null);
    }

    public static Type getActualParameterizedType(Class<?> sourceClazz, Class<?> targetClazz, int index) {
        Type[] types;
        if (index <0 || (types = getActualParameterizedTypes(sourceClazz, targetClazz))
                == null || index >= types.length) {
            return null;
        }
        return types[index];
    }

    public static Type getActualParameterizedType(Class<?> sourceClazz, int index) {
        Type[] types;
        if (index <0 || (types = getActualParameterizedTypes(sourceClazz)) == null
                || index >= types.length) {
            return null;
        }
        return types[index];
    }
    
    public static List<Field> parseAllFields(@NonNull Class<?> clazz) throws IllegalAccessException {
        return parseAllFields(clazz, null);
    }
    
    private static List<Field> parseAllFields(@NonNull Class<?> clazz, List<Field> collector)
            throws IllegalAccessException {
        if (Object.class.equals(clazz)) {
            return null;
        }
        if (collector == null) {
            collector = new LinkedList<>();
        }
        for (Field field : clazz.getDeclaredFields()) {
            collector.add(field);
        }
        Class<?> superClazz;
        if ((superClazz = clazz.getSuperclass()) != null) {
            parseAllFields(superClazz, collector);
        }
        return collector;
    }
    
    /**
     * 将 class 转换成 json schema
     */
    public static JsonNode classToJson(Class<?> clazz) {
        JsonSchemaFactory schemaFactory = new JsonSchemaV4Factory();
        schemaFactory.setAutoPutDollarSchema(false);
        Class<? extends CustomAttributesProccessor> originParser = JJSchemaUtil.getCustomAttributesParser();
        JJSchemaUtil.setCustomAttributesParser(AttributesProccessor.class);
        try {
            return schemaFactory.createSchema(clazz);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            throw new RuntimeException(e);
        } finally {
            JJSchemaUtil.setCustomAttributesParser(originParser);
        }
    }

    /**
     * 解析 Class 中的字段属性定义(通过 jjschema/Attributes 注解)
     */
    public static List<FieldAttribute> parseClassFields(@NonNull Class<?> clazz) throws Exception {
        Class<? extends CustomAttributesProccessor> originParser = JJSchemaUtil.getCustomAttributesParser();
        JJSchemaUtil.setCustomAttributesParser(AttributesProccessor.class);
        try {
            List<FieldAttribute> result = new ArrayList<>();
            Map<Method, Field> properties = CustomSchemaWrapper.findProperties(clazz);
            for (Map.Entry<Method, Field> property : properties.entrySet()) {
                String field;
                AccessibleObject prop;
                if ((prop = CommonUtil.ifNull(property.getValue(), property.getKey())) == null
                        || StringUtils.isBlank(field = PropertyWrapper.parsePropertyName(prop))) {
                    continue;
                }
                String title = field;
                String pattern = null;
                String description = null;
                boolean editable = true;
                boolean required = false;
                List<? extends FieldOption> options = null;
                Attributes fieldAttributes;
                if ((fieldAttributes = prop.getAnnotation(Attributes.class)) != null) {
                    title = fieldAttributes.title();
                    pattern = fieldAttributes.pattern();
                    required = fieldAttributes.required();
                    editable = !fieldAttributes.readonly();
                    description = fieldAttributes.description();
                    NamedTypeEntity nameTypeEntity = JJSchemaUtil.parseTypeAttributes(fieldAttributes);
                    if (nameTypeEntity != null && FieldOptionsType.STATIC.equals(nameTypeEntity.getOptionsType())) {
                        options = nameTypeEntity.getStaticOptions();
                    }
                }
                FieldAttribute finalAttrs = new FieldAttribute(
                        clazz,
                        property.getKey(),
                        property.getValue(),
                        field,
                        DynamicMethodUtil.isDynamicMethod(property.getKey())
                )
                        .setTitle(title)
                        .setEditable(editable)
                        .setRequired(required)
                        .setPattern(pattern)
                        .setDescription(description)
                        .setOptions(options);
                FieldCustomizedProperty customAttrs;
                if ((customAttrs = AttributesProccessor.getFieldCustomizedProperty(clazz.getName(), field)) == null) {
                    customAttrs = new FieldCustomizedProperty().setField(field);
                }
                customAttrs.setOriginClass(clazz).setOriginMethod(property.getKey()).setOriginAttrs(fieldAttributes);
                for (FieldAttributeConverter converter : customizedProperties) {
                    converter.setValue(finalAttrs, customAttrs, finalAttrs.isCustom(),
                            InternalFieldsModifiable.class.isAssignableFrom(clazz));
                }
                result.add(finalAttrs);
            }
            return result;
        } finally {
            JJSchemaUtil.setCustomAttributesParser(originParser);
        }
    }
    /**
     * 根据字段的定义，检查给定对要是否符合规范
     */
    public static void checkFormRequiredAndOpValue(@NonNull Object instance, String... exclusions)
            throws FormValidationException {
        checkFormRequiredAndOpValue(instance, false, exclusions);
    }

    public static void checkFormRequiredAndOpValue(@NonNull Object instance, boolean skipReadOnly, String... exclusions)
            throws FormValidationException {
        try {
            Collection<FieldAttribute> fields;
            if ((fields = parseClassFields(instance.getClass())) == null) {
                return;
            }
            for (FieldAttribute field : fields) {
                String fieldName = field.getField();
                if (exclusions != null && ArrayUtils.contains(exclusions, fieldName)) {
                    continue;
                }
                if (skipReadOnly && !field.isEditable()) {
                    continue;
                }
                Object fieldValue = getFieldValue(instance.getClass(), instance, fieldName);
                if (field.isRequired()) {
                    if (fieldValue == null || StringUtils.isBlank(fieldValue.toString())
                            || (fieldValue.getClass().isArray() && ((Object[]) fieldValue).length <= 0)
                            || ((fieldValue instanceof Collection) && ((Collection<?>) fieldValue).isEmpty())) {
                        throw new FormValidationException(
                                String.format("字段（%s）的值被要求但未提供", field.getTitle()));
                    }
                }
                if (fieldValue == null || StringUtils.isBlank(fieldValue.toString())) {
                    continue;
                }
                List<? extends FieldOption> staticOptions;
                if ((staticOptions = field.getOptions()) != null && fieldValue != null) {
                    Object unknownFound = null;
                    Object[] arrayValues = null;
                    if (fieldValue instanceof Collection) {
                        arrayValues = ((Collection<?>) fieldValue).toArray();
                    } else if (fieldValue.getClass().isArray()) {
                        arrayValues = (Object[]) fieldValue;
                    } else {
                        arrayValues = new Object[] { fieldValue };
                    }
                    LOOP_TOP: for (Object av : arrayValues) {
                        if (av == null) {
                            continue;
                        }
                        for (FieldOption option : staticOptions) {
                            if (av.toString().equals(option.getOptionValue())) {
                                continue LOOP_TOP;
                            }
                        }
                        unknownFound = av;
                        break LOOP_TOP;
                    }
                    if (unknownFound != null) {
                        throw new FormValidationException(
                                String.format("字段（%s）的值(%s)不在可选范围", field.getTitle(), unknownFound));
                    }
                }
                if (StringUtils.isNotBlank(field.getPattern()) && (CharSequence.class.equals(fieldValue.getClass())
                        || Number.class.equals(fieldValue.getClass()))) {
                    Pattern valueRegexp = null;
                    try {
                        valueRegexp = Pattern.compile(field.getPattern(), Pattern.DOTALL);
                    } catch (Exception e) {
                        throw new FormValidationException(
                                String.format("字段（%s）校验正则模式非法：%s", field.getTitle(), field.getPattern()));
                    }
                    if (!valueRegexp.matcher(fieldValue.toString()).find()) {
                        throw new FormValidationException(
                                String.format("字段（%s）校验正则模式未通过 -%s", field.getTitle(), field.getPattern()));
                    }
                }
            }
        } catch (MessageException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.toString(), e);
            throw new FormValidationException("解析表单的字段定义信息失败");
        }
    }

    private static Object getFieldValue(@NonNull Class<?> clazz, Object instance, String feild) throws IllegalAccessException {
        try {
            Field field = clazz.getDeclaredField(feild);
            field.setAccessible(true);
            return field.get(instance);
        } catch (NoSuchFieldException e) {
            if ((clazz = clazz.getSuperclass()) == Object.class) {
                return null;
            }
            return getFieldValue(clazz, instance, feild);
        }
    }

    private static final Map<String, Class<?>> STATE_FORM_CACHED_CLASSES
            = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Object> STATE_FORM_CACHED_INSTANCES
            = new ConcurrentHashMap<>();

    public static Class<?> loadClass(@NonNull String clazzPath) throws ClassNotFoundException {
        if (!STATE_FORM_CACHED_CLASSES.containsKey(clazzPath)) {
            synchronized(clazzPath.intern()) {
                if (!STATE_FORM_CACHED_CLASSES.containsKey(clazzPath)) {
                    try {
                        STATE_FORM_CACHED_CLASSES.put(clazzPath, Class.forName(clazzPath));
                    } catch (ClassNotFoundException | MessageException e ) {
                        throw e;
                    } catch (Exception ex) {
                        throw new MessageException(String.format("类加载失败: %s", clazzPath),
                                ex);
                    }
                }
            }
        }
        return STATE_FORM_CACHED_CLASSES.get(clazzPath);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> loadClass(@NonNull String clazzPath, Class<T> superClazz) throws ClassNotFoundException {
        Class<?> clazz = loadClass(clazzPath);
        if (superClazz != null && !superClazz.isAssignableFrom(clazz)) {
            throw new MessageException(
                    String.format("类继承关系不匹配: 要求 %s 必须实现或继承自 %s", clazz.getName(), superClazz.getName()));
        }
        return (Class<? extends T>) clazz;
    }

    public static <T> T getSingltonInstance(@NonNull String clazzPath, Class<T> superClazz)
            throws ClassNotFoundException {
        return (T) getSingltonInstance(loadClass(clazzPath, superClazz));
    }

    @SuppressWarnings("unchecked")
    public static <T> T getSingltonInstance(@NonNull Class<T> clazz) {
        if (!STATE_FORM_CACHED_INSTANCES.containsKey(clazz)) {
            synchronized(clazz) {
                if (!STATE_FORM_CACHED_INSTANCES.containsKey(clazz)) {
                    T instance;
                    try {
                        instance = (T) clazz.getMethod("getInstance").invoke(null);
                    } catch (Throwable e) {
                        try {
                            instance = clazz.getDeclaredConstructor().newInstance();
                        } catch (Exception ex) {
                            throw new MessageException(String.format("类实例化失败: %s", clazz.getName(), ex),
                                    e);
                        }
                    }
                    STATE_FORM_CACHED_INSTANCES.put(clazz, instance);
                }
            }
        }
        return (T)STATE_FORM_CACHED_INSTANCES.get(clazz);
    }
}
