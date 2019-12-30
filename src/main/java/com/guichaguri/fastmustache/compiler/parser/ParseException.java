package com.guichaguri.fastmustache.compiler.parser;

import com.guichaguri.fastmustache.compiler.MustacheException;

/**
 * Represents an exception thrown by the parser
 */
public class ParseException extends MustacheException {

    private final int line, position;

    public ParseException(String message, int line, int position) {
        super(message + " (line " + line + ":" + position + ")");
        this.line = line;
        this.position = position;
    }

    public int getLine() {
        return line;
    }

    public int getPosition() {
        return position;
    }

}
