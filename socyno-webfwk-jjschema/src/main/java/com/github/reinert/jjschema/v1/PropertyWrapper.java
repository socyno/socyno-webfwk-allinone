/*
 * Copyright (c) 2014, Danilo Reinert (daniloreinert@growbit.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of both licenses is available under the src/resources/ directory of
 * this project (under the names LGPL-3.0.txt and ASL-2.0.txt respectively).
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.reinert.jjschema.v1;

import static com.github.reinert.jjschema.JJSchemaUtil.processCommonAttributes;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.ManagedReference;
import com.github.reinert.jjschema.Nullable;
import com.github.reinert.jjschema.SchemaIgnore;
import com.github.reinert.jjschema.SchemaIgnoreProperties;

/**
 * @author Danilo Reinert
 */

public class PropertyWrapper extends SchemaWrapper {

    final static String[] getterPrefixes = {"get", "is"};
	final static String propertiesStr = "/properties/";
	final static String itemsStr = "/items";

	enum ReferenceType {NONE, FORWARD, BACKWARD}

    final CustomSchemaWrapper ownerSchemaWrapper;
    final SchemaWrapper schemaWrapper;
    final Field field;
    final Method method;
    String name;
    boolean required;
    ManagedReference managedReference;
    ReferenceType referenceType;

    public PropertyWrapper(CustomSchemaWrapper ownerSchemaWrapper, Set<ManagedReference> managedReferences, Method method, Field field) throws Exception {
        super(null);

        if (method == null)
            throw new RuntimeException("Error at " + ownerSchemaWrapper.getJavaType().getName() + ": Cannot instantiate a PropertyWrapper with a null method.");

        this.ownerSchemaWrapper = ownerSchemaWrapper;
        this.field = field;
        this.method = method;

        String relativeId;
        Class<?> collectionType = null;
        Type genericType = method.getGenericReturnType();
        Type propertyType = method.getReturnType();

        if (genericType instanceof TypeVariable) {
        	ParameterizedType p = ownerSchemaWrapper.getParameterizedType();
        	if (p!=null) {
        		final int i= Arrays.asList(((Class<?>)p.getRawType()).getTypeParameters()).indexOf(genericType);
        		propertyType = (Class<?>) p.getActualTypeArguments()[i];
        	}
        }
        
        if (Collection.class.isAssignableFrom((Class<?>) propertyType)
                 || ((Class<?>) propertyType).isArray()) {
            collectionType = (Class<?>)propertyType;
            if (Collection.class.isAssignableFrom((Class<?>) propertyType)) {
                if (!(genericType instanceof ParameterizedType)) {
                    genericType = ((Class<?>) propertyType).getGenericSuperclass();
                    while (!(genericType instanceof ParameterizedType)) {
                        genericType = ((Class<?>) genericType).getGenericSuperclass();
                    }
                }
                propertyType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
            } else {
                propertyType = ((Class<?>) propertyType).getComponentType();
            }
            relativeId = propertiesStr + getName() + itemsStr;
        } else {
            relativeId = propertiesStr + getName();
        }

        processReference(propertyType);

        if (shouldIgnoreField()) {
            this.schemaWrapper = new EmptySchemaWrapper();
        } else if (getReferenceType() == ReferenceType.BACKWARD) {
            SchemaWrapper schemaWrapperLocal;
            String id = processId(method.getReturnType());
            if (id != null) {
                schemaWrapperLocal = new RefSchemaWrapper(propertyType, id);
                ownerSchemaWrapper.pushReference(getManagedReference());
            } else {
                if (ownerSchemaWrapper.pushReference(getManagedReference())) {
                    String relativeId1 = ownerSchemaWrapper.getRelativeId();
                    if (relativeId1.endsWith(itemsStr)) {
                        relativeId1 = relativeId1.substring(0, relativeId1.substring(0, relativeId1.length() - itemsStr.length()).lastIndexOf("/") - (propertiesStr.length() - 1));
                    } else {
                        relativeId1 = relativeId1.substring(0, relativeId1.lastIndexOf("/") - (propertiesStr.length() - 1));
                    }
                    schemaWrapperLocal = new RefSchemaWrapper(propertyType, relativeId1);
                } else
                    schemaWrapperLocal = new EmptySchemaWrapper();
            }
            if (schemaWrapperLocal.isRefWrapper() && collectionType != null)
                this.schemaWrapper = SchemaWrapperFactory.createArrayRefWrapper((RefSchemaWrapper) schemaWrapperLocal);
            else
                this.schemaWrapper = schemaWrapperLocal;
        } else if (ownerSchemaWrapper.getJavaType() == propertyType) {
            SchemaWrapper schemaWrapperLocal = new RefSchemaWrapper(propertyType, ownerSchemaWrapper.getRelativeId());
            if (collectionType != null) {
                this.schemaWrapper = SchemaWrapperFactory.createArrayRefWrapper((RefSchemaWrapper) schemaWrapperLocal);
            } else {
                this.schemaWrapper = schemaWrapperLocal;
            }
        } else {
            if (getReferenceType() == ReferenceType.FORWARD) {
                ownerSchemaWrapper.pullReference(getManagedReference());
            }
            String relativeId1 = ownerSchemaWrapper.getRelativeId() + relativeId;
            if (collectionType != null) {
                this.schemaWrapper = createArrayWrapper(managedReferences, propertyType, collectionType, relativeId1);
            } else if (genericType instanceof ParameterizedType) {
            	this.schemaWrapper=createWrapper(managedReferences, genericType, relativeId);
            } else {
                this.schemaWrapper = createWrapper(managedReferences, propertyType, relativeId1);
            }
            processAttributes(getNode());
            processNullable();
        }
    }

