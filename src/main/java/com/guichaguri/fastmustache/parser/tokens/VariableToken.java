package com.guichaguri.fastmustache.parser.tokens;

/**
 * Represents a variable
 */
public class VariableToken extends MustacheToken {

    public String variable;
    public boolean escaped;

    @Override
    public String toString() {
        return "VariableToken{" +
                "variable='" + variable + '\'' +
                ", escaped=" + escaped +
                '}';
    }
}
