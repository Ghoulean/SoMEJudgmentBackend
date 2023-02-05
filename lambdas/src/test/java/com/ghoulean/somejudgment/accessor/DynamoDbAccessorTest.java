package com.ghoulean.somejudgment.accessor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ghoulean.somejudgment.accessor.database.DynamoDbAccessor;
import com.ghoulean.somejudgment.model.enums.SubmissionType;
import com.ghoulean.somejudgment.model.pojo.ActiveCase;
import com.ghoulean.somejudgment.model.pojo.Feedback;
import com.ghoulean.somejudgment.model.pojo.Judgment;
import com.ghoulean.somejudgment.model.pojo.JudgmentCount;
import com.ghoulean.somejudgment.model.pojo.NewActiveCaseOptions;
import com.google.gson.Gson;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbResponseMetadata;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

@ExtendWith(MockitoExtension.class)
public class DynamoDbAccessorTest {
        @Mock
        DynamoDbClient mockDynamoDbClient;
        @Mock
        Gson mockGson;
        @Mock
        GetItemResponse mockGetItemResponse;
        @Mock
        PutItemResponse mockPutItemResponse;
        @Mock
        UpdateItemResponse mockUpdateItemResponse;
        @Mock
        DeleteItemResponse mockDeleteItemResponse;
        @Mock
        Map<String, AttributeValue> mockDdbItem;
        @Mock
        AttributeValue mockAttributeValue;
        @Mock
        DynamoDbResponseMetadata mockDynamoDbResponseMetadata;
        private static final String TABLE_NAME = "TableName";
        private static final String JUDGE_ID = "JudgeId";
        private static final String WINNER_ID = "winnerId";
        private static final String LOSER_ID = "loserId";
        private static final Judgment JUDGMENT = Judgment.builder()
                        .judgeId(JUDGE_ID)
                        .winnerId(WINNER_ID)
                        .loserId(LOSER_ID)
                        .build();
        private static NewActiveCaseOptions NEW_ACTIVE_CASE_OPTIONS = NewActiveCaseOptions.builder()
                        .submissionType(SubmissionType.VIDEO)
                        .build();
        private static final ActiveCase ACTIVE_CASE = ActiveCase.builder()
                        .judgeId(JUDGE_ID)
                        .submission1(WINNER_ID)
                        .submission2(LOSER_ID)
                        .createdAt(Instant.EPOCH)
                        .createdOptions(NEW_ACTIVE_CASE_OPTIONS)
                        .build();
        private static final Map<String, AttributeValue> ACTIVE_CASE_MAP = Map.of("judgeId",
                        AttributeValue.fromS(JUDGE_ID),
                        "submission1", AttributeValue.fromS(WINNER_ID),
                        "submission2", AttributeValue.fromS(LOSER_ID),
                        "createdAt",
                        AttributeValue.fromN(Long.valueOf(Instant.EPOCH.getEpochSecond()).toString()),
                        "createdOptions", AttributeValue.fromS(NEW_ACTIVE_CASE_OPTIONS.toString()));
        private static final Feedback FEEDBACK = Feedback.builder()
                        .judgeId(JUDGE_ID)
                        .submissionId(WINNER_ID)
                        .feedback("")
                        .build();
        private static final UUID UUID_TEST = UUID.randomUUID();

        private static DynamoDbAccessor dynamoDbAccessor;

        @BeforeEach
        public void setup() {
                dynamoDbAccessor = new DynamoDbAccessor(mockDynamoDbClient, TABLE_NAME,
                                mockGson);
        }

        @Test
        public void getActiveCaseThatExists() {
                final GetItemRequest expectedGetItemRequest = buildGetItemRequest("USER#" + JUDGE_ID, "CASE#ACTIVE",
                                true);
                when(mockDynamoDbClient.getItem(expectedGetItemRequest)).thenReturn(mockGetItemResponse);
                when(mockGetItemResponse.hasItem()).thenReturn(true);
                when(mockGetItemResponse.item()).thenReturn(ACTIVE_CASE_MAP);
                when(mockGson.fromJson(anyString(), any()))
                                .thenReturn(NEW_ACTIVE_CASE_OPTIONS);
                assertEquals(ACTIVE_CASE, dynamoDbAccessor.getActiveCase(JUDGE_ID));
        }

