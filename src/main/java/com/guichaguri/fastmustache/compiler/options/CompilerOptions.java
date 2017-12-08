package com.guichaguri.fastmustache.compiler;

/**
 * @author Guichaguri
 */
public class CompilerOptions {

    public static final CompilerOptions DEFAULT = new CompilerOptions();

    private String delimiterLeft = "{{";
    private String delimiterRight = "}}";

    public String getDelimiterLeft() {
        return delimiterLeft;
    }

    public String getDelimiterRight() {
        return delimiterRight;
    }

    public void setDelimiterLeft(String delimiterLeft) {
        this.delimiterLeft = delimiterLeft;
    }

    public void setDelimiterRight(String delimiterRight) {
        this.delimiterRight = delimiterRight;
    }
}