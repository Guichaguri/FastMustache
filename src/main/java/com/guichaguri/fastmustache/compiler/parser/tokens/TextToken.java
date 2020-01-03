package com.guichaguri.fastmustache.compiler.parser.tokens;

import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator;
import com.guichaguri.fastmustache.compiler.bytecode.data.DataSource;
import java.util.List;

/**
 * Represents raw string
 */
public class TextToken extends MustacheToken {

    /**
     * Gets the minimum length in text of the template.
     * Ignores text inside sections as they are not guaranteed to be included.
     * Also ignores variables and partials as their length is not predictable.
     * @param tokens The token list
     * @return The minimum length
     */
    public static int getMinimumLength(List<MustacheToken> tokens) {
        int minLength = 0;

        for(MustacheToken token : tokens) {
            if (token instanceof TextToken) {
                minLength += ((TextToken) token).text.length();
            }
        }

        return minLength;
    }

    public String text;

    @Override
    public void add(BytecodeGenerator generator) {
        generator.addText(text);
    }

    @Override
    public String toString() {
        return "TextToken{" +
                "text='" + text + '\'' +
                '}';
    }
}
