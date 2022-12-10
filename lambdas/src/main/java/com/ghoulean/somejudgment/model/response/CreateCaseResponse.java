package com.ghoulean.somejudgment.model.response;

import com.ghoulean.somejudgment.model.pojo.ActiveCase;

import lombok.Data;

@Data
public final class CreateCaseResponse {
    private final ActiveCase createdCase;
}
