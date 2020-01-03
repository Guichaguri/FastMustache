package com.guichaguri.fastmustache.compiler.parser.tokens;

import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator;
import com.guichaguri.fastmustache.compiler.bytecode.CompilerException;

public abstract class MustacheToken {

    public int line, position;
    public SectionToken parent;

    /**
     * Converts this token to Java Bytecode
     * @param generator The bytecode generator
     * @throws CompilerException Whether it wasn't possible to generate the bytecode
     */
    public abstract void add(BytecodeGenerator generator) throws CompilerException;

}
