package com.guichaguri.fastmustache.compiler;

/**
 * @author Guichaguri
 */
public class CompilerException extends Exception {

    public CompilerException(String error) {
        super(error);
    }

    public CompilerException(Throwable t) {
        super(t);
    }

}
