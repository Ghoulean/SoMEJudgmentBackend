package com.ghoulean.somejudgment.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.lambda.runtime.Context;
import com.ghoulean.somejudgment.handler.SubmitJudgmentHandler;
import com.ghoulean.somejudgment.model.pojo.Judgment;
import com.ghoulean.somejudgment.model.request.SubmitJudgmentRequest;
import com.ghoulean.somejudgment.model.response.SubmitJudgmentResponse;
import com.google.gson.Gson;

import jakarta.ws.rs.ForbiddenException;

@ExtendWith(MockitoExtension.class)
public final class SubmitJudgmentLambdaTest {
    @Mock
    SubmitJudgmentHandler mockSubmitJudgmentHandler;
    @Mock
    Gson mockGson;
    @Mock
    Context mockContext;

    private static final String JUDGE_ID = "1234567890";
    private static final String GOOD_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyfQ.L8i6g3PfcHlioHCCPURC9pmXT7gdJpx3kOoyAfNUwCc";
    private static final String BAD_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwOTg3NjU0MzIxIiwiaWF0IjoxNTE2MjM5MDIyfQ.pb_oZmOW_RtINeiLV25ysL2wqesRj3QTR-PQB3nAIS8";
    private static final SubmitJudgmentRequest SUBMIT_JUDGMENT_REQUEST = SubmitJudgmentRequest.builder()
            .judgment(Judgment.builder()
                    .judgeId(JUDGE_ID)
                    .winnerId("winnerId")
                    .loserId("loserId")
                    .build())
            .build();
    private static EasyRandom easyRandom;
    private static SubmitJudgmentLambda submitJudgmentLambda;

    @BeforeEach
    public void setup() {
        easyRandom = new EasyRandom();
        submitJudgmentLambda = new SubmitJudgmentLambda(mockSubmitJudgmentHandler, mockGson);
    }

    @Test
    public void successfulHandleRequest() {
        final String encodedRequest = easyRandom.nextObject(String.class);
        final Map<String, Object> input = Map.of("headers", Map.of("Authorization", "Bearer " + GOOD_JWT_TOKEN),
                "body", encodedRequest);
        final SubmitJudgmentResponse SubmitJudgmentResponse = easyRandom.nextObject(SubmitJudgmentResponse.class);
        final String serializedResponse = easyRandom.nextObject(String.class);

        when(mockGson.fromJson(encodedRequest, SubmitJudgmentRequest.class)).thenReturn(SUBMIT_JUDGMENT_REQUEST);
        when(mockSubmitJudgmentHandler.handle(SUBMIT_JUDGMENT_REQUEST)).thenReturn(SubmitJudgmentResponse);
        when(mockGson.toJson(SubmitJudgmentResponse)).thenReturn(serializedResponse);

        final Map<String, Object> expectedResponse = Map.of("statusCode", 200,
                "headers", Map.of("Content-Type", "application/json"),
                "body", serializedResponse);

        assertEquals(expectedResponse, submitJudgmentLambda.handleRequest(input, mockContext));
    }

    @Test
    public void shouldThrowForbiddenWhenBadBody() {
        final String badEncodedRequest = easyRandom.nextObject(String.class);
        final Map<String, Object> input = Map.of("headers", Map.of("Authorization", "Bearer " + GOOD_JWT_TOKEN),
                "body", badEncodedRequest);

        when(mockGson.fromJson(badEncodedRequest, SubmitJudgmentRequest.class)).thenThrow(new RuntimeException());

        assertThrows(ForbiddenException.class, () -> {
            submitJudgmentLambda.handleRequest(input, mockContext);
        });

    }

    @Test
    public void shouldThrowForbiddenWhenBadJwt() {
        final String encodedRequest = easyRandom.nextObject(String.class);
        final Map<String, Object> input = Map.of("headers", Map.of("Authorization", "Bearer " + BAD_JWT_TOKEN),
                "body", encodedRequest);

        when(mockGson.fromJson(encodedRequest, SubmitJudgmentRequest.class)).thenReturn(SUBMIT_JUDGMENT_REQUEST);

        assertThrows(ForbiddenException.class, () -> {
            submitJudgmentLambda.handleRequest(input, mockContext);
        });
    }
}
