package com.ghoulean.somejudgment.domain.pairstrategy;

import com.ghoulean.somejudgment.model.pojo.ActiveCase;

public interface PairStrategy {
    ActiveCase createNewActiveCase(String judgeId);
}
