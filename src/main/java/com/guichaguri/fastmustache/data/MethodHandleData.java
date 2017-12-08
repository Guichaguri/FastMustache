package com.guichaguri.fastmustache.data;

import com.guichaguri.fastmustache.template.MustacheLambda;
import com.guichaguri.fastmustache.template.MustacheType;
import com.guichaguri.fastmustache.template.SimpleTemplate;
import com.guichaguri.fastmustache.template.TemplateData;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

/**
 * @author Guichaguri
 */
public class MethodHandleData implements TemplateData {
    private final Object obj;
    private final MethodHandle[] fields;

    public MethodHandleData(Object obj) throws IllegalAccessException {
        this.obj = obj;

        Lookup lookup = MethodHandles.lookup();
        Field[] reflectFields = obj.getClass().getFields();
        fields = new MethodHandle[reflectFields.length];

        for(int i = 0; i < reflectFields.length; i++) {
            fields[i] = lookup.unreflectGetter(reflectFields[i]);
        }
    }

    public MethodHandleData(Object obj, MethodHandle[] fields) {
        this.obj = obj;
        this.fields = fields;
    }

    @Override
    public String getUnescaped(String key) {
        // o = fields[0].invoke();
        return null;
    }

    @Override
    public boolean getBoolean(String key) {
        return false;
    }

    @Override
    public TemplateData[] getArray(String key) {
        return new TemplateData[0];
    }

    @Override
    public MustacheLambda getLambda(String key) {
        return null;
    }

    @Override
    public TemplateData getData(String key) {
        return null;
    }

    @Override
    public SimpleTemplate getPartial(String key) {
        return null;
    }

    @Override
    public MustacheType getType(String key) {
        return null;
    }

    @Override
    public boolean hasProperty(String key) {
        return false;
    }
}
