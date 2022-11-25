package com.ghoulean.garbageoncall.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Builder
@Data
@DynamoDbImmutable(builder = Judgment.Builder.class)
public final class Judgment {
    @Getter(onMethod_ = @DynamoDbPartitionKey)
    final String judge;
    final String winnerId;
    final String loserId;
    final String winnerFeedback;
    final String loserFeedback;
}
