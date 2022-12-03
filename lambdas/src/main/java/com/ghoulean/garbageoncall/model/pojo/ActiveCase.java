package com.ghoulean.garbageoncall.model.pojo;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@FieldNameConstants
public final class ActiveCase {
    final String judgeId;
    final String submission1;
    final String submission2;
    final Instant createdAt;
}
