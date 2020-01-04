package com.guichaguri.fastmustache.template;

/**
 * A template built from any {@link T object}
 *
 * @author Guichaguri
 */
public interface Template<T> {

    /**
     * Renders the template into a string
     * @param data The data object
     * @return The rendered template
     */
    String render(T data);

}
