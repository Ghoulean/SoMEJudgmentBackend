package com.ghoulean.somejudgment.domain.pairstrategy;

import com.ghoulean.somejudgment.model.pojo.ActiveCase;
import com.ghoulean.somejudgment.model.pojo.NewActiveCaseOptions;

public interface PairStrategy {
    ActiveCase createNewActiveCase(String judgeId, NewActiveCaseOptions options);
}
