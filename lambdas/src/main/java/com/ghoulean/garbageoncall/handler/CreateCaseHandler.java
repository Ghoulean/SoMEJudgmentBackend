package com.ghoulean.garbageoncall.handler;

import javax.inject.Inject;

import com.ghoulean.garbageoncall.accessor.DynamoDbAccessor;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CreateCaseHandler {
    private @NonNull final DynamoDbAccessor dynamoDbAccessor;

    @Inject
    public CreateCaseHandler(@NonNull final DynamoDbAccessor dynamoDbAccessor) {
    }

    public void handle() {
    }
}
