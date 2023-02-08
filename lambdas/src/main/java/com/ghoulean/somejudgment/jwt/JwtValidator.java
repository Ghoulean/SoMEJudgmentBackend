package com.ghoulean.somejudgment.jwt;

import java.util.Base64;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.ws.rs.ForbiddenException;
import lombok.NonNull;

public final class JwtValidator {
    private static final int EXPECTED_JWT_SPLITS = 3;
    public static void verifyUserId(@NonNull final String jwtToken, @NonNull final String userId) {
        final String[] chunks = jwtToken.split("\\.");
        if (chunks.length != EXPECTED_JWT_SPLITS) {
            throw new ForbiddenException();
        }
        final String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));
        final JsonObject parsedPayload = JsonParser.parseString(payload).getAsJsonObject();
        final JsonElement jsonElement = parsedPayload.get("sub");
        if (jsonElement == null) {
            throw new ForbiddenException();
        }
        final String sub = jsonElement.getAsString();
        if (!sub.equals(userId)) {
            throw new ForbiddenException();
        }
    }
}
