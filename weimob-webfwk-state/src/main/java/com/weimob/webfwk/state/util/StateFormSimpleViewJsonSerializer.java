package com.weimob.webfwk.state.util;

import com.google.gson.*;
import com.weimob.webfwk.state.abs.AbstractStateFormEventResultView;
import com.weimob.webfwk.util.context.HttpMessageConverter;

import java.lang.reflect.Type;

public class StateFormSimpleViewJsonSerializer implements JsonSerializer<AbstractStateFormEventResultView>,
        JsonDeserializer<AbstractStateFormEventResultView> {
    
    @Override
    public AbstractStateFormEventResultView deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext deserializer) throws JsonParseException {
        return HttpMessageConverter.getDefault().fromJson(json, typeOfT);
    }
    
    @Override
    public JsonElement serialize(AbstractStateFormEventResultView obj, Type typeOfT,
            JsonSerializationContext serializer) {
        if (obj == null) {
            return null;
        }
        JsonElement json = HttpMessageConverter.getDefault().toJsonTree(obj);
        if (json != null && json.isJsonObject()) {
            ((JsonObject) json).add("form", json.deepCopy());
        }
        return json;
    }
    
}
