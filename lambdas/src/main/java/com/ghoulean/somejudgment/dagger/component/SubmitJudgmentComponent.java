package com.ghoulean.somejudgment.dagger.component;

import javax.inject.Singleton;

import com.ghoulean.somejudgment.dagger.ClientModule;
import com.ghoulean.somejudgment.dagger.DomainModule;
import com.ghoulean.somejudgment.dagger.EnvironmentModule;
import com.ghoulean.somejudgment.handler.SubmitJudgmentHandler;
import com.google.gson.Gson;

import dagger.Component;

@Singleton
@Component(modules = {ClientModule.class, DomainModule.class, EnvironmentModule.class})
public interface SubmitJudgmentComponent {
    SubmitJudgmentHandler getSubmitJudgmentHandler();
    Gson getGson();
}
