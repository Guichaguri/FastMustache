package com.guichaguri.fastmustache;

import com.guichaguri.fastmustache.compiler.TemplateClassLoader;
import com.guichaguri.fastmustache.compiler.bytecode.CompilerException;
import com.guichaguri.fastmustache.compiler.bytecode.MustacheCompiler;
import com.guichaguri.fastmustache.compiler.bytecode.data.ClassDataSource;
import com.guichaguri.fastmustache.compiler.bytecode.data.DataSource;
import com.guichaguri.fastmustache.compiler.bytecode.data.SimpleDataSource;
import com.guichaguri.fastmustache.compiler.bytecode.data.TypedDataSource;
import com.guichaguri.fastmustache.compiler.parser.MustacheParser;
import com.guichaguri.fastmustache.compiler.parser.ParseException;
import com.guichaguri.fastmustache.compiler.parser.tokens.MustacheToken;
import com.guichaguri.fastmustache.template.CompilerOptions;
import com.guichaguri.fastmustache.template.MustacheType;
import com.guichaguri.fastmustache.template.Template;
import com.guichaguri.fastmustache.template.TemplateData;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * FastMustache compiles Mustache templates into Java bytecode to reduce overhead for blazing fast rendering.
 */
public class FastMustache implements Closeable {

    private static final TemplateClassLoader classLoader = new TemplateClassLoader();
    private static int classNameCount = 0;

    /**
     * Compiles the template using the data class
     * @param template The template reader
     * @param dataClass The data class
     * @param <T> The data class type
     * @return The compiled template instance
     * @throws IOException Thrown when an IO error occurs
     * @throws ParseException Thrown when the template couldn't be parsed
     * @throws CompilerException Thrown when the class couldn't be generated
     */
    public static <T> Template<T> compile(Reader template, Class<T> dataClass) throws IOException, CompilerException, ParseException {
        return new FastMustache(template).withAutoClassName(true).compile(dataClass);
    }

    /**
     * Compiles the template
     * @param template The template reader
     * @return The compiled template instance
     * @throws IOException Thrown when an IO error occurs
     * @throws ParseException Thrown when the template couldn't be parsed
     * @throws CompilerException Thrown when the class couldn't be generated
     */
    public static Template<TemplateData> compileSimple(Reader template) throws IOException, CompilerException, ParseException {
        return new FastMustache(template).withAutoClassName(true).compileSimple();
    }

    /**
     * Compiles the template
     * @param template The template reader
     * @param types The map of each property type
     * @return The compiled template instance
     * @throws IOException Thrown when an IO error occurs
     * @throws ParseException Thrown when the template couldn't be parsed
     * @throws CompilerException Thrown when the class couldn't be generated
     */
    public static Template<TemplateData> compileTyped(Reader template, Map<String, MustacheType> types) throws IOException, CompilerException, ParseException {
        return new FastMustache(template).withAutoClassName(true).compileTyped(types);
    }


    private final Reader template;
    private CompilerOptions options = CompilerOptions.DEFAULT;
    private String templateName, className;

    private List<MustacheToken> tokens = null;

    /**
     * Initializes the builder
     * @param template The template reader
     */
    public FastMustache(Reader template) {
        this.template = template;
    }

    /**
     * Initializes the builder
     * @param template The template stream
     */
    public FastMustache(InputStream template) {
        this.template = new InputStreamReader(template);
    }

    /**
     * Initializes the builder
     * @param template The template file
     */
    public FastMustache(File template) throws FileNotFoundException {
        this.template = new FileReader(template);
        this.templateName = template.getName();
    }

    /**
     * Initializes the builder
     * @param template The template string
     */
    public FastMustache(String template) {
        this.template = new StringReader(template);
    }

    /**
     * Sets the template file name (e.g. test.mustache)
     *
     * @param fileName The file name
     * @return The builder itself
     */
    public FastMustache withTemplateName(String fileName) {
        this.templateName = fileName;
        return this;
    }

