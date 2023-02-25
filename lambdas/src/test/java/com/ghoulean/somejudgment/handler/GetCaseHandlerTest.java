package com.ghoulean.somejudgment.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ghoulean.somejudgment.accessor.database.DatabaseAccessor;
import com.ghoulean.somejudgment.domain.pairstrategy.PairStrategy;
import com.ghoulean.somejudgment.domain.submissionmanager.SubmissionManager;
import com.ghoulean.somejudgment.model.enums.SubmissionType;
import com.ghoulean.somejudgment.model.pojo.ActiveCase;
import com.ghoulean.somejudgment.model.pojo.NewActiveCaseOptions;
import com.ghoulean.somejudgment.model.pojo.Submission;
import com.ghoulean.somejudgment.model.request.GetCaseRequest;
import com.ghoulean.somejudgment.model.response.GetCaseResponse;

@ExtendWith(MockitoExtension.class)
public final class GetCaseHandlerTest {

    @Mock
    DatabaseAccessor mockDatabaseAccessor;
    @Mock
    SubmissionManager mockSubmissionManager;
    @Mock
    PairStrategy mockPairStrategy;

    private static final EasyRandom easyRandom = new EasyRandom();
    private static GetCaseHandler getCaseHandler;

    @BeforeEach
    public void setup() {
        getCaseHandler = new GetCaseHandler(mockDatabaseAccessor, mockSubmissionManager, mockPairStrategy);
    }

    @Test
    public void shouldReturnDdbEntryIfExists() {
        final GetCaseRequest getCaseRequest = easyRandom.nextObject(GetCaseRequest.class);
        final Submission submission1 = easyRandom.nextObject(Submission.class);
        final Submission submission2 = easyRandom.nextObject(Submission.class);
        final ActiveCase activeCase = easyRandom.nextObject(ActiveCase.class)
                .toBuilder()
                .createdOptions(getCaseRequest.getNewActiveCaseOptions())
                .build();

        when(mockDatabaseAccessor.getActiveCase(getCaseRequest.getJudgeId())).thenReturn(activeCase);
        when(mockSubmissionManager.getSubmission(activeCase.getSubmission1())).thenReturn(submission1);
        when(mockSubmissionManager.getSubmission(activeCase.getSubmission2())).thenReturn(submission2);

        final GetCaseResponse expectedGetCaseResponse = GetCaseResponse.builder()
                .submission1(submission1)
                .submission2(submission2)
                .build();

        assertEquals(expectedGetCaseResponse, getCaseHandler.handle(getCaseRequest));
    }

    @Test
    public void shouldCreateNewEntryIfDoesntExist() {
        final GetCaseRequest getCaseRequest = easyRandom.nextObject(GetCaseRequest.class);
        final Submission submission1 = easyRandom.nextObject(Submission.class);
        final Submission submission2 = easyRandom.nextObject(Submission.class);
        final ActiveCase activeCase = easyRandom.nextObject(ActiveCase.class)
                .toBuilder()
                .createdOptions(getCaseRequest.getNewActiveCaseOptions())
                .build();

        when(mockDatabaseAccessor.getActiveCase(getCaseRequest.getJudgeId())).thenReturn(null);
        when(mockPairStrategy.createNewActiveCase(getCaseRequest.getJudgeId(),
                getCaseRequest.getNewActiveCaseOptions())).thenReturn(activeCase);
        when(mockSubmissionManager.getSubmission(activeCase.getSubmission1())).thenReturn(submission1);
        when(mockSubmissionManager.getSubmission(activeCase.getSubmission2())).thenReturn(submission2);

        final GetCaseResponse expectedGetCaseResponse = GetCaseResponse.builder()
                .submission1(submission1)
                .submission2(submission2)
                .build();

        assertEquals(expectedGetCaseResponse, getCaseHandler.handle(getCaseRequest));

        verify(mockDatabaseAccessor, times(1)).upsertActiveCase(activeCase);
        verify(mockDatabaseAccessor, times(1))
                .incrementJudgmentCount(getCaseRequest.getNewActiveCaseOptions().getSubmissionType());

    }

    @Test
    public void shouldCreateNewEntryIfDatabaseEntryDoesntMatch() {
        final NewActiveCaseOptions requestedNewActiveCaseOptions = NewActiveCaseOptions.builder()
            .submissionType(SubmissionType.VIDEO)
            .build();
            final NewActiveCaseOptions dbNewActiveCaseOptions = NewActiveCaseOptions.builder()
                .submissionType(SubmissionType.NONVIDEO)
                .build();
        final GetCaseRequest getCaseRequest = easyRandom.nextObject(GetCaseRequest.class)
            .toBuilder()
            .newActiveCaseOptions(requestedNewActiveCaseOptions)
            .build();
        final Submission submission1 = easyRandom.nextObject(Submission.class);
        final Submission submission2 = easyRandom.nextObject(Submission.class);
        final ActiveCase oldActiveCase = easyRandom.nextObject(ActiveCase.class)
                .toBuilder()
                .createdOptions(dbNewActiveCaseOptions)
                .build();
        final ActiveCase activeCase = easyRandom.nextObject(ActiveCase.class)
                .toBuilder()
                .createdOptions(requestedNewActiveCaseOptions)
                .build();

        when(mockDatabaseAccessor.getActiveCase(getCaseRequest.getJudgeId())).thenReturn(oldActiveCase);
        when(mockPairStrategy.createNewActiveCase(getCaseRequest.getJudgeId(),
                getCaseRequest.getNewActiveCaseOptions())).thenReturn(activeCase);
        when(mockSubmissionManager.getSubmission(activeCase.getSubmission1())).thenReturn(submission1);
        when(mockSubmissionManager.getSubmission(activeCase.getSubmission2())).thenReturn(submission2);

        final GetCaseResponse expectedGetCaseResponse = GetCaseResponse.builder()
                .submission1(submission1)
                .submission2(submission2)
                .build();

        assertEquals(expectedGetCaseResponse, getCaseHandler.handle(getCaseRequest));

        verify(mockDatabaseAccessor, times(1)).upsertActiveCase(activeCase);
        verify(mockDatabaseAccessor, times(1)).incrementJudgmentCount(getCaseRequest.getNewActiveCaseOptions().getSubmissionType());
    }
}
