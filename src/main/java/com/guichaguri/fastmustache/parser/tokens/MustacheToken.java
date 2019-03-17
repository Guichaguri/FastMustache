package com.guichaguri.fastmustache.parser.tokens;

public abstract class MustacheToken {

    public int line, position;
    public SectionToken parent;

}
