package com.ghoulean.somejudgment.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@FieldNameConstants
public final class NewActiveCaseOptions {
    private final boolean nonVideoSubmission;
}
