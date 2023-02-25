package com.ghoulean.somejudgment.dagger;

import java.time.Duration;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import software.amazon.awssdk.regions.Region;

@Module
@UtilityClass
@SuppressWarnings({ "checkstyle:hideutilityclassconstructor" })
public final class EnvironmentModule {
    @Provides
    public static Region provideAwsRegion() {
        return Region.of(getEnv("AWS_REGION"));
    }

    @Named(Constants.TABLE_NAME)
    @Provides
    public static String provideTableName() {
        return getEnv(Constants.TABLE_NAME);
    }

    @Named(Constants.WAIT_TIME_SECONDS)
    @Provides
    public static Duration provideForceWaitDuration() {
        return Duration.ofSeconds(Long.parseLong(getEnv(Constants.WAIT_TIME_SECONDS)));
    }

    private static String getEnv(@NonNull final String key) {
        return System.getenv(key);
    }
}
