package com.ghoulean.somejudgment.model.response;

import com.ghoulean.somejudgment.model.pojo.Submission;

import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public final class GetCaseResponse {
    private final Submission submission1;
    private final Submission submission2;
}
