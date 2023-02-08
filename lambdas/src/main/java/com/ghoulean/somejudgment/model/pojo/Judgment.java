package com.ghoulean.somejudgment.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@FieldNameConstants
public final class Judgment {
    @NonNull
    private final String judgeId;
    @NonNull
    private final String winnerId;
    @NonNull
    private final String loserId;
    private final Feedback winnerFeedback;
    private final Feedback loserFeedback;
}
