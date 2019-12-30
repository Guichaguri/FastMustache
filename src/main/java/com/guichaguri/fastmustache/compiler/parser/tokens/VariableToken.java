package com.guichaguri.fastmustache.compiler.parser.tokens;

import com.guichaguri.fastmustache.compiler.CompilerException;
import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator2;
import com.guichaguri.fastmustache.compiler.bytecode.data.DataManager;

/**
 * Represents a variable
 */
public class VariableToken extends MustacheToken {

    public String variable;
    public boolean escaped;

    @Override
    public void add(BytecodeGenerator2 generator, DataManager data) throws CompilerException {
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
