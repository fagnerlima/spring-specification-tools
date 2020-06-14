package com.github.fagnerlima.springspecificationtools;

import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import com.github.fagnerlima.springspecificationtools.annotation.SpecBetween;
import com.github.fagnerlima.springspecificationtools.annotation.SpecField;
import com.github.fagnerlima.springspecificationtools.util.FieldUtils;
import com.github.fagnerlima.springspecificationtools.util.StringUtils;

/**
 * @author Fagner Lima
 * @since 0.1.0
 */
public class SpecUtils {

    public static Boolean hasProperty(Field filterField, List<Field> entityFields) {
        SpecBetween specBetween = filterField.getAnnotation(SpecBetween.class);

        if (specBetween != null) {
            return hasProperty(specBetween.left(), entityFields) && hasProperty(specBetween.right(), entityFields);
        }

        return hasProperty(getPropertyName(filterField), entityFields);
    }

    public static Boolean hasProperty(String value, List<Field> entityFields) {
        return entityFields.stream().filter(ef -> {
            if (isDeepProperty(value)) {
                return hasDeepProperty(value, ef);
            }

            return ef.getName().equals(value);
        }).findFirst().isPresent();
    }

    public static Boolean isDeepProperty(String value) {
        return value.contains(".");
    }

    public static Boolean hasDeepProperty(String value, Field entityField) {
        String[] splittedValue = value.split("\\.");

        if (!entityField.getName().equals(splittedValue[0])) {
            return false;
        }

        Integer index = 1;

        do {
            List<Field> deepEntityFields = FieldUtils.getAllFields(entityField.getType());

            if (!hasProperty(splittedValue[index], deepEntityFields)) {
                return false;
            }

            index++;
        } while (index < splittedValue.length);

        return true;
    }

    public static String getPropertyName(Field field) {
        SpecField specField = field.getAnnotation(SpecField.class);

        return specField != null && !StringUtils.isBlank(specField.value())
                ? specField.value()
                : field.getName();
    }

    public static SpecOperation getOperation(Field field) {
        SpecField specField = field.getAnnotation(SpecField.class);

        return specField != null ? specField.operation() : SpecOperation.EQUAL;
    }

    public static String[] getDeepProperties(Field field) {
        return getPropertyName(field).split("\\.");
    }

    public static <T> Path<?> getPath(Root<T> root, String property) {
        if (!isDeepProperty(property)) {
            return root.get(property);
        }

        String[] splittedProperty = property.split("\\.");
        Path<Object> path = root.get(splittedProperty[0]);

        for (int i = 1; i < splittedProperty.length; i++) {
            path = path.get(splittedProperty[i]);
        }

        return path;
    }

    public static <T> Path<?> getPath(Root<T> root, Field field) {
        return getPath(root, getPropertyName(field));
    }

}
