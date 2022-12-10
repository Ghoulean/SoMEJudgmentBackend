package com.ghoulean.somejudgment.model.request;

import com.ghoulean.somejudgment.model.pojo.Judgment;

import lombok.Data;

@Data
public final class SubmitCaseRequest {
    private final String judge;
    private final Judgment judgment;
}
