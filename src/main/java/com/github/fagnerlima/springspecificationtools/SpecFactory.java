package com.github.fagnerlima.springspecificationtools;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;

import org.springframework.data.jpa.domain.Specification;

import com.github.fagnerlima.springspecificationtools.util.DateUtils;
import com.github.fagnerlima.springspecificationtools.util.StringUtils;

/**
 * Used for create Specification objects.
 * @author Fagner Lima
 * @since 0.1.0
 *
 * @param <T> entity class
 */
public class SpecFactory<T extends Serializable> {

    private static final String POSTGRESQL_UNACCENT_FUNCTION = "unaccent";

    public Specification<T> distinct() {
        return (root, query, criteriaBuilder) -> query.distinct(true).getRestriction();
    }

    public Specification<T> create(Field field, Object value) {
        return (root, query, criteriaBuilder) -> create(SpecUtils.getPath(root, field), field, value)
                .toPredicate(root, query, criteriaBuilder);
    }

    @SuppressWarnings("unchecked")
    public <Y extends Comparable<? super Y>> Specification<T> create(String property, Y value, SpecOperation operation) {
        return (root, query, criteriaBuilder) -> {
            return create((Path<? extends Y>) SpecUtils.getPath(root, property), value, operation)
                    .toPredicate(root, query, criteriaBuilder);
        };
    }

    public Specification<T> create(String property, LocalDateTime value, SpecOperation operation) {
        return (root, query, criteriaBuilder) -> {
            return create((Path<LocalDateTime>) SpecUtils.getPath(root, property).as(LocalDateTime.class), (LocalDateTime) value, operation)
                    .toPredicate(root, query, criteriaBuilder);
        };
    }

    @SuppressWarnings("unchecked")
    public Specification<T> create(String property, Collection<?> values) {
        return (root, query, criteriaBuilder) -> {
            return create((Path<Collection<?>>) SpecUtils.getPath(root, property), values)
                    .toPredicate(root, query, criteriaBuilder);
        };
    }

    public Specification<T> create(String property, String value, SpecOperation operation) {
        return (root, query, criteriaBuilder) -> {
            return create((Path<String>) SpecUtils.getPath(root, property).as(String.class), value, operation)
                    .toPredicate(root, query, criteriaBuilder);
        };
    }

    public Specification<T> create(String property, Boolean value) {
        return (root, query, criteriaBuilder) -> {
            return create((Path<Boolean>) SpecUtils.getPath(root, property).as(Boolean.class), value)
                    .toPredicate(root, query, criteriaBuilder);
        };
    }

    @SuppressWarnings("unchecked")
    public Specification<T> create(String property, Enum<?> value) {
        return (root, query, criteriaBuilder) -> {
            return create((Path<Enum<?>>) SpecUtils.getPath(root, property), value)
                    .toPredicate(root, query, criteriaBuilder);
        };
    }

    @SuppressWarnings("unchecked")
    public Specification<T> between(String leftProperty, String rightProperty, Object value) {
        return (root, query, criteriaBuilder) -> {
            Expression<?> x = SpecUtils.getPath(root, leftProperty);
            Expression<?> y = SpecUtils.getPath(root, rightProperty);

            if (value instanceof Number) {
                return criteriaBuilder.between(criteriaBuilder.literal(Long.valueOf(value.toString())),
                        (Expression<Long>) x,
                        (Expression<Long>) y);
            } else if (value instanceof LocalDate) {
                return criteriaBuilder.between(criteriaBuilder.literal((LocalDate) value),
                        (Expression<LocalDate>) x,
                        (Expression<LocalDate>) y);
            }

            return null;
        };
    }

    @SuppressWarnings("unchecked")
    public <Y extends Comparable<? super Y>> Specification<T> between(String property, Y leftValue, Y rightValue) {
        return (root, query, criteriaBuilder) -> {
            Expression<Y> x = (Expression<Y>) SpecUtils.getPath(root, property);

            return criteriaBuilder.between(x, criteriaBuilder.literal(leftValue), criteriaBuilder.literal(rightValue));
        };
    }

