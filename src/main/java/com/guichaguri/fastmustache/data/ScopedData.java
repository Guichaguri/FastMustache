package com.guichaguri.fastmustache.data;

import com.guichaguri.fastmustache.template.*;

/**
 * Implements a {@link TemplateData} under a scope.
 * Also used internally to render templates.
 *
 * @author Guichaguri
 */
public class ScopedData implements TemplateData {
    private final TemplateData original;
    private final TemplateData scoped;

    public ScopedData(TemplateData original, TemplateData scoped) {
        this.original = original;
        this.scoped = scoped;
    }

    @Override
    public Object get(String key) {
        return scoped.hasProperty(key) ? scoped.get(key) : original.get(key);
    }

    @Override
    public String getEscaped(String key) {
        return scoped.hasProperty(key) ? scoped.getEscaped(key) : original.getEscaped(key);
    }

    @Override
    public String getUnescaped(String key) {
        return scoped.hasProperty(key) ? scoped.getUnescaped(key) : original.getUnescaped(key);
    }

    @Override
    public boolean getBoolean(String key) {
        return scoped.hasProperty(key) ? scoped.getBoolean(key) : original.getBoolean(key);
    }

    @Override
    public TemplateData[] getArray(String key) {
        return scoped.hasProperty(key) ? scoped.getArray(key) : original.getArray(key);
    }

    @Override
    public MustacheLambda<TemplateData> getLambda(String key) {
        return scoped.hasProperty(key) ? scoped.getLambda(key) : original.getLambda(key);
    }

    @Override
    public TemplateData getData(String key) {
        return scoped.hasProperty(key) ? scoped.getData(key) : original.getData(key);
    }

    @Override
    public Template<TemplateData> getPartial(String key) {
        return scoped.hasProperty(key) ? scoped.getPartial(key) : original.getPartial(key);
    }

    @Override
    public MustacheType getType(String key) {
        return scoped.hasProperty(key) ? scoped.getType(key) : original.getType(key);
    }

    @Override
    public boolean hasProperty(String key) {
        return scoped.hasProperty(key) || original.hasProperty(key);
    }

}
