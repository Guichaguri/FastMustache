package com.guichaguri.fastmustache.template;

/**
 * Represents a render section
 * @param <T> The section data type
 */
public interface Section<T> {

    /**
     * Renders the section into the {@link StringBuilder}
     * @param builder The builder
     * @param data The data object
     */
    void render(StringBuilder builder, T data);

}
