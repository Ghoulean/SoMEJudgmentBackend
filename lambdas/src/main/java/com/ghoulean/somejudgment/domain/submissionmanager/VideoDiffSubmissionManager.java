package com.ghoulean.somejudgment.domain.submissionmanager;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ghoulean.somejudgment.model.enums.SubmissionType;
import com.ghoulean.somejudgment.model.pojo.Submission;
import com.opencsv.CSVReader;

import lombok.NonNull;

// Video-differentiating Submission Manager
public final class VideoDiffSubmissionManager implements SubmissionManager {

    // TODO: env var
    private static final String CSV_FILE = "MOCK_DATA.csv";
    private final List<Submission> videoSubmissions;
    private final List<Submission> nonVideoSubmissions;
    private final Map<String, Submission> idToSubmissionMap;
    private final Map<String, SubmissionType> idToSubmissionTypeMap;

    @SuppressWarnings("checkstyle:MagicNumber")
    public VideoDiffSubmissionManager() {
        videoSubmissions = new ArrayList<>();
        nonVideoSubmissions = new ArrayList<>();
        idToSubmissionMap = new HashMap<>();
        idToSubmissionTypeMap = new HashMap<>();
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final CSVReader reader = new CSVReader(new InputStreamReader(classloader.getResourceAsStream(CSV_FILE)));
        try {
            // TODO: abstract this away into some kind of submission ddb parser class
            // throw away header
            String[] line = reader.readNext();
            while ((line = reader.readNext()) != null) {
                boolean isVideo = Boolean.parseBoolean(line[4]);
                Submission submission = Submission.builder()
                        .id(line[0])
                        .submissionLink(line[1])
                        .submitters(line[2])
                        .emails(line[3])
                        .build();
                if (isVideo) {
                    videoSubmissions.add(submission);
                    idToSubmissionTypeMap.put(submission.getId(), SubmissionType.VIDEO);
                } else {
                    idToSubmissionTypeMap.put(submission.getId(), SubmissionType.NONVIDEO);
                    nonVideoSubmissions.add(submission);
                }
                if (idToSubmissionMap.putIfAbsent(submission.getId(), submission) != null) {
                    reader.close();
                    throw new RuntimeException(submission.getId() + " is a duplicate id");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Submission getSubmission(final int enumeratedId, @NonNull final SubmissionType submissionType) {
        switch (submissionType) {
            case VIDEO:
                return videoSubmissions.get(enumeratedId);
            case NONVIDEO:
                return nonVideoSubmissions.get(enumeratedId);
            case ALL:
            default:
                throw new UnsupportedOperationException(
                        "Video-differentiating submission manager only operates on "
                                + "SubmissionType.VIDEO and SubmissionType.NONVIDEO");
        }
    }

    @Override
    public int getSubmissionCount(@NonNull final SubmissionType submissionType) {
        switch (submissionType) {
            case VIDEO:
                return videoSubmissions.size();
            case NONVIDEO:
                return nonVideoSubmissions.size();
            case ALL:
            default:
                throw new UnsupportedOperationException(
                        "Video-differentiating submission manager only operates on "
                                + "SubmissionType.VIDEO and SubmissionType.NONVIDEO");
        }
    }

    @Override
    public SubmissionType getSubmissionType(@NonNull final String id) {
        return idToSubmissionTypeMap.get(id);
    }

    @Override
    public Submission getSubmission(@NonNull final String id) {
        return idToSubmissionMap.get(id);
    }
}
