# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- **Breaking:** requires Java 21 or later.
- **Breaking:** requires Spring Data JPA 4 (Spring Boot 4) and Jakarta Persistence: the library now uses
  `jakarta.persistence` instead of `javax.persistence`.
- **Breaking:** Spring Data JPA and the Jakarta Persistence API are now `provided` dependencies. Your application must
  declare them (for example through `spring-boot-starter-data-jpa`), so your Spring Boot BOM decides their versions.
- `SpecBuilder.build()` returns `Specification.unrestricted()` instead of `null` when there are no conditions, as
  Spring Data JPA 4 no longer accepts `null` specifications. Empty `@SpecGroup` filters are ignored the same way.
- `SpecBuilder.add(Specification)` ignores `null` specifications.
- `SpecBuilder.add(Object)` throws an `IllegalArgumentException` explaining the problem, instead of a
  `NullPointerException`, when the filter class is not annotated with `@SpecEntity`.
- The property-name `SpecFactory.create(String, ...)` overloads for `String`, `Boolean` and `LocalDateTime` values no
  longer cast the attribute type with `.as(..)`, which fails with a `ClassCastException` on Hibernate 6 and later.

### Removed

- Support for Java 8 to 20, Spring Boot 2 and 3 and `javax.persistence`. Keep using the 0.3.x versions on those
  platforms.
- The dependency on Apache Commons Lang (`commons-lang3`); the library now relies only on JDK APIs.

## [0.3.1] - 2020-08-21

### Fixed

- Addition of the **distinct** restriction in `SpecBuilder`.

## [0.3.0] - 2020-07-28

### Changed

- Operations for unaccent renamed.

## [0.2.0] - 2020-06-21

### Added

- Support for all `SpecOperation`s in join queries.

## [0.1.0] - 2020-06-14

### Added

- Initial release.

[Unreleased]: https://github.com/fagnerlima/spring-specification-tools/compare/v0.3.1...HEAD
[0.3.1]: https://github.com/fagnerlima/spring-specification-tools/compare/v0.3.0...v0.3.1
[0.3.0]: https://github.com/fagnerlima/spring-specification-tools/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/fagnerlima/spring-specification-tools/compare/0.1.0...v0.2.0
[0.1.0]: https://github.com/fagnerlima/spring-specification-tools/releases/tag/0.1.0
