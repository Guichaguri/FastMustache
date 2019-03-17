package com.guichaguri.fastmustache.parser.tokens;

/**
 * Represents a partial
 */
public class PartialToken extends MustacheToken {

    public String partial;

    @Override
    public String toString() {
        return "PartialToken{" +
                "partial='" + partial + '\'' +
                '}';
    }
}
