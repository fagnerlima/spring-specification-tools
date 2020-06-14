package com.github.fagnerlima.springspecificationtools.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Utils for Fields with Reflection
 * @author Fagner Lima
 * @since 0.1.0
 */
public class FieldUtils {

    /**
     * Get all fields from {@code type}, including all fields from your superclass.
     *
     * @param type class type
     * @return all fields from {@code type}
     */
    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();

        for (Class<?> t = type; t != null; t = t.getSuperclass()) {
            fields.addAll(List.of(t.getDeclaredFields()));
        }

        return fields;
    }

    /**
     * Get the field from {@code type} annotated with {@code annotationClass}.
     *
     * @param type class type
     * @param annotationClass annotation type
     * @return the field annotated with {@code annotationClass}
     * @throws NoSuchFieldException if the field is not found
     */
    public static Field getField(Class<?> type, Class<? extends Annotation> annotationClass) throws NoSuchFieldException {
        List<Field> fields = getAllFields(type);

        return fields.stream().filter(field -> field.getAnnotation(annotationClass) != null).findFirst()
                .orElseThrow(() -> new NoSuchFieldException(annotationClass.getName()));
    }

    /**
     * Get the getter method of the field with name {@code propertyName} from {@code type}.
     *
     * @param propertyName property name
     * @param type class type
     * @return the getter method
     * @throws IntrospectionException if an exception occurs during introspection
     */
    public static Method findGetterMethod(String propertyName, Class<?> type) throws IntrospectionException {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, type);

        return propertyDescriptor.getReadMethod();
    }

    /**
     * Get the setter method of the field with name {@code propertyName} from {@code type}.
     *
     * @param propertyName property name
     * @param type class type
     * @return the setter method
     * @throws IntrospectionException if an exception occurs during introspection
     */
    public static Method findSetterMethod(String propertyName, Class<?> type) throws IntrospectionException {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, type);

        return propertyDescriptor.getWriteMethod();
    }

}
