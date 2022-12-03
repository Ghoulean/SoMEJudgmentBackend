package com.ghoulean.garbageoncall.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@FieldNameConstants
public final class Submission {
    final int id; // Must be enumerated as 0 to (num of entries - 1)
    final String submissionLink;
    final String submitters;
    final String emails;
}
