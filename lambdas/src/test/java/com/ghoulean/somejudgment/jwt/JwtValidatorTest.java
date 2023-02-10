package com.ghoulean.somejudgment.jwt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.ForbiddenException;

public final class JwtValidatorTest {
    private static final String EXPECTED_SUB = "1234567890";

    @Test
    public void nullPointerExceptionOnNullInputs() {
        assertThrows(NullPointerException.class, () -> {
            JwtValidator.verifyUserId(null, EXPECTED_SUB);
        });
        assertThrows(NullPointerException.class, () -> {
            JwtValidator.verifyUserId(EXPECTED_SUB, null);
        });
    }

    @Test
    public void verifyCorrectUserId() {
        final String encodedPayload = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyfQ.L8i6g3PfcHlioHCCPURC9pmXT7gdJpx3kOoyAfNUwCc";
        assertDoesNotThrow(() -> {
            JwtValidator.verifyUserId(encodedPayload, EXPECTED_SUB);
        });
    }

    @Test
    public void rejectMissingUserId() {
        final String encodedPayload = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE1MTYyMzkwMjJ9.tbDepxpstvGdW8TC3G8zg4B6rUYAOvfzdceoH48wgRQ";
        assertThrows(ForbiddenException.class, () -> {
            JwtValidator.verifyUserId(encodedPayload, EXPECTED_SUB);
        });
    }

    @Test
    public void rejectIncorrectUserId() {
        final String encodedPayload = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwOTg3NjU0MzIxIiwiaWF0IjoxNTE2MjM5MDIyfQ.pb_oZmOW_RtINeiLV25ysL2wqesRj3QTR-PQB3nAIS8";
        assertThrows(ForbiddenException.class, () -> {
            JwtValidator.verifyUserId(encodedPayload, EXPECTED_SUB);
        });

    }

    @Test
    public void rejectMalformedPayload() {
        final String encodedPayload = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwOTdfasdfa0MzIxIiwiaWF0IjoxNTE2MjM5MDIyfQ";
        assertThrows(ForbiddenException.class, () -> {
            JwtValidator.verifyUserId(encodedPayload, EXPECTED_SUB);
        });
    }
}
