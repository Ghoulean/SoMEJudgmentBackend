package com.ghoulean.somejudgment.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@FieldNameConstants
public final class JudgmentCount {
    private final int amount;
}