        @Test
        public void getActiveCaseThatDoesNotExist() {
                final GetItemRequest expectedGetItemRequest = buildGetItemRequest("USER#" + JUDGE_ID, "CASE#ACTIVE",
                                true);
                when(mockDynamoDbClient.getItem(expectedGetItemRequest)).thenReturn(mockGetItemResponse);
                when(mockGetItemResponse.hasItem()).thenReturn(false);
                assertEquals(null, dynamoDbAccessor.getActiveCase(JUDGE_ID));
        }

        @Test
        public void getActiveCaseRethrowsExceptions() {
                final GetItemRequest expectedGetItemRequest = buildGetItemRequest("USER#" + JUDGE_ID, "CASE#ACTIVE",
                                true);
                when(mockDynamoDbClient.getItem(expectedGetItemRequest)).thenThrow(DynamoDbException.builder().build());
                assertThrows(RuntimeException.class, () -> {
                        dynamoDbAccessor.getActiveCase(JUDGE_ID);
                });
        }

        @Test
        public void getJudgmentCountThatExists() {
                final GetItemRequest expectedGetItemRequest = buildGetItemRequest("COUNT#JUDGMENTS",
                                "SUBMISSION#" + SubmissionType.VIDEO.name(),
                                true);
                when(mockDynamoDbClient.getItem(expectedGetItemRequest)).thenReturn(mockGetItemResponse);
                when(mockGetItemResponse.hasItem()).thenReturn(true);
                when(mockGetItemResponse.item()).thenReturn(mockDdbItem);
                when(mockDdbItem.get(anyString())).thenReturn(mockAttributeValue);
                when(mockAttributeValue.n()).thenReturn("1");
                final JudgmentCount expectedResponse = JudgmentCount.builder()
                                .amount(1)
                                .build();
                assertEquals(expectedResponse, dynamoDbAccessor.getJudgmentCount(SubmissionType.VIDEO));
        }

        @Test
        public void getJudgmentCountThatDoesNotExist() {
                final GetItemRequest expectedGetItemRequest = buildGetItemRequest("COUNT#JUDGMENTS",
                                "SUBMISSION#" + SubmissionType.VIDEO.name(),
                                true);
                when(mockDynamoDbClient.getItem(expectedGetItemRequest)).thenReturn(mockGetItemResponse);
                when(mockGetItemResponse.hasItem()).thenReturn(false);
                final JudgmentCount expectedResponse = JudgmentCount.builder()
                                .amount(0)
                                .build();
                assertEquals(expectedResponse, dynamoDbAccessor.getJudgmentCount(SubmissionType.VIDEO));
        }

        @Test
        public void getJudgmentCountRethrowsExceptions() {
                final GetItemRequest expectedGetItemRequest = buildGetItemRequest("COUNT#JUDGMENTS",
                                "SUBMISSION#" + SubmissionType.VIDEO.name(),
                                true);
                when(mockDynamoDbClient.getItem(expectedGetItemRequest)).thenThrow(DynamoDbException.builder().build());
                assertThrows(RuntimeException.class, () -> {
                        dynamoDbAccessor.getJudgmentCount(SubmissionType.VIDEO);
                });
        }

        @Test
        public void insertJudgmentSucceeds() {
                try (MockedStatic<UUID> mocked = mockStatic(UUID.class)) {
                        mocked.when(UUID::randomUUID).thenReturn(UUID_TEST);
                        final PutItemRequest expectedPutItemRequest = buildPutItemRequest("USER#" + JUDGE_ID,
                                        "JUDGMENT#" + UUID_TEST.toString(),
                                        Map.of("winnerId", AttributeValue.fromS(WINNER_ID),
                                                        "loserId", AttributeValue.fromS(LOSER_ID)));
                        when(mockDynamoDbClient.putItem(expectedPutItemRequest)).thenReturn(mockPutItemResponse);
                        when(mockPutItemResponse.responseMetadata()).thenReturn(mockDynamoDbResponseMetadata);
                        when(mockDynamoDbResponseMetadata.requestId()).thenReturn("");
                        assertDoesNotThrow(() -> {
                                dynamoDbAccessor.insertJudgment(JUDGMENT);
                        });
                }
        }

