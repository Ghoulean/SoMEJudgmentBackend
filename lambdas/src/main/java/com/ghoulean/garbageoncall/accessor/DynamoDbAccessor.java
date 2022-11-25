package com.ghoulean.garbageoncall.accessor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Slf4j
@Singleton
public final class DynamoDbAccessor {
    private @NonNull final DynamoDbClient dynamoDB;
    private @NonNull final String tableName;

    @Inject
    public DynamoDbAccessor(@NonNull final DynamoDbClient dynamoDB, @Named("tableName") final String tableName) {
        this.dynamoDB = dynamoDB;
        this.tableName = tableName;
    }

}
