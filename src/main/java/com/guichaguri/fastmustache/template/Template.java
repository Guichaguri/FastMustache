package com.guichaguri.fastmustache.template;

/**
 * A template built from any {@link T object}
 *
 * @author Guichaguri
 */
public interface Template<T> {

    String render(T data);

}
