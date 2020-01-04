package com.guichaguri.fastmustache.data;

import com.guichaguri.fastmustache.template.TemplateData;
import java.util.Map;

/**
 * A {@link TemplateData} implementation using a {@link Map}.
 *
 * @author Guichaguri
 */
public class MapData implements TemplateData {

    private final Map<String, Object> map;

    public MapData(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public Object get(String key) {
        return map.get(key);
    }

    @Override
    public boolean hasProperty(String key) {
        return map.containsKey(key);
    }
}
