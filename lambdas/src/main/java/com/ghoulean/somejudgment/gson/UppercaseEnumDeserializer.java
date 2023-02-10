package com.ghoulean.somejudgment.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public final class UppercaseEnumDeserializer<E extends Enum<E>> implements JsonDeserializer<E> {
    @Override
    @SuppressWarnings("unchecked")
    public E deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context)
            throws JsonParseException {
        if (type instanceof Class && ((Class<?>) type).isEnum()) {
            try {
                return (E) Enum.valueOf((Class<E>) type, json.getAsString().toUpperCase());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
