package com.ghoulean.somejudgment.jwt;

import java.util.Base64;
import java.util.Base64.Decoder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.ws.rs.ForbiddenException;
import lombok.NonNull;

public final class JwtValidator {
    private static final Decoder decoder = Base64.getUrlDecoder();

    public static void verifyUserId(@NonNull final String jwtToken, @NonNull final String userId) {
        final String[] chunks = jwtToken.split("\\.");
        final String payload = new String(decoder.decode(chunks[1]));
        final JsonObject parsedPayload = JsonParser.parseString(payload).getAsJsonObject();
        final String sub = parsedPayload.get("sub").getAsString();
        if (!sub.equals(userId)) {
            throw new ForbiddenException();
        }
    }
}
