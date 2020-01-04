package com.guichaguri.fastmustache.data;

import com.guichaguri.fastmustache.template.TemplateData;
import com.guichaguri.fastmustache.template.TemplateUtils;

/**
 * A {@link TemplateData} implementation using the object as a simple type,
 * where only the implicit iterator is handled.
 *
 * It will not expose the object fields as properties.
 *
 * @author Guichaguri
 */
public class ImplicitData implements TemplateData {
    private final Object data;

    public ImplicitData(Object data) {
        this.data = data;
    }

    @Override
    public Object get(String key) {
        return TemplateUtils.isImplicitIterator(key) ? data : null;
    }

    @Override
    public boolean hasProperty(String key) {
        return TemplateUtils.isImplicitIterator(key);
    }
}
