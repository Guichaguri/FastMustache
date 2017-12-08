package com.guichaguri.fastmustache.template;

/**
 * A template built with {@link TemplateData}
 *
 * @author Guichaguri
 */
public interface SimpleTemplate extends Template<TemplateData> {

    @Override
    String render(TemplateData data);

}
