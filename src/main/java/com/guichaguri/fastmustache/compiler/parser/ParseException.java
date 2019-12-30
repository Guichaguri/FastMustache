package com.guichaguri.fastmustache.compiler.parser;

public class ParseException extends RuntimeException {

    public ParseException(String message, int line, int position) {
        super(message + " (line " + line + ":" + position + ")");
    }

}
