package com.ghoulean.somejudgment.model.response;

import com.ghoulean.somejudgment.model.pojo.ActiveCase;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public final class CreateCaseResponse {
    private final ActiveCase createdCase;
}
