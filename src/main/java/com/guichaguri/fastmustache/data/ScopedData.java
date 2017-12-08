package com.guichaguri.fastmustache.data;

import com.guichaguri.fastmustache.template.MustacheLambda;
import com.guichaguri.fastmustache.template.MustacheType;
import com.guichaguri.fastmustache.template.SimpleTemplate;
import com.guichaguri.fastmustache.template.TemplateData;

/**
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
    public String get(String key) {
        return scoped.hasProperty(key) ? scoped.get(key) : original.get(key);
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
    public MustacheLambda getLambda(String key) {
        return scoped.hasProperty(key) ? scoped.getLambda(key) : original.getLambda(key);
    }

    @Override
    public TemplateData getData(String key) {
        return scoped.hasProperty(key) ? scoped.getData(key) : original.getData(key);
    }

    @Override
    public SimpleTemplate getPartial(String key) {
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
