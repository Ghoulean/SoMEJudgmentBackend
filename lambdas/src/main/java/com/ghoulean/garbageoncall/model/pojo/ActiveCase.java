package com.ghoulean.garbageoncall.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Builder
@Data
@DynamoDbImmutable(builder = ActiveCase.Builder.class)
public final class ActiveCase {
    @Getter(onMethod_ = @DynamoDbPartitionKey)
    final String judge;
    final String submission1;
    final String submission2;
    final String createdAt;
}
