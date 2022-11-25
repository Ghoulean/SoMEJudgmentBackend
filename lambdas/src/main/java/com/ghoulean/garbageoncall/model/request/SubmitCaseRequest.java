package com.ghoulean.garbageoncall.model.request;

import com.ghoulean.garbageoncall.model.pojo.Judgment;

import lombok.Data;

@Data
public class SubmitCaseRequest {
    final String judge;
    final Judgment judgment;
}
