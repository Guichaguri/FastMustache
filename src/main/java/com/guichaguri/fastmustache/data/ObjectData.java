package com.guichaguri.fastmustache.data;

import com.guichaguri.fastmustache.compiler.util.resolver.ClassMemberResolver;
import com.guichaguri.fastmustache.template.TemplateData;
import com.guichaguri.fastmustache.template.TemplateUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link TemplateData} implementation using object fields through reflection.
 *
 * @author Guichaguri
 */
public class ObjectData implements TemplateData {
    private final Class<?> clazz;
    private final ClassMemberResolver resolver;
    private final Map<String, Member[]> members = new HashMap<>();

    private Object obj;

    public ObjectData(Object obj, ClassMemberResolver resolver) {
        this.obj = obj;
        this.clazz = obj.getClass();
        this.resolver = resolver;

        if (!resolver.isValid(clazz)) {
            throw new IllegalArgumentException("The class " + clazz.getName() + " isn't a valid POJO.");
        }
    }

    public ObjectData(Object obj) {
        this(obj, ClassMemberResolver.getInstance());
    }

    /**
     * Changes the data object as long as it is the same class as the original object.
     *
     * @param obj The object
     */
    public void setObject(Object obj) {
        if (!clazz.isAssignableFrom(obj.getClass())) {
            throw new IllegalArgumentException("Object must be assignable of " + clazz.getName());
        }
        this.obj = obj;
    }

    public Object getObject() {
        return obj;
    }

    private Member[] find(String key) {
        // Uses cached handles to improve performance
        if (members.containsKey(key)) {
            return members.get(key);
        }

        Member[] path = resolver.findPath(clazz, key);
        members.put(key, path);
        return path;
    }

    @Override
    public Object get(String key) {
        if (TemplateUtils.isImplicitIterator(key)) return obj;

        try {
            Member[] members = find(key);
            if (members == null) return null;

            Object o = obj;

            for(Member member : members) {
                if (o == null) {
                    return null;
                } else if (member instanceof Field) {
                    o = ((Field) member).get(o);
                } else if (member instanceof Method) {
                    o = ((Method) member).invoke(o);
                }
            }

            return o;
        } catch(Exception ex) {
            return null;
        }
    }

    @Override
    public boolean hasProperty(String key) {
        return TemplateUtils.isImplicitIterator(key) || find(key) != null;
    }
}
