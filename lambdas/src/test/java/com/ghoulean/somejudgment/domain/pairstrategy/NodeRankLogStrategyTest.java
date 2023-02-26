package com.ghoulean.somejudgment.domain.pairstrategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.stream.Stream;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ghoulean.somejudgment.accessor.database.DatabaseAccessor;
import com.ghoulean.somejudgment.domain.submissionmanager.SubmissionManager;
import com.ghoulean.somejudgment.model.enums.SubmissionType;
import com.ghoulean.somejudgment.model.pojo.ActiveCase;
import com.ghoulean.somejudgment.model.pojo.JudgmentCount;
import com.ghoulean.somejudgment.model.pojo.NewActiveCaseOptions;
import com.ghoulean.somejudgment.model.pojo.Submission;

@ExtendWith(MockitoExtension.class)
public final class NodeRankLogStrategyTest {

    @Mock
    DatabaseAccessor mockDatabaseAccessor;
    @Mock
    SubmissionManager mockSubmissionManager;

    private static final EasyRandom easyRandom = new EasyRandom();
    private static NodeRankLogStrategy nodeRankLogStrategy;

    @BeforeEach
    public void setup() {
        nodeRankLogStrategy = new NodeRankLogStrategy(mockDatabaseAccessor, mockSubmissionManager);
    }

    @ParameterizedTest
    @MethodSource("providePairingData")
    public void successfulPairing(final Integer totalCount, final Integer currentCount,
            final Integer expectedCurrentIndex, final Integer expectedPairedIndex) {
        final String judgeId = easyRandom.nextObject(String.class);
        final NewActiveCaseOptions options = easyRandom.nextObject(NewActiveCaseOptions.class);
        final Submission submission1 = easyRandom.nextObject(Submission.class);
        final Submission submission2 = easyRandom.nextObject(Submission.class);
        final SubmissionType submissionType = options.getSubmissionType();
        final Instant createdAt = Instant.now();

        when(mockSubmissionManager.getSubmissionCount(submissionType)).thenReturn(totalCount);
        when(mockDatabaseAccessor.getJudgmentCount(submissionType))
                .thenReturn(JudgmentCount.builder().amount(currentCount).build());
        when(mockSubmissionManager.getSubmission(expectedCurrentIndex, submissionType)).thenReturn(submission1);
        when(mockSubmissionManager.getSubmission(expectedPairedIndex, submissionType)).thenReturn(submission2);

        final ActiveCase expectedActiveCase = ActiveCase.builder()
                .judgeId(judgeId)
                .submission1(submission1.getId())
                .submission2(submission2.getId())
                .createdAt(createdAt)
                .createdOptions(options)
                .build();

        // TODO: dependency inject clock and use Instant.now(clock)
        try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
            mockedStatic.when(() -> Instant.now()).thenReturn(createdAt);
            assertEquals(expectedActiveCase, nodeRankLogStrategy.createNewActiveCase(judgeId, options));
        }

    }

    private static Stream<Arguments> providePairingData() {
        return Stream.of(
                Arguments.of(100, 0, 0, 1),
                Arguments.of(100, 1, 1, 2),
                Arguments.of(100, 99, 99, 0),
                Arguments.of(100, 100, 0, 50),
                Arguments.of(100, 101, 1, 51),
                Arguments.of(100, 200, 0, 37),
                Arguments.of(100, 300, 0, 32),
                Arguments.of(100, 400, 0, 29),
                Arguments.of(100, 500, 0, 27),
                Arguments.of(100, 600, 0, 26));
    }
}
