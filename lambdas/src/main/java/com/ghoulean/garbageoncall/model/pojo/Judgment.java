package com.ghoulean.garbageoncall.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@FieldNameConstants
public final class Judgment {
    final String judgeId;
    final String winnerId;
    final String loserId;
}
