package com.ghoulean.somejudgment.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Builder(toBuilder = true)
@Data
@FieldNameConstants
public final class JudgmentCount {
    private final int amount;
}
