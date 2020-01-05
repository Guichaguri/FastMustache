package com.guichaguri.fastmustache.compiler.util.resolver;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Resolves fields and methods from a class.
 *
 * Matches methods prefixed with "get" and "is".
 * Allows nested paths (such as {@code user.profile.name}, which could match {@code data.getUser().profile.name()})
 */
public class ClassMemberResolver {

    private static ClassMemberResolver singleton = null;

    public static ClassMemberResolver getInstance() {
        if (singleton == null) singleton = new ClassMemberResolver();
        return singleton;
    }

    protected ClassMemberResolver() {

    }

    /**
     * Whether the given class is a valid POJO
     * @param clazz The class
     * @return Whether it is a POJO and valid
     */
    public boolean isValid(Class<?> clazz) {
        return !clazz.isPrimitive() && !clazz.isArray();
    }

    /**
     * Whether the specified field can be used
     * @param field The field
     * @return Whether the field is valid
     */
    protected boolean isValid(Field field) {
        return true;
    }

    /**
     * Whether the specified method can be used
     * @param method The method
     * @return Whether the method is valid
     */
    protected boolean isValid(Method method) {
        // The method must have a return type and must not need any parameters
        return method.getReturnType() != void.class && method.getParameterTypes().length == 0;
    }

    /**
     * Finds the object path through fields and getters
     * @param context The start class
     * @param key The path
     * @return The list of members composing this path or {@code null} if the path is invalid
     */
    public Member[] findPath(Class<?> context, String key) {
        String[] path = key.split("\\.");
        Member[] members = new Member[path.length];

        for(int i = 0; i < path.length; i++) {
            String k = path[i];

            if (!isValid(context)) {
                return null;
            }

            Method m = getMethod(context, k);
            if(m != null) {
                members[i] = m;
                context = m.getReturnType();
                continue;
            }

            Field f = getField(context, k);
            if(f != null) {
                members[i] = f;
                context = f.getType();
                continue;
            }

            return null;
        }

        return members;
    }

    /**
     * Tries to find a field with the specified name
     * @param clazz The class to look into
     * @param key The field name
     * @return The field or {@code null}
     */
    protected Field getField(Class<?> clazz, String key) {
        try {
            Field f = clazz.getField(key);
            if (isValid(f)) return f;
        } catch(Exception ignored) {}

        return null;
    }

    /**
     * Tries to find a getter method with the specified name.
     *
     * It will return methods with the exact name, prefixed with "get" or prefixed with "is"
     *
     * @param clazz The class to look into
     * @param key The method name
     * @return The method or {@code null}
     */
    protected Method getMethod(Class<?> clazz, String key) {
        Method m;

        // Exact name
        try {
            m = clazz.getMethod(key);
            if (isValid(m)) return m;
        } catch(Exception ignored) {}

        String camelCase = Character.toUpperCase(key.charAt(0)) + key.substring(1);

        // Prefixed with "get"
        try {
            m = clazz.getMethod("get" + camelCase);
            if (isValid(m)) return m;
        } catch(Exception ignored) {}

        // Prefixed with "is"
        try {
            m = clazz.getMethod("is" + camelCase);
            if (isValid(m)) return m;
        } catch(Exception ignored) {}

        return null;
    }

}
