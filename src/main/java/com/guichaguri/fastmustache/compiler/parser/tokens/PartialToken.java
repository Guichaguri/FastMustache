package com.guichaguri.fastmustache.compiler.parser.tokens;

import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator2;
import com.guichaguri.fastmustache.compiler.bytecode.data.DataManager;

/**
 * Represents a partial
 */
public class PartialToken extends MustacheToken {

    public String partial;

    @Override
    public void add(BytecodeGenerator2 generator, DataManager data) {
        generator.addPartial(partial);
    }

    @Override
    public String toString() {
        return "PartialToken{" +
                "partial='" + partial + '\'' +
                '}';
    }
}
