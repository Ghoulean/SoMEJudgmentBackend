package com.ghoulean.somejudgment.domain.pairstrategy;

import java.time.Instant;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.ghoulean.somejudgment.accessor.database.DatabaseAccessor;
import com.ghoulean.somejudgment.domain.submissionmanager.SubmissionManager;
import com.ghoulean.somejudgment.model.enums.SubmissionType;
import com.ghoulean.somejudgment.model.pojo.ActiveCase;
import com.ghoulean.somejudgment.model.pojo.NewActiveCaseOptions;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public final class NodeRankLogStrategy implements PairStrategy {

    private @NonNull final DatabaseAccessor databaseAccessor;
    private @NonNull final SubmissionManager submissionManager;

    @Inject
    public NodeRankLogStrategy(@NonNull final DatabaseAccessor databaseAccessor,
            @NonNull final SubmissionManager submissionManager) {
        this.submissionManager = submissionManager;
        this.databaseAccessor = databaseAccessor;
    }

    @Override
    public ActiveCase createNewActiveCase(final String judgeId, final NewActiveCaseOptions options) {
        log.info("NodeRankLogStrategy::createNewActiveCase invoked with judgeId={}, options={}", judgeId, options);
        final SubmissionType submissionType = options.getSubmissionType();
        int totalNumberSubmissions = submissionManager.getSubmissionCount(submissionType);
        int judgmentCountAmount = databaseAccessor.getJudgmentCount(submissionType).getAmount();
        int currentStepNum = judgmentCountAmount / totalNumberSubmissions;
        int nextSubmissionIndex = judgmentCountAmount % totalNumberSubmissions;
        int offset = Math.max(1, (int) (totalNumberSubmissions / (2 + Math.log(currentStepNum))));
        int pairedIndex = (nextSubmissionIndex + offset) % totalNumberSubmissions;
        log.info("NodeRankLogStrategy::createNewActiveCase: totalNumbersSubmissions: {}", totalNumberSubmissions);
        log.info("NodeRankLogStrategy::createNewActiveCase: judgmentCountAmount: {}", judgmentCountAmount);
        log.info("NodeRankLogStrategy::createNewActiveCase: currentStepNum: {}", currentStepNum);
        log.info("NodeRankLogStrategy::createNewActiveCase: nextSubmissionIndex: {}", nextSubmissionIndex);
        log.info("NodeRankLogStrategy::createNewActiveCase: offset: {}", offset);
        log.info("NodeRankLogStrategy::createNewActiveCase: pairedIndex: {}", pairedIndex);
        return ActiveCase.builder()
                .judgeId(judgeId)
                .submission1(submissionManager.getSubmission(nextSubmissionIndex, submissionType).getId())
                .submission2(submissionManager.getSubmission(pairedIndex, submissionType).getId())
                .createdAt(Instant.now())
                .createdOptions(options)
                .build();
    }

}
