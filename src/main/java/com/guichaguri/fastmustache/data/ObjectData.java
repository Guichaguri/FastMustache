package com.guichaguri.fastmustache.data;

import com.guichaguri.fastmustache.template.TemplateData;
import com.guichaguri.fastmustache.template.TemplateUtils;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link TemplateData} implementation using object fields through reflection.
 *
 * @author Guichaguri
 */
public class ObjectData implements TemplateData {
    private final Object obj;
    private final Class<?> clazz;
    private final Map<String, Field> fields = new HashMap<>();

    public ObjectData(Object obj) {
        this.obj = obj;
        this.clazz = obj.getClass();
    }

    private Field getField(String key) {
        // Uses cached fields to improve performance
        if (fields.containsKey(key)) {
            return fields.get(key);
        }

        try {
            Field field = clazz.getField(key);
            fields.put(key, field);
            return field;
        } catch(Exception ex) {
            fields.put(key, null);
            return null;
        }
    }

    @Override
    public Object get(String key) {
        if (TemplateUtils.isImplicitIterator(key)) return obj;

        try {
            Field field = getField(key);
            return field == null ? null : field.get(obj);
        } catch(Exception ex) {
            return null;
        }
    }

    @Override
    public boolean hasProperty(String key) {
        return TemplateUtils.isImplicitIterator(key) || getField(key) != null;
    }
}
