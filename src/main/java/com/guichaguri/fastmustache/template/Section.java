package com.guichaguri.fastmustache.template;

public interface Section<T> {

    void render(StringBuilder builder, T data);

}
