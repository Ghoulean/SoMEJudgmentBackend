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
import com.ghoulean.somejudgment.handler.GetCaseHandler;
import com.ghoulean.somejudgment.model.enums.SubmissionType;
import com.ghoulean.somejudgment.model.pojo.NewActiveCaseOptions;
import com.ghoulean.somejudgment.model.request.GetCaseRequest;
import com.ghoulean.somejudgment.model.response.GetCaseResponse;
import com.google.gson.Gson;

import jakarta.ws.rs.ForbiddenException;

@ExtendWith(MockitoExtension.class)
public final class GetCaseLambdaTest {
    @Mock
    GetCaseHandler mockGetCaseHandler;
    @Mock
    Gson mockGson;
    @Mock
    Context mockContext;

    private static final String JUDGE_ID = "1234567890";
    private static final String GOOD_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyfQ.L8i6g3PfcHlioHCCPURC9pmXT7gdJpx3kOoyAfNUwCc";
    private static final String BAD_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwOTg3NjU0MzIxIiwiaWF0IjoxNTE2MjM5MDIyfQ.pb_oZmOW_RtINeiLV25ysL2wqesRj3QTR-PQB3nAIS8";
    private static final NewActiveCaseOptions NEW_ACTIVE_CASE_OPTIONS = NewActiveCaseOptions.builder()
            .submissionType(SubmissionType.VIDEO)
            .build();
    private static final GetCaseRequest GET_CASE_REQUEST = GetCaseRequest.builder()
            .judgeId(JUDGE_ID)
            .newActiveCaseOptions(NEW_ACTIVE_CASE_OPTIONS)
            .build();

    private static final EasyRandom easyRandom = new EasyRandom();
    private static GetCaseLambda getCaseLambda;

    @BeforeEach
    public void setup() {
        getCaseLambda = new GetCaseLambda(mockGetCaseHandler, mockGson);
    }

    @Test
    public void successfulHandleRequest() {
        final String encodedRequest = easyRandom.nextObject(String.class);
        final Map<String, Object> input = Map.of("headers", Map.of("Authorization", "Bearer " + GOOD_JWT_TOKEN),
                "body", encodedRequest);
        final GetCaseResponse getCaseResponse = easyRandom.nextObject(GetCaseResponse.class);
        final String serializedResponse = easyRandom.nextObject(String.class);

        when(mockGson.fromJson(encodedRequest, GetCaseRequest.class)).thenReturn(GET_CASE_REQUEST);
        when(mockGetCaseHandler.handle(GET_CASE_REQUEST)).thenReturn(getCaseResponse);
        when(mockGson.toJson(getCaseResponse)).thenReturn(serializedResponse);

        final Map<String, Object> expectedResponse = Map.of("statusCode", 200,
                "headers", Map.of("Content-Type", "application/json"),
                "body", serializedResponse);

        assertEquals(expectedResponse, getCaseLambda.handleRequest(input, mockContext));
    }

    @Test
    public void shouldThrowForbiddenWhenBadBody() {
        final String badEncodedRequest = easyRandom.nextObject(String.class);
        final Map<String, Object> input = Map.of("headers", Map.of("Authorization", "Bearer " + GOOD_JWT_TOKEN),
                "body", badEncodedRequest);

        when(mockGson.fromJson(badEncodedRequest, GetCaseRequest.class)).thenThrow(new RuntimeException());

        assertThrows(ForbiddenException.class, () -> {
            getCaseLambda.handleRequest(input, mockContext);
        });

    }

    @Test
    public void shouldThrowForbiddenWhenBadJwt() {
        final String encodedRequest = easyRandom.nextObject(String.class);
        final Map<String, Object> input = Map.of("headers", Map.of("Authorization", "Bearer " + BAD_JWT_TOKEN),
                "body", encodedRequest);

        when(mockGson.fromJson(encodedRequest, GetCaseRequest.class)).thenReturn(GET_CASE_REQUEST);

        assertThrows(ForbiddenException.class, () -> {
            getCaseLambda.handleRequest(input, mockContext);
        });
    }
}
