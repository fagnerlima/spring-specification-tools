# Spring Specification Tools

[![CI](https://github.com/fagnerlima/spring-specification-tools/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/fagnerlima/spring-specification-tools/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.fagnerlima/spring-specification-tools)](https://central.sonatype.com/artifact/com.github.fagnerlima/spring-specification-tools)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A library with tools to generate Specifications for use with Spring Data JPA.

## Table of Contents

- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [SpecBuilder](#specbuilder)
- [SpecOperation](#specoperation)
- [Migrating from 0.3.x](#migrating-from-03x)
- [Building](#building)
- [Releasing](#releasing)

## Requirements

| spring-specification-tools | Java | Spring Data JPA | Spring Boot | Persistence API |
| --- | --- | --- | --- | --- |
| 1.x | 21+ | 4.x | 4.x | `jakarta.persistence` |
| 0.3.x (legacy) | 8+ | 2.x | 2.x | `javax.persistence` |

Version 1.x is built and tested against Spring Boot 4.1 (Spring Data JPA 4.1, Hibernate 7).

Spring Data JPA and the Jakarta Persistence API are `provided` dependencies: the library uses the versions your
application already has (for example through `spring-boot-starter-data-jpa`), so your Spring Boot BOM stays in control.

The `*_IGNORE_CASE_UNACCENT` operations additionally require PostgreSQL with the
[unaccent](https://www.postgresql.org/docs/current/unaccent.html) extension.

## Getting Started

### Installation

Replace `${version}` with the latest version shown in the Maven Central badge above.

Maven:

```xml
<dependency>
  <groupId>com.github.fagnerlima</groupId>
  <artifactId>spring-specification-tools</artifactId>
  <version>${version}</version>
</dependency>
```

Gradle:

```groovy
implementation "com.github.fagnerlima:spring-specification-tools:${version}"
```

### Quick Teaser

> Code extracted from [sample-specification-api](https://github.com/fagnerlima/sample-specification-api).

```java
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
// ...

@Entity
@Table(name = "task")
public class Task extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Valid
    @Embedded
    private Period period;

    @NotNull
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToMany
    @Fetch(FetchMode.JOIN)
    @JoinTable(name = "tag_task",
            joinColumns = @JoinColumn(name = "id_task"),
            inverseJoinColumns = @JoinColumn(name = "id_tag"))
    private Set<Tag> tags;

    // getters, setters, equals and hashCode
}
```

```java
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
// ...

@Embeddable
public class Period implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    // getters, setters, equals and hashCode
}
```

```java
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
// ...

@Entity
@Table(name = "tag")
public class Tag extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String description;

    // getters, setters, equals and hashCode
}
```

```java
@SpecEntity(Task.class)
public class TaskFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @SpecBetween(left = "period.startDate", right = "period.endDate")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @SpecPeriod(start = "period.startDate", end = "period.endDate")
    private PeriodFilter period;

    @SpecField(operation = SpecOperation.LIKE_IGNORE_CASE_UNACCENT)
    private String description;

    private Task.Status status;

    @SpecJoin
    @SpecField("tags.id")
    private List<Long> tagId;

    @SpecGroup(operator = SpecOperator.OR)
    private DescriptionOrTagFilter descriptionOrTag;

    // getters and setters
}
```

```java
public class PeriodFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    @SpecPeriodStartDate
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @SpecPeriodEndDate
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    // getters and setters
}
```

```java
public class DescriptionOrTagFilter implements Serializable {

    private static final long serialVersionUID = -3125120719715109975L;

    @SpecField(operation = SpecOperation.LIKE_IGNORE_CASE_UNACCENT)
    private String description;

    @SpecJoin
    @SpecField(value = "tags.description", operation = SpecOperation.LIKE_IGNORE_CASE_UNACCENT)
    private String tagDescription;

    // ...
}
```

```java
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

}
```

```java
@Service
public class TaskService {

    private TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
      this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public Page<Task> findAll(Specification<Task> specification, Pageable pageable) {
        return taskRepository.findAll(specification, pageable);
    }
}
```

```java
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<Page<Task>> findAll(TaskFilter taskFilter, Pageable pageable) {
        Specification<Task> specification = new SpecBuilder<Task>().add(taskFilter).build();
        Page<Task> tasksPage = taskService.findAll(specification, pageable);

        return ResponseEntity.ok(tasksPage);
    }
}
```

## SpecBuilder

The **SpecBuilder** build Specifications using filter classes with Spec annotations. The filter class requires, at least, the SpecEntity annotation indicating the entity class; otherwise `add` throws an `IllegalArgumentException`.

```java
@SpecEntity(Task.class)
public class TaskFilter implements Serializable {
  // ...
}
```

The properties of the filter will be used by SpecBuilder to build the Specification. All of the properties require *getters* and *setters*.

Properties with `null` values are ignored (unless `@SpecField(canBeNull = true)` is set), and plain properties without a matching entity field are skipped. When no condition applies, `build()` returns `Specification.unrestricted()`, which matches every entity.

By default, the property will result a single condition with the "equal operator", but you can custom the conditions with the following annotations:

### SpecField

Used for single conditions.

Params:

- **value**: the name of the field in the entity;
  - **default**: the name of the field in the filter.
- **operation**: the operation of the query (`SpecOperation` enum);
  - **default**: SpecOperation.EQUAL.
- **canBeNull**: if `true` and the value is `null`, the condition `IS NULL` will be included.
  - **default**: false.

> At the moment, the SpecOperation's `EQUAL_IGNORE_CASE_UNACCENT` and `LIKE_IGNORE_CASE_UNACCENT` can be used only with PostgreSQL and require the [unaccent](https://www.postgresql.org/docs/current/unaccent.html) extension.

**Example**:

```java
@SpecEntity(Task.class)
public class TaskFilter implements Serializable {
  // ...

  @SpecField(operation = SpecOperation.LIKE_IGNORE_CASE)
  private String description;

  private Status status;

  // getters and setters
}
```

### SpecBetween

Used for between conditions.

Params:

- **left**: the left property in the condition;
- **right**: the right property in the condition.

**Example**:

```java
@SpecEntity(Task.class)
public class TaskFilter implements Serializable {
  // ...

  @SpecBetween(left = "period.startDate", right = "period.endDate")
  private LocalDate date;

  // getters and setters
}
```

### SpecJoin

Used for indicate a join query.

**Example**:

```java
@SpecEntity(Task.class)
public class TaskFilter implements Serializable {
  // ...

  @SpecJoin
  @SpecField("tags.id")
  private Long tagId;

  @SpecJoin
  @SpecField(value = "tags.description", operation = SpecOperation.LIKE_IGNORE_CASE)
  private String tagDescription;

  // getters and setters
}
```

### SpecGroup

Define a new group of conditions.

Params:

- **operator**: the operator used in the group (`SpecOperator` enum).

**Example**:

```java
@SpecEntity(Task.class)
public class TaskFilter implements Serializable {
  // ...

  @SpecGroup(operator = SpecOperator.OR)
  private SampleFilter sample;

  // getters and setters
}
```

### SpecPeriod

Used for a period of time. Includes the annotations `SpecPeriodStartDate` and `SpecPeriodEndDate`.

Params:

- **start**: the name of the field that represents the start date;
- **end**: the name of the field that represents the end date.

**Example**:

```java
@SpecEntity(Task.class)
public class TaskFilter implements Serializable {
  // ...

  @SpecPeriod(start = "period.startDate", end = "period.endDate")
  private PeriodFilter period;

  // getters and setters
}

public class PeriodFilter implements Serializable {

  private static final long serialVersionUID = 1L;

  @SpecPeriodStartDate
  private LocalDate startDate;

  @SpecPeriodEndDate
  private LocalDate endDate;

  // getters and setters
}
```

## SpecOperation

| Operation | Condition |
| --- | --- |
| `EQUAL` (default) | `field = value`; a collection value becomes `field IN (values)` |
| `EQUAL_IGNORE_CASE` | `lower(field) = lower(value)` |
| `LIKE` | `field LIKE %value%`, with whitespace in the value replaced by `%` |
| `LIKE_IGNORE_CASE` | `LIKE`, ignoring case |
| `GREATER_THAN`, `GREATER_THAN_OR_EQUAL` | `field > value`, `field >= value` |
| `LESS_THAN`, `LESS_THAN_OR_EQUAL` | `field < value`, `field <= value` |
| `DATETIME_TO_DATE` | a `LocalDateTime` field within the whole day of a `LocalDate` value |
| `EQUAL_IGNORE_CASE_UNACCENT` | `EQUAL_IGNORE_CASE`, also ignoring accents (PostgreSQL only) |
| `LIKE_IGNORE_CASE_UNACCENT` | `LIKE_IGNORE_CASE`, also ignoring accents (PostgreSQL only) |

## Migrating from 0.3.x

1. Upgrade the application to Java 21 and Spring Boot 4 (Spring Data JPA 4).
2. Make sure the application declares Spring Data JPA itself, for example with `spring-boot-starter-data-jpa`; the
   library no longer brings it transitively.
3. Replace `javax.persistence` imports with `jakarta.persistence` in your entities.
4. If your code checked `SpecBuilder.build()` for `null`, remove that check: it now returns
   `Specification.unrestricted()` when there are no conditions, which you can pass straight to the repository.
5. If you relied on `commons-lang3` coming through this library, declare it in your application.

The annotations, `SpecBuilder`, `SpecOperation` and `SpecOperator` keep the same packages and usage.

## Building

```shell
./mvnw verify
```

The build requires Java 21. The PostgreSQL tests use [Testcontainers](https://testcontainers.com/), so a running Docker
daemon is required too.

## Releasing

Releases are fully automated by the [Release workflow](.github/workflows/release.yml):

1. Open a pull request that moves the `Unreleased` entries of [CHANGELOG.md](CHANGELOG.md) into a new
   `## [X.Y.Z] - YYYY-MM-DD` section, and merge it.
2. Tag the merged commit and push the tag:

   ```shell
   git tag vX.Y.Z
   git push origin vX.Y.Z
   ```

3. The workflow checks that `CHANGELOG.md` has the `## [X.Y.Z]` section, builds and tests the project with
   `-Drevision=X.Y.Z`, signs the artifacts, publishes them to Maven Central through the
   [Central Portal](https://central.sonatype.com/) and creates the GitHub Release with that changelog section as notes.

One-time setup, done by the maintainer:

- The `com.github.fagnerlima` namespace must be verified on the Central Portal for the publishing account.
- Repository (or `maven-central` environment) secrets:

  | Secret | Value |
  | --- | --- |
  | `MAVEN_CENTRAL_USERNAME` | Central Portal user token username |
  | `MAVEN_CENTRAL_PASSWORD` | Central Portal user token password |
  | `MAVEN_GPG_PRIVATE_KEY` | ASCII-armored GPG private key used to sign the artifacts (`gpg --armor --export-secret-keys <key-id>`) |
  | `MAVEN_GPG_PASSPHRASE` | Passphrase of that GPG key |

- The public part of the GPG key must be published to a key server supported by Maven Central, such as
  `keys.openpgp.org`.
