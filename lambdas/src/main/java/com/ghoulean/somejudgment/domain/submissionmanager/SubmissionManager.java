package com.ghoulean.somejudgment.domain.submissionmanager;

import com.ghoulean.somejudgment.model.enums.SubmissionType;
import com.ghoulean.somejudgment.model.pojo.Submission;

public interface SubmissionManager {
    SubmissionType getSubmissionType(String id);
    Submission getSubmission(String id);
    Submission getSubmission(int enumeratedId, SubmissionType submissionType);
    int getSubmissionCount(SubmissionType submissionType);
}