        @Test
        public void insertJudgmentRethrowsExceptions() {
                try (MockedStatic<UUID> mocked = mockStatic(UUID.class)) {
                        mocked.when(UUID::randomUUID).thenReturn(UUID_TEST);
                        final PutItemRequest expectedPutItemRequest = buildPutItemRequest("USER#" + JUDGE_ID,
                                        "JUDGMENT#" + UUID_TEST.toString(),
                                        Map.of("winnerId", AttributeValue.fromS(WINNER_ID),
                                                        "loserId", AttributeValue.fromS(LOSER_ID)));
                        when(mockDynamoDbClient.putItem(expectedPutItemRequest))
                                        .thenThrow(DynamoDbException.builder().build());
                        assertThrows(RuntimeException.class, () -> {
                                dynamoDbAccessor.insertJudgment(JUDGMENT);
                        });
                }
        }

        @Test
        public void upsertActiveCaseSucceeds() {
                final UpdateItemRequest expectedUpdateItemRequest = buildUpdateItemRequest("USER#" + JUDGE_ID,
                                "CASE#ACTIVE",
                                "SET createdAt = :createdAt,createdOptions = :createdOptions,submission1 = :submission1,submission2 = :submission2",
                                Map.of(":submission1", AttributeValue.fromS(WINNER_ID),
                                                ":submission2", AttributeValue.fromS(LOSER_ID),
                                                ":createdAt",
                                                AttributeValue.fromN(Long.valueOf(Instant.EPOCH.getEpochSecond())
                                                                .toString()),
                                                ":createdOptions",
                                                AttributeValue.fromS(NEW_ACTIVE_CASE_OPTIONS.toString())));
                when(mockGson.toJson(NEW_ACTIVE_CASE_OPTIONS)).thenReturn(NEW_ACTIVE_CASE_OPTIONS.toString());
                when(mockDynamoDbClient.updateItem(expectedUpdateItemRequest)).thenReturn(mockUpdateItemResponse);
                when(mockUpdateItemResponse.responseMetadata()).thenReturn(mockDynamoDbResponseMetadata);
                when(mockDynamoDbResponseMetadata.requestId()).thenReturn("");
                assertDoesNotThrow(() -> {
                        dynamoDbAccessor.upsertActiveCase(ACTIVE_CASE);
                });
        }

        @Test
        public void upsertActiveCaseRethrowsExceptions() {
                final UpdateItemRequest expectedUpdateItemRequest = buildUpdateItemRequest("USER#" + JUDGE_ID,
                                "CASE#ACTIVE",
                                "SET createdAt = :createdAt,createdOptions = :createdOptions,submission1 = :submission1,submission2 = :submission2",
                                Map.of(":submission1", AttributeValue.fromS(WINNER_ID),
                                                ":submission2", AttributeValue.fromS(LOSER_ID),
                                                ":createdAt",
                                                AttributeValue.fromN(Long.valueOf(Instant.EPOCH.getEpochSecond())
                                                                .toString()),
                                                ":createdOptions",
                                                AttributeValue.fromS(NEW_ACTIVE_CASE_OPTIONS.toString())));
                when(mockDynamoDbClient.updateItem(expectedUpdateItemRequest))
                                .thenThrow(DynamoDbException.builder().build());
                assertThrows(RuntimeException.class, () -> {
                        dynamoDbAccessor.upsertActiveCase(ACTIVE_CASE);
                });

        }

        @Test
        public void upsertFeedbackSucceeds() {
                final UpdateItemRequest expectedUpdateItemRequest = buildUpdateItemRequest("USER#" + JUDGE_ID,
                                "FEEDBACK#" + WINNER_ID,
                                "SET feedback = :feedback",
                                Map.of(":feedback", AttributeValue.fromS("")));
                when(mockDynamoDbClient.updateItem(expectedUpdateItemRequest)).thenReturn(mockUpdateItemResponse);
                when(mockUpdateItemResponse.responseMetadata()).thenReturn(mockDynamoDbResponseMetadata);
                when(mockDynamoDbResponseMetadata.requestId()).thenReturn("");
                assertDoesNotThrow(() -> {
                        dynamoDbAccessor.upsertFeedback(FEEDBACK);
                });
        }

