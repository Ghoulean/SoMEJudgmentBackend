package com.ghoulean.somejudgment.model.request;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public final class CreateCaseRequest {
    private final String judge;
}
