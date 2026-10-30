package com.github.fagnerlima.springspecificationtools.sample;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.EntityManager;

import com.github.fagnerlima.springspecificationtools.sample.Task.Status;

/**
 * Fixed data set shared by the H2 integration tests.
 *
 * <pre>
 * | description         | status | priority | period                  | createdAt           | done  | estimate | notes  | tags         |
 * | Write documentation | TODO   | 1        | 2026-01-01 - 2026-01-10 | 2026-01-05T09:30    | false | 1 - 3    | null   | docs         |
 * | Review pull request | DOING  | 2        | 2026-01-08 - 2026-01-20 | 2026-01-05T23:59:59 | true  | 2 - 5    | urgent | code, review |
 * | Fix login bug       | DONE   | 3        | 2026-02-01 - 2026-02-05 | 2026-01-06T00:00    | true  | 4 - 8    | null   | code         |
 * | Plan sprint         | TODO   | 2        | 2026-03-01 - 2026-03-02 | 2026-01-04T23:59:59 | false | 1 - 1    | weekly | (none)       |
 * </pre>
 */
public final class SampleTasks {

    public static final String WRITE_DOCUMENTATION = "Write documentation";
    public static final String REVIEW_PULL_REQUEST = "Review pull request";
    public static final String FIX_LOGIN_BUG = "Fix login bug";
    public static final String PLAN_SPRINT = "Plan sprint";

    public static final String DOCS = "docs";
    public static final String CODE = "code";
    public static final String REVIEW = "review";

    private SampleTasks() {
    }

    public static Data persist(EntityManager entityManager) {
        Tag docs = new Tag(DOCS);
        Tag code = new Tag(CODE);
        Tag review = new Tag(REVIEW);

        Task writeDocumentation = new Task(WRITE_DOCUMENTATION)
                .status(Status.TODO)
                .priority(1)
                .period(new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10)))
                .createdAt(LocalDateTime.of(2026, 1, 5, 9, 30))
                .done(false)
                .estimate(1, 3)
                .tags(docs);
        Task reviewPullRequest = new Task(REVIEW_PULL_REQUEST)
                .status(Status.DOING)
                .priority(2)
                .period(new Period(LocalDate.of(2026, 1, 8), LocalDate.of(2026, 1, 20)))
                .createdAt(LocalDateTime.of(2026, 1, 5, 23, 59, 59))
                .done(true)
                .estimate(2, 5)
                .notes("urgent")
                .tags(code, review);
        Task fixLoginBug = new Task(FIX_LOGIN_BUG)
                .status(Status.DONE)
                .priority(3)
                .period(new Period(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 5)))
                .createdAt(LocalDateTime.of(2026, 1, 6, 0, 0))
                .done(true)
                .estimate(4, 8)
                .tags(code);
        Task planSprint = new Task(PLAN_SPRINT)
                .status(Status.TODO)
                .priority(2)
                .period(new Period(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2)))
                .createdAt(LocalDateTime.of(2026, 1, 4, 23, 59, 59))
                .done(false)
                .estimate(1, 1)
                .notes("weekly");

        for (Object entity : new Object[] { docs, code, review, writeDocumentation, reviewPullRequest, fixLoginBug,
                planSprint }) {
            entityManager.persist(entity);
        }

        entityManager.flush();
        entityManager.clear();

        return new Data(writeDocumentation.getId(), docs.getId(), code.getId(), review.getId());
    }

    /**
     * Generated identifiers needed by the tests.
     */
    public record Data(Long writeDocumentationId, Long docsTagId, Long codeTagId, Long reviewTagId) {
    }

}
