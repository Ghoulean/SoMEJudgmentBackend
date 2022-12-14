package com.ghoulean.somejudgment.lambda;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ForbiddenException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.ghoulean.somejudgment.dagger.component.DaggerGetCaseComponent;
import com.ghoulean.somejudgment.dagger.component.GetCaseComponent;
import com.ghoulean.somejudgment.handler.GetCaseHandler;
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
        final GetCaseRequest getCaseRequest = buildGetCaseRequest(input);
        validateRequest(input, getCaseRequest);
        log.info("Received: {}, serialized into: {}", input, getCaseRequest);
        final GetCaseResponse getCaseResponse = getCaseHandler.handle(getCaseRequest);
        final Map<String, Object> response = buildResponse(getCaseResponse);
        log.info("Returning: {}, serialized from: {}", response, getCaseResponse);
        return response;
    }

    private void validateRequest(@NonNull final Map<String, Object> input,
            @NonNull final GetCaseRequest getCaseRequest) {
        @NonNull
        final Map<String, String> headers = (Map<String, String>) input.get("headers");
        @NonNull
        final String judgeIdHeader = headers.get("judgeId");
        if (!getCaseRequest.getJudgeId().equals(judgeIdHeader)) {
            throw new ForbiddenException();
        }
    }

    private GetCaseRequest buildGetCaseRequest(@NonNull final Map<String, Object> input) {
        return gson.fromJson(input.get("body").toString(), GetCaseRequest.class);
    }

    private Map<String, Object> buildResponse(@NonNull final GetCaseResponse getCaseResponse) {
        final HashMap<String, Object> response = new HashMap<>();
        response.put("statusCode", SUCCESS_CODE);
        response.put("headers", Map.of("Content-Type", "application/json"));
        response.put("body", gson.toJson(getCaseResponse));
        return response;
    }
}
