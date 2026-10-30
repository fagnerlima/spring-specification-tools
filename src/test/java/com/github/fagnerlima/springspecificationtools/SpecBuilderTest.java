package com.github.fagnerlima.springspecificationtools;

import static com.github.fagnerlima.springspecificationtools.sample.SampleTasks.CODE;
import static com.github.fagnerlima.springspecificationtools.sample.SampleTasks.FIX_LOGIN_BUG;
import static com.github.fagnerlima.springspecificationtools.sample.SampleTasks.PLAN_SPRINT;
import static com.github.fagnerlima.springspecificationtools.sample.SampleTasks.REVIEW_PULL_REQUEST;
import static com.github.fagnerlima.springspecificationtools.sample.SampleTasks.WRITE_DOCUMENTATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.github.fagnerlima.springspecificationtools.annotation.SpecBetween;
import com.github.fagnerlima.springspecificationtools.annotation.SpecEntity;
import com.github.fagnerlima.springspecificationtools.annotation.SpecField;
import com.github.fagnerlima.springspecificationtools.annotation.SpecGroup;
import com.github.fagnerlima.springspecificationtools.annotation.SpecJoin;
import com.github.fagnerlima.springspecificationtools.annotation.SpecPeriod;
import com.github.fagnerlima.springspecificationtools.annotation.SpecPeriodEndDate;
import com.github.fagnerlima.springspecificationtools.annotation.SpecPeriodStartDate;
import com.github.fagnerlima.springspecificationtools.sample.SampleTasks;
import com.github.fagnerlima.springspecificationtools.sample.Task;
import com.github.fagnerlima.springspecificationtools.sample.Task.Status;
import com.github.fagnerlima.springspecificationtools.sample.TaskRepository;

/**
 * Runs the Specifications built from annotated filters against a real (H2) database.
 */
@DataJpaTest
class SpecBuilderTest {

    private static final List<String> ALL_TASKS = List.of(FIX_LOGIN_BUG, PLAN_SPRINT, REVIEW_PULL_REQUEST,
            WRITE_DOCUMENTATION);

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private SampleTasks.Data data;

    @BeforeEach
    void setUp() {
        data = SampleTasks.persist(entityManager.getEntityManager());
    }

    private List<String> find(Object filter) {
        return taskRepository.findDescriptions(new SpecBuilder<Task>().add(filter).build());
    }

    @Nested
    class EmptyAndInvalidFilters {

        @Test
        void buildWithoutConditionsMatchesEverything() {
            Specification<Task> specification = new SpecBuilder<Task>().build();

            assertThat(specification).isNotNull();
            assertThat(taskRepository.findDescriptions(specification)).isEqualTo(ALL_TASKS);
        }

        @Test
        void filterWithOnlyNullValuesMatchesEverything() {
            assertThat(find(new TaskFilter())).isEqualTo(ALL_TASKS);
        }

