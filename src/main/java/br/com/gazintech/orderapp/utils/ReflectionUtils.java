package br.com.gazintech.orderapp.utils;

import br.com.gazintech.orderapp.exception.ReflectionException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for reflection operations.
 * Provides methods to retrieve annotation values from classes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionUtils {

    /**
     * Retrieves the value of a specified property from an annotation on a class.
     *
     * @param clazz           The class to inspect for the annotation.
     * @param annotationClass The annotation class to look for.
     * @param propertyName    The name of the property whose value is to be retrieved.
     * @return The value of the specified property, or null if the annotation or property does not exist.
     * @throws ReflectionException If there is an error accessing the annotation or its properties.
     */
    public static Object getAnnotationValue(
            Class<?> clazz,
            Class<? extends java.lang.annotation.Annotation> annotationClass,
            String propertyName
    ) throws ReflectionException {
        java.lang.annotation.Annotation annotation = clazz.getAnnotation(annotationClass);
        if (annotation != null) {
            try {
                var method = annotationClass.getDeclaredMethod(propertyName);
                return method.invoke(annotation);
            } catch (ReflectiveOperationException e) {
                throw new ReflectionException(e);
            }
        } else {
            return null;
        }
    }
}