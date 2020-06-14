package br.pro.fagnerlima.springspecificationtools;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;

import org.springframework.data.jpa.domain.Specification;

import br.pro.fagnerlima.springspecificationtools.util.DateUtils;
import br.pro.fagnerlima.springspecificationtools.util.StringUtils;

public class SpecFactory<T> {

    private static final String POSTGRESQL_UNACCENT_FUNCTION = "unaccent";

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Specification<T> create(Field field, Object value) {
        String property = SpecUtils.getPropertyName(field);
        SpecOperation operation = SpecUtils.getOperation(field);

        if (value == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(SpecUtils.getPath(root, field));
        }

        if (value instanceof String) {
            return create(property, (String) value, operation);
        }

        if (value instanceof Collection) {
            return create(property, (Collection<?>) value);
        }

        if (value instanceof LocalDate && operation.equals(SpecOperation.DATETIME_TO_DATE)) {
            return create(property, ((LocalDate) value).atStartOfDay(), operation);
        }

        if (value instanceof Comparable) {
            return create(property, (Comparable) value, operation);
        }

        if (value instanceof Boolean) {
            return create(property, (Boolean) value, operation);
        }

        if (value instanceof Enum) {
            return create(property, (Enum) value, operation);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <Y extends Comparable<? super Y>> Specification<T> create(String property, Y value, SpecOperation operation) {
        return (root, query, criteriaBuilder) -> {
            Path<? extends Y> x = (Path<? extends Y>) SpecUtils.getPath(root, property);

            return create(x, value, operation).toPredicate(root, query, criteriaBuilder);
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Specification<T> create(String property, LocalDateTime value, SpecOperation operation) {
        return (root, query, criteriaBuilder) -> {
            Expression<LocalDateTime> x = SpecUtils.getPath(root, property).as(LocalDateTime.class);

            if (operation.equals(SpecOperation.DATETIME_TO_DATE)) {
                LocalDateTime startOfDay = DateUtils.atStartOfDay(value);
                LocalDateTime endOfDay = DateUtils.atEndOfDay(value);

                return criteriaBuilder.between(x, startOfDay, endOfDay);
            }

            return create(property, (Comparable) value, operation).toPredicate(root, query, criteriaBuilder);
        };
    }

    public Specification<T> create(String property, Collection<?> values) {
        return (root, query, criteriaBuilder) -> {
            In<Object> predicate = criteriaBuilder.in(SpecUtils.getPath(root, property));
            values.stream().forEach(value -> predicate.value(value));

            return predicate;
        };
    }

    public Specification<T> create(String property, String value, SpecOperation operation) {
        return (root, query, criteriaBuilder) -> {
            Expression<String> x = SpecUtils.getPath(root, property).as(String.class);
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
                case EQUALS_IGNORE_CASE_UNACCENT:
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

    public Specification<T> create(String property, Boolean value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(SpecUtils.getPath(root, property), value);
    }

    public Specification<T> create(String property, Enum<?> value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(SpecUtils.getPath(root, property), value);
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
                } while (index < deepProperties.length - 2);
            }

            return create(joinRoot.get(deepProperties[deepProperties.length - 1]), field, value)
                    .toPredicate(root, query, criteriaBuilder);
        };
    }

    @SuppressWarnings("unchecked")
    private Specification<T> create(Path<?> path, Field field, Object value) {
        SpecOperation operation = SpecUtils.getOperation(field);

        if (value == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(path);
        }

        if (value instanceof Number) {
            return create((Path<Long>) path, Long.valueOf(value.toString()), operation);
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

    private String prepareForLike(String value) {
        return "%" + value.replaceAll("\\s+", "%") + "%";
    }

}
