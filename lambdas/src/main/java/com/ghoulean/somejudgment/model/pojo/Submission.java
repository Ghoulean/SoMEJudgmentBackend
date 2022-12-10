package com.ghoulean.somejudgment.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@FieldNameConstants
public final class Submission {
    private final int id; // Must be enumerated as 0 to (num of entries - 1)
    private final String submissionLink;
    private final String submitters;
    private final String emails;
}