    /**
     * Sets the generated class name. May contain a package (e.g. com.guichaguri.fastmustache.TemplateTest)
     *
     * @param className The full class name
     * @return The builder itself
     */
    public FastMustache withClassName(String className) {
        this.className = className;
        return this;
    }

    /**
     * Sets the options to parse and compile the template
     * @param options The options
     * @return The builder itself
     */
    public FastMustache withOptions(CompilerOptions options) {
        this.options = options;
        return this;
    }

    /**
     * Generates automatically a class name.
     * It will incorporate the template name when possible, otherwise it'll generate a random string.
     *
     * @param sequentialCount Whether it will include a sequential number to the end to avoid collisions
     * @return The builder itself
     */
    public FastMustache withAutoClassName(boolean sequentialCount) {
        StringBuilder builder = new StringBuilder();
        builder.append("com.guichaguri.fastmustache.compiled.Template");

        if (templateName == null || templateName.isEmpty()) {
            // Calculates a random name
            Random rand = new Random();

            for(int i = 0; i < 8; i++) {
                builder.append((char) ('A' + rand.nextInt('Z' - 'A')));
            }
        } else {
            // Removes the extension, makes it camel-case and removes points
            String name = templateName.replace('.', '_');
            int ext = templateName.lastIndexOf('.');
            builder.append(name.substring(0, 1).toUpperCase()).append(name, 1, ext < 2 ? templateName.length() : ext);
        }

        if (sequentialCount) {
            builder.append(++classNameCount);
        }

        this.className = builder.toString();
        return this;
    }

    /**
     * Parses a template into a list of tokens.
     *
     * @return The list of tokens
     * @throws ParseException Thrown when the template couldn't be parsed
     * @throws IOException Thrown when an IO error occurs
     */
    public List<MustacheToken> parse() throws ParseException, IOException {
        if (tokens != null) return tokens;

        tokens = new MustacheParser(options, template).parse();
        template.close();

        return tokens;
    }

    /**
     * Compiles the template into a Java class using the given data source.
     *
     * @param dataSource The data source
     * @return The bytes for the compiled class
     * @throws ParseException Thrown when the template couldn't be parsed
     * @throws CompilerException Thrown when the class couldn't be generated
     * @throws IOException Thrown when an IO error occurs
     */
    public byte[] compileClass(DataSource dataSource) throws ParseException, CompilerException, IOException {
        if (className == null || className.isEmpty()) {
            withAutoClassName(true);
        }

        if (templateName == null) {
            templateName = "";
        }

        MustacheCompiler compiler = new MustacheCompiler(className, templateName, dataSource.getDataType());
        compiler.insertConstructor();
        compiler.insertObjectRender();
        compiler.insertRender(options, dataSource, parse());
        return compiler.toByteArray();
    }

    /**
     * Compiles the template into a Java class using a {@link ClassDataSource}.
     *
     * Generates bytecode using the public fields and getter methods from the provided class.
     *
     * It's the fastest data source, as it reduces the overhead and knows the type of each member,
     * allowing better bytecode optimization.
     *
     * @param dataClass The data class
     * @return The bytes for the compiled class
     * @throws ParseException Thrown when the template couldn't be parsed
     * @throws CompilerException Thrown when the class couldn't be generated
     * @throws IOException Thrown when an IO error occurs
     */
    public byte[] compileClass(Class<?> dataClass) throws ParseException, CompilerException, IOException {
        return compileClass(new ClassDataSource(dataClass));
    }

    /**
     * Compiles the template into a Java class using a {@link SimpleDataSource}.
     *
     * Generates bytecode using the getters from {@link TemplateData}.
     *
     * Prefer using {@link #compileTypedClass(Map)} whenever possible, as it allows better optimizations.
     *
     * @return The bytes for the compiled class
     * @throws ParseException Thrown when the template couldn't be parsed
     * @throws CompilerException Thrown when the class couldn't be generated
     * @throws IOException Thrown when an IO error occurs
     */
    public byte[] compileSimpleClass() throws ParseException, CompilerException, IOException {
        return compileClass(new SimpleDataSource());
    }

