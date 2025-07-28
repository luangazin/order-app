package br.com.gazintech.orderapp.utils;

import br.com.gazintech.orderapp.exception.ReflectionException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionUtils {

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