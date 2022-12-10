package com.ghoulean.somejudgment.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@FieldNameConstants
public final class Judgment {
    private final String judgeId;
    private final String winnerId;
    private final String loserId;
}
