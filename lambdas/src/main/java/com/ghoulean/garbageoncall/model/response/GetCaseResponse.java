package com.ghoulean.garbageoncall.model.response;

import com.ghoulean.garbageoncall.model.pojo.Submission;

import lombok.Data;

@Data
public class GetCaseResponse {
    final Submission submission1;
    final Submission submission2;
}
