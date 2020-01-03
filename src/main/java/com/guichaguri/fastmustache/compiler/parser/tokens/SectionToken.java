package com.guichaguri.fastmustache.compiler.parser.tokens;

import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator;
import com.guichaguri.fastmustache.compiler.bytecode.CompilerException;
import com.guichaguri.fastmustache.compiler.bytecode.data.DataSource;
import java.util.List;

/**
 * Represents a section (condition, loop, lambda)
 */
public class SectionToken extends MustacheToken {

    public String variable;
    public boolean inverted;
    public List<MustacheToken> content;

    @Override
    public void add(BytecodeGenerator generator) throws CompilerException {
        generator.addSection(this);
    }

    @Override
    public String toString() {
        return "SectionToken{" +
                "variable='" + variable + '\'' +
                ", inverted=" + inverted +
                ", content=" + content +
                '}';
    }
}
