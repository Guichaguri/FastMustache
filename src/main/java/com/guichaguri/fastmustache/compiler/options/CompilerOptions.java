package com.guichaguri.fastmustache.compiler.options;

/**
 * @author Guichaguri
 */
public class CompilerOptions {

    public static final CompilerOptions DEFAULT = new CompilerOptions();

    private String delimiterLeft = "{{";
    private String delimiterRight = "}}";
    private boolean defaultDelimiters = true;
    private PartialResolver resolver;

    public String getDelimiterLeft() {
        return delimiterLeft;
    }

    public String getDelimiterRight() {
        return delimiterRight;
    }

    public boolean isUsingDefaultDelimiters() {
        return defaultDelimiters;
    }

    public PartialResolver getResolver() {
        return resolver;
    }

    public void setDelimiterLeft(String delimiterLeft) {
        this.delimiterLeft = delimiterLeft;
    }

    public void setDelimiterRight(String delimiterRight) {
        this.delimiterRight = delimiterRight;
    }
}
