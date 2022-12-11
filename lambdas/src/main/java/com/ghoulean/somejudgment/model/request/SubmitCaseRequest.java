package com.ghoulean.somejudgment.model.request;

import com.ghoulean.somejudgment.model.pojo.Judgment;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public final class SubmitCaseRequest {
    private final String judge;
    private final Judgment judgment;
}
