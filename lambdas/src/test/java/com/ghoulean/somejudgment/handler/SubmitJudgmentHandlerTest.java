package com.ghoulean.somejudgment.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ghoulean.somejudgment.accessor.database.DatabaseAccessor;
import com.ghoulean.somejudgment.domain.submissionmanager.SubmissionManager;
import com.ghoulean.somejudgment.model.pojo.ActiveCase;
import com.ghoulean.somejudgment.model.pojo.NewActiveCaseOptions;
import com.ghoulean.somejudgment.model.request.SubmitJudgmentRequest;
import com.ghoulean.somejudgment.model.response.SubmitJudgmentResponse;

import jakarta.ws.rs.BadRequestException;

@ExtendWith(MockitoExtension.class)
public class SubmitJudgmentHandlerTest {

    @Mock
    DatabaseAccessor mockDatabaseAccessor;
    @Mock
    SubmissionManager mockSubmissionManager;

    private static final Duration FORCE_WAIT_DURATION = Duration.ofMinutes(2);

    private static final EasyRandom easyRandom = new EasyRandom();
    private static SubmitJudgmentHandler submitJudgmentHandler;

    @BeforeEach
    public void setup() {
        submitJudgmentHandler = new SubmitJudgmentHandler(mockDatabaseAccessor, mockSubmissionManager,
                FORCE_WAIT_DURATION);
    }

    @Test
    public void successfullyHandleRequest() {
        final SubmitJudgmentRequest submitJudgmentRequest = easyRandom.nextObject(SubmitJudgmentRequest.class);
        final ActiveCase activeCase = ActiveCase.builder()
                .judgeId(submitJudgmentRequest.getJudgment().getJudgeId())
                .submission1(submitJudgmentRequest.getJudgment().getWinnerId())
                .submission2(submitJudgmentRequest.getJudgment().getLoserId())
                .createdAt(Instant.MIN)
                .createdOptions(easyRandom.nextObject(NewActiveCaseOptions.class))
                .build();

        when(mockDatabaseAccessor.getActiveCase(submitJudgmentRequest.getJudgment().getJudgeId()))
                .thenReturn(activeCase);

        assertEquals(SubmitJudgmentResponse.builder().build(), submitJudgmentHandler.handle(submitJudgmentRequest));
        verify(mockDatabaseAccessor, times(1)).insertJudgment(submitJudgmentRequest.getJudgment());
        verify(mockDatabaseAccessor, times(1)).deleteActiveCase(activeCase.getJudgeId());
        verify(mockDatabaseAccessor, times(1))
                .incrementJudgmentCount(activeCase.getCreatedOptions().getSubmissionType());
    }

    @ParameterizedTest
    @MethodSource("provideBadRequests")
    public void shouldErrorWhenBadRequest(final SubmitJudgmentRequest submitJudgmentRequest, final ActiveCase activeCase) {   
        when(mockDatabaseAccessor.getActiveCase(submitJudgmentRequest.getJudgment().getJudgeId())).thenReturn(null);

        assertThrows(BadRequestException.class, () -> {
            submitJudgmentHandler.handle(submitJudgmentRequest);
        });
    }

    private static Stream<Arguments> provideBadRequests() {
        final SubmitJudgmentRequest baseRequest = easyRandom.nextObject(SubmitJudgmentRequest.class);
        final ActiveCase baseActiveCase = ActiveCase.builder()
                .judgeId(baseRequest.getJudgment().getJudgeId())
                .submission1(baseRequest.getJudgment().getWinnerId())
                .submission2(baseRequest.getJudgment().getLoserId())
                .createdAt(Instant.MIN)
                .createdOptions(easyRandom.nextObject(NewActiveCaseOptions.class))
                .build();
        return Stream.of(
            Arguments.of(baseRequest, null),
            Arguments.of(baseRequest, baseActiveCase.toBuilder().judgeId(easyRandom.nextObject(String.class)).build()),
            Arguments.of(baseRequest, baseActiveCase.toBuilder().submission1(easyRandom.nextObject(String.class)).build()),
            Arguments.of(baseRequest, baseActiveCase.toBuilder().submission2(easyRandom.nextObject(String.class)).build()),
            Arguments.of(baseRequest, baseActiveCase.toBuilder().createdAt(Instant.now()).build())
        );
    }
}
