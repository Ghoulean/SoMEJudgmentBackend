package com.ghoulean.garbageoncall.accessor;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.ghoulean.garbageoncall.dagger.Constants;
import com.ghoulean.garbageoncall.model.pojo.ActiveCase;
import com.ghoulean.garbageoncall.model.pojo.Feedback;
import com.ghoulean.garbageoncall.model.pojo.Judgment;
import com.ghoulean.garbageoncall.model.pojo.TableSize;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

@Slf4j
@Singleton
public final class DynamoDbAccessor {
    private static final String PARTITION_KEY = "PK";
    private static final String SORT_KEY = "SK";
    private static final String ACTIVE_CASE_SORT_KEY = "CASE#ACTIVE";
    private static final String JUDGMENT_COUNT_PARTITION_KEY = "COUNT#JUDGMENTS";
    private static final String JUDGMENT_COUNT_SORT_KEY = "NOT_USED";

    private static final int JUDGMENT_MAX_PAGE_SIZE = 10;
    private @NonNull final DynamoDbClient dynamoDB;
    private @NonNull final String tableName;

    @Inject
    public DynamoDbAccessor(@NonNull final DynamoDbClient dynamoDB,
            @Named(Constants.TABLE_NAME) final String tableName) {
        this.dynamoDB = dynamoDB;
        this.tableName = tableName;
    }

    public ActiveCase getActiveCase(@NonNull final String judgeId) {
        log.info("DynamoDbAccessor::getActiveCase invoked with judgeId={}", judgeId);
        final HashMap<String, AttributeValue> keyToGet = new HashMap<String, AttributeValue>();
        keyToGet.put(PARTITION_KEY, AttributeValue.builder()
                .s(createUserIdPartitionKey(judgeId)).build());
        keyToGet.put(SORT_KEY, AttributeValue.builder()
                .s(ACTIVE_CASE_SORT_KEY).build());
        final GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(tableName)
                .build();
        try {
            final Map<String, AttributeValue> item = dynamoDB.getItem(request).item();
            if (item != null) {
                ActiveCase activeCase = ActiveCase.builder()
                        .judgeId(judgeId)
                        .submission1(item.get(ActiveCase.Fields.submission1).s())
                        .submission2(item.get(ActiveCase.Fields.submission2).s())
                        .createdAt(convertEpochSecondToInstant(item.get(ActiveCase.Fields.createdAt).n()))
                        .build();
                log.info("DynamoDbAccessor::getActiveCase: Received activeCase={} for judgeId={}", activeCase, judgeId);
                return activeCase;
            } else {
                throw new RuntimeException("Could not find ActiveCase ddb entry for " + judgeId);
            }
        } catch (DynamoDbException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Judgment> getJudgments(@NonNull final String judgeId, final String token, final Integer pageSize) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public TableSize getTotalNumberOfJudgments() {
        log.info("DynamoDbAccessor::getTotalNumberOfJudgments");
        final HashMap<String, AttributeValue> keyToGet = new HashMap<String, AttributeValue>();
        keyToGet.put(PARTITION_KEY, AttributeValue.builder()
                .s(JUDGMENT_COUNT_PARTITION_KEY).build());
        keyToGet.put(SORT_KEY, AttributeValue.builder()
                .s(JUDGMENT_COUNT_SORT_KEY).build());
        final GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(tableName)
                .build();
        try {
            final Map<String, AttributeValue> item = dynamoDB.getItem(request).item();
            if (item != null) {
                TableSize tableSize = TableSize.builder()
                        .amount(Integer.valueOf(item.get(TableSize.Fields.amount).n()))
                        .build();
                log.info("DynamoDbAccessor::getTotalNumberOfJudgments: Received tableSize={}", tableSize);
                return tableSize;
            } else {
                throw new RuntimeException("Could not find TableSize ddb entry");
            }
        } catch (DynamoDbException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void insertJudgment(@NonNull final Judgment judgment) {
        log.info("DynamoDbAccessor::insertJudgment invoked with judgment={}", judgment);
        final HashMap<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put(PARTITION_KEY,
                AttributeValue.builder().s(createUserIdPartitionKey(judgment.getJudgeId())).build());
        itemValues.put(SORT_KEY, AttributeValue.builder().s(createJudgmentSortKey()).build());
        itemValues.put(Judgment.Fields.winnerId, AttributeValue.builder().s(judgment.getWinnerId()).build());
        itemValues.put(Judgment.Fields.loserId, AttributeValue.builder().s(judgment.getLoserId()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemValues)
                .build();
        try {
            PutItemResponse response = dynamoDB.putItem(request);
            log.info("DynamoDbAccessor::insertJudgment successful with requestId={}",
                    response.responseMetadata().requestId());
        } catch (DynamoDbException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void upsertActiveCase(@NonNull final ActiveCase activeCase) {
        log.info("DynamoDbAccessor::upsertActiveCase invoked with activeCase={}", activeCase);
        final HashMap<String, AttributeValue> keyValues = new HashMap<>();
        final HashMap<String, AttributeValue> itemValues = new HashMap<>();
        keyValues.put(PARTITION_KEY,
                AttributeValue.builder().s(createUserIdPartitionKey(activeCase.getJudgeId())).build());
        keyValues.put(SORT_KEY, AttributeValue.builder().s(ACTIVE_CASE_SORT_KEY).build());
        itemValues.put(ActiveCase.Fields.submission1, AttributeValue.builder().s(activeCase.getSubmission1()).build());
        itemValues.put(ActiveCase.Fields.submission2, AttributeValue.builder().s(activeCase.getSubmission2()).build());
        itemValues.put(ActiveCase.Fields.createdAt,
                AttributeValue.builder().s(convertInstantToEpochSecond(activeCase.getCreatedAt())).build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(keyValues)
                .updateExpression(generateUpdateExpression(itemValues))
                .expressionAttributeValues(generateExpressionAttributeValues(itemValues))
                .build();
        try {
            UpdateItemResponse response = dynamoDB.updateItem(request);
            log.info("DynamoDbAccessor::upsertActiveCase successful with requestId={}",
                    response.responseMetadata().requestId());
        } catch (DynamoDbException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void upsertFeedback(@NonNull final Feedback feedback) {
        log.info("DynamoDbAccessor::upsertFeedback invoked with feedback={}", feedback);
        final HashMap<String, AttributeValue> keyValues = new HashMap<>();
        final HashMap<String, AttributeValue> itemValues = new HashMap<>();
        keyValues.put(PARTITION_KEY,
                AttributeValue.builder().s(createUserIdPartitionKey(feedback.getJudgeId())).build());
        keyValues.put(SORT_KEY, AttributeValue.builder().s(createFeedbackSortKey(feedback.getSubmissionId())).build());
        itemValues.put(Feedback.Fields.feedback, AttributeValue.builder().s(feedback.getFeedback()).build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(keyValues)
                .updateExpression(generateUpdateExpression(itemValues))
                .expressionAttributeValues(generateExpressionAttributeValues(itemValues))
                .build();
        try {
            UpdateItemResponse response = dynamoDB.updateItem(request);
            log.info("DynamoDbAccessor::upsertFeedback successful with requestId={}",
                    response.responseMetadata().requestId());
        } catch (DynamoDbException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void incrementTableSize() {
        log.info("DynamoDbAccessor::incrementTableSize");
        final HashMap<String, AttributeValue> keyValues = new HashMap<>();
        keyValues.put(PARTITION_KEY,
                AttributeValue.builder().s(JUDGMENT_COUNT_PARTITION_KEY).build());
        keyValues.put(SORT_KEY, AttributeValue.builder().s(JUDGMENT_COUNT_SORT_KEY).build());

        final String updateExpression = String.format("SET %s = %s + :c", TableSize.Fields.amount,
                TableSize.Fields.amount);

        final HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":c", AttributeValue.fromN("1"));

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(keyValues)
                .updateExpression(updateExpression)
                .expressionAttributeValues(expressionAttributeValues)
                .build();
        try {
            UpdateItemResponse response = dynamoDB.updateItem(request);
            log.info("DynamoDbAccessor::incrementTableSize successful with requestId={}",
                    response.responseMetadata().requestId());
        } catch (DynamoDbException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String createUserIdPartitionKey(@NonNull final String userId) {
        return "USER#" + userId;
    }

    private String createJudgmentSortKey() {
        return "JUDGMENT#" + UUID.randomUUID();
    }

    private String createFeedbackSortKey(@NonNull final String submissionId) {
        return "FEEDBACK#" + submissionId;
    }

    private Instant convertEpochSecondToInstant(final String epochSecond) {
        return Instant.ofEpochSecond(Long.valueOf(epochSecond));
    }

    private String convertInstantToEpochSecond(final Instant instant) {
        return String.valueOf(instant.getEpochSecond());
    }

    private String generateUpdateExpression(final Map<String, AttributeValue> itemValues) {
        final StringBuilder expressionBuilder = new StringBuilder("SET ");
        for (final String key : itemValues.keySet()) {
            expressionBuilder.append(key + " = :" + key + ",");
        }
        expressionBuilder.deleteCharAt(expressionBuilder.length() - 1);
        return expressionBuilder.toString();
    }

    private Map<String, AttributeValue> generateExpressionAttributeValues(
            final Map<String, AttributeValue> itemValues) {
        final HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        for (final Map.Entry<String, AttributeValue> entry : itemValues.entrySet()) {
            final String key = entry.getKey();
            final AttributeValue value = entry.getValue();
            expressionAttributeValues.put(":" + key, value);
        }
        return expressionAttributeValues;
    }
}
