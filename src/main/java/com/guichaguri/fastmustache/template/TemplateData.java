package com.guichaguri.fastmustache.template;

/**
 * @author Guichaguri
 */
public interface TemplateData {

    /**
     * Gets an escaped string
     *
     * @param key The property key
     * @return The escaped string
     */
    default String get(String key) {
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
    String getUnescaped(String key);

    /**
     * Gets a boolean.
     *
     * Returns {@code false} if the value is not found or is not a boolean.
     *
     * @param key The property key
     * @return The boolean value
     */
    boolean getBoolean(String key);

    /**
     * Gets a data array.
     *
     * Returns null or an empty array if nothing is found.
     *
     * @param key The property key
     * @return The array
     */
    TemplateData[] getArray(String key);

    /**
     * Gets a lambda renderer.
     *
     * @param key The property key
     * @return The lambda function
     */
    MustacheLambda getLambda(String key);

    /**
     * Gets a data object.
     *
     * @param key The property key
     * @return The data object
     */
    TemplateData getData(String key);

    SimpleTemplate getPartial(String key);//TODO?

    /**
     * Gets the type of a property.
     *
     * Returns {@link MustacheType#UNKNOWN} if it's not found or invalid.
     *
     * @param key The property key
     * @return The type value
     */
    MustacheType getType(String key);

    /**
     * Gets whether a property exists.
     *
     * @param key The property key
     * @return Whether the property exists
     */
    boolean hasProperty(String key);

}
