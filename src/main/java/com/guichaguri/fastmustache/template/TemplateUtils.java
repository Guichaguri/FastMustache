package com.guichaguri.fastmustache.template;

import com.guichaguri.fastmustache.data.ImplicitData;
import com.guichaguri.fastmustache.data.MapData;
import com.guichaguri.fastmustache.data.ObjectData;
import java.util.Collection;
import java.util.Map;

/**
 * @author Guichaguri
 */
public class TemplateUtils {

    /**
     * Simple way to escape a string.
     *
     * @param string The original string
     * @return The escaped string
     */
    public static String escapeString(String string) {
        // Note: This method is invoked in compiled templates
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

    public static boolean isImplicitIterator(String key) {
        return key.equals(".");
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
            return new MapData((Map<String, Object>) o);
        } else if(o instanceof CharSequence || o instanceof Number || o instanceof Boolean ||
                o instanceof Collection || o.getClass().isArray() ||
                o instanceof Template || o instanceof MustacheLambda) {
            // All "reserved" types will be passed into an implicit data
            return new ImplicitData(o);
        } else {
            return new ObjectData(o);
        }
    }

}
