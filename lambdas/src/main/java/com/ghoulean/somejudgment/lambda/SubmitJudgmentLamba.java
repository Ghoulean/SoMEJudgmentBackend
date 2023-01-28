package com.ghoulean.somejudgment.lambda;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ForbiddenException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.ghoulean.somejudgment.dagger.component.DaggerSubmitJudgmentComponent;
import com.ghoulean.somejudgment.dagger.component.SubmitJudgmentComponent;
import com.ghoulean.somejudgment.handler.SubmitJudgmentHandler;
import com.ghoulean.somejudgment.jwt.JwtValidator;
import com.ghoulean.somejudgment.model.request.SubmitJudgmentRequest;
import com.ghoulean.somejudgment.model.response.SubmitJudgmentResponse;
import com.google.gson.Gson;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SubmitJudgmentLamba implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    @NonNull
    private final SubmitJudgmentHandler submitJudgmentHandler;
    @NonNull
    private final Gson gson;

    private static final int SUCCESS_CODE = 200;

    public SubmitJudgmentLamba() {
        SubmitJudgmentComponent submitJudgmentComponent = DaggerSubmitJudgmentComponent.create();
        submitJudgmentHandler = submitJudgmentComponent.getSubmitJudgmentHandler();
        gson = submitJudgmentComponent.getGson();
    }

    @Override
    public Map<String, Object> handleRequest(@NonNull final Map<String, Object> input,
            @NonNull final Context context) {
        log.info("Received request: {}", input);
        final SubmitJudgmentRequest submitJudgmentRequest = buildSubmitJudgmentRequest(input);
        validateRequest(input, submitJudgmentRequest);
        log.info("Serialized request into: {}", submitJudgmentRequest);
        final SubmitJudgmentResponse submitJudgmentResponse = submitJudgmentHandler.handle(submitJudgmentRequest);
        final Map<String, Object> response = buildResponse(submitJudgmentResponse);
        log.info("Returning: {}, serialized from: {}", response, submitJudgmentResponse);
        return response;

    }

    private void validateRequest(@NonNull final Map<String, Object> input,
            @NonNull final SubmitJudgmentRequest submitJudgmentRequest) {
        final String jwtToken = getJwtToken(input);
        try {
            JwtValidator.verifyUserId(jwtToken, submitJudgmentRequest.getJudgment().getJudgeId());
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

    private SubmitJudgmentRequest buildSubmitJudgmentRequest(@NonNull final Map<String, Object> input) {
        try {
            return gson.fromJson(input.get("body").toString(), SubmitJudgmentRequest.class);
        } catch (final Exception e) {
            throw new ForbiddenException(e);
        }
    }

    private Map<String, Object> buildResponse(@NonNull final SubmitJudgmentResponse submitJudgmentResponse) {
        final HashMap<String, Object> response = new HashMap<>();
        response.put("statusCode", SUCCESS_CODE);
        response.put("headers", Map.of("Content-Type", "application/json"));
        response.put("body", gson.toJson(submitJudgmentResponse));
        return response;
    }

}
