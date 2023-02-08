package com.ghoulean.somejudgment.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@FieldNameConstants
public final class Feedback {
    @NonNull
    private final String judgeId;
    @NonNull
    private final String submissionId;
    @NonNull
    private final String feedback;
}