        @Test
        public void upsertFeedbackRethrowsExceptions() {
                final UpdateItemRequest expectedUpdateItemRequest = buildUpdateItemRequest("USER#" + JUDGE_ID,
                                "FEEDBACK#" + WINNER_ID,
                                "SET feedback = :feedback",
                                Map.of(":feedback", AttributeValue.fromS("")));
                when(mockDynamoDbClient.updateItem(expectedUpdateItemRequest))
                                .thenThrow(DynamoDbException.builder().build());
                assertThrows(RuntimeException.class, () -> {
                        dynamoDbAccessor.upsertFeedback(FEEDBACK);
                });

        }

        @Test
        public void incrementJudgmentCountSucceeds() {
                final UpdateItemRequest expectedUpdateItemRequest = buildUpdateItemRequest("COUNT#JUDGMENTS",
                                "SUBMISSION#" + SubmissionType.VIDEO.name(),
                                "SET amount = amount + :c",
                                Map.of(":c", AttributeValue.fromN("1")));
                when(mockDynamoDbClient.updateItem(expectedUpdateItemRequest)).thenReturn(mockUpdateItemResponse);
                when(mockUpdateItemResponse.responseMetadata()).thenReturn(mockDynamoDbResponseMetadata);
                when(mockDynamoDbResponseMetadata.requestId()).thenReturn("");
                assertDoesNotThrow(() -> {
                        dynamoDbAccessor.incrementJudgmentCount(SubmissionType.VIDEO);
                });
        }

        @Test
        public void incrementJudgmentCountAtZero() {
                final UpdateItemRequest expectedUpdateItemRequest = buildUpdateItemRequest("COUNT#JUDGMENTS",
                                "SUBMISSION#" + SubmissionType.VIDEO.name(),
                                "SET amount = amount + :c",
                                Map.of(":c", AttributeValue.fromN("1")));
                when(mockDynamoDbClient.updateItem(expectedUpdateItemRequest))
                                .thenThrow(DynamoDbException.builder().build());

                final PutItemRequest expectedPutItemRequest = buildPutItemRequest("COUNT#JUDGMENTS",
                                "SUBMISSION#" + SubmissionType.VIDEO.name(),
                                Map.of("amount", AttributeValue.fromN("1")));
                when(mockDynamoDbClient.putItem(expectedPutItemRequest)).thenReturn(mockPutItemResponse);
                when(mockPutItemResponse.responseMetadata()).thenReturn(mockDynamoDbResponseMetadata);
                when(mockDynamoDbResponseMetadata.requestId()).thenReturn("");

                assertDoesNotThrow(() -> {
                        dynamoDbAccessor.incrementJudgmentCount(SubmissionType.VIDEO);
                });
        }

        @Test
        public void incrementJudgmentCountRethrowsExceptions() {
                final UpdateItemRequest expectedUpdateItemRequest = buildUpdateItemRequest("USER#" + JUDGE_ID,
                                "FEEDBACK#" + WINNER_ID,
                                "SET amount = amount + :c",
                                Map.of(":c", AttributeValue.fromN("1")));
                when(mockDynamoDbClient.updateItem(expectedUpdateItemRequest))
                                .thenThrow(new RuntimeException());
                assertThrows(RuntimeException.class, () -> {
                        dynamoDbAccessor.incrementJudgmentCount(SubmissionType.VIDEO);
                });

        }

