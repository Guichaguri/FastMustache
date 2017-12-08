package com.guichaguri.fastmustache.template;

import com.guichaguri.fastmustache.data.MapData;
import com.guichaguri.fastmustache.data.ObjectData;
import com.guichaguri.fastmustache.data.ScopedData;
import java.util.Map;

/**
 * @author Guichaguri
 */
public class TemplateUtils {

    /**
     * Renders a Mustache section.
     * This is only used when the type is not specified at compile time.
     *
     * @param builder  The builder
     * @param data     The template data
     * @param key      The section key
     * @param template The section renderer
     * @param inverted Whether this section is inverted
     * @return The builder
     */
    public static StringBuilder renderSection(StringBuilder builder, TemplateData data, String key, SimpleTemplate template, boolean inverted) {
        // Don't change the method name or descriptor without also changing it in the invoke bytecode instructions
        MustacheType type = data.getType(key);

        if(type == MustacheType.BOOLEAN) {
            if(data.getBoolean(key) == !inverted) {
                builder.append(template.render(data));
            }
        } else if(type == MustacheType.LAMBDA) {
            data.getLambda(key).render(builder, template, data);
        } else if(type == MustacheType.ARRAY) {
            TemplateData[] array = data.getArray(key);

            if(array.length == 0 && inverted) {
                builder.append(template.render(data));
            }

            for(TemplateData d : array) {
                builder.append(template.render(d));
            }
        } else if(type == MustacheType.DATA) {
            builder.append(template.render(new ScopedData(data, data.getData(key))));
        }

        return builder;
    }

    /**
     * Simple way to escape a string.
     *
     * @param string The original string
     * @return The escaped string
     */
    public static String escapeString(String string) {
        // Don't change the method name or descriptor without also changing it in the invoke bytecode instructions
        StringBuilder builder = new StringBuilder();

        for(char c : string.toCharArray()) {
            if(c == '<') {
                builder.append("&lt;");
            } else if(c == '>') {
                builder.append("&gt;");
            } else if(c == '&') {
                builder.append("&amp;");
            } else if(c == '"') {
                builder.append("&quot;");
            } else if(c == '\'') {
                builder.append("&#39;");
            } else if(c >= 0x80) {
                builder.append("&#").append((int)c).append(';');
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    /**
     * Gets or creates a {@link TemplateData} from a {@link Object}
     *
     * @param o The object
     * @return The template data
     */
    public static TemplateData fromObject(Object o) {
        if(o == null) {
            return null;
        } else if(o instanceof TemplateData) {
            return (TemplateData)o;
        } else if(o instanceof Map) {
            return new MapData((Map)o);
        } else {
            return new ObjectData(o);
        }
    }

}
