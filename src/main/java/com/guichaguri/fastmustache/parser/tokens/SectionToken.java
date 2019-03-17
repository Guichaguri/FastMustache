package com.guichaguri.fastmustache.parser.tokens;

import java.util.List;

/**
 * Represents a section (condition, loop, lambda)
 */
public class SectionToken extends MustacheToken {

    public String variable;
    public boolean inverted;
    public List<MustacheToken> content;

    @Override
    public String toString() {
        return "SectionToken{" +
                "variable='" + variable + '\'' +
                ", inverted=" + inverted +
                ", content=" + content +
                '}';
    }
}
