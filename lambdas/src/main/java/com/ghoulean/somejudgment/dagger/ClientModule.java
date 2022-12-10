package com.ghoulean.somejudgment.dagger;

import com.google.gson.Gson;

import dagger.Module;
import dagger.Provides;
import lombok.NonNull;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Module
public final class ClientModule {
    @Provides
    public DynamoDbClient provideDynamoDB(@NonNull final Region region) {
        DynamoDbClient ddb = DynamoDbClient.builder()
            .region(region)
            .build();
       return ddb;
    }

    @Provides
    public Gson provideGson() {
        return new Gson();
    }
}
