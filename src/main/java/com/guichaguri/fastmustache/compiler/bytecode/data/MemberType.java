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

    public Class<?> getComponent(Class<?> baseType) {
        if(clazz.isArray()) {
            return clazz.getComponentType();
        } else {
            return GenericResolver.resolve(genericType, baseType).getActualClassArguments()[0];
        }
    }

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