    /**
     * Compiles the template into a Java class using a {@link TypedDataSource}.
     *
     * Works the same as {@link #compileSimpleClass()}, but allows allows providing a map of property types.
     * The compiler will optimize the generated bytecode for the properties that have a type set.
     *
     * It's faster than {@link #compileSimpleClass()} but not as fast as {@link #compileClass(Class)}.
     *
     * @param types The map of each property type
     * @return The bytes for the compiled class
     * @throws ParseException Thrown when the template couldn't be parsed
     * @throws CompilerException Thrown when the class couldn't be generated
     * @throws IOException Thrown when an IO error occurs
     */
    public byte[] compileTypedClass(Map<String, MustacheType> types) throws ParseException, CompilerException, IOException {
        return compileClass(new TypedDataSource(types));
    }

    /**
     * Compiles the template for the given data source, loads and initializes its class.
     *
     * @param dataSource The data source
     * @return The compiled template instance
     * @throws ParseException Thrown when the template couldn't be parsed
     * @throws CompilerException Thrown when the class couldn't be generated
     * @throws IOException Thrown when an IO error occurs
     */
    public Template<?> compile(DataSource dataSource) throws ParseException, CompilerException, IOException {
        Class<?> clazz = classLoader.loadClass(className, compileClass(dataSource));

        try {
            return (Template<?>) clazz.getConstructor().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't load the compiled template", ex);
        }
    }

    /**
     * Compiles the template using a {@link ClassDataSource}.
     *
     * Generates bytecode using the public fields and getter methods from the provided class.
     *
     * It's the fastest data source, as it reduces the overhead and knows the type of each member,
     * allowing better bytecode optimization.
     *
     * @param dataClass The data class
     * @param <T> The data class type
     * @return The compiled template instance
     * @throws ParseException Thrown when the template couldn't be parsed
     * @throws CompilerException Thrown when the class couldn't be generated
     * @throws IOException Thrown when an IO error occurs
     */
    public <T> Template<T> compile(Class<T> dataClass) throws ParseException, CompilerException, IOException {
        return (Template<T>) compile(new ClassDataSource(dataClass));
    }

    /**
     * Compiles the template using a {@link SimpleDataSource}.
     *
     * Generates bytecode using the getters from {@link TemplateData}.
     *
     * Prefer using {@link #compileTyped(Map)} whenever possible, as it allows better optimizations.
     *
     * @return The compiled template instance
     * @throws ParseException Thrown when the template couldn't be parsed
     * @throws CompilerException Thrown when the class couldn't be generated
     * @throws IOException Thrown when an IO error occurs
     */
    public Template<TemplateData> compileSimple() throws ParseException, CompilerException, IOException {
        return (Template<TemplateData>) compile(new SimpleDataSource());
    }

    /**
     * Compiles the template using a {@link TypedDataSource}.
     *
     * Works the same as {@link #compileSimple()}, but allows allows providing a map of property types.
     * The compiler will optimize the generated bytecode for the properties that have a type set.
     *
     * It's faster than {@link #compileSimple()} but not as fast as {@link #compile(Class)}.
     *
     * @param types The map of each property type
     * @return The compiled template instance
     * @throws ParseException Thrown when the template couldn't be parsed
     * @throws CompilerException Thrown when the class couldn't be generated
     * @throws IOException Thrown when an IO error occurs
     */
    public Template<TemplateData> compileTyped(Map<String, MustacheType> types) throws ParseException, CompilerException, IOException {
        return (Template<TemplateData>) compile(new TypedDataSource(types));
    }

    @Override
    public void close() throws IOException {
        template.close();
    }
}
