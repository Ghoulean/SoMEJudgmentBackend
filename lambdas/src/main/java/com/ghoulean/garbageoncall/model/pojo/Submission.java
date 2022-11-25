package com.ghoulean.garbageoncall.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Builder
@Data
@DynamoDbImmutable(builder = Submission.Builder.class)
public final class Submission {
    @Getter(onMethod_ = @DynamoDbPartitionKey)
    final int id; // Must be enumerated as 0 to (num of entries - 1)
    final String submissionLink;
    final String submitters;
    final String emails;
}
