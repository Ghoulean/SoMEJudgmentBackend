package com.ghoulean.somejudgment.dagger;

import javax.inject.Singleton;

import com.ghoulean.somejudgment.accessor.database.DatabaseAccessor;
import com.ghoulean.somejudgment.accessor.database.DynamoDbAccessor;

import dagger.Binds;
import dagger.Module;
import lombok.NonNull;

@Module(includes = {ClientModule.class, EnvironmentModule.class})
public abstract class AccessorModule {

    @Binds
    @Singleton
    public abstract DatabaseAccessor bindDatabaseAccessor(@NonNull DynamoDbAccessor dynamoDbAccessor);
}
