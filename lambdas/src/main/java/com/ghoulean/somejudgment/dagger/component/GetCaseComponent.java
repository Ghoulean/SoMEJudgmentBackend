package com.ghoulean.somejudgment.dagger.component;

import javax.inject.Singleton;

import com.ghoulean.somejudgment.dagger.AccessorModule;
import com.ghoulean.somejudgment.dagger.ClientModule;
import com.ghoulean.somejudgment.dagger.DomainModule;
import com.ghoulean.somejudgment.dagger.EnvironmentModule;
import com.ghoulean.somejudgment.handler.GetCaseHandler;
import com.google.gson.Gson;

import dagger.Component;

@Singleton
@Component(modules = {AccessorModule.class, ClientModule.class, DomainModule.class, EnvironmentModule.class})
public interface GetCaseComponent {
    GetCaseHandler getGetCaseHandler();
    Gson getGson();
}
