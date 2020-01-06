package com.guichaguri.fastmustache.compiler.bytecode.data;

import com.guichaguri.fastmustache.compiler.util.generics.GenericResolver;
import org.objectweb.asm.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 * Represents a field/method type
 * @author Guichaguri
 */
public class MemberType {

    public final Class<?> clazz;
    public final Type clazzType;
    public final java.lang.reflect.Type genericType;

    public MemberType(Class<?> clazz, Type clazzType) {
        this.clazz = clazz;
        this.clazzType = clazzType;
        this.genericType = null;
    }

    public MemberType(Field field, Type clazzType) {
        this.clazz = field.getType();
        this.clazzType = clazzType;
        this.genericType = field.getGenericType();
    }

    public MemberType(Method method, Type clazzType) {
        this.clazz = method.getReturnType();
        this.clazzType = clazzType;
        this.genericType = method.getGenericReturnType();
    }

    /**
     * Gets the component class from base type class.
     *
     * This won't work for Java 8 lambdas or when the generics types are not specified.
     * It will work on arrays, nested argument variables and wildcard arguments.
     *
     * It will return {@code null} or {@link Object} when the type couldn't be resolved properly.
     *
     * @param baseType The base class
     * @return The argument type
     */
    public Class<?> getComponent(Class<?> baseType) {
        if(clazz.isArray()) {
            return clazz.getComponentType();
        } else {
            return GenericResolver.resolve(genericType, baseType).getActualClassArguments()[0];
        }
    }

    /**
     * Gets the component class.
     *
     * This won't work for Java 8 lambdas or when the generics types are not directly specified.
     * It will work on arrays, but not on nested argument variables and wildcard arguments.
     *
     * @return The argument type
     */
    public Class<?> getComponent() {
        if(clazz.isArray()) {
            return clazz.getComponentType();
        } else {
            return (Class<?>)((ParameterizedType)genericType).getActualTypeArguments()[0];
        }
    }

    @Override
    public String toString() {
        return clazz.toString();
    }

}
