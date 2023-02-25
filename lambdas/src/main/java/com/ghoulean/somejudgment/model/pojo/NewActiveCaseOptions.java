package com.ghoulean.somejudgment.model.pojo;

import com.ghoulean.somejudgment.model.enums.SubmissionType;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Builder(toBuilder = true)
@Data
@FieldNameConstants
public final class NewActiveCaseOptions {
    private final SubmissionType submissionType;
}
