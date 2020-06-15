# Spring Specification Tools

[![Build Status](https://travis-ci.org/fagnerlima/spring-specification-tools.svg?branch=master)](https://travis-ci.org/fagnerlima/spring-specification-tools)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.fagnerlima/spring-specification-tools/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.fagnerlima/spring-specification-tools)

A library with tools to generate Specifications for use with Spring Data JPA.

## Table of Contents

- [Requirements](#requirements)
- [Getting Started](#getting-started)

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
@SpecEntity(Task.class)
public class TaskFilter implements Serializable {

  private static final long serialVersionUID = 5002772490664267241L;

  private Long id;

  @SpecField(value = "dateTime", operation = Operation.DATETIME_TO_DATE)
  private LocalDate date;

  @SpecField(operation = Operation.LIKE_IGNORE_CASE_UNACCENT)
  private String description;

  private Status status;

  @SpecJoin
  @SpecField("tags.id")
  private Long tagId;

  @SpecJoin
  @SpecField("notifications.id")
  private Long notificationId;

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
