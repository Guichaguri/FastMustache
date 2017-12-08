package com.guichaguri.fastmustache.template;

/**
 * @author Guichaguri
 */
@FunctionalInterface
public interface MustacheLambda {

    void render(StringBuilder builder, SimpleTemplate template, TemplateData data);

}
