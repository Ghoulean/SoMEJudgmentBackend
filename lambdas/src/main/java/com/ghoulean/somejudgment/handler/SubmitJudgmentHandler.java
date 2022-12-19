package com.ghoulean.somejudgment.handler;

import javax.inject.Inject;

import com.ghoulean.somejudgment.accessor.DynamoDbAccessor;
import com.ghoulean.somejudgment.domain.pairstrategy.PairStrategy;
import com.ghoulean.somejudgment.domain.submissionmanager.SubmissionManager;
import com.ghoulean.somejudgment.model.request.SubmitJudgmentRequest;
import com.ghoulean.somejudgment.model.response.SubmitJudgmentResponse;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SubmitJudgmentHandler {
    private @NonNull final DynamoDbAccessor dynamoDbAccessor;
    private @NonNull final SubmissionManager submissionManager;

    @Inject
    public SubmitJudgmentHandler(@NonNull final DynamoDbAccessor dynamoDbAccessor,
            @NonNull final SubmissionManager submissionManager,
            @NonNull final PairStrategy pairStrategy) {
        this.dynamoDbAccessor = dynamoDbAccessor;
        this.submissionManager = submissionManager;
    }

    public SubmitJudgmentResponse handle(@NonNull final SubmitJudgmentRequest getCaseRequest) {
        // TODO: implement
        return null;
    }
}
