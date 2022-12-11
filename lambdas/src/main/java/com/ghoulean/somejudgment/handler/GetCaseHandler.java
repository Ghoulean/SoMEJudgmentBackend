package com.ghoulean.somejudgment.handler;

import javax.inject.Inject;

import com.ghoulean.somejudgment.accessor.DynamoDbAccessor;
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
    private @NonNull final DynamoDbAccessor dynamoDbAccessor;
    private @NonNull final SubmissionManager submissionManager;
    private @NonNull final PairStrategy pairStrategy;

    @Inject
    public GetCaseHandler(@NonNull final DynamoDbAccessor dynamoDbAccessor,
            @NonNull final SubmissionManager submissionManager,
            @NonNull final PairStrategy pairStrategy) {
        this.dynamoDbAccessor = dynamoDbAccessor;
        this.submissionManager = submissionManager;
        this.pairStrategy = pairStrategy;
    }

    public GetCaseResponse handle(@NonNull final GetCaseRequest getCaseRequest) {
        final ActiveCase activeCase = dynamoDbAccessor.getActiveCase(getCaseRequest.getJudgeId());
        if (activeCase != null && activeCase.getCreatedOptions().equals(getCaseRequest.getNewActiveCaseOptions())) {
            log.info("Found activeCase {}, returning", activeCase);
            return buildResponse(activeCase);
        }
        log.info("Did not find activeCase for NewActiveCaseOptions, creating a new one");
        final ActiveCase newActiveCase = pairStrategy.createNewActiveCase(getCaseRequest.getJudgeId(),
                getCaseRequest.getNewActiveCaseOptions());
        log.info("Created new activeCase {}", newActiveCase);
        dynamoDbAccessor.upsertActiveCase(newActiveCase);
        log.info("Upserted into dynamoDb");
        // TODO: cleaner mapping from NewActiveCaseOptions to SubmissionType
        final SubmissionType submissionType = newActiveCase.getCreatedOptions().isNonVideoSubmission()
                ? SubmissionType.NONVIDEO
                : SubmissionType.VIDEO;
        dynamoDbAccessor.incrementJudgmentCount(submissionType);
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
