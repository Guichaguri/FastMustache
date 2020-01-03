package com.guichaguri.fastmustache.compiler.parser.tokens;

import com.guichaguri.fastmustache.compiler.bytecode.CompilerException;
import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator;
import com.guichaguri.fastmustache.compiler.bytecode.data.DataSource;

/**
 * Represents a partial
 */
public class PartialToken extends MustacheToken {

    public String partial;

    @Override
    public void add(BytecodeGenerator generator) throws CompilerException {
        generator.addPartial(partial);
    }

    @Override
    public String toString() {
        return "PartialToken{" +
                "partial='" + partial + '\'' +
                '}';
    }
}
