package com.ghoulean.somejudgment.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Builder(toBuilder = true)
@Data
@FieldNameConstants
public final class Submission {
    private final String id;
    private final String submissionLink;
    private final String submitters;
    private final String emails;
}