        @Test
        public void incrementJudgmentCountRethrowsExceptionsAtZero() {
                final UpdateItemRequest expectedUpdateItemRequest = buildUpdateItemRequest("COUNT#JUDGMENTS",
                                "SUBMISSION#" + SubmissionType.VIDEO.name(),
                                "SET amount = amount + :c",
                                Map.of(":c", AttributeValue.fromN("1")));
                when(mockDynamoDbClient.updateItem(expectedUpdateItemRequest))
                                .thenThrow(DynamoDbException.builder().build());

                final PutItemRequest expectedPutItemRequest = buildPutItemRequest("COUNT#JUDGMENTS",
                                "SUBMISSION#" + SubmissionType.VIDEO.name(),
                                Map.of("amount", AttributeValue.fromN("1")));
                when(mockDynamoDbClient.putItem(expectedPutItemRequest))
                                .thenThrow(DynamoDbException.builder().build());
                assertThrows(RuntimeException.class, () -> {
                        dynamoDbAccessor.incrementJudgmentCount(SubmissionType.VIDEO);
                });

        }

        @Test
        public void deleteActiveCaseSucceeds() {
                final DeleteItemRequest expectedDeleteItemRequest = DeleteItemRequest.builder()
                                .tableName(TABLE_NAME)
                                .key(Map.of("PK", AttributeValue.fromS("USER#" + JUDGE_ID), "SK",
                                                AttributeValue.fromS("CASE#ACTIVE")))
                                .build();
                when(mockDynamoDbClient.deleteItem(expectedDeleteItemRequest))
                                .thenReturn(mockDeleteItemResponse);
                when(mockDeleteItemResponse.responseMetadata()).thenReturn(mockDynamoDbResponseMetadata);
                when(mockDynamoDbResponseMetadata.requestId()).thenReturn("");
                assertDoesNotThrow(() -> {
                        dynamoDbAccessor.deleteActiveCase(JUDGE_ID);
                });

        }

        @Test
        public void deleteActiveCaseRethrowsExceptions() {
                final DeleteItemRequest expectedDeleteItemRequest = DeleteItemRequest.builder()
                                .tableName(TABLE_NAME)
                                .key(Map.of("PK", AttributeValue.fromS("USER#" + JUDGE_ID), "SK",
                                                AttributeValue.fromS("CASE#ACTIVE")))
                                .build();
                when(mockDynamoDbClient.deleteItem(expectedDeleteItemRequest))
                                .thenThrow(DynamoDbException.builder().build());
                assertThrows(RuntimeException.class, () -> {
                        dynamoDbAccessor.deleteActiveCase(JUDGE_ID);
                });

        }

        private GetItemRequest buildGetItemRequest(final String partitionKeyValue, final String sortKeyValue,
                        boolean consistentRead) {
                final HashMap<String, AttributeValue> keyToGet = new HashMap<String, AttributeValue>();
                keyToGet.put("PK", AttributeValue.fromS(partitionKeyValue));
                keyToGet.put("SK", AttributeValue.fromS(sortKeyValue));
                return GetItemRequest.builder()
                                .key(keyToGet)
                                .tableName(TABLE_NAME)
                                .consistentRead(consistentRead)
                                .build();
        }

        private PutItemRequest buildPutItemRequest(final String partitionKeyValue, final String sortKeyValue,
                        Map<String, AttributeValue> additionalFields) {
                final HashMap<String, AttributeValue> itemValues = new HashMap<String, AttributeValue>(
                                additionalFields);
                itemValues.put("PK", AttributeValue.fromS(partitionKeyValue));
                itemValues.put("SK", AttributeValue.fromS(sortKeyValue));
                return PutItemRequest.builder()
                                .item(itemValues)
                                .tableName(TABLE_NAME)
                                .build();
        }

        private UpdateItemRequest buildUpdateItemRequest(final String partitionKeyValue, final String sortKeyValue,
                        final String updateExpression, final Map<String, AttributeValue> expressionAttributeValues) {
                final HashMap<String, AttributeValue> keyValues = new HashMap<String, AttributeValue>();
                keyValues.put("PK", AttributeValue.fromS(partitionKeyValue));
                keyValues.put("SK", AttributeValue.fromS(sortKeyValue));
                return UpdateItemRequest.builder()
                                .key(keyValues)
                                .tableName(TABLE_NAME)
                                .updateExpression(updateExpression)
                                .expressionAttributeValues(expressionAttributeValues)
                                .build();
        }
}
