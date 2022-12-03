package com.ghoulean.garbageoncall.accessor;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public boolean insertJudgment(@NonNull final Judgment judgment) {
        return false;
    }

    public boolean upsertActiveCase(@NonNull final ActiveCase activeCase) {
        return false;
    }

    public boolean upsertFeedback(@NonNull final Feedback feedback) {
        return false;
    }

    private String createUserIdPartitionKey(@NonNull final String userId) {
        return "USER#" + userId;
    }

    private Instant convertEpochSecondToInstant(final String epochSecond) {
        return Instant.ofEpochSecond(Long.valueOf(epochSecond));
    }

}
