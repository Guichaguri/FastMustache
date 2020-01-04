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

    public static MustacheType getByClass(Class<?> c) {
        if(c.isPrimitive()) {
            if(c == boolean.class) {
                return BOOLEAN;
            }

            // Any primitive can be converted into a string
            return STRING;
        }

        if(Boolean.class.isAssignableFrom(c)) {
            return BOOLEAN;
        } else if(MustacheLambda.class.isAssignableFrom(c)) {
            return LAMBDA;
        } else if(CharSequence.class.isAssignableFrom(c) || Number.class.isAssignableFrom(c)) {
            return STRING;
        } else if(c.isArray() || Collection.class.isAssignableFrom(c)) {
            return ARRAY;
        } else if(Template.class.isAssignableFrom(c)) {
            return PARTIAL;
        } else {
            // Any other object can be treated as data
            return DATA;
        }
    }

    public static MustacheType getByObject(Object o) {
        if(o == null || o instanceof Boolean) {
            // null values are considered false
            return BOOLEAN;
        } else if(o instanceof MustacheLambda) {
            return LAMBDA;
        } else if(o instanceof CharSequence || o instanceof Number) {
            return STRING;
        } else if(o.getClass().isArray() || o instanceof Collection) {
            return ARRAY;
        } else if (o instanceof Template) {
            return PARTIAL;
        } else {
            // Any other object can be treated as data
            return DATA;
        }
    }

}
