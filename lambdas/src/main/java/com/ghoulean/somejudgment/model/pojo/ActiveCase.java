package com.ghoulean.somejudgment.model.pojo;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@FieldNameConstants
public final class ActiveCase {
    private final String judgeId;
    private final String submission1;
    private final String submission2;
    private final Instant createdAt;
    private final NewActiveCaseOptions createdOptions;
}
