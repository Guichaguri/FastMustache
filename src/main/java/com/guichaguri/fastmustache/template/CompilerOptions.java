package com.guichaguri.fastmustache.template;

/**
 * @author Guichaguri
 */
public class CompilerOptions {

    public static final CompilerOptions DEFAULT = new CompilerOptions();

    private String delimiterLeft = "{{";
    private String delimiterRight = "}}";
    private boolean escapingEnabled = true;
    private PartialResolver resolver;

    public String getDelimiterLeft() {
        return delimiterLeft;
    }

    public String getDelimiterRight() {
        return delimiterRight;
    }

    public PartialResolver getResolver() {
        return resolver;
    }

    public boolean isEscapingEnabled() {
        return escapingEnabled;
    }

    public void setEscapingEnabled(boolean escapingEnabled) {
        this.escapingEnabled = escapingEnabled;
    }

    public void setDelimiterLeft(String delimiterLeft) {
        this.delimiterLeft = delimiterLeft;
    }

    public void setDelimiterRight(String delimiterRight) {
        this.delimiterRight = delimiterRight;
    }
}
