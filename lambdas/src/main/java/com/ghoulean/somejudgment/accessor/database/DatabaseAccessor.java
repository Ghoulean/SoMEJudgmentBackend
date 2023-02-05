package com.ghoulean.somejudgment.accessor.database;

import java.util.List;

import com.ghoulean.somejudgment.model.enums.SubmissionType;
import com.ghoulean.somejudgment.model.pojo.ActiveCase;
import com.ghoulean.somejudgment.model.pojo.Feedback;
import com.ghoulean.somejudgment.model.pojo.Judgment;
import com.ghoulean.somejudgment.model.pojo.JudgmentCount;

import lombok.NonNull;

public interface DatabaseAccessor {
    ActiveCase getActiveCase(@NonNull String judgeId);

    List<Judgment> getJudgments(@NonNull String judgeId, String token, Integer pageSize);

    JudgmentCount getJudgmentCount(@NonNull SubmissionType submissionType);

    void insertJudgment(@NonNull Judgment judgment);

    void upsertActiveCase(@NonNull ActiveCase activeCase);

    void upsertFeedback(@NonNull Feedback feedback);

    void incrementJudgmentCount(@NonNull SubmissionType submissionType);

    void deleteActiveCase(@NonNull String judgeId);
}
