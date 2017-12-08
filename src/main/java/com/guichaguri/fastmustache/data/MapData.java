package com.guichaguri.fastmustache.data;

import com.guichaguri.fastmustache.template.MustacheLambda;
import com.guichaguri.fastmustache.template.MustacheType;
import com.guichaguri.fastmustache.template.SimpleTemplate;
import com.guichaguri.fastmustache.template.TemplateData;
import com.guichaguri.fastmustache.template.TemplateUtils;
import java.lang.reflect.Array;
import java.util.Map;

/**
 * @author Guichaguri
 */
public class MapData implements TemplateData {

    private final Map<String, Object> map;

    public MapData(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public String getUnescaped(String key) {
        Object o = map.get(key);
        if(o == null) return null;

        return o.toString();
    }

    @Override
    public boolean getBoolean(String key) {
        Object o = map.get(key);

        if(o == null) {
            return false;
        } else if(o instanceof Boolean) {
            return (Boolean)o;
        } else {
            return Boolean.parseBoolean(o.toString());
        }
    }

    @Override
    public TemplateData[] getArray(String key) {
        Object o = map.get(key);
        if(o == null || !o.getClass().isArray()) {
            return null;
        }

        int length = Array.getLength(o);
        TemplateData[] data = new TemplateData[length];

        for(int i = 0; i < length; i++) {
            data[i] = TemplateUtils.fromObject(Array.get(o, i));
        }

        return data;
    }

    @Override
    public MustacheLambda getLambda(String key) {
        Object o = map.get(key);
        if(o == null) return null;

        if(o instanceof MustacheLambda) {
            return (MustacheLambda)o;
        }
        return null;
    }

    @Override
    public TemplateData getData(String key) {
        return TemplateUtils.fromObject(map.get(key));
    }

    @Override
    public SimpleTemplate getPartial(String key) {
        return null;//TODO
    }

    @Override
    public MustacheType getType(String key) {
        Object c = map.get(key);

         if(c == null || c instanceof Boolean) {
            return MustacheType.BOOLEAN;
        } else if(c instanceof MustacheLambda) {
            return MustacheType.LAMBDA;
        } else if(c instanceof CharSequence || c instanceof Number) {
            return MustacheType.STRING;
        } else if(c instanceof TemplateData || c instanceof Map) {
            return MustacheType.DATA;
        } else if(c.getClass().isArray()) {
            return MustacheType.ARRAY;
        }

        return MustacheType.STRING;
    }

    @Override
    public boolean hasProperty(String key) {
        return map.containsKey(key);
    }
}
