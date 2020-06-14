package com.github.fagnerlima.springspecificationtools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.github.fagnerlima.springspecificationtools.annotation.SpecBetween;
import com.github.fagnerlima.springspecificationtools.annotation.SpecEntity;
import com.github.fagnerlima.springspecificationtools.annotation.SpecField;
import com.github.fagnerlima.springspecificationtools.annotation.SpecGroup;
import com.github.fagnerlima.springspecificationtools.annotation.SpecJoin;
import com.github.fagnerlima.springspecificationtools.annotation.SpecPeriod;
import com.github.fagnerlima.springspecificationtools.annotation.SpecPeriodEndDate;
import com.github.fagnerlima.springspecificationtools.annotation.SpecPeriodStartDate;
import com.github.fagnerlima.springspecificationtools.util.FieldUtils;

public class SpecBuilder<T> {

    private List<Specification<T>> specs = new ArrayList<>();
    private SpecFactory<T> specFactory = new SpecFactory<>();

    public void clear() {
        specs = new ArrayList<>();
    }

    public SpecBuilder<T> add(Specification<T> spec) {
        specs.add(spec);

        return this;
    }

    /**
     * Accept an object annotated with {@link SpecEntity}.
     *
     * @param filter object annotated with {@link SpecEntity}
     * @return SpecBuilder instance
     */
    @SuppressWarnings("unchecked")
    public SpecBuilder<T> add(Object filter) {
        Class<T> entityClass = (Class<T>) filter.getClass().getAnnotation(SpecEntity.class).value();

        return add(filter, entityClass);
    }

    public Specification<T> build() {
        return build(SpecOperator.AND);
    }

    public Specification<T> build(SpecOperator operator) {
        if (specs.isEmpty()) {
            return null;
        }

        Specification<T> result = specs.get(0);

        if (specs.size() > 1) {
            for (int i = 1; i < specs.size(); i++) {
                result = Specification.where(result);
                result = operator.equals(SpecOperator.AND)
                        ? result.and(specs.get(i))
                        : result.or(specs.get(i));
            }
        }

        return result;
    }

    private SpecBuilder<T> add(Object filter, Class<T> entityClass) {
        List<Field> filterFields = FieldUtils.getAllFields(filter.getClass());
        List<Field> entityFields = FieldUtils.getAllFields(entityClass);

        for (Field filterField : filterFields) {
            try {
                Method getterMethod = FieldUtils.findGetterMethod(filterField.getName(), filter.getClass());
                Object value = getterMethod.invoke(filter);
                SpecField specField = filterField.getAnnotation(SpecField.class);

                if (value == null && (specField == null || !specField.canBeNull())) {
                    continue;
                }

                SpecBetween specBetween = filterField.getAnnotation(SpecBetween.class);

                if (specBetween != null) {
                    addBetween(specBetween, value);
                    continue;
                }

                SpecJoin specJoin = filterField.getAnnotation(SpecJoin.class);

                if (specJoin != null) {
                    addJoin(filterField, value);
                    continue;
                }

                SpecGroup specGroup = filterField.getAnnotation(SpecGroup.class);

                if (specGroup != null) {
                    addGroup(specGroup, value, entityClass);
                    continue;
                }

                SpecPeriod specPeriod = filterField.getAnnotation(SpecPeriod.class);

                if (specPeriod != null) {
                    addPeriod(specPeriod, value);
                    continue;
                }

                if (SpecUtils.hasProperty(filterField, entityFields)) {
                    specs.add(specFactory.create(filterField, value));
                }
            } catch (Exception exception) {
                continue;
            }
        }

        return this;
    }

    private void addBetween(SpecBetween specBetween, Object value) {
        specs.add(specFactory.between(specBetween.left(), specBetween.right(), value));
    }

    private void addJoin(Field filterField, Object value) {
        specs.add(specFactory.join(filterField, value));
    }

    private void addGroup(SpecGroup specGroup, Object value, Class<T> entityClass) {
        specs.add(new SpecBuilder<T>()
                .add(value, entityClass)
                .build(specGroup.operator()));
    }

    private void addPeriod(SpecPeriod specPeriod, Object value) throws Exception {
        Field startField = FieldUtils.getField(value.getClass(), SpecPeriodStartDate.class);
        Field endField = FieldUtils.getField(value.getClass(), SpecPeriodEndDate.class);

        Method startGetterMethod = FieldUtils.findGetterMethod(startField.getName(), value.getClass());
        Method endGetterMethod = FieldUtils.findGetterMethod(endField.getName(), value.getClass());

        LocalDate startValue = (LocalDate) startGetterMethod.invoke(value);
        LocalDate endValue = (LocalDate) endGetterMethod.invoke(value);

        if (startValue == null || endValue == null || startValue.isAfter(endValue)) {
            return;
        }

        specs.add(new SpecBuilder<T>()
                .add(specFactory.between(specPeriod.start(), startValue, endValue))
                .add(specFactory.between(specPeriod.end(), startValue, endValue))
                .build(SpecOperator.OR));
    }

}
