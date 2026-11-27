package com.github.fagnerlima.springspecificationtools;

import static com.github.fagnerlima.springspecificationtools.sample.SampleTasks.FIX_LOGIN_BUG;
import static com.github.fagnerlima.springspecificationtools.sample.SampleTasks.PLAN_SPRINT;
import static com.github.fagnerlima.springspecificationtools.sample.SampleTasks.REVIEW_PULL_REQUEST;
import static com.github.fagnerlima.springspecificationtools.sample.SampleTasks.WRITE_DOCUMENTATION;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.github.fagnerlima.springspecificationtools.sample.SampleTasks;
import com.github.fagnerlima.springspecificationtools.sample.Task;
import com.github.fagnerlima.springspecificationtools.sample.Task.Status;
import com.github.fagnerlima.springspecificationtools.sample.TaskRepository;

/**
 * Covers the public {@link SpecFactory} methods that take property names, used without annotated filters.
 */
@DataJpaTest
class SpecFactoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private final SpecFactory<Task> specFactory = new SpecFactory<>();

    @BeforeEach
    void setUp() {
        SampleTasks.persist(entityManager.getEntityManager());
    }

    @Test
    void comparableWithOperation() {
        assertThat(taskRepository.findDescriptions(specFactory.create("priority", 2, SpecOperation.GREATER_THAN_OR_EQUAL)))
                .containsExactly(FIX_LOGIN_BUG, PLAN_SPRINT, REVIEW_PULL_REQUEST);
    }

    @Test
    void comparableOnNestedProperty() {
        assertThat(taskRepository.findDescriptions(
                specFactory.create("period.startDate", LocalDate.of(2026, 1, 8), SpecOperation.LESS_THAN_OR_EQUAL)))
                .containsExactly(REVIEW_PULL_REQUEST, WRITE_DOCUMENTATION);
    }

    @Test
    void dateTimeToDate() {
        assertThat(taskRepository.findDescriptions(
                specFactory.create("createdAt", LocalDateTime.of(2026, 1, 5, 12, 0), SpecOperation.DATETIME_TO_DATE)))
                .containsExactly(REVIEW_PULL_REQUEST, WRITE_DOCUMENTATION);
    }

    @Test
    void dateTimeWithComparison() {
        assertThat(taskRepository.findDescriptions(
                specFactory.create("createdAt", LocalDateTime.of(2026, 1, 5, 23, 59, 59), SpecOperation.GREATER_THAN_OR_EQUAL)))
                .containsExactly(FIX_LOGIN_BUG, REVIEW_PULL_REQUEST);
    }

    @Test
    void string() {
        assertThat(taskRepository.findDescriptions(specFactory.create("description", "LOGIN", SpecOperation.LIKE_IGNORE_CASE)))
                .containsExactly(FIX_LOGIN_BUG);
    }

    @Test
    void collection() {
        assertThat(taskRepository.findDescriptions(specFactory.create("status", List.of(Status.DOING, Status.DONE))))
                .containsExactly(FIX_LOGIN_BUG, REVIEW_PULL_REQUEST);
    }

    @Test
    void booleanValue() {
        assertThat(taskRepository.findDescriptions(specFactory.create("done", Boolean.FALSE)))
                .containsExactly(PLAN_SPRINT, WRITE_DOCUMENTATION);
    }

    @Test
    void enumValue() {
        assertThat(taskRepository.findDescriptions(specFactory.create("status", (Enum<?>) Status.TODO)))
                .containsExactly(PLAN_SPRINT, WRITE_DOCUMENTATION);
    }

    @Test
    void propertyBetweenValues() {
        assertThat(taskRepository.findDescriptions(specFactory.between("priority", 2, 3)))
                .containsExactly(FIX_LOGIN_BUG, PLAN_SPRINT, REVIEW_PULL_REQUEST);
    }

    @Test
    void valueBetweenProperties() {
        assertThat(taskRepository.findDescriptions(specFactory.between("minEstimate", "maxEstimate", 1)))
                .containsExactly(PLAN_SPRINT, WRITE_DOCUMENTATION);
    }

}
