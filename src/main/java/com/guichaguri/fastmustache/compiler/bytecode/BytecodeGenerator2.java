package com.guichaguri.fastmustache.compiler.bytecode;

import com.guichaguri.fastmustache.compiler.bytecode.data.DataSource;
import com.guichaguri.fastmustache.compiler.bytecode.data.DataSourceContext;
import com.guichaguri.fastmustache.compiler.bytecode.data.MemberType;
import com.guichaguri.fastmustache.compiler.parser.tokens.MustacheToken;
import com.guichaguri.fastmustache.compiler.parser.tokens.SectionToken;
import com.guichaguri.fastmustache.template.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import static org.objectweb.asm.Opcodes.*;

public class BytecodeGenerator2 {

    public static final Type TEMPLATE = Type.getType(Template.class);
    public static final Type SIMPLE_TEMPLATE = Type.getType(SimpleTemplate.class);
    public static final Type MUSTACHE_TYPE = Type.getType(MustacheType.class);
    public static final Type SECTION = Type.getType(Section.class);
    public static final Type UTILS = Type.getType(TemplateUtils.class);
    public static final Type BUILDER = Type.getType(StringBuilder.class);
    public static final Type STRING = Type.getType(String.class);
    public static final Type OBJECT = Type.getType(Object.class);

    private final MustacheCompiler compiler;
    private final CompilerOptions options;
    private final DataSource data;
    private DataSourceContext context;

    private final Label start = new Label();
    private final Label end = new Label();
    private MethodVisitor mv;

    private LocalVariable thisVar;
    private LocalVariable dataVar;
    private LocalVariable builderVar;

    private List<LocalVariable> locals = new ArrayList<>();
    private Stack<LocalVariable> stack = new Stack<>();

    private String name;
    private int lambdaCount = 0;

    public BytecodeGenerator2(MustacheCompiler compiler, CompilerOptions options, DataSource data) {
        this.compiler = compiler;
        this.options = options;
        this.data = data;
    }

    /**
     * Starts building the render method
     * @param minimumLength The minimum capacity for the StringBuilder
     */
    public void start(int minimumLength) {
        Type dataType = data.getDataType();
        Class<?> dataClass = data.getDataClass();

        mv = compiler.getClassWriter().visitMethod(ACC_PUBLIC, "render", Type.getMethodDescriptor(STRING, dataType), null, null);
        mv.visitCode();

        // new StringBuilder()
        mv.visitLabel(start);
        mv.visitTypeInsn(NEW, BUILDER.getInternalName());
        mv.visitInsn(DUP);

        if (minimumLength > 16) {
            // Initializes the builder with an initial capacity set
            // Reduces the frequency in which the builder resizes itself, thus improving the performance

            if(minimumLength < Byte.MAX_VALUE) {
                // Loads a byte into the stack
                mv.visitIntInsn(BIPUSH, minimumLength);
            } else {
                // Loads an int into the stack
                mv.visitLdcInsn(minimumLength);
            }

            // StringBuilder(length)
            mv.visitMethodInsn(INVOKESPECIAL, BUILDER.getInternalName(), "<init>", "(I)V", false);
        } else {
            // StringBuilder()
            mv.visitMethodInsn(INVOKESPECIAL, BUILDER.getInternalName(), "<init>", "()V", false);
        }

        thisVar = insertLocalStart(compiler.getClassType().getDescriptor(), null, true, start);
        dataVar = insertLocalStart(dataType.getDescriptor(), dataClass, true, start);
        builderVar = insertLocalStart(BUILDER.getDescriptor(), StringBuilder.class, false, start);

        name = "render";
        context = data.createContext(this, mv, dataVar);

        stack.push(builderVar);
    }

    public void end() {
        // Loads the builder into the stack
        loadVarStack(builderVar);

        // return builder.toString()
        mv.visitMethodInsn(INVOKEVIRTUAL, BUILDER.getInternalName(), "toString", Type.getMethodDescriptor(STRING), false);
        mv.visitInsn(ARETURN);
        mv.visitLabel(end);

        for(LocalVariable var : locals) {
            if (!var.declared) continue;

            mv.visitLocalVariable("var" + var.index, var.desc, null,
                    var.start, var.end == null ? end : var.end, var.index);
        }

        mv.visitMaxs(10, locals.size());//TODO
        mv.visitEnd();
    }