    public Field getField() {
        return field;
    }

    public Method getMethod() {
        return method;
    }

    public SchemaWrapper getOwnerSchema() {
        return ownerSchemaWrapper;
    }

    public String getName() {
        if (name == null) {
            name = parsePropertyName(getAccessibleObject());
        }
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public ManagedReference getManagedReference() {
        return managedReference;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public boolean isReference() {
        return managedReference != null;
    }

    @Override
    public JsonNode asJson() {
        return schemaWrapper.asJson();
    }

    @Override
    public String getDollarSchema() {
        return schemaWrapper.getDollarSchema();
    }

    @Override
    public String getId() {
        return schemaWrapper.getId();
    }

    @Override
    public String getRef() {
        return schemaWrapper.getRef();
    }

    @Override
    public String getType() {
        return schemaWrapper.getType();
    }

    @Override
    public Class<?> getJavaType() {
        return schemaWrapper.getJavaType();
    }

    @Override
    public boolean isEnumWrapper() {
        return schemaWrapper.isEnumWrapper();
    }

    @Override
    public boolean isSimpleWrapper() {
        return schemaWrapper.isSimpleWrapper();
    }

    @Override
    public boolean isCustomWrapper() {
        return schemaWrapper.isCustomWrapper();
    }

    @Override
    public boolean isRefWrapper() {
        return schemaWrapper.isRefWrapper();
    }

    @Override
    public boolean isArrayWrapper() {
        return schemaWrapper.isArrayWrapper();
    }

    @Override
    public boolean isNullWrapper() {
        return schemaWrapper.isNullWrapper();
    }

    @Override
    public boolean isEmptyWrapper() {
        return schemaWrapper.isEmptyWrapper();
    }

    @Override
    public <T extends SchemaWrapper> T cast() {
        return schemaWrapper.cast();
    }
    
    protected SchemaWrapper createWrapper(Set<ManagedReference> managedReferences, Type genericType, String relativeId1)
            throws Exception {
        return SchemaWrapperFactory.createWrapper(genericType, managedReferences, relativeId1,
                shouldIgnoreProperties());
    }
    
    protected SchemaWrapper createArrayWrapper(Set<ManagedReference> managedReferences, Type propertyType,
            Class<?> collectionType, String relativeId1) throws Exception {
        return SchemaWrapperFactory.createArrayWrapper(collectionType, propertyType, managedReferences, relativeId1, shouldIgnoreProperties());
    }

    protected void setRequired(boolean required) {
        this.required = required;
    }

    protected AccessibleObject getAccessibleObject() {
        return getAccessibleObject(method, field);
    }

    public static AccessibleObject getAccessibleObject(Method method, Field field) {
        return (field == null) ? method : field;
    }

    protected String processId(Class<?> accessibleObject) {
        final Attributes attributes = accessibleObject.getAnnotation(Attributes.class);
        if (attributes != null && !attributes.id().isEmpty()) {
            return attributes.id();
        }
        return null;
    }
    
    protected void processAttributes(ObjectNode node) {
        node.remove("$schema");
        final Attributes attributes = getAccessibleObject().getAnnotation(Attributes.class);
        processCommonAttributes(node, attributes, getOwnerSchema().getJavaType(), getName(), method);
        if (node != null && (node.get("required") != null && node.get("required").isBoolean() && node.get("required").asBoolean())) {
            setRequired(true);
        }
        node.remove("required");
    }

    protected void processReference(Type propertyType) {
        boolean referenceExists = false;

        JsonManagedReference refAnn = getAccessibleObject().getAnnotation(JsonManagedReference.class);
        if (refAnn != null) {
            referenceExists = true;
            managedReference = new ManagedReference(getOwnerSchema().getJavaType(), refAnn.value(), propertyType);
            referenceType = ReferenceType.FORWARD;
        }

        JsonBackReference backRefAnn = getAccessibleObject().getAnnotation(JsonBackReference.class);
        if (backRefAnn != null) {
            if (referenceExists)
                throw new RuntimeException("Error at " + getOwnerSchema().getJavaType().getName() + ": Cannot reference " + propertyType.getTypeName() + " both as Managed and Back Reference.");
            managedReference = new ManagedReference(propertyType, backRefAnn.value(), getOwnerSchema().getJavaType());
            referenceType = ReferenceType.BACKWARD;
        }
    }

    @Override
    protected ObjectNode getNode() {
        return schemaWrapper.getNode();
    }

    @Override
    protected void processNullable() {
        final Nullable nullable = getAccessibleObject().getAnnotation(Nullable.class);
        if (nullable != null) {
            if (isEnumWrapper()) {
                ((ArrayNode) getNode().get("enum")).addNull();
            } else {
                String oldType = getType();
                ArrayNode typeArray = getNode().putArray("type");
                typeArray.add(oldType);
                typeArray.add("null");
            }
        }
    }

    @Override
    protected String getNodeTextValue(JsonNode node) {
        return schemaWrapper.getNodeTextValue(node);
    }

    @Override
    protected void setType(String type) {
        schemaWrapper.setType(type);
    }

    public static String firstToLowerCase(String string) {
        return Character.toLowerCase(string.charAt(0))
                + (string.length() > 1 ? string.substring(1) : "");
    }

    public static String parsePropertyName(AccessibleObject field) {
        if (field == null) {
            return null;
        }
        if (field.getAnnotation(SchemaIgnore.class) != null) {
            return null;
        }
        if (Method.class.isAssignableFrom(field.getClass())) {
            Method method = (Method)field;
            String name = method.getName();
            Class<?> declaringClass = method.getDeclaringClass();
            if (declaringClass.equals(Object.class)
                    || Map.class.isAssignableFrom(declaringClass)
                    || Collection.class.isAssignableFrom(declaringClass)) {
                return null;
            }
            if (name == null || method.getParameterTypes().length != 0
                    || (method.getModifiers() & Modifier.STATIC) != 0) {
                return null;
            }
            for (String prefix : getterPrefixes) {
                if (prefix.equals(name)) {
                    return prefix;
                }
                if (name.startsWith(prefix)) {
                    return firstToLowerCase(name.substring(prefix.length()));
                }
            }
            return null;
        } else if (Field.class.isAssignableFrom(field.getClass())) {
            Field method = (Field) field;
            String name = method.getName();
            Class<?> declaringClass = method.getDeclaringClass();
            if (declaringClass.equals(Object.class) || Map.class.isAssignableFrom(declaringClass)
                    || Collection.class.isAssignableFrom(declaringClass)) {
                return null;
            }
            int modifiers = method.getModifiers();
            if (name == null || method.isSynthetic() || Modifier.isStatic(modifiers)
                    || Modifier.isTransient(modifiers)) {
                return null;
            }
            return name.trim();
        }
        return null;
    }
    
    public static boolean shouldIgnoreField(AccessibleObject field) {
        String name = parsePropertyName(field);
        return name == null || name.isEmpty();
    }
    
    protected boolean shouldIgnoreField() {
        return shouldIgnoreField(getAccessibleObject());
    }

    protected boolean shouldIgnoreProperties() {
        return getAccessibleObject().getAnnotation(SchemaIgnoreProperties.class) != null;
    }
}
