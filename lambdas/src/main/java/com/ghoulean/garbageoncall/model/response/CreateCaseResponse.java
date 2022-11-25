package com.ghoulean.garbageoncall.model.response;

import com.ghoulean.garbageoncall.model.pojo.ActiveCase;

import lombok.Data;

@Data
public final class CreateCaseResponse {
    final ActiveCase createdCase;
}
