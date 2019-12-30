package com.guichaguri.fastmustache.compiler.parser.tokens;

import com.guichaguri.fastmustache.compiler.bytecode.CompilerException;
import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator2;
import com.guichaguri.fastmustache.compiler.bytecode.data.DataManager;

public abstract class MustacheToken {

    public int line, position;
    public SectionToken parent;

    /**
     * Converts this token to Java Bytecode
     * @param generator The bytecode generator
     * @param data The bytecode data getter
     * @throws CompilerException Whether it wasn't possible to generate the bytecode
     */
    public abstract void add(BytecodeGenerator2 generator, DataManager data) throws CompilerException;

}
