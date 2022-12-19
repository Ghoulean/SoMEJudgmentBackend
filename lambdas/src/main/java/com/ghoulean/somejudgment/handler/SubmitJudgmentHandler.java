package com.ghoulean.somejudgment.handler;

import java.time.Duration;
import java.time.Instant;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;

import com.ghoulean.somejudgment.accessor.DynamoDbAccessor;
import com.ghoulean.somejudgment.domain.submissionmanager.SubmissionManager;
import com.ghoulean.somejudgment.model.enums.SubmissionType;
import com.ghoulean.somejudgment.model.pojo.ActiveCase;
import com.ghoulean.somejudgment.model.pojo.Judgment;
import com.ghoulean.somejudgment.model.request.SubmitJudgmentRequest;
import com.ghoulean.somejudgment.model.response.SubmitJudgmentResponse;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SubmitJudgmentHandler {
    private @NonNull final DynamoDbAccessor dynamoDbAccessor;
    private @NonNull final SubmissionManager submissionManager;
    private static final Duration FORCE_WAIT_DURATION = Duration.ofMinutes(2);

    @Inject
    public SubmitJudgmentHandler(@NonNull final DynamoDbAccessor dynamoDbAccessor,
            @NonNull final SubmissionManager submissionManager) {
        this.dynamoDbAccessor = dynamoDbAccessor;
        this.submissionManager = submissionManager;
    }

    public SubmitJudgmentResponse handle(@NonNull final SubmitJudgmentRequest submitJudgmentRequest) {
        final Judgment judgment = submitJudgmentRequest.getJudgment();
        final ActiveCase activeCase = dynamoDbAccessor.getActiveCase(judgment.getJudgeId());
        log.info("Validating judgement={} and activeCase={}", judgment, activeCase);
        validateJudgmentOrThrowException(judgment, activeCase);
        log.info("Validation successful");
        log.info("Inserting judgment={}", judgment);
        dynamoDbAccessor.insertJudgment(judgment);
        log.info("Inserting activeCase={}", activeCase);
        dynamoDbAccessor.deleteActiveCase(activeCase.getJudgeId());
        // TODO: cleaner mapping from NewActiveCaseOptions to SubmissionType
        final SubmissionType submissionType = activeCase.getCreatedOptions().isNonVideoSubmission()
                ? SubmissionType.NONVIDEO
                : SubmissionType.VIDEO;
        log.info("Incrementing judgment count for {}", submissionType);
        dynamoDbAccessor.incrementJudgmentCount(submissionType);
        return buildResponse();
    }

    private void validateJudgmentOrThrowException(@NonNull final Judgment judgment,
            @NonNull final ActiveCase currentActiveCase) {
        if (!judgment.getJudgeId().equals(currentActiveCase.getJudgeId())) {
            throw new BadRequestException("judgeId mismatch");
        }
        if (!judgment.getWinnerId().equals(currentActiveCase.getSubmission1()) &&
                judgment.getWinnerId().equals(currentActiveCase.getSubmission2())) {
            throw new BadRequestException("Submission Id mismatch (winner)");
        }
        if (!judgment.getLoserId().equals(currentActiveCase.getSubmission1()) &&
                judgment.getLoserId().equals(currentActiveCase.getSubmission2())) {
            throw new BadRequestException("Submission Id mismatch (loser)");
        }
        if (Instant.now().minus(FORCE_WAIT_DURATION).isBefore(currentActiveCase.getCreatedAt())) {
            throw new BadRequestException("Need to wait at least 2 minutes before submitting judgment");
        }
    }

    private SubmitJudgmentResponse buildResponse() {
        return SubmitJudgmentResponse.builder().build();
    }
}