    private LocalVariable insertLocalStart(String desc, Class<?> clazz, boolean declared, Label start) {
        // Tries to reuse a local variable that is not being used anymore
        // This optimizes the amount of pointers
        for (LocalVariable local : locals) {
            if (local.desc.equals(desc) && local.end != null && local.declared == declared) {
                local.end = null;
                return local;
            }
        }

        // Creates a new local variable
        LocalVariable local = new LocalVariable(locals.size(), desc, clazz, declared);
        local.start = start;
        locals.add(local);
        return local;
    }

    private void insertLocalEnd(LocalVariable local, Label end) {
        local.end = end;
    }

    private void loadVarStack(LocalVariable var) {
        // Check if the variable is already loaded into the top of the stack
        if (!stack.empty() && stack.peek() == var) return;

        var.load(mv);
        stack.push(var);
    }

    private void popVarStack() {
        LocalVariable var = stack.pop();

        if (!builderVar.declared && var == builderVar) {
            mv.visitVarInsn(ASTORE, builderVar.index);
            builderVar.declared = true;
            return;
        }

        var.pop(mv);
    }

    private void clearStack() {
        while(!stack.empty()) {
            popVarStack();
        }
    }

    /**
     * Adds the token list into the method
     */
    public void add(List<MustacheToken> tokens) throws CompilerException {
        for(MustacheToken token : tokens) {
            token.add(this, data);
        }
    }

    /**
     * Adds a raw text
     */
    public void addText(String str) {
        String desc;

        // Loads the builder into the stack
        loadVarStack(builderVar);

        if(str.length() == 1) {
            // Uses append(char)
            int c = str.charAt(0);

            if(c < Byte.MAX_VALUE) {
                // Loads a byte into the stack
                mv.visitIntInsn(BIPUSH, c);
            } else {
                // Loads an int into the stack
                mv.visitLdcInsn(c);
            }

            desc = Type.getMethodDescriptor(BUILDER, Type.CHAR_TYPE);
        } else {
            // Uses append(String)
            // Loads a string into the stack
            mv.visitLdcInsn(str);
            desc = Type.getMethodDescriptor(BUILDER, STRING);
        }

        // builder.append(...)
        mv.visitMethodInsn(INVOKEVIRTUAL, BUILDER.getInternalName(), "append", desc, false);

        // As it returns itself, the builder remains in the stack
    }

    /**
     * Adds a variable
     */
    public void addVariable(String variable, boolean escaped) throws CompilerException {
        // Loads the builder into the stack
        loadVarStack(builderVar);

        // Loads a string into the stack
        data.insertStringGetter(context, variable, escaped);

        // builder.append(...)
        mv.visitMethodInsn(INVOKEVIRTUAL, BUILDER.getInternalName(), "append", Type.getMethodDescriptor(BUILDER, STRING), false);

        // As it returns itself, the builder remains in the stack
    }

    /**
     * Adds a boolean condition
     */
    public void addCondition(SectionToken token) throws CompilerException {
        Label ifEnd = new Label();

        // Clears the whole stack
        clearStack();

        // Loads a boolean into the stack
        data.insertBooleanGetter(context, token.variable);

        // if(...) or if(!...)
        mv.visitJumpInsn(token.inverted ? IFNE : IFEQ, ifEnd);

        // Inserts all tokens inside the condition
        add(token.content);

        // Clears again the whole stack
        clearStack();

        mv.visitLabel(ifEnd);

        // COMPUTE_FRAMES is enabled, so we'll leave the frame construction to the ASM lib
        //mv.visitFrame(F_APPEND, 1, new Object[]{BUILDER.getInternalName()}, 0, null);
    }

    /**
     * Adds a not null condition that also adds the data into the scope
     */
    public void addDataCondition(SectionToken token) throws CompilerException {
        addDataCondition(token, null);
    }

