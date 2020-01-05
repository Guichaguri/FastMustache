package com.guichaguri.fastmustache.compiler.util.resolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Exactly the same as {@link ClassMemberResolver} but require all members to have the specified annotation.
 */
public class AnnotatedMemberResolver extends ClassMemberResolver {

    private final Class<? extends Annotation> annotationClass;

    public AnnotatedMemberResolver(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    @Override
    protected boolean isValid(Field field) {
        return field.isAnnotationPresent(annotationClass) && super.isValid(field);
    }

    @Override
    protected boolean isValid(Method method) {
        return method.isAnnotationPresent(annotationClass) && super.isValid(method);
    }

}
