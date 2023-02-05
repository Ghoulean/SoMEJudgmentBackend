package com.ghoulean.somejudgment.accessor.database;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.ghoulean.somejudgment.dagger.Constants;
import com.ghoulean.somejudgment.model.enums.SubmissionType;
import com.ghoulean.somejudgment.model.pojo.ActiveCase;
import com.ghoulean.somejudgment.model.pojo.Feedback;
import com.ghoulean.somejudgment.model.pojo.Judgment;
import com.ghoulean.somejudgment.model.pojo.JudgmentCount;
import com.ghoulean.somejudgment.model.pojo.NewActiveCaseOptions;
import com.google.gson.Gson;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

@Slf4j
@Singleton
public final class DynamoDbAccessor implements DatabaseAccessor {
    private static final String PARTITION_KEY = "PK";
    private static final String SORT_KEY = "SK";
    private static final String ACTIVE_CASE_SORT_KEY = "CASE#ACTIVE";
    private static final String JUDGMENT_COUNT_PARTITION_KEY = "COUNT#JUDGMENTS";

    // private static final int JUDGMENT_MAX_PAGE_SIZE = 10;
    private @NonNull final DynamoDbClient dynamoDB;
    private @NonNull final String tableName;
    private @NonNull final Gson gson;

    @Inject
    public DynamoDbAccessor(@NonNull final DynamoDbClient dynamoDB,
            @Named(Constants.TABLE_NAME) final String tableName,
            @NonNull final Gson gson) {
        this.dynamoDB = dynamoDB;
        this.tableName = tableName;
        this.gson = gson;
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
                .consistentRead(true)
                .build();
        try {
            final GetItemResponse getItemResponse = dynamoDB.getItem(request);
            if (getItemResponse.hasItem()) {
                final Map<String, AttributeValue> item = getItemResponse.item();
                ActiveCase activeCase = ActiveCase.builder()
                        .judgeId(judgeId)
                        .submission1(item.get(ActiveCase.Fields.submission1).s())
                        .submission2(item.get(ActiveCase.Fields.submission2).s())
                        .createdAt(convertEpochSecondToInstant(item.get(ActiveCase.Fields.createdAt).n()))
                        .createdOptions(gson.fromJson(item.get(ActiveCase.Fields.createdOptions).s(),
                                NewActiveCaseOptions.class))
                        .build();
                log.info("DynamoDbAccessor::getActiveCase: Received activeCase={} for judgeId={}", activeCase, judgeId);
                return activeCase;
            } else {
                log.info("DynamoDbAccessor::getActiveCase: Could not find ActiveCase ddb entry for {}", judgeId);
                return null;
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

    public JudgmentCount getJudgmentCount(@NonNull final SubmissionType submissionType) {
        log.info("DynamoDbAccessor::getJudgmentCount with submissionType={}", submissionType);
        final HashMap<String, AttributeValue> keyToGet = new HashMap<String, AttributeValue>();
        keyToGet.put(PARTITION_KEY, AttributeValue.builder()
                .s(JUDGMENT_COUNT_PARTITION_KEY).build());
        keyToGet.put(SORT_KEY, AttributeValue.builder()
                .s(createJudgmentCountSortKey(submissionType)).build());
        final GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(tableName)
                .consistentRead(true)
                .build();
        try {
            final GetItemResponse getItemResponse = dynamoDB.getItem(request);
            if (getItemResponse.hasItem()) {
                final Map<String, AttributeValue> item = getItemResponse.item();
                JudgmentCount judgmentCount = JudgmentCount.builder()
                        .amount(Integer.valueOf(item.get(JudgmentCount.Fields.amount).n()))
                        .build();
                log.info("DynamoDbAccessor::getJudgmentCount: Received tableSize={}", judgmentCount);
                return judgmentCount;
            } else {
                log.info("DynamoDbAccessor::getJudgmentCount: Could not find JudgmentCount ddb entry, defaulting to 0");
                return JudgmentCount.builder()
                        .amount(0)
                        .build();
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
                AttributeValue.fromS(createUserIdPartitionKey(judgment.getJudgeId())));
        itemValues.put(SORT_KEY, AttributeValue.fromS(createJudgmentSortKey()));
        itemValues.put(Judgment.Fields.winnerId, AttributeValue.fromS(judgment.getWinnerId()));
        itemValues.put(Judgment.Fields.loserId, AttributeValue.fromS(judgment.getLoserId()));

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
                AttributeValue.fromS(createUserIdPartitionKey(activeCase.getJudgeId())));
        keyValues.put(SORT_KEY, AttributeValue.fromS(ACTIVE_CASE_SORT_KEY));
        itemValues.put(ActiveCase.Fields.submission1, AttributeValue.fromS(activeCase.getSubmission1()));
        itemValues.put(ActiveCase.Fields.submission2, AttributeValue.fromS(activeCase.getSubmission2()));
        itemValues.put(ActiveCase.Fields.createdAt,
                AttributeValue.fromN(convertInstantToEpochSecond(activeCase.getCreatedAt())));
        itemValues.put(ActiveCase.Fields.createdOptions,
                AttributeValue.fromS(gson.toJson(activeCase.getCreatedOptions())));

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
                AttributeValue.fromS(createUserIdPartitionKey(feedback.getJudgeId())));
        keyValues.put(SORT_KEY, AttributeValue.fromS(createFeedbackSortKey(feedback.getSubmissionId())));
        itemValues.put(Feedback.Fields.feedback, AttributeValue.fromS(feedback.getFeedback()));

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

    public void incrementJudgmentCount(@NonNull final SubmissionType submissionType) {
        log.info("DynamoDbAccessor::incrementJudgmentCount with submissionType={}", submissionType);
        final HashMap<String, AttributeValue> keyValues = new HashMap<>();
        keyValues.put(PARTITION_KEY,
                AttributeValue.fromS(JUDGMENT_COUNT_PARTITION_KEY));
        keyValues.put(SORT_KEY, AttributeValue.fromS(createJudgmentCountSortKey(submissionType)));

        final String updateExpression = String.format("SET %s = %s + :c", JudgmentCount.Fields.amount,
                JudgmentCount.Fields.amount);

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
            log.info("DynamoDbAccessor::incrementTableSize unsuccessful, "
                    + "likely due to no judgment count entry. Trying to insert with count=1:");
            insertJudgmentCount(submissionType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void insertJudgmentCount(@NonNull final SubmissionType submissionType) {
        log.info("DynamoDbAccessor::insertJudgmentCount with submissionType={} and initial amount=1", submissionType);
        final HashMap<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put(PARTITION_KEY,
                AttributeValue.fromS(JUDGMENT_COUNT_PARTITION_KEY));
        itemValues.put(SORT_KEY, AttributeValue.fromS(createJudgmentCountSortKey(submissionType)));
        itemValues.put(JudgmentCount.Fields.amount, AttributeValue.fromN("1"));

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemValues)
                .build();

        try {
            PutItemResponse response = dynamoDB.putItem(request);
            log.info("DynamoDbAccessor::insertJudgmentCount successful with requestId={}",
                    response.responseMetadata().requestId());
        } catch (DynamoDbException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteActiveCase(@NonNull final String judgeId) {
        log.info("DynamoDbAccessor::deleteActiveCase with judgeId={}", judgeId);
        HashMap<String, AttributeValue> keyValues = new HashMap<>();
        keyValues.put(PARTITION_KEY,
                AttributeValue.fromS(createUserIdPartitionKey(judgeId)));
        keyValues.put(SORT_KEY, AttributeValue.fromS(ACTIVE_CASE_SORT_KEY));

        DeleteItemRequest deleteRequest = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(keyValues)
                .build();

        try {
            DeleteItemResponse response = dynamoDB.deleteItem(deleteRequest);
            log.info("DynamoDbAccessor::deleteActiveCase successful with requestId={}",
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

    private String createJudgmentCountSortKey(@NonNull final SubmissionType submissionType) {
        return "SUBMISSION#" + submissionType.name();
    }

    private Instant convertEpochSecondToInstant(@NonNull final String epochSecond) {
        return Instant.ofEpochSecond(Long.valueOf(epochSecond));
    }

    private String convertInstantToEpochSecond(@NonNull final Instant instant) {
        return String.valueOf(instant.getEpochSecond());
    }

    private String generateUpdateExpression(@NonNull final Map<String, AttributeValue> itemValues) {
        final StringBuilder expressionBuilder = new StringBuilder("SET ");
        for (final String key : itemValues.keySet()) {
            expressionBuilder.append(key + " = :" + key + ",");
        }
        expressionBuilder.deleteCharAt(expressionBuilder.length() - 1);
        return expressionBuilder.toString();
    }

    private Map<String, AttributeValue> generateExpressionAttributeValues(
            @NonNull final Map<String, AttributeValue> itemValues) {
        final HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        for (final Map.Entry<String, AttributeValue> entry : itemValues.entrySet()) {
            final String key = entry.getKey();
            final AttributeValue value = entry.getValue();
            expressionAttributeValues.put(":" + key, value);
        }
        return expressionAttributeValues;
    }
}
