package com.ghoulean.somejudgment.model.request;

import com.ghoulean.somejudgment.model.pojo.NewActiveCaseOptions;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public final class GetCaseRequest {
    private final String judgeId;
    private final NewActiveCaseOptions newActiveCaseOptions;
}
