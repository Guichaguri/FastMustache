package com.guichaguri.fastmustache.compiler;

/**
 * Represents an exception caused in FastMustache
 */
public abstract class MustacheException extends Exception {

    public MustacheException(String error) {
        super(error);
    }

    public MustacheException(Throwable throwable) {
        super(throwable);
    }

}
