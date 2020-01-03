package com.guichaguri.fastmustache.compiler.util.generics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Represents a resolved {@link ParameterizedType}
 */
public class ResolvedGenericType implements ParameterizedType {

    private final ParameterizedType parameter;
    protected final Type[] genericTypes;
    protected Class<?>[] genericClasses;

    public ResolvedGenericType(ParameterizedType parameter) {
        this.parameter = parameter;
        this.genericTypes = parameter.getActualTypeArguments();
    }

    private void rebuildGenericClasses() {
        genericClasses = new Class<?>[genericTypes.length];
        for(int i = 0; i < genericTypes.length; i++) {
            Type type = genericTypes[i];

            if (type instanceof Class<?>) {

                genericClasses[i] = (Class<?>) type;

            } else if (type instanceof WildcardType) {

                for(Type bound : ((WildcardType) type).getUpperBounds()) {
                    if (!(bound instanceof Class<?>)) continue;
                    genericClasses[i] = (Class<?>) bound;
                    break;
                }

            }

        }
    }

    /**
     * Gets the original {@link ParameterizedType}, with no generics resolved
     * @return The original parameterized type
     */
    public ParameterizedType getOriginal() {
        return parameter;
    }

    /**
     * Gets the resolved classes arguments
     * @return The classes
     */
    public Class<?>[] getActualClassArguments() {
        if (genericClasses == null) rebuildGenericClasses();
        return genericClasses;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return genericTypes;
    }

    @Override
    public Type getRawType() {
        return parameter.getRawType();
    }

    @Override
    public Type getOwnerType() {
        return parameter.getOwnerType();
    }

    @Override
    public String getTypeName() {
        StringBuilder builder = new StringBuilder();

        Type owner = parameter.getOwnerType();
        if (owner != null) {
            builder.append(owner.getTypeName()).append('$');
        }

        builder.append(parameter.getRawType().getTypeName())
                .append('<');

        for(int i = 0; i < genericTypes.length; i++) {
            if (i > 0) builder.append(", ");
            builder.append(genericTypes[i].getTypeName());
        }

        return builder.append('>').toString();
    }

    @Override
    public String toString() {
        return getTypeName();
    }

}
