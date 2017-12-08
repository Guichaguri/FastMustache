package com.guichaguri.fastmustache.template;

/**
 * @author Guichaguri
 */
public enum MustacheType {

    /**
     * Represents a string or anything else that can be represented as a string
     */
    STRING,

    /**
     * Represents a boolean or anything else that can be parsed as a boolean
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
     * Unknown type
     */
    UNKNOWN

}
