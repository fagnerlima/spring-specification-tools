package com.github.fagnerlima.springspecificationtools;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.github.fagnerlima.springspecificationtools.annotation.SpecEntity;
import com.github.fagnerlima.springspecificationtools.annotation.SpecField;
import com.github.fagnerlima.springspecificationtools.annotation.SpecGroup;
import com.github.fagnerlima.springspecificationtools.annotation.SpecJoin;
import com.github.fagnerlima.springspecificationtools.sample.Tag;
import com.github.fagnerlima.springspecificationtools.sample.Task;
import com.github.fagnerlima.springspecificationtools.sample.TaskRepository;

/**
 * The {@code *_IGNORE_CASE_UNACCENT} operations rely on PostgreSQL's {@code unaccent} extension, so they run against a
 * real PostgreSQL started by Testcontainers (requires Docker).
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgreSqlUnaccentTest.PostgreSqlContainerConfiguration.class)
class PostgreSqlUnaccentTest {

    private static final String CODE_REVIEW = "Revisão de código";
    private static final String WEEKLY_MEETING = "Reunião semanal";
    private static final String COFFEE_BREAK = "Cafe com leite";

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        Tag programming = entityManager.persist(new Tag("Programação"));
        Tag management = entityManager.persist(new Tag("Gestão"));
        Tag leisure = entityManager.persist(new Tag("Lazer"));

        entityManager.persist(new Task(CODE_REVIEW).tags(programming));
        entityManager.persist(new Task(WEEKLY_MEETING).tags(management, programming));
        entityManager.persist(new Task(COFFEE_BREAK).tags(leisure));
        entityManager.flush();
        entityManager.clear();
    }

    private List<String> find(Object filter) {
        return taskRepository.findDescriptions(new SpecBuilder<Task>().add(filter).build());
    }

    @Test
    void equalIgnoreCaseUnaccentIgnoresCaseAndAccentsOnBothSides() {
        UnaccentFilter filter = new UnaccentFilter();
        filter.setDescription("REVISAO DE CÓDIGO");

        assertThat(find(filter)).containsExactly(CODE_REVIEW);
    }

    @Test
    void equalIgnoreCaseUnaccentRequiresTheWholeValue() {
        UnaccentFilter filter = new UnaccentFilter();
        filter.setDescription("revisao");

        assertThat(find(filter)).isEmpty();
    }

    @Test
    void likeIgnoreCaseUnaccentMatchesUnaccentedInput() {
        UnaccentFilter filter = new UnaccentFilter();
        filter.setDescriptionLike("reuniao");

        assertThat(find(filter)).containsExactly(WEEKLY_MEETING);
    }

    @Test
    void likeIgnoreCaseUnaccentMatchesAccentedInput() {
        UnaccentFilter filter = new UnaccentFilter();
        filter.setDescriptionLike("CAFÉ leite");

        assertThat(find(filter)).containsExactly(COFFEE_BREAK);
    }

    @Test
    void likeIgnoreCaseUnaccentOnJoinedProperty() {
        UnaccentFilter filter = new UnaccentFilter();
        filter.setTagDescription("programacao");

        assertThat(find(filter)).containsExactly(WEEKLY_MEETING, CODE_REVIEW);
    }

    @Test
    void orGroupWithJoinIsDistinctAndPaginated() {
        UnaccentFilter filter = new UnaccentFilter();
        filter.setDescriptionOrTag(new DescriptionOrTagFilter("cafe", "programacao"));

        Page<Task> page = taskRepository.findAll(new SpecBuilder<Task>().add(filter).build(),
                PageRequest.of(0, 2, Sort.by("description")));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).extracting(Task::getDescription).containsExactly(COFFEE_BREAK, WEEKLY_MEETING);
    }

    /**
     * Declaring the container as a bean lets Spring stop it only after the DataSource that uses it is closed.
     */
    @TestConfiguration(proxyBeanMethods = false)
    static class PostgreSqlContainerConfiguration {

        @Bean
        @ServiceConnection
        PostgreSQLContainer postgreSqlContainer() {
            return new PostgreSQLContainer("postgres:18-alpine").withInitScript("db/postgresql-init.sql");
        }

    }

    @SpecEntity(Task.class)
    public static class UnaccentFilter {

        @SpecField(operation = SpecOperation.EQUAL_IGNORE_CASE_UNACCENT)
        private String description;

        @SpecField(value = "description", operation = SpecOperation.LIKE_IGNORE_CASE_UNACCENT)
        private String descriptionLike;

        @SpecJoin
        @SpecField(value = "tags.description", operation = SpecOperation.LIKE_IGNORE_CASE_UNACCENT)
        private String tagDescription;

        @SpecGroup(operator = SpecOperator.OR)
        private DescriptionOrTagFilter descriptionOrTag;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescriptionLike() {
            return descriptionLike;
        }

        public void setDescriptionLike(String descriptionLike) {
            this.descriptionLike = descriptionLike;
        }

        public String getTagDescription() {
            return tagDescription;
        }

        public void setTagDescription(String tagDescription) {
            this.tagDescription = tagDescription;
        }

        public DescriptionOrTagFilter getDescriptionOrTag() {
            return descriptionOrTag;
        }

        public void setDescriptionOrTag(DescriptionOrTagFilter descriptionOrTag) {
            this.descriptionOrTag = descriptionOrTag;
        }

    }

    public static class DescriptionOrTagFilter {

        @SpecField(operation = SpecOperation.LIKE_IGNORE_CASE_UNACCENT)
        private String description;

        @SpecJoin
        @SpecField(value = "tags.description", operation = SpecOperation.LIKE_IGNORE_CASE_UNACCENT)
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

}
