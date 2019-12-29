package com.guichaguri.fastmustache.compiler.bytecode.data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import org.objectweb.asm.Type;

/**
 * Represents a field/method type
 * @author Guichaguri
 */
public class MemberType {

    public final Class<?> clazz, component;
    public final Type clazzType;

    public MemberType(Class<?> clazz, Type clazzType) {
        this(clazz, null, clazzType);
    }

    public MemberType(Class<?> clazz, Class<?> component, Type clazzType) {
        this.clazz = clazz;
        this.component = component;
        this.clazzType = clazzType;
    }

    public MemberType(Field field, Type clazzType) {
        this.clazz = field.getType();
        this.clazzType = clazzType;

        if(clazz.isArray()) {
            component = clazz.getComponentType();
        } else {
            component = (Class<?>)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
        }
    }

    public MemberType(Method method, Type clazzType) {
        this.clazz = method.getReturnType();
        this.clazzType = clazzType;

        if(clazz.isArray()) {
            component = clazz.getComponentType();
        } else {
            component = (Class<?>)((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0];
        }
    }
}
