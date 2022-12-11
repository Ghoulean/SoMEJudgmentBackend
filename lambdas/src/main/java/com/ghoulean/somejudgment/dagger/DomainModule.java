package com.ghoulean.somejudgment.dagger;

import javax.inject.Singleton;

import com.ghoulean.somejudgment.domain.pairstrategy.NodeRankLogStrategy;
import com.ghoulean.somejudgment.domain.pairstrategy.PairStrategy;
import com.ghoulean.somejudgment.domain.submissionmanager.SubmissionManager;
import com.ghoulean.somejudgment.domain.submissionmanager.VideoDiffSubmissionManager;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import lombok.NonNull;

@Module(includes = {ClientModule.class, EnvironmentModule.class})
public abstract class DomainModule {

    // @Binds doesn't work for some reason
    @Provides
    public static SubmissionManager provideSubmissionManager() {
        return new VideoDiffSubmissionManager();
    }

    @Binds
    @Singleton
    public abstract PairStrategy bindPairStrategy(@NonNull NodeRankLogStrategy nodeRankLogStrategy);
}
