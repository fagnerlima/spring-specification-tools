# Spring Specification Tools

[![Build Status](https://travis-ci.org/fagnerlima/spring-specification-tools.svg?branch=master)](https://travis-ci.org/fagnerlima/spring-specification-tools)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.fagnerlima/spring-specification-tools/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.fagnerlima/spring-specification-tools)

A library with tools to generate Specifications for use with Spring Data JPA.

## Table of Contents

- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [SpecBuilder](#specbuilder)

## Requirements

* JDK 1.8.

## Getting Started

### Maven Configuration

```xml
<dependency>
  <groupId>com.github.fagnerlima</groupId>
  <artifactId>spring-specification-tools</artifactId>
  <version>${version}</version>
</dependency>
```

### Quick Teaser

```java
@Entity
public class Task implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded
  private Period period;

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

@Entity
public class Tag implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String description;

  // getters, setters, equals and hashCode
}

@Embeddable
public class Period implements Serializable {

  private static final long serialVersionUID = 1L;

  private LocalDate startDate;

  private LocalDate endDate;

  // getters and setters
}

@SpecEntity(Task.class)
public class TaskFilter implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;

  @SpecBetween(left = "period.startDate", right = "period.endDate")
  private LocalDate date;

  @SpecField(operation = Operation.LIKE_IGNORE_CASE_UNACCENT)
  private String description;

  private Status status;

  @SpecJoin
  @SpecField("tags.id")
  private Long tagId;

  // getters and setters
}

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

}

@Service
public class TaskService {

  private TaskRepository taskRepository;

  public TaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<T> findAll(Specification<T> specification, Pageable pageable) {
    return taskRepository.findAll(specification, pageable);
  }
}

@RestController
@RequestMapping("/tasks")
public class TaskController {

  private TaskService taskService;

  public TaskController(TaskService taskService) {
      this.taskService = taskService;
  }

  @GetMapping
  public ResponseEntity<Page<Task>> findAll(TaskFilter taskFilter, Pageable pageable) {
      Specification<Task> specification = new SpecBuilder<Task>()
        .add(taskFilter)
        .build();
      Page<Task> tasksPage = taskService.findAll(specification, pageable);

      return ResponseEntity.ok(tasksPage);
  }
}
```

## SpecBuilder

The **SpecBuilder** build Specifications using filter classes with Spec annotations. The filter class requires, at least, the SpecEntity annotation indicating the entity class.

```java
@SpecEntity(Task.class)
public class TaskFilter implements Serializable {
  // ...
}
```

The properties of the filter will be used by SpecBuilder to build the Specification. All of the properties require *getters* and *setters*.

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

> At the moment, the SpecOperation's `EQUALS_IGNORE_CASE_UNACCENT` and `EQUALS_IGNORE_CASE_UNACCENT` can be used only with PostgreSQL and require the [unaccent](https://www.postgresql.org/docs/10/unaccent.html) extension.

**Example**:

```java
@SpecEntity(Task.class)
public class TaskFilter implements Serializable {
  // ...

  @SpecField(operation = Operation.LIKE_IGNORE_CASE)
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
  @SpecField(value = "tags.description", operation = Operation.LIKE_IGNORE_CASE)
  private Long tagDescription;

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

Used for a period of time. Includes the annotations `SpecPeriodStartDate` and `SpecPeriodStartDate`.

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