    public Specification<T> join(Field field, Object value) {
        return (root, query, criteriaBuilder) -> {
            String[] deepProperties = SpecUtils.getDeepProperties(field);
            Join<T, Object> joinRoot = root.join(deepProperties[0]);

            if (deepProperties.length > 2) {
                Integer index = 1;

                do {
                    joinRoot = joinRoot.join(deepProperties[index]);
                } while (++index < deepProperties.length - 2);
            }

            return create(joinRoot.get(deepProperties[deepProperties.length - 1]), field, value)
                    .toPredicate(root, query, criteriaBuilder);
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Specification<T> create(Path<?> path, Field field, Object value) {
        SpecOperation operation = SpecUtils.getOperation(field);

        if (value == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(path);
        }

        if (value instanceof String) {
            return create((Path<String>) path, (String) value, operation);
        }

        if (value instanceof Collection) {
            return create((Path<Collection<?>>) path, (Collection<?>) value);
        }

        if (value instanceof LocalDate && operation.equals(SpecOperation.DATETIME_TO_DATE)) {
            return create((Path<LocalDateTime>) path, ((LocalDate) value).atStartOfDay(), operation);
        }

        if (value instanceof Comparable) {
            return create((Path<Comparable>) path, (Comparable) value, operation);
        }

        return null;
    }

    private <Y extends Comparable<? super Y>> Specification<T> create(Path<? extends Y> path, Y value, SpecOperation operation) {
        return (root, query, criteriaBuilder) -> {
            switch (operation) {
                case GREATER_THAN:
                    return criteriaBuilder.greaterThan(path, value);
                case LESS_THAN:
                    return criteriaBuilder.lessThan(path, value);
                case GREATER_THAN_OR_EQUAL:
                    return criteriaBuilder.greaterThanOrEqualTo(path, value);
                case LESS_THAN_OR_EQUAL:
                    return criteriaBuilder.lessThanOrEqualTo(path, value);
                default:
                    return criteriaBuilder.equal(path, value);
            }
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Specification<T> create(Path<LocalDateTime> path, LocalDateTime value, SpecOperation operation) {
        return (root, query, criteriaBuilder) -> {
            if (operation.equals(SpecOperation.DATETIME_TO_DATE)) {
                LocalDateTime startOfDay = DateUtils.atStartOfDay(value);
                LocalDateTime endOfDay = DateUtils.atEndOfDay(value);

                return criteriaBuilder.between(path, startOfDay, endOfDay);
            }

            return create((Path<? extends Comparable>) path, (Comparable) value, operation).toPredicate(root, query, criteriaBuilder);
        };
    }

    private Specification<T> create(Path<Collection<?>> collection, Collection<?> values) {
        return (root, query, criteriaBuilder) -> {
            In<Object> predicate = criteriaBuilder.in(collection);
            values.stream().forEach(value -> predicate.value(value));

            return predicate;
        };
    }

    public Specification<T> create(Path<String> path, String value, SpecOperation operation) {
        return (root, query, criteriaBuilder) -> {
            Expression<String> x = path;
            String y = value;

            switch (operation) {
                case EQUAL_IGNORE_CASE:
                    x = criteriaBuilder.lower(x);
                    y = y.toLowerCase();
                    return criteriaBuilder.equal(x, y);
                case LIKE:
                    y = prepareForLike(y);
                    return criteriaBuilder.like(x, y);
                case LIKE_IGNORE_CASE:
                    x = criteriaBuilder.lower(x);
                    y = prepareForLike(y.toLowerCase());
                    return criteriaBuilder.like(x, y);
                case EQUAL_IGNORE_CASE_UNACCENT:
                    x = criteriaBuilder.function(POSTGRESQL_UNACCENT_FUNCTION, String.class, criteriaBuilder.lower(x));
                    y = StringUtils.unaccent(y.toLowerCase());
                    return criteriaBuilder.equal(x, y);
                case LIKE_IGNORE_CASE_UNACCENT:
                    x = criteriaBuilder.function(POSTGRESQL_UNACCENT_FUNCTION, String.class, criteriaBuilder.lower(x));
                    y = prepareForLike(StringUtils.unaccent(y.toLowerCase()));
                    return criteriaBuilder.like(x, y);
                default:
                    return criteriaBuilder.equal(x, y);
            }
        };
    }

    public Specification<T> create(Path<Boolean> path, Boolean value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(path, value);
    }

    public Specification<T> create(Path<Enum<?>> path, Enum<?> value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(path, value);
    }

    private String prepareForLike(String value) {
        return "%" + value.replaceAll("\\s+", "%") + "%";
    }

}
