package com.guichaguri.fastmustache.compiler.bytecode.data;

import com.guichaguri.fastmustache.compiler.CompilerException;
import com.guichaguri.fastmustache.compiler.bytecode.LocalVariable;
import com.guichaguri.fastmustache.template.MustacheType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Generates the bytecode that gets data from a key
 *
 * @author Guichaguri
 */
public interface DataManager {

    /**
     * The render method argument
     * @return The type
     */
    Type getDataType();

    /**
     * Gets the type from a key if possible
     *
     * It should return {@link MustacheType#UNKNOWN} if it's not possible.
     *
     * @param key The key
     * @return The type
     */
    MustacheType getType(String key);

    /**
     * Loads into the stack an object
     * @param mv The visitor
     * @param var The data variable
     * @param key The key
     */
    void insertObjectGetter(MethodVisitor mv, LocalVariable var, String key) throws CompilerException;

    /**
     * Loads into the stack a string
     * @param mv The visitor
     * @param var The data variable
     * @param key The key
     * @param escaped Whether the string must be escaped first
     */
    void insertStringGetter(MethodVisitor mv, LocalVariable var, String key, boolean escaped) throws CompilerException;

    /**
     * Loads into the stack a primitive boolean
     * @param mv The visitor
     * @param var The data variable
     * @param key The key
     */
    void insertBooleanGetter(MethodVisitor mv, LocalVariable var, String key) throws CompilerException;

    /**
     * Loads into the stack a collection or an array
     * @param mv The visitor
     * @param var The data variable
     * @param key The key
     * @return The array type
     */
    MemberType insertArrayGetter(MethodVisitor mv, LocalVariable var, String key) throws CompilerException;

    /**
     * Prepares a data item when it is loaded
     * @param var The data item local variable
     * @param type The data item type
     */
    void loadDataItem(MethodVisitor mv, LocalVariable var, Class<?> type) throws CompilerException;

    /**
     * Cleans up any preparation done in {@link #loadDataItem(MethodVisitor, LocalVariable, Class)}
     * @param var The data item local variable
     */
    void unloadDataItem(MethodVisitor mv, LocalVariable var);

}
