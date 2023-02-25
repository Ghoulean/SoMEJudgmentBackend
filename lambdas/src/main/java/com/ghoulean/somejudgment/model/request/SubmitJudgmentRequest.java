package com.ghoulean.somejudgment.model.request;

import com.ghoulean.somejudgment.model.pojo.Judgment;

import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public final class SubmitJudgmentRequest {
    private final Judgment judgment;
}
