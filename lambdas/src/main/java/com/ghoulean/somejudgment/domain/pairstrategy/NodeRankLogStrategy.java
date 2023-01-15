package com.ghoulean.somejudgment.domain.pairstrategy;

import java.time.Instant;

import javax.inject.Inject;

import com.ghoulean.somejudgment.accessor.DynamoDbAccessor;
import com.ghoulean.somejudgment.domain.submissionmanager.SubmissionManager;
import com.ghoulean.somejudgment.model.enums.SubmissionType;
import com.ghoulean.somejudgment.model.pojo.ActiveCase;
import com.ghoulean.somejudgment.model.pojo.NewActiveCaseOptions;

import lombok.NonNull;

public final class NodeRankLogStrategy implements PairStrategy {

    private @NonNull final DynamoDbAccessor dynamoDbAccessor;
    private @NonNull final SubmissionManager submissionManager;

    @Inject
    public NodeRankLogStrategy(@NonNull final SubmissionManager submissionManager,
            @NonNull final DynamoDbAccessor dynamoDbAccessor) {
        this.submissionManager = submissionManager;
        this.dynamoDbAccessor = dynamoDbAccessor;
    }

    @Override
    public ActiveCase createNewActiveCase(final String judgeId, final NewActiveCaseOptions options) {
        // TODO: cleaner mapping from NewActiveCaseOptions to SubmissionType
        final SubmissionType submissionType = options.isNonVideoSubmission() ? SubmissionType.NONVIDEO
                : SubmissionType.VIDEO;
        int totalNumbersSubmissions = submissionManager.getSubmissionCount(submissionType);
        int judgmentCountAmount = dynamoDbAccessor.getJudgmentCount(submissionType).getAmount();
        int currentStepNum = judgmentCountAmount / totalNumbersSubmissions;
        int nextSubmissionIndex = judgmentCountAmount % totalNumbersSubmissions;
        int offset = (int) (totalNumbersSubmissions / (2 + Math.log(currentStepNum)));
        int pairedIndex = (nextSubmissionIndex + offset) % totalNumbersSubmissions;
        return ActiveCase.builder()
            .judgeId(judgeId)
            .submission1(submissionManager.getSubmission(nextSubmissionIndex, submissionType).getId())
            .submission2(submissionManager.getSubmission(pairedIndex, submissionType).getId())
            .createdAt(Instant.now())
            .createdOptions(options)
            .build();
    }

}
