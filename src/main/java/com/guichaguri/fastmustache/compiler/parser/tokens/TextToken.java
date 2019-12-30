package com.guichaguri.fastmustache.compiler.parser.tokens;

import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator2;
import com.guichaguri.fastmustache.compiler.bytecode.data.DataManager;

/**
 * Represents raw string
 */
public class TextToken extends MustacheToken {

    public String text;

    @Override
    public void add(BytecodeGenerator2 generator, DataManager data) {
        generator.addText(text);
    }

    @Override
    public String toString() {
        return "TextToken{" +
                "text='" + text + '\'' +
                '}';
    }
}
