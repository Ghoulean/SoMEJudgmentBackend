package com.ghoulean.garbageoncall.lambda;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.ghoulean.garbageoncall.dagger.component.CreateCaseComponent;
import com.ghoulean.garbageoncall.handler.CreateCaseHandler;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CreateCaseLambda implements RequestStreamHandler {
    @NonNull
    private final CreateCaseHandler changeOncallHandler;

    public CreateCaseLambda() {
        CreateCaseComponent changeOncallUpdateComponent = DaggerCreateCaseComponent.create();
        changeOncallHandler = changeOncallUpdateComponent.changeOncallHandler();
    }

    @Override
    public void handleRequest(@NonNull final InputStream inputStream,
            @NonNull final OutputStream outputStream,
            @NonNull final Context context) {
        final String inputString = convertStreamToString(inputStream);
    }

    private String convertStreamToString(final InputStream inputStream) {
        try {
            final BufferedInputStream bis = new BufferedInputStream(inputStream);
            final ByteArrayOutputStream buf = new ByteArrayOutputStream();
            for (int result = bis.read(); result != -1; result = bis.read()) {
                buf.write((byte) result);
            }
            // StandardCharsets.UTF_8.name() > JDK 7
            return buf.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
