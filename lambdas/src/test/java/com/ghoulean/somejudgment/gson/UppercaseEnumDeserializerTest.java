package com.ghoulean.somejudgment.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ghoulean.somejudgment.model.enums.SubmissionType;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class UppercaseEnumDeserializerTest {
    private static UppercaseEnumDeserializer<SubmissionType> uppercaseEnumDeserializer;

    @BeforeEach
    public void setup() {
        uppercaseEnumDeserializer = new UppercaseEnumDeserializer<>();
    }

    @Test
    public void successfulDeserialize() {
        final JsonElement uppercase = new JsonPrimitive(SubmissionType.VIDEO.name().toUpperCase());
        final JsonElement lowercase = new JsonPrimitive(SubmissionType.VIDEO.name().toLowerCase());
        final JsonElement arbitraryCase = new JsonPrimitive("viDeO");
        assertEquals(SubmissionType.VIDEO, uppercaseEnumDeserializer.deserialize(uppercase, SubmissionType.class, null));
        assertEquals(SubmissionType.VIDEO, uppercaseEnumDeserializer.deserialize(lowercase, SubmissionType.class, null));
        assertEquals(SubmissionType.VIDEO, uppercaseEnumDeserializer.deserialize(arbitraryCase, SubmissionType.class, null));
    }

    @Test
    public void returnNullOnInvalidEnumValue() {
        final JsonElement badValue = new JsonPrimitive("something");
        assertNull(uppercaseEnumDeserializer.deserialize(badValue, SubmissionType.class, null));
    }

    @Test
    public void returnNullOnNonstringJsonValue() {
        final JsonElement badValue = new JsonPrimitive(12345);
        assertNull(uppercaseEnumDeserializer.deserialize(badValue, SubmissionType.class, null));
    }

    @Test
    public void returnNullOnInvalidType() {
        final JsonElement uppercase = new JsonPrimitive(SubmissionType.VIDEO.name().toUpperCase());
        assertNull(uppercaseEnumDeserializer.deserialize(uppercase, null, null));
        assertNull(uppercaseEnumDeserializer.deserialize(uppercase, JsonElement.class, null));
    }
}
