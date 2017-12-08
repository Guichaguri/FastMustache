package com.guichaguri.fastmustache.compiler;

/**
 * @author Guichaguri
 */
public class TemplateClassLoader extends ClassLoader {

    public Class loadClass(String className, byte[] bytes) {
        return defineClass(className, bytes, 0, bytes.length);
    }

}
