package com.guichaguri.fastmustache.data;

import com.guichaguri.fastmustache.template.MustacheLambda;
import com.guichaguri.fastmustache.template.MustacheType;
import com.guichaguri.fastmustache.template.SimpleTemplate;
import com.guichaguri.fastmustache.template.TemplateData;
import com.guichaguri.fastmustache.template.TemplateUtils;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * @author Guichaguri
 */
public class ObjectData implements TemplateData {
    private final Object obj;
    private final Class clazz;

    public ObjectData(Object obj) {
        this.obj = obj;
        this.clazz = obj.getClass();
    }

    @Override
    public String getUnescaped(String key) {
        try {
            Object o = clazz.getField(key).get(obj);
            if(o == null) return null;
            return o.toString();
        } catch(Exception ex) {
            return null;
        }
    }

    @Override
    public boolean getBoolean(String key) {
        try {
            return clazz.getField(key).getBoolean(obj);
        } catch(Exception ex) {
            return false;
        }
    }

    @Override
    public TemplateData[] getArray(String key) {
        try {
            Object o = clazz.getField(key).get(obj);
            if(o == null) return null;

            int length = Array.getLength(o);
            TemplateData[] data = new TemplateData[length];

            for(int i = 0; i < length; i++) {
                data[i] = TemplateUtils.fromObject(Array.get(o, i));
            }

            return data;
        } catch(Exception ex) {
            return null;
        }
    }

    @Override
    public MustacheLambda getLambda(String key) {
        try {
            Object o = clazz.getField(key).get(obj);
            if(o == null) return null;
            return (MustacheLambda)o;
        } catch(Exception ex) {
            return null;
        }
    }

    @Override
    public TemplateData getData(String key) {
        try {
            return TemplateUtils.fromObject(clazz.getField(key).get(obj));
        } catch(Exception ex) {
            return null;
        }
    }

    @Override
    public SimpleTemplate getPartial(String key) {
        return null;//TODO
    }

    @Override
    public MustacheType getType(String key) {
        try {
            Field field = clazz.getField(key);
            Object value = field.get(obj);

            if(value == null) {
                return MustacheType.BOOLEAN;
            } else if(value instanceof MustacheLambda) {
                return MustacheType.LAMBDA;
            } else if(value instanceof Boolean) {
                return MustacheType.BOOLEAN;
            } else if(value instanceof CharSequence || value instanceof Number) {
                return MustacheType.STRING;
            } else if(field.getType().isArray()) {
                return MustacheType.ARRAY;
            } else {
                return MustacheType.DATA;
            }
        } catch(Exception ex) {
            return MustacheType.UNKNOWN;
        }
    }

    @Override
    public boolean hasProperty(String key) {
        try {
            clazz.getField(key);
            return true;
        } catch(Exception ex) {
            return false;
        }
    }
}
