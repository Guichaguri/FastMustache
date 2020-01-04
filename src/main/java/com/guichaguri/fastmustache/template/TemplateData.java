package com.guichaguri.fastmustache.template;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * Represents a data adapter class for the "simple" and "typed" compilation types.
 *
 * @author Guichaguri
 */
@FunctionalInterface
public interface TemplateData {

    /**
     * Gets an object
     *
     * @param key The property key
     * @return The object
     */
    Object get(String key);

    /**
     * Gets an escaped string
     *
     * @param key The property key
     * @return The escaped string
     */
    default String getEscaped(String key) {
        String o = getUnescaped(key);
        if(o == null) return null;

        return TemplateUtils.escapeString(o);
    }

    /**
     * Gets an unescaped string
     *
     * @param key The property key
     * @return The unescaped string
     */
    default String getUnescaped(String key) {
        Object o = get(key);
        return o == null ? null : o.toString();
    }

    /**
     * Gets a boolean.
     *
     * Returns {@code false} if the value is not found or is not a boolean.
     *
     * @param key The property key
     * @return The boolean value
     */
    default boolean getBoolean(String key) {
        Object o = get(key);

        if(o == null) {
            return false;
        } else if(o instanceof Boolean) {
            return (Boolean) o;
        } else {
            return true;
        }
    }

    /**
     * Gets a data array.
     *
     * Returns null or an empty array if nothing is found.
     *
     * @param key The property key
     * @return The array
     */
    default TemplateData[] getArray(String key) {
        Object o = get(key);

        if (o instanceof TemplateData[]) {

            return (TemplateData[]) o;

        } else if (o.getClass().isArray()) {

            int length = Array.getLength(o);
            TemplateData[] data = new TemplateData[length];

            for(int i = 0; i < length; i++) {
                data[i] = TemplateUtils.fromObject(Array.get(o, i));
            }

            return data;

        } else if (o instanceof Collection) {

            Collection<?> collection = (Collection<?>) o;
            int length = collection.size();
            TemplateData[] data = new TemplateData[length];
            int i = 0;

            for(Object obj : collection) {
                data[i++] = TemplateUtils.fromObject(obj);
            }

            return data;

        }

        return null;
    }

    /**
     * Gets a lambda renderer.
     *
     * @param key The property key
     * @return The lambda function
     */
    default MustacheLambda<TemplateData> getLambda(String key) {
        Object o = get(key);

        if(o instanceof MustacheLambda) {
            return (MustacheLambda<TemplateData>) o;
        }

        return null;
    }

    /**
     * Gets a data object.
     *
     * @param key The property key
     * @return The data object
     */
    default TemplateData getData(String key) {
        return TemplateUtils.fromObject(get(key));
    }

    /**
     * Gets a partial template.
     *
     * @param key The property key
     * @return The template
     */
    default Template<TemplateData> getPartial(String key) {
        Object o = get(key);

        if (o instanceof Template) {
            return (Template<TemplateData>) o;
        }

        return null;
    }

    /**
     * Gets the type of a property.
     *
     * Returns {@link MustacheType#UNKNOWN} if the property is not found or invalid.
     *
     * @param key The property key
     * @return The type value
     */
    default MustacheType getType(String key) {
        return MustacheType.getByObject(get(key));
    }

    /**
     * Gets whether a property exists.
     *
     * @param key The property key
     * @return Whether the property exists
     */
    default boolean hasProperty(String key) {
        return get(key) != null;
    }

}
