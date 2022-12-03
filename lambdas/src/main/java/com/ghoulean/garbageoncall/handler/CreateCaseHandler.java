package com.ghoulean.garbageoncall.handler;

import javax.inject.Inject;

import com.ghoulean.garbageoncall.accessor.DynamoDbAccessor;
import com.ghoulean.garbageoncall.model.request.CreateCaseRequest;
import com.ghoulean.garbageoncall.model.response.CreateCaseResponse;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CreateCaseHandler {
    private @NonNull final DynamoDbAccessor dynamoDbAccessor;

    @Inject
    public CreateCaseHandler(@NonNull final DynamoDbAccessor dynamoDbAccessor) {
        this.dynamoDbAccessor = dynamoDbAccessor;
    }

    public CreateCaseResponse handle(CreateCaseRequest createCaseRequest) {
        return null;
    }
}
