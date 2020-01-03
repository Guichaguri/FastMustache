package com.guichaguri.fastmustache.compiler.util.generics;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Walks through a hierarchy of classes and resolves generic types
 */
public class GenericResolver {

    /**
     * Resolves the base type generics using the type as the starting point
     * @param type The type
     * @param baseType The base type
     * @return The resolved {@link ParameterizedType}
     */
    public static ResolvedGenericType resolve(Type type, Class<?> baseType) {
        GenericResolver resolver = new GenericResolver(baseType);
        resolver.walk(type);
        return resolver.baseType;
    }

    private final Class<?> baseClass;
    private List<ResolvedGenericType> types = new ArrayList<>();
    private ResolvedGenericType baseType;

    private GenericResolver(Class<?> baseClass) {
        this.baseClass = baseClass;
    }

    private boolean walk(Type type) {
        if (type == null) return false;

        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;

            if (baseClass == clazz || !baseClass.isAssignableFrom(clazz)) return false;

            for(Type itf : clazz.getGenericInterfaces()) {
                if (walk(itf)) return true;
            }

            Type superClass = clazz.getGenericSuperclass();
            return walk(superClass);
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType param = (ParameterizedType) type;
            ResolvedGenericType ctx = new ResolvedGenericType(param);
            types.add(ctx);

            boolean found = walk(param.getRawType());

            if (!found) {
                baseType = ctx;
                replaceGenerics(ctx.genericTypes);
                return true;
            }

            return found;
        }

        throw new RuntimeException("The " + type.getClass() + " is not supported.");
    }

    private void replaceGenerics(Type[] arguments) {
        for(int i = 0; i < arguments.length; i++) {
            Type argument = arguments[i];

            if (argument instanceof TypeVariable) {

                TypeVariable<?> typeVariable = (TypeVariable<?>) argument;
                GenericDeclaration declaration = typeVariable.getGenericDeclaration();
                ResolvedGenericType ctx = findContext(declaration);

                if (ctx == null) {
                    // No generic declaration found - we'll analyse its bounds instead
                    Type[] bounds = typeVariable.getBounds();

                    if (bounds.length > 0) {
                        replaceGenerics(bounds);
                        arguments[i] = bounds[0];
                    }

                    continue;
                }

                // We'll go through and validate all generics
                replaceGenerics(ctx.genericTypes);

                TypeVariable<?>[] parameters = declaration.getTypeParameters();
                String name = typeVariable.getName();

                // Replace each type variable with the appropriate type
                for(int o = 0; o < parameters.length; o++) {
                    if(!name.equals(parameters[o].getName())) continue;
                    arguments[i] = ctx.getActualTypeArguments()[o];
                    break;
                }

            }

        }
    }

    private ResolvedGenericType findContext(GenericDeclaration declaration) {
        for(ResolvedGenericType ctx : types) {
            if (ctx.getRawType() == declaration) return ctx;
        }
        return null;
    }

}
