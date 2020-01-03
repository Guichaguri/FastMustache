package com.guichaguri.fastmustache.compiler.parser.tokens;

import com.guichaguri.fastmustache.compiler.bytecode.CompilerException;
import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator;
import com.guichaguri.fastmustache.compiler.bytecode.data.DataSource;

/**
 * Represents a variable
 */
public class VariableToken extends MustacheToken {

    public String variable;
    public boolean escaped;

    @Override
    public void add(BytecodeGenerator generator) throws CompilerException {
        generator.addVariable(variable, escaped);
    }

    @Override
    public String toString() {
        return "VariableToken{" +
                "variable='" + variable + '\'' +
                ", escaped=" + escaped +
                '}';
    }
}
