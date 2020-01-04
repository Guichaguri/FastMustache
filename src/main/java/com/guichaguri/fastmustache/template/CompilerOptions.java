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
    private boolean variableNullChecksEnabled = false; // Whether it will null check variables before
    private boolean booleanNullChecksEnabled = false;
    private boolean arrayNullChecksEnabled = false;
    private boolean partialNullChecksEnabled = false;
    private boolean explodeDataSectionProperties = false; // Whether it will explode data objects in non-null sections

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

    public boolean isVariableNullChecksEnabled() {
        return variableNullChecksEnabled;
    }

    public void setVariableNullChecksEnabled(boolean variableNullChecksEnabled) {
        this.variableNullChecksEnabled = variableNullChecksEnabled;
    }

    public boolean isBooleanNullChecksEnabled() {
        return booleanNullChecksEnabled;
    }

    public void setBooleanNullChecksEnabled(boolean booleanNullChecksEnabled) {
        this.booleanNullChecksEnabled = booleanNullChecksEnabled;
    }

    public boolean isArrayNullChecksEnabled() {
        return arrayNullChecksEnabled;
    }

    public void setArrayNullChecksEnabled(boolean arrayNullChecksEnabled) {
        this.arrayNullChecksEnabled = arrayNullChecksEnabled;
    }

    public boolean isPartialNullChecksEnabled() {
        return partialNullChecksEnabled;
    }

    public void setPartialNullChecksEnabled(boolean partialNullChecksEnabled) {
        this.partialNullChecksEnabled = partialNullChecksEnabled;
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

    public boolean isExplodeDataSectionProperties() {
        return explodeDataSectionProperties;
    }

    public void setExplodeDataSectionProperties(boolean explodeDataSectionProperties) {
        this.explodeDataSectionProperties = explodeDataSectionProperties;
    }
}
