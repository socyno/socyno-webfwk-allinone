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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.reinert.jjschema.Nullable;

/**
 * @author Danilo Reinert
 */

public abstract class SchemaWrapper {
    private final Type type;
    private final ObjectNode node = SchemaWrapperFactory.MAPPER.createObjectNode();
    public static final String DRAFT_04 = "http://json-schema.org/draft-04/schema#";

    public SchemaWrapper(Type type) {
        this.type = type;
    }

    public JsonNode asJson() {
        return node;
    }

    public String getDollarSchema() {
        return getNodeTextValue(node.get("$schema"));
    }

    public SchemaWrapper putDollarSchema() {
        node.put("$schema", DRAFT_04);
        return this;
    }

    public String getId() {
        return getNodeTextValue(node.get("id"));
    }

    public String getRef() {
        return getNodeTextValue(node.get("$ref"));
    }

    public String getType() {
        return getNodeTextValue(node.get("type"));
    }

    public ParameterizedType getParameterizedType() {
    	return (ParameterizedType) ((type instanceof ParameterizedType) ? type : null);
    }
    
    public Class<?> getJavaType() {    	
        return (Class<?>) ((type instanceof ParameterizedType) ? ( (ParameterizedType) type).getRawType() : type);
    }

    public boolean isEnumWrapper() {
        return false;
    }

    public boolean isSimpleWrapper() {
        return false;
    }

    public boolean isCustomWrapper() {
        return false;
    }

    public boolean isRefWrapper() {
        return false;
    }

    public boolean isArrayWrapper() {
        return false;
    }

    public boolean isEmptyWrapper() {
        return false;
    }

    public boolean isNullWrapper() {
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T extends SchemaWrapper> T cast() {
        return (T) this;
    }

    protected ObjectNode getNode() {
        return node;
    }

    // TODO: Shouldn't I check the Nullable annotation only on fields or methods?
    protected void processNullable() {
        final Nullable nullable = getJavaType().getAnnotation(Nullable.class);
        if (nullable != null) {
            String oldType = node.get("type").asText();
            ArrayNode typeArray = node.putArray("type");
            typeArray.add(oldType);
            typeArray.add("null");
        }
    }

    protected String getNodeTextValue(JsonNode node) {
        return node == null ? null : node.textValue();
    }

    protected void setType(String type) {
        node.put("type", type);
    }

    protected void setClassPath() {
        node.put("classPath", getJavaType().getName());
    }
}
