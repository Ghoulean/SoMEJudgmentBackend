package com.ghoulean.somejudgment.jwt;

import io.jsonwebtoken.Jwts;
import lombok.NonNull;

public final class JwtValidator {
    public static void verifyUserId(@NonNull final String jwtToken, @NonNull final String userId) {
        Jwts.parserBuilder()
                .requireSubject(userId)
                .build()
                .parse(jwtToken);
    }
}
