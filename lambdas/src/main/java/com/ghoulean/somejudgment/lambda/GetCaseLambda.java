package com.ghoulean.somejudgment.lambda;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.ghoulean.somejudgment.dagger.component.DaggerGetCaseComponent;
import com.ghoulean.somejudgment.dagger.component.GetCaseComponent;
import com.ghoulean.somejudgment.handler.GetCaseHandler;
import com.ghoulean.somejudgment.model.request.GetCaseRequest;
import com.ghoulean.somejudgment.model.response.GetCaseResponse;
import com.google.gson.Gson;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class GetCaseLambda implements RequestStreamHandler {
    @NonNull
    private final GetCaseHandler getCaseHandler;
    @NonNull
    private final Gson gson;

    public GetCaseLambda() {
        GetCaseComponent changeOncallUpdateComponent = DaggerGetCaseComponent.create();
        getCaseHandler = changeOncallUpdateComponent.getCaseHandler();
        gson = changeOncallUpdateComponent.getGson();
    }

    @Override
    public void handleRequest(@NonNull final InputStream inputStream,
            @NonNull final OutputStream outputStream,
            @NonNull final Context context) {
        final String inputString = convertStreamToString(inputStream);
        final GetCaseRequest getCaseRequest = gson.fromJson(inputString, GetCaseRequest.class);
        log.info("Received: {}, serialized into: {}", inputString, getCaseRequest);
        final GetCaseResponse getCaseResponse = getCaseHandler.handle(getCaseRequest);
        log.info("Returning: {}", getCaseResponse);
        writeStringToStream(outputStream, gson.toJson(getCaseResponse));
    }

    private String convertStreamToString(final InputStream inputStream) {
        try {
            final BufferedInputStream bis = new BufferedInputStream(inputStream);
            final ByteArrayOutputStream buf = new ByteArrayOutputStream();
            for (int result = bis.read(); result != -1; result = bis.read()) {
                buf.write((byte) result);
            }
            return buf.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeStringToStream(final OutputStream outputStream, final String str) {
        try (Writer w = new OutputStreamWriter(outputStream, "UTF-8")) {
            w.write("Hello, World!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