    /**
     * Adds a not null condition that also adds the data into the scope
     */
    private void addDataCondition(SectionToken token, Label ifStart) throws CompilerException {
        if (token.inverted) {
            addObjectCondition(token);
            return;
        }

        Label ifEnd = new Label();

        // Clears the whole stack
        clearStack();

        if (ifStart == null) {
            ifStart = new Label();
            mv.visitLabel(ifStart);
        }

        // Loads the object into the stack
        MemberType member = data.insertDataGetter(context, token.variable);
        LocalVariable objectVar = insertLocalStart(member.clazzType.getDescriptor(), member.clazz, true, ifStart);

        // Stores it into a variable
        mv.visitVarInsn(ASTORE, objectVar.index);

        if (!member.clazz.isPrimitive()) {
            // If it's an object, we can null check it

            // Loads the object back into the stack
            mv.visitVarInsn(ALOAD, objectVar.index);

            // if(... != null)
            mv.visitJumpInsn(IFNULL, ifEnd);
        }

        // Loads the variable into the data source, so it can use its properties
        data.loadDataItem(context, objectVar);

        // Inserts all tokens inside the condition
        add(token.content);

        // Unloads the variable
        data.unloadDataItem(context, objectVar);

        // Clears again the whole stack
        clearStack();

        mv.visitLabel(ifEnd);

        // COMPUTE_FRAMES is enabled, so we'll leave the frame construction to the ASM lib
        //mv.visitFrame(F_APPEND, 1, new Object[]{BUILDER.getInternalName()}, 0, null);

        // Free the object local
        insertLocalEnd(objectVar, ifEnd);
    }

    /**
     * Adds a not null condition
     */
    public void addObjectCondition(SectionToken token) throws CompilerException {
        Label ifEnd = new Label();

        // Clears the whole stack
        clearStack();

        // Loads the object into the stack
        data.insertObjectGetter(context, token.variable);

        // if(... == null) or if(... != null)
        mv.visitJumpInsn(token.inverted ? IFNONNULL : IFNULL, ifEnd);

        // Inserts all tokens inside the condition
        add(token.content);

        // Clears again the whole stack
        clearStack();

        mv.visitLabel(ifEnd);

        // COMPUTE_FRAMES is enabled, so we'll leave the frame construction to the ASM lib
        //mv.visitFrame(F_APPEND, 1, new Object[]{BUILDER.getInternalName()}, 0, null);
    }

    /**
     * Adds a loop
     */
    public void addLoop(SectionToken token) throws CompilerException {
        Label sectionStart = new Label();

        clearStack();

        mv.visitLabel(sectionStart);

        addLoop(token, sectionStart);
    }

    /**
     * Adds a loop
     */
    private void addLoop(SectionToken token, Label sectionStart) throws CompilerException {
        // Gets the array
        MemberType member = data.insertArrayGetter(context, token.variable);

        if (member.clazz.isArray()) {
            if (token.inverted) {
                addEmptyArrayCondition(token, member, sectionStart);
            } else {
                addArrayLoop(token, member, sectionStart);
            }
        } else {
            if (token.inverted) {
                addEmptyCollectionCondition(token, member, sectionStart);
            } else {
                addCollectionLoop(token, member, sectionStart);
            }
        }
    }

    /**
     * Adds an array loop
     */
    private void addArrayLoop(SectionToken token, MemberType member, Label sectionStart) throws CompilerException {
        Label sectionEnd = new Label();
        Label loopStart = new Label();
        Label loopEnd = new Label();

        // Allocate variables
        LocalVariable varArray = insertLocalStart(member.clazzType.getDescriptor(), member.clazz, false, sectionStart);
        LocalVariable varLength = insertLocalStart("I", int.class, false, sectionStart);
        LocalVariable varIndex = insertLocalStart("I", int.class, false, sectionStart);
        LocalVariable varObject = insertLocalStart(Type.getDescriptor(member.component), member.component, true, sectionStart);

        // Store the array in a local variable
        mv.visitVarInsn(ASTORE, varArray.index);

        if (options.isArrayNullChecksEnabled()) {
            mv.visitVarInsn(ALOAD, varArray.index);
            mv.visitJumpInsn(IFNULL, sectionEnd);
        }

        // length = array.length
        mv.visitVarInsn(ALOAD, varArray.index);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitVarInsn(ISTORE, varLength.index);

        // i = 0
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, varIndex.index);

        mv.visitLabel(loopStart);

        // COMPUTE_FRAMES is enabled, so we'll leave the frame construction to the ASM lib
        /*mv.visitFrame(F_FULL, 6, new Object[]{generator.className,
                generator.data.getDataType().getInternalName(), BUILDER.getInternalName(),
                member.clazzType.getInternalName(), INTEGER, INTEGER}, 0, new Object[0]);*/

        // if(index >= length) break;
        mv.visitVarInsn(ILOAD, varIndex.index);
        mv.visitVarInsn(ILOAD, varLength.index);
        mv.visitJumpInsn(IF_ICMPGE, sectionEnd);

