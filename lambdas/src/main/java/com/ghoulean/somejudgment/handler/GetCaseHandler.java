package com.ghoulean.somejudgment.handler;

import javax.inject.Inject;

import com.ghoulean.somejudgment.accessor.database.DatabaseAccessor;
import com.ghoulean.somejudgment.domain.pairstrategy.PairStrategy;
import com.ghoulean.somejudgment.domain.submissionmanager.SubmissionManager;
import com.ghoulean.somejudgment.model.enums.SubmissionType;
import com.ghoulean.somejudgment.model.pojo.ActiveCase;
import com.ghoulean.somejudgment.model.request.GetCaseRequest;
import com.ghoulean.somejudgment.model.response.GetCaseResponse;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class GetCaseHandler {
    private @NonNull final DatabaseAccessor databaseAccessor;
    private @NonNull final SubmissionManager submissionManager;
    private @NonNull final PairStrategy pairStrategy;

    @Inject
    public GetCaseHandler(@NonNull final DatabaseAccessor databaseAccessor,
            @NonNull final SubmissionManager submissionManager,
            @NonNull final PairStrategy pairStrategy) {
        this.databaseAccessor = databaseAccessor;
        this.submissionManager = submissionManager;
        this.pairStrategy = pairStrategy;
    }

    public GetCaseResponse handle(@NonNull final GetCaseRequest getCaseRequest) {
        final ActiveCase activeCase = databaseAccessor.getActiveCase(getCaseRequest.getJudgeId());
        if (activeCase != null && activeCase.getCreatedOptions().equals(getCaseRequest.getNewActiveCaseOptions())) {
            log.info("Found activeCase {}, returning", activeCase);
            return buildResponse(activeCase);
        }
        log.info("Did not find activeCase for NewActiveCaseOptions, creating a new one");
        final ActiveCase newActiveCase = pairStrategy.createNewActiveCase(getCaseRequest.getJudgeId(),
                getCaseRequest.getNewActiveCaseOptions());
        log.info("Created new activeCase {}", newActiveCase);
        databaseAccessor.upsertActiveCase(newActiveCase);
        log.info("Upserted into dynamoDb");
        final SubmissionType submissionType = newActiveCase.getCreatedOptions().getSubmissionType();
        databaseAccessor.incrementJudgmentCount(submissionType);
        log.info("Incremented judgment count for {}", submissionType);
        return buildResponse(newActiveCase);
    }

    private GetCaseResponse buildResponse(@NonNull final ActiveCase activeCase) {
        return GetCaseResponse.builder()
                .submission1(submissionManager.getSubmission(activeCase.getSubmission1()))
                .submission2(submissionManager.getSubmission(activeCase.getSubmission2()))
                .build();
    }
}
