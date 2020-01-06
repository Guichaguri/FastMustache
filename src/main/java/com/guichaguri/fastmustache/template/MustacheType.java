package com.guichaguri.fastmustache.template;

import java.util.Collection;
import java.util.Map;

/**
 * @author Guichaguri
 */
public enum MustacheType {

    /**
     * Represents a string or anything else that can be represented as a string
     */
    STRING,

    /**
     * Represents a boolean or anything else that can be parsed as a boolean.
     * Also means this boolean can be converted to a string.
     */
    BOOLEAN,

    /**
     * Represents a data structure (a object) or a map
     */
    DATA,

    /**
     * Represents an array or a collection
     */
    ARRAY,

    /**
     * Represents a lambda
     */
    LAMBDA,

    /**
     * Represents another template
     */
    PARTIAL,

    /**
     * Unknown type
     *
     * Avoid using it whenever possible, as it makes the bytecode bigger and less efficient.
     */
    UNKNOWN;

    /**
     * Gets the equivalent type from a class
     * @param clazz The class
     * @return The type
     */
    public static MustacheType getByClass(Class<?> clazz) {
        if(clazz.isPrimitive()) {
            if(clazz == boolean.class) {
                return BOOLEAN;
            }

            // Any other primitive can be converted into a string
            return STRING;
        }

        if(Boolean.class.isAssignableFrom(clazz)) {
            return BOOLEAN;
        } else if(MustacheLambda.class.isAssignableFrom(clazz)) {
            return LAMBDA;
        } else if(CharSequence.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz) || clazz == Character.class) {
            return STRING;
        } else if(clazz.isArray() || Collection.class.isAssignableFrom(clazz)) {
            return ARRAY;
        } else if(Template.class.isAssignableFrom(clazz)) {
            return PARTIAL;
        } else {
            // Any other object can be treated as data
            return DATA;
        }
    }

    /**
     * Gets the equivalent type from an object
     * @param obj The object
     * @return The type
     */
    public static MustacheType getByObject(Object obj) {
        if(obj == null || obj instanceof Boolean) {
            // null values are considered false
            return BOOLEAN;
        } else if(obj instanceof MustacheLambda) {
            return LAMBDA;
        } else if(obj instanceof CharSequence || obj instanceof Number || obj instanceof Character) {
            return STRING;
        } else if(obj.getClass().isArray() || obj instanceof Collection) {
            return ARRAY;
        } else if (obj instanceof Template) {
            return PARTIAL;
        } else {
            // Any other object can be treated as data
            return DATA;
        }
    }

}
