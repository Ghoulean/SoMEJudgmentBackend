package com.ghoulean.somejudgment.lambda;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ForbiddenException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.ghoulean.somejudgment.dagger.component.DaggerGetCaseComponent;
import com.ghoulean.somejudgment.dagger.component.GetCaseComponent;
import com.ghoulean.somejudgment.handler.GetCaseHandler;
import com.ghoulean.somejudgment.jwt.JwtValidator;
import com.ghoulean.somejudgment.model.request.GetCaseRequest;
import com.ghoulean.somejudgment.model.response.GetCaseResponse;
import com.google.gson.Gson;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class GetCaseLambda implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    @NonNull
    private final GetCaseHandler getCaseHandler;
    @NonNull
    private final Gson gson;

    private static final int SUCCESS_CODE = 200;

    public GetCaseLambda() {
        GetCaseComponent getCaseComponent = DaggerGetCaseComponent.create();
        getCaseHandler = getCaseComponent.getGetCaseHandler();
        gson = getCaseComponent.getGson();
    }

    @Override
    public Map<String, Object> handleRequest(@NonNull final Map<String, Object> input,
            @NonNull final Context context) {
        log.info("Received request: {}", input);
        final GetCaseRequest getCaseRequest = buildGetCaseRequest(input);
        validateRequest(input, getCaseRequest);
        log.info("Serialized request into: {}", getCaseRequest);
        final GetCaseResponse getCaseResponse = getCaseHandler.handle(getCaseRequest);
        final Map<String, Object> response = buildResponse(getCaseResponse);
        log.info("Returning: {}, serialized from: {}", response, getCaseResponse);
        return response;
    }

    private void validateRequest(@NonNull final Map<String, Object> input,
            @NonNull final GetCaseRequest getCaseRequest) {
        final String jwtToken = getJwtToken(input);
        try {
            JwtValidator.verifyUserId(jwtToken, getCaseRequest.getJudgeId());
        } catch (Exception e) {
            throw new ForbiddenException();
        }
    }

    private String getJwtToken(@NonNull final Map<String, Object> input) {
        @NonNull
        final Map<String, String> headers = (Map<String, String>) input.get("headers");
        @NonNull
        final String authHeader = headers.get("Authorization");
        final String jwtToken = authHeader.split(" ")[1];
        return jwtToken;
    }

    private GetCaseRequest buildGetCaseRequest(@NonNull final Map<String, Object> input) {
        try {
            return gson.fromJson(input.get("body").toString(), GetCaseRequest.class);
        } catch (final Exception e) {
            throw new ForbiddenException(e);
        }
    }

    private Map<String, Object> buildResponse(@NonNull final GetCaseResponse getCaseResponse) {
        final HashMap<String, Object> response = new HashMap<>();
        response.put("statusCode", SUCCESS_CODE);
        response.put("headers", Map.of("Content-Type", "application/json"));
        response.put("body", gson.toJson(getCaseResponse));
        return response;
    }
}
