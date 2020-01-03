package com.guichaguri.fastmustache.template;

/**
 * @author Guichaguri
 */
@FunctionalInterface
public interface MustacheLambda<T> {

    void render(StringBuilder builder, Section<T> template, T data);

}
