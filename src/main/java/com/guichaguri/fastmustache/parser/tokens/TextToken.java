package com.guichaguri.fastmustache.parser.tokens;

/**
 * Represents raw string
 */
public class TextToken extends MustacheToken {

    public String text;

    @Override
    public String toString() {
        return "TextToken{" +
                "text='" + text + '\'' +
                '}';
    }
}
