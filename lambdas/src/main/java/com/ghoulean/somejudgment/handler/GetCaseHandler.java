package com.ghoulean.somejudgment.handler;

import javax.inject.Inject;

import com.ghoulean.somejudgment.accessor.DynamoDbAccessor;
import com.ghoulean.somejudgment.model.request.GetCaseRequest;
import com.ghoulean.somejudgment.model.response.GetCaseResponse;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class GetCaseHandler {
    private @NonNull final DynamoDbAccessor dynamoDbAccessor;

    @Inject
    public GetCaseHandler(@NonNull final DynamoDbAccessor dynamoDbAccessor) {
        this.dynamoDbAccessor = dynamoDbAccessor;
    }

    public GetCaseResponse handle(final GetCaseRequest getCaseRequest) {
        return null;
    }
}
