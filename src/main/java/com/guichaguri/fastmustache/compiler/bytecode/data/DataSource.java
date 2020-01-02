package com.guichaguri.fastmustache.compiler.bytecode.data;

import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator2;
import com.guichaguri.fastmustache.compiler.bytecode.CompilerException;
import com.guichaguri.fastmustache.compiler.bytecode.LocalVariable;
import com.guichaguri.fastmustache.template.MustacheType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Generates the bytecode that gets data from a key
 *
 * @author Guichaguri
 */
public interface DataSource {

    /**
     * The render method argument
     * @return The type
     */
    Type getDataType();

    /**
     * The render method argument
     * @return The class
     */
    Class<?> getDataClass();

    /**
     * Creates a context for the generator
     * @param generator The generator
     * @param mv The method visitor
     * @param data The data local variable
     * @return The context that will be reused in other methods
     */
    default DataSourceContext createContext(BytecodeGenerator2 generator, MethodVisitor mv, LocalVariable data) {
        return new DataSourceContext(generator, mv, data);
    }

    /**
     * Gets the type from a key or a type that the key can be converted to.
     *
     * It should return {@link MustacheType#UNKNOWN} if it's not possible.
     *
     * @param context The context
     * @param key The key
     * @return The type
     */
    MustacheType getType(DataSourceContext context, String key);

    /**
     * Loads into the stack an {@link Object}
     * @param context The context
     * @param key The key
     */
    void insertObjectGetter(DataSourceContext context, String key) throws CompilerException;

    /**
     * Loads into the stack a data object
     * @param context The context
     * @param key The key
     * @return The object type that has been loaded
     */
    MemberType insertDataGetter(DataSourceContext context, String key) throws CompilerException;

    /**
     * Loads into the stack a string
     * @param context The context
     * @param key The key
     * @param escaped Whether the string must be escaped first
     */
    void insertStringGetter(DataSourceContext context, String key, boolean escaped) throws CompilerException;

    /**
     * Loads into the stack a primitive boolean
     * @param context The context
     * @param key The key
     */
    void insertBooleanGetter(DataSourceContext context, String key) throws CompilerException;

    /**
     * Loads into the stack an integer from {@link MustacheType#ordinal()}
     * @param context The context
     * @param key The key
     */
    void insertTypeGetter(DataSourceContext context, String key) throws CompilerException;

    /**
     * Loads into the stack a collection or an array
     * @param context The context
     * @param key The key
     * @return The array type
     */
    MemberType insertArrayGetter(DataSourceContext context, String key) throws CompilerException;

    /**
     * Loads into the stack a {@link com.guichaguri.fastmustache.template.Template} and the data parameter
     * @param context The context
     * @param key The key
     */
    void insertPartialGetter(DataSourceContext context, String key) throws CompilerException;

    /**
     * Prepares a data item when it is loaded
     * @param context The context
     * @param var The data item local variable
     */
    void loadDataItem(DataSourceContext context, LocalVariable var) throws CompilerException;

    /**
     * Cleans up any preparation done in {@link #loadDataItem(DataSourceContext, LocalVariable)}
     * @param context The context
     * @param var The data item local variable
     */
    void unloadDataItem(DataSourceContext context, LocalVariable var);

}
