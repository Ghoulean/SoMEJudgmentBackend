package com.ghoulean.somejudgment.model.pojo;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@FieldNameConstants
public final class ActiveCase {
    @NonNull
    private final String judgeId;
    @NonNull
    private final String submission1;
    @NonNull
    private final String submission2;
    @NonNull
    private final Instant createdAt;
    @NonNull
    private final NewActiveCaseOptions createdOptions;
}
