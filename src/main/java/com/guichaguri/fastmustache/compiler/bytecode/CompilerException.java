package com.guichaguri.fastmustache.compiler.bytecode;

import com.guichaguri.fastmustache.compiler.MustacheException;

/**
 * Represents an exception thrown by the compiler
 */
public class CompilerException extends MustacheException {

    public CompilerException(String error) {
        super(error);
    }

    public CompilerException(Throwable throwable) {
        super(throwable);
    }

}