        // obj = array[i]
        mv.visitVarInsn(ALOAD, varArray.index);
        mv.visitVarInsn(ILOAD, varIndex.index);
        mv.visitInsn(AALOAD);
        mv.visitVarInsn(ASTORE, varObject.index);

        // Loads the variable into the data manager, so it can use its properties
        data.loadDataItem(context, varObject);

        // Inserts all tokens inside the loop
        add(token.content);

        // Unloads the variable
        data.unloadDataItem(context, varObject);

        clearStack();

        mv.visitLabel(loopEnd);

        mv.visitIincInsn(varIndex.index, 1); // i++
        mv.visitJumpInsn(GOTO, loopStart);
        mv.visitLabel(sectionEnd);

        // COMPUTE_FRAMES is enabled, so we'll leave the frame construction to the ASM lib
        //mv.visitFrame(F_CHOP, 3, null, 0, null);

        // Free the array, object, length and index locals
        insertLocalEnd(varArray, sectionEnd);
        insertLocalEnd(varLength, sectionEnd);
        insertLocalEnd(varIndex, sectionEnd);
        insertLocalEnd(varObject, loopEnd);
    }

    private void addEmptyArrayCondition(SectionToken token, MemberType member, Label sectionStart) throws CompilerException {
        Label sectionEnd = new Label();
        LocalVariable varArray = null;

        if (options.isArrayNullChecksEnabled()) {
            Label contentStart = new Label();

            // As we'll need to use the array twice, we'll store it in a variable
            varArray = insertLocalStart(member.clazzType.getDescriptor(), member.clazz, false, sectionStart);
            mv.visitVarInsn(ASTORE, varArray.index);

            // if(array == null)
            mv.visitVarInsn(ALOAD, varArray.index);
            mv.visitJumpInsn(IFNULL, contentStart);

            // if(array.length == 0)
            mv.visitVarInsn(ALOAD, varArray.index);
            mv.visitInsn(ARRAYLENGTH);
            mv.visitJumpInsn(IFNE, sectionEnd);

            mv.visitLabel(contentStart);
        } else {
            // if(array.length == 0)
            mv.visitInsn(ARRAYLENGTH);
            mv.visitJumpInsn(IFNE, sectionEnd);
        }

        add(token.content);

        clearStack();

        mv.visitLabel(sectionEnd);

        if (varArray != null) {
            insertLocalEnd(varArray, sectionEnd);
        }
    }

    /**
     * Adds a collection loop
     */
    private void addCollectionLoop(SectionToken token, MemberType member, Label sectionStart) throws CompilerException {
        Label loopStart = new Label();
        Label loopEnd = new Label();

        // Allocate variables
        LocalVariable varCollection = null;
        LocalVariable varIterator = insertLocalStart("Ljava/util/Iterator;", Iterator.class, false, sectionStart);
        LocalVariable varObject = insertLocalStart(OBJECT.getDescriptor(), Object.class, false, sectionStart);

        if (options.isArrayNullChecksEnabled()) {
            // As we'll have to use the collection twice, we'll store it in a variable
            varCollection = insertLocalStart(member.clazzType.getDescriptor(), member.clazz, false, sectionStart);
            mv.visitVarInsn(ASTORE, varCollection.index);

            // if(collection == null)
            mv.visitVarInsn(ALOAD, varCollection.index);
            mv.visitJumpInsn(IFNULL, loopEnd);

            mv.visitVarInsn(ALOAD, varCollection.index);
        }

        // iterator = collection.iterator()
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "iterator", "()Ljava/util/Iterator;", true);
        mv.visitVarInsn(ASTORE, varIterator.index);

        mv.visitLabel(loopStart);

        // Append a new frame preserving the same locals from the last one
        // COMPUTE_FRAMES is enabled, so we'll leave the frame construction to the ASM lib
        //mv.visitFrame(F_APPEND, 2, new Object[]{BUILDER.getInternalName(), "java/util/Iterator"}, 0, null);

        // if(iterator.hasNext()) break;
        mv.visitVarInsn(ALOAD, varIterator.index);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
        mv.visitJumpInsn(IFEQ, loopEnd);

        // obj = (T)iterator.next()
        mv.visitVarInsn(ALOAD, varIterator.index);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(member.component));
        mv.visitVarInsn(ASTORE, varObject.index);

        // Loads the variable into the data manager, so it can use its properties
        data.loadDataItem(context, varObject);

        // Inserts all tokens inside the loop
        add(token.content);

        // Unloads the variable
        data.unloadDataItem(context, varObject);

        clearStack();

        mv.visitJumpInsn(GOTO, loopStart);
        mv.visitLabel(loopEnd);

        // COMPUTE_FRAMES is enabled, so we'll leave the frame construction to the ASM lib
        //mv.visitFrame(F_CHOP, 1, null, 0, null);

        insertLocalEnd(varIterator, loopEnd);
        insertLocalEnd(varObject, loopEnd);

        if (varCollection != null) {
            insertLocalEnd(varCollection, loopEnd);
        }
    }

    private void addEmptyCollectionCondition(SectionToken token, MemberType member, Label sectionStart) throws CompilerException {
        Label sectionEnd = new Label();
        LocalVariable varCollection = null;

        if (options.isArrayNullChecksEnabled()) {
            Label contentStart = new Label();

            // As we'll have to use the collection twice, we'll store it in a variable
            varCollection = insertLocalStart(member.clazzType.getDescriptor(), member.clazz, false, sectionStart);
            mv.visitVarInsn(ASTORE, varCollection.index);

            // if(collection == null)
            mv.visitVarInsn(ALOAD, varCollection.index);
            mv.visitJumpInsn(IFNULL, contentStart);

            // if(collection.isEmpty())
            mv.visitVarInsn(ALOAD, varCollection.index);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "isEmpty", "()Z", true);
            mv.visitJumpInsn(IFEQ, sectionEnd);

            mv.visitLabel(contentStart);
        } else {
            // if(collection.isEmpty())
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "isEmpty", "()Z", true);
            mv.visitJumpInsn(IFEQ, sectionEnd);
        }

        add(token.content);

        clearStack();

        mv.visitLabel(sectionEnd);

        if (varCollection != null) {
            insertLocalEnd(varCollection, sectionEnd);
        }
    }

    /**
     * Adds an unknown section
     *
     * Checks the type in runtime and does the proper processing based on it.
     * Adds a lot more bytecode and should be avoid whenever possible.
     */
    public void addUnknownSection(SectionToken token) throws CompilerException {
        Label switchEnd = new Label();
        Label switchDefault = new Label();
        Label booleanSection = new Label();
        Label arraySection = new Label();
        Label dataSection = new Label();
        Label lambdaSection = new Label();

        clearStack();

        // Adds the type ordinal to the stack
        data.insertTypeGetter(context, token.variable);

        // switch(...)
        mv.visitLookupSwitchInsn(switchDefault,
                new int[]{MustacheType.BOOLEAN.ordinal(), MustacheType.ARRAY.ordinal(), MustacheType.DATA.ordinal(), MustacheType.LAMBDA.ordinal()},
                new Label[]{booleanSection, arraySection, dataSection, lambdaSection});

        // case BOOLEAN
        mv.visitLabel(booleanSection);
        addCondition(token);
        clearStack(); // Make sure the stack is empty
        mv.visitJumpInsn(GOTO, switchEnd);

        // case ARRAY
        mv.visitLabel(arraySection);
        addLoop(token, arraySection);
        clearStack(); // Make sure the stack is empty
        mv.visitJumpInsn(GOTO, switchEnd);

        // case DATA
        mv.visitLabel(dataSection);
        addDataCondition(token, dataSection);
        clearStack(); // Make sure the stack is empty
        mv.visitJumpInsn(GOTO, switchEnd);

        // case LAMBDA
        mv.visitLabel(lambdaSection);
        // TODO
        clearStack(); // Make sure the stack is empty
        mv.visitJumpInsn(GOTO, switchEnd);

        // default
        mv.visitLabel(switchDefault);
        addObjectCondition(token);
        clearStack(); // Make sure the stack is empty

        mv.visitLabel(switchEnd);
    }

    /**
     * Adds a lambda
     */
    public void addLambda(SectionToken token) {
        // TODO
    }

    /**
     * Adds a partial
     */
    public void addPartial(String partial) throws CompilerException {
        // Loads the builder into the stack
        loadVarStack(builderVar);

        data.insertPartialGetter(context, partial);

        // partial.render(data)
        mv.visitMethodInsn(INVOKEINTERFACE, TEMPLATE.getInternalName(), "render",
                Type.getMethodDescriptor(STRING, OBJECT), true);

        // builder.append(...)
        mv.visitMethodInsn(INVOKEVIRTUAL, BUILDER.getInternalName(), "append",
                Type.getMethodDescriptor(BUILDER, STRING), false);
    }

}
