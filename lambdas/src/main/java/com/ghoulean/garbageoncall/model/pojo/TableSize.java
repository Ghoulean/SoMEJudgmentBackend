package com.ghoulean.garbageoncall.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@FieldNameConstants
public final class TableSize {
    final int amount;
}
