package com.ghoulean.somejudgment.dagger;

import com.ghoulean.somejudgment.gson.UppercaseEnumDeserializer;
import com.ghoulean.somejudgment.model.enums.SubmissionType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
        return new GsonBuilder()
            .registerTypeAdapter(SubmissionType.class, new UppercaseEnumDeserializer<SubmissionType>())
            .create();
    }
}