        @Test
        void filterWithoutSpecEntityIsRejected() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new SpecBuilder<Task>().add(new FilterWithoutSpecEntity()))
                    .withMessageContaining(FilterWithoutSpecEntity.class.getName())
                    .withMessageContaining("@SpecEntity");
        }

        @Test
        void nullSpecificationIsIgnored() {
            Specification<Task> specification = new SpecBuilder<Task>()
                    .add((Specification<Task>) null)
                    .build();

            assertThat(taskRepository.findDescriptions(specification)).isEqualTo(ALL_TASKS);
        }

        @Test
        void filterFieldWithoutMatchingEntityPropertyIsIgnored() {
            TaskFilter filter = new TaskFilter();
            filter.setKeyword("anything");

            assertThat(find(filter)).isEqualTo(ALL_TASKS);
        }

        @Test
        void clearRemovesPreviousConditions() {
            TaskFilter filter = new TaskFilter();
            filter.setStatus(Status.DONE);
            SpecBuilder<Task> builder = new SpecBuilder<Task>().add(filter);

            builder.clear();

            assertThat(taskRepository.findDescriptions(builder.build())).isEqualTo(ALL_TASKS);
        }

    }

    @Nested
    class DefaultEqualCondition {

        @Test
        void matchesInheritedField() {
            TaskFilter filter = new TaskFilter();
            filter.setId(data.writeDocumentationId());

            assertThat(find(filter)).containsExactly(WRITE_DOCUMENTATION);
        }

        @Test
        void matchesStringIsCaseSensitive() {
            TaskFilter filter = new TaskFilter();
            filter.setDescription(PLAN_SPRINT);
            TaskFilter lowerCaseFilter = new TaskFilter();
            lowerCaseFilter.setDescription(PLAN_SPRINT.toLowerCase());

            assertThat(find(filter)).containsExactly(PLAN_SPRINT);
            assertThat(find(lowerCaseFilter)).isEmpty();
        }

        @Test
        void matchesEnum() {
            TaskFilter filter = new TaskFilter();
            filter.setStatus(Status.DOING);

            assertThat(find(filter)).containsExactly(REVIEW_PULL_REQUEST);
        }

        @Test
        void matchesBoolean() {
            TaskFilter filter = new TaskFilter();
            filter.setDone(true);

            assertThat(find(filter)).containsExactly(FIX_LOGIN_BUG, REVIEW_PULL_REQUEST);
        }

        @Test
        void combinesConditionsWithAndByDefault() {
            TaskFilter filter = new TaskFilter();
            filter.setStatus(Status.TODO);
            filter.setPriority(2);

            assertThat(find(filter)).containsExactly(PLAN_SPRINT);
        }

        @Test
        void combinesConditionsWithOrWhenRequested() {
            TaskFilter filter = new TaskFilter();
            filter.setStatus(Status.DONE);
            filter.setPriority(1);

            Specification<Task> specification = new SpecBuilder<Task>().add(filter).build(SpecOperator.OR);

            assertThat(taskRepository.findDescriptions(specification)).containsExactly(FIX_LOGIN_BUG,
                    WRITE_DOCUMENTATION);
        }

        @Test
        void combinesFilterWithCustomSpecification() {
            TaskFilter filter = new TaskFilter();
            filter.setStatus(Status.TODO);
            Specification<Task> notesIsNull = (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("notes"));

            Specification<Task> specification = new SpecBuilder<Task>().add(filter).add(notesIsNull).build();

            assertThat(taskRepository.findDescriptions(specification)).containsExactly(WRITE_DOCUMENTATION);
        }

    }

    @Nested
    class SpecFieldAnnotation {

        @Test
        void equal() {
            OperationFilter filter = new OperationFilter();
            filter.setPriorityEqual(2);

            assertThat(find(filter)).containsExactly(PLAN_SPRINT, REVIEW_PULL_REQUEST);
        }

        @Test
        void equalIgnoreCase() {
            OperationFilter filter = new OperationFilter();
            filter.setDescriptionEqualIgnoreCase("PLAN SPRINT");

            assertThat(find(filter)).containsExactly(PLAN_SPRINT);
        }

        @Test
        void likeReplacesWhitespaceWithWildcards() {
            OperationFilter filter = new OperationFilter();
            filter.setDescriptionLike("Review request");

            assertThat(find(filter)).containsExactly(REVIEW_PULL_REQUEST);
        }

        @Test
        void likeIsCaseSensitive() {
            OperationFilter filter = new OperationFilter();
            filter.setDescriptionLike("review");

            assertThat(find(filter)).isEmpty();
        }

        @Test
        void likeIgnoreCase() {
            OperationFilter filter = new OperationFilter();
            filter.setDescriptionLikeIgnoreCase("REVIEW request");

            assertThat(find(filter)).containsExactly(REVIEW_PULL_REQUEST);
        }

        @Test
        void greaterThan() {
            OperationFilter filter = new OperationFilter();
            filter.setPriorityGreaterThan(2);

            assertThat(find(filter)).containsExactly(FIX_LOGIN_BUG);
        }

        @Test
        void greaterThanOrEqual() {
            OperationFilter filter = new OperationFilter();
            filter.setPriorityGreaterThanOrEqual(2);

            assertThat(find(filter)).containsExactly(FIX_LOGIN_BUG, PLAN_SPRINT, REVIEW_PULL_REQUEST);
        }

        @Test
        void lessThan() {
            OperationFilter filter = new OperationFilter();
            filter.setPriorityLessThan(2);

            assertThat(find(filter)).containsExactly(WRITE_DOCUMENTATION);
        }

        @Test
        void lessThanOrEqual() {
            OperationFilter filter = new OperationFilter();
            filter.setPriorityLessThanOrEqual(2);

            assertThat(find(filter)).containsExactly(PLAN_SPRINT, REVIEW_PULL_REQUEST, WRITE_DOCUMENTATION);
        }

        @Test
        void comparisonWithDates() {
            OperationFilter filter = new OperationFilter();
            filter.setEndDateAfter(LocalDate.of(2026, 2, 5));

            assertThat(find(filter)).containsExactly(PLAN_SPRINT);
        }

        @Test
        void datetimeToDateMatchesTheWholeDay() {
            OperationFilter filter = new OperationFilter();
            filter.setCreatedOn(LocalDate.of(2026, 1, 5));

            assertThat(find(filter)).containsExactly(REVIEW_PULL_REQUEST, WRITE_DOCUMENTATION);
        }

        @Test
        void collectionValueUsesIn() {
            OperationFilter filter = new OperationFilter();
            filter.setStatusIn(List.of(Status.TODO, Status.DONE));

            assertThat(find(filter)).containsExactly(FIX_LOGIN_BUG, PLAN_SPRINT, WRITE_DOCUMENTATION);
        }

        @Test
        void nestedEmbeddedProperty() {
            OperationFilter filter = new OperationFilter();
            filter.setStartDate(LocalDate.of(2026, 2, 1));

            assertThat(find(filter)).containsExactly(FIX_LOGIN_BUG);
        }

        @Test
        void canBeNullAddsIsNullConditionForNullValue() {
            assertThat(find(new NullableNotesFilter())).containsExactly(FIX_LOGIN_BUG, WRITE_DOCUMENTATION);
        }

        @Test
        void canBeNullStillMatchesNonNullValue() {
            NullableNotesFilter filter = new NullableNotesFilter();
            filter.setNotes("urgent");

            assertThat(find(filter)).containsExactly(REVIEW_PULL_REQUEST);
        }

    }

    @Nested
    class SpecBetweenAnnotation {

        @Test
        void dateBetweenTwoProperties() {
            BetweenFilter filter = new BetweenFilter();
            filter.setDate(LocalDate.of(2026, 1, 9));

            assertThat(find(filter)).containsExactly(REVIEW_PULL_REQUEST, WRITE_DOCUMENTATION);
        }

        @Test
        void boundariesAreInclusive() {
            BetweenFilter filter = new BetweenFilter();
            filter.setDate(LocalDate.of(2026, 1, 20));

            assertThat(find(filter)).containsExactly(REVIEW_PULL_REQUEST);
        }

        @Test
        void numberBetweenTwoProperties() {
            BetweenFilter filter = new BetweenFilter();
            filter.setEstimate(4);

            assertThat(find(filter)).containsExactly(FIX_LOGIN_BUG, REVIEW_PULL_REQUEST);
        }

        @Test
        void unsupportedValueTypeAddsNoCondition() {
            BetweenFilter filter = new BetweenFilter();
            filter.setText("anything");

            assertThat(find(filter)).isEqualTo(ALL_TASKS);
        }

    }

    @Nested
    class SpecJoinAnnotation {

        @Test
        void equalOnJoinedProperty() {
            JoinFilter filter = new JoinFilter();
            filter.setTagDescription(CODE);

            assertThat(find(filter)).containsExactly(FIX_LOGIN_BUG, REVIEW_PULL_REQUEST);
        }

        @Test
        void operationOnJoinedProperty() {
            JoinFilter filter = new JoinFilter();
            filter.setTagDescriptionLike("DOC");

            assertThat(find(filter)).containsExactly(WRITE_DOCUMENTATION);
        }

        @Test
        void collectionOnJoinedPropertyReturnsDistinctResults() {
            JoinFilter filter = new JoinFilter();
            filter.setTagId(List.of(data.codeTagId(), data.reviewTagId()));

            // "Review pull request" has both tags and must still be returned only once.
            assertThat(find(filter)).containsExactly(FIX_LOGIN_BUG, REVIEW_PULL_REQUEST);
        }

        @Test
        void distinctResultsArePaginatedAndCountedCorrectly() {
            JoinFilter filter = new JoinFilter();
            filter.setTagId(List.of(data.codeTagId(), data.reviewTagId()));
            Specification<Task> specification = new SpecBuilder<Task>().add(filter).build();

            Page<Task> page = taskRepository.findAll(specification, PageRequest.of(0, 1, Sort.by("description")));

            assertThat(page.getTotalElements()).isEqualTo(2);
            assertThat(page.getContent()).extracting(Task::getDescription).containsExactly(FIX_LOGIN_BUG);
        }

    }

    @Nested
    class SpecGroupAnnotation {

        @Test
        void orGroup() {
            GroupFilter filter = new GroupFilter();
            filter.setDescriptionOrTag(new DescriptionOrTagFilter("documentation", "review"));

            assertThat(find(filter)).containsExactly(REVIEW_PULL_REQUEST, WRITE_DOCUMENTATION);
        }

        @Test
        void andGroup() {
            GroupFilter filter = new GroupFilter();
            filter.setStatusAndPriority(new StatusAndPriorityFilter(Status.TODO, 2));

            assertThat(find(filter)).containsExactly(PLAN_SPRINT);
        }

        @Test
        void groupsAreCombinedWithTheOtherConditions() {
            GroupFilter filter = new GroupFilter();
            filter.setDescriptionOrTag(new DescriptionOrTagFilter("documentation", "review"));
            filter.setDone(true);

            assertThat(find(filter)).containsExactly(REVIEW_PULL_REQUEST);
        }

        @Test
        void emptyGroupAddsNoCondition() {
            GroupFilter filter = new GroupFilter();
            filter.setStatusAndPriority(new StatusAndPriorityFilter(null, null));

            assertThat(find(filter)).isEqualTo(ALL_TASKS);
        }

    }

    @Nested
    class SpecPeriodAnnotation {

        @Test
        void matchesTasksStartingOrEndingWithinThePeriod() {
            PeriodFilter filter = new PeriodFilter();
            filter.setPeriod(new DateRange(LocalDate.of(2026, 1, 15), LocalDate.of(2026, 2, 1)));

            assertThat(find(filter)).containsExactly(FIX_LOGIN_BUG, REVIEW_PULL_REQUEST);
        }

        @Test
        void periodWithoutStartDateAddsNoCondition() {
            PeriodFilter filter = new PeriodFilter();
            filter.setPeriod(new DateRange(null, LocalDate.of(2026, 2, 1)));

            assertThat(find(filter)).isEqualTo(ALL_TASKS);
        }

        @Test
        void periodWithoutEndDateAddsNoCondition() {
            PeriodFilter filter = new PeriodFilter();
            filter.setPeriod(new DateRange(LocalDate.of(2026, 1, 15), null));

            assertThat(find(filter)).isEqualTo(ALL_TASKS);
        }

        @Test
        void periodStartingAfterItsEndAddsNoCondition() {
            PeriodFilter filter = new PeriodFilter();
            filter.setPeriod(new DateRange(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 15)));

            assertThat(find(filter)).isEqualTo(ALL_TASKS);
        }

    }

    public static class BaseFilter {

        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

    }

    @SpecEntity(Task.class)
    public static class TaskFilter extends BaseFilter {

        private String description;
        private Status status;
        private Integer priority;
        private Boolean done;
        private String keyword;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }

        public Boolean getDone() {
            return done;
        }

        public void setDone(Boolean done) {
            this.done = done;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

    }

    public static class FilterWithoutSpecEntity {

        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }

    @SpecEntity(Task.class)
    public static class OperationFilter {

        @SpecField(value = "priority", operation = SpecOperation.EQUAL)
        private Integer priorityEqual;

        @SpecField(value = "description", operation = SpecOperation.EQUAL_IGNORE_CASE)
        private String descriptionEqualIgnoreCase;

        @SpecField(value = "description", operation = SpecOperation.LIKE)
        private String descriptionLike;

        @SpecField(value = "description", operation = SpecOperation.LIKE_IGNORE_CASE)
        private String descriptionLikeIgnoreCase;

        @SpecField(value = "priority", operation = SpecOperation.GREATER_THAN)
        private Integer priorityGreaterThan;

        @SpecField(value = "priority", operation = SpecOperation.GREATER_THAN_OR_EQUAL)
        private Integer priorityGreaterThanOrEqual;

        @SpecField(value = "priority", operation = SpecOperation.LESS_THAN)
        private Integer priorityLessThan;

        @SpecField(value = "priority", operation = SpecOperation.LESS_THAN_OR_EQUAL)
        private Integer priorityLessThanOrEqual;

        @SpecField(value = "period.endDate", operation = SpecOperation.GREATER_THAN)
        private LocalDate endDateAfter;

        @SpecField(value = "createdAt", operation = SpecOperation.DATETIME_TO_DATE)
        private LocalDate createdOn;

        @SpecField("status")
        private List<Status> statusIn;

        @SpecField("period.startDate")
        private LocalDate startDate;

        public Integer getPriorityEqual() {
            return priorityEqual;
        }

        public void setPriorityEqual(Integer priorityEqual) {
            this.priorityEqual = priorityEqual;
        }

        public String getDescriptionEqualIgnoreCase() {
            return descriptionEqualIgnoreCase;
        }

        public void setDescriptionEqualIgnoreCase(String descriptionEqualIgnoreCase) {
            this.descriptionEqualIgnoreCase = descriptionEqualIgnoreCase;
        }

        public String getDescriptionLike() {
            return descriptionLike;
        }

        public void setDescriptionLike(String descriptionLike) {
            this.descriptionLike = descriptionLike;
        }

        public String getDescriptionLikeIgnoreCase() {
            return descriptionLikeIgnoreCase;
        }

        public void setDescriptionLikeIgnoreCase(String descriptionLikeIgnoreCase) {
            this.descriptionLikeIgnoreCase = descriptionLikeIgnoreCase;
        }

        public Integer getPriorityGreaterThan() {
            return priorityGreaterThan;
        }

        public void setPriorityGreaterThan(Integer priorityGreaterThan) {
            this.priorityGreaterThan = priorityGreaterThan;
        }

        public Integer getPriorityGreaterThanOrEqual() {
            return priorityGreaterThanOrEqual;
        }

        public void setPriorityGreaterThanOrEqual(Integer priorityGreaterThanOrEqual) {
            this.priorityGreaterThanOrEqual = priorityGreaterThanOrEqual;
        }

        public Integer getPriorityLessThan() {
            return priorityLessThan;
        }

        public void setPriorityLessThan(Integer priorityLessThan) {
            this.priorityLessThan = priorityLessThan;
        }

        public Integer getPriorityLessThanOrEqual() {
            return priorityLessThanOrEqual;
        }

        public void setPriorityLessThanOrEqual(Integer priorityLessThanOrEqual) {
            this.priorityLessThanOrEqual = priorityLessThanOrEqual;
        }

        public LocalDate getEndDateAfter() {
            return endDateAfter;
        }

        public void setEndDateAfter(LocalDate endDateAfter) {
            this.endDateAfter = endDateAfter;
        }

        public LocalDate getCreatedOn() {
            return createdOn;
        }

        public void setCreatedOn(LocalDate createdOn) {
            this.createdOn = createdOn;
        }

        public List<Status> getStatusIn() {
            return statusIn;
        }

        public void setStatusIn(List<Status> statusIn) {
            this.statusIn = statusIn;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

    }

    @SpecEntity(Task.class)
    public static class NullableNotesFilter {

        @SpecField(canBeNull = true)
        private String notes;

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

    }

    @SpecEntity(Task.class)
    public static class BetweenFilter {

        @SpecBetween(left = "period.startDate", right = "period.endDate")
        private LocalDate date;

        @SpecBetween(left = "minEstimate", right = "maxEstimate")
        private Integer estimate;

        @SpecBetween(left = "description", right = "notes")
        private String text;

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public Integer getEstimate() {
            return estimate;
        }

        public void setEstimate(Integer estimate) {
            this.estimate = estimate;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

    }

    @SpecEntity(Task.class)
    public static class JoinFilter {

        @SpecJoin
        @SpecField("tags.id")
        private List<Long> tagId;

        @SpecJoin
        @SpecField("tags.description")
        private String tagDescription;

        @SpecJoin
        @SpecField(value = "tags.description", operation = SpecOperation.LIKE_IGNORE_CASE)
        private String tagDescriptionLike;

        public List<Long> getTagId() {
            return tagId;
        }

        public void setTagId(List<Long> tagId) {
            this.tagId = tagId;
        }

        public String getTagDescription() {
            return tagDescription;
        }

        public void setTagDescription(String tagDescription) {
            this.tagDescription = tagDescription;
        }

        public String getTagDescriptionLike() {
            return tagDescriptionLike;
        }

        public void setTagDescriptionLike(String tagDescriptionLike) {
            this.tagDescriptionLike = tagDescriptionLike;
        }

    }

    @SpecEntity(Task.class)
    public static class GroupFilter {

        private Boolean done;

        @SpecGroup(operator = SpecOperator.OR)
        private DescriptionOrTagFilter descriptionOrTag;

        @SpecGroup
        private StatusAndPriorityFilter statusAndPriority;

        public Boolean getDone() {
            return done;
        }

        public void setDone(Boolean done) {
            this.done = done;
        }

        public DescriptionOrTagFilter getDescriptionOrTag() {
            return descriptionOrTag;
        }

        public void setDescriptionOrTag(DescriptionOrTagFilter descriptionOrTag) {
            this.descriptionOrTag = descriptionOrTag;
        }

        public StatusAndPriorityFilter getStatusAndPriority() {
            return statusAndPriority;
        }

        public void setStatusAndPriority(StatusAndPriorityFilter statusAndPriority) {
            this.statusAndPriority = statusAndPriority;
        }

    }

    public static class DescriptionOrTagFilter {

        @SpecField(operation = SpecOperation.LIKE_IGNORE_CASE)
        private String description;

        @SpecJoin
        @SpecField(value = "tags.description", operation = SpecOperation.LIKE_IGNORE_CASE)
        private String tagDescription;

        public DescriptionOrTagFilter(String description, String tagDescription) {
            this.description = description;
            this.tagDescription = tagDescription;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTagDescription() {
            return tagDescription;
        }

        public void setTagDescription(String tagDescription) {
            this.tagDescription = tagDescription;
        }

    }

    public static class StatusAndPriorityFilter {

        private Status status;
        private Integer priority;

        public StatusAndPriorityFilter(Status status, Integer priority) {
            this.status = status;
            this.priority = priority;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }

    }

    @SpecEntity(Task.class)
    public static class PeriodFilter {

        @SpecPeriod(start = "period.startDate", end = "period.endDate")
        private DateRange period;

        public DateRange getPeriod() {
            return period;
        }

        public void setPeriod(DateRange period) {
            this.period = period;
        }

    }

    public static class DateRange {

        @SpecPeriodStartDate
        private LocalDate startDate;

        @SpecPeriodEndDate
        private LocalDate endDate;

        public DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

    }

}
