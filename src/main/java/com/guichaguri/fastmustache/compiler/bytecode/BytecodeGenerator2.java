package com.guichaguri.fastmustache.compiler.bytecode;

import com.guichaguri.fastmustache.compiler.CompilerException;
import com.guichaguri.fastmustache.compiler.bytecode.data.DataManager;
import com.guichaguri.fastmustache.compiler.bytecode.data.MemberType;
import com.guichaguri.fastmustache.compiler.parser.tokens.MustacheToken;
import com.guichaguri.fastmustache.compiler.parser.tokens.SectionToken;
import com.guichaguri.fastmustache.template.Template;
import com.guichaguri.fastmustache.template.TemplateUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.objectweb.asm.Opcodes.*;

public class BytecodeGenerator2 {

    public static final Type TEMPLATE = Type.getType(Template.class);
    public static final Type UTILS = Type.getType(TemplateUtils.class);
    public static final Type BUILDER = Type.getType(StringBuilder.class);
    public static final Type STRING = Type.getType(String.class);
    public static final Type OBJECT = Type.getType(Object.class);

    private final DataManager data;
    private final ClassWriter cw;

    private final Label start = new Label();
    private final Label end = new Label();
    private MethodVisitor mv;

    private Type clazzType;

    private LocalVariable thisVar;
    private LocalVariable dataVar;
    private LocalVariable builderVar;

    private List<LocalVariable> locals = new ArrayList<>();
    private Stack<LocalVariable> stack = new Stack<>();

    public BytecodeGenerator2(String className, String templateName, DataManager data) {
        this.data = data;
        this.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        String internalName = className.replace('.', '/');
        clazzType = Type.getObjectType(internalName);

        String signature = OBJECT.getDescriptor() + "L" +
                TEMPLATE.getInternalName() + "<" + data.getDataType().getDescriptor() + ">;";

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, internalName, signature,
                OBJECT.getInternalName(),
                new String[]{TEMPLATE.getInternalName()});

        cw.visitSource(templateName, null);

        insertConstructor();
        insertObjectRender();
    }

    /**
     * Inserts the default constructor
     */
    public void insertConstructor() {
        Label start = new Label();
        Label end = new Label();

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitLabel(start);

        // super();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, OBJECT.getInternalName(), "<init>", "()V", false);

        mv.visitInsn(RETURN);

        mv.visitLabel(end);
        mv.visitLocalVariable("this", clazzType.getDescriptor(), null, start, end, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /**
     * Inserts the bridge render(Object) method
     */
    public void insertObjectRender() {
        Label start = new Label();
        Label end = new Label();
        Type dataType = data.getDataType();

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "render",
                Type.getMethodDescriptor(STRING, OBJECT), null, null);
        mv.visitCode();
        mv.visitLabel(start);

        // return render((T) obj)
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, dataType.getInternalName());
        mv.visitMethodInsn(INVOKEVIRTUAL, clazzType.getInternalName(), "render", Type.getMethodDescriptor(STRING, dataType), false);
        mv.visitInsn(ARETURN);

        mv.visitLabel(end);
        mv.visitLocalVariable("this", clazzType.getDescriptor(), null, start, end, 0);
        mv.visitLocalVariable("obj", dataType.getDescriptor(), null, start, end, 1);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    public void insertMethodStart(String methodName) {
        int access = ACC_PUBLIC;//parent == null ? ACC_PUBLIC : ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC;

        mv = cw.visitMethod(access, methodName, Type.getMethodDescriptor(STRING, data.getDataType()), null, null);
        mv.visitCode();

        // new StringBuilder()
        mv.visitLabel(start);
        mv.visitTypeInsn(NEW, BUILDER.getInternalName());
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, BUILDER.getInternalName(), "<init>", "()V", false);

        thisVar = insertLocalStart(clazzType.getDescriptor(), true, start);
        dataVar = insertLocalStart(data.getDataType().getDescriptor(), true, start);
        builderVar = insertLocalStart(BUILDER.getDescriptor(), false, start);

        stack.push(builderVar);
    }

    public void insertMethodEnd() {
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


        /*if(parent == null && thisVar >= 0) {
            mv.visitLocalVariable("this", classDesc, null, start, end, varThis);
        }*/

        //mv.visitLocalVariable("data", data.getDataType().getDescriptor(), null, start, end, dataVar.index);//TODO

        /*if(needLocal) {
            mv.visitLocalVariable("builder", BUILDER.getDescriptor(), null, start, end, builderVar);
        }*/

        mv.visitMaxs(10, locals.size());//TODO
        mv.visitEnd();
    }

    public LocalVariable insertLocalStart(String desc, boolean declared, Label start) {
        // Tries to reuse a local variable that is not being used anymore
        // This optimizes the amount of pointers
        for (LocalVariable local : locals) {
            if (local.desc.equals(desc) && local.end != null && local.declared == declared) {
                local.start = start;
                local.end = null;
                return local;
            }
        }

        // Creates a new local variable
        LocalVariable local = new LocalVariable(locals.size(), desc, declared);
        local.start = start;
        locals.add(local);
        return local;
    }

    public void insertLocalEnd(LocalVariable local, Label end) {
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
            int c = (int)str.charAt(0);

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
        data.insertStringGetter(mv, dataVar, variable, escaped);

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
        data.insertBooleanGetter(mv, dataVar, token.variable);

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
     * Adds a not null condition
     */
    public void addObjectCondition(SectionToken token) throws CompilerException {
        Label ifEnd = new Label();

        // Clears the whole stack
        clearStack();

        // Loads the object into the stack
        data.insertObjectGetter(mv, dataVar, token.variable);

        // if(... == null) or if(... != null)
        mv.visitJumpInsn(token.inverted ? IFNULL : IFNONNULL, ifEnd);

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

        // Gets the array
        MemberType member = data.insertArrayGetter(mv, dataVar, token.variable);

        if (member.clazz.isArray()) {
            addArrayLoop(token, member, sectionStart);
        } else {
            addCollectionLoop(token, member, sectionStart);
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
        LocalVariable varArray = insertLocalStart(member.clazzType.getDescriptor(), false, sectionStart);
        LocalVariable varLength = insertLocalStart("I", false, sectionStart);
        LocalVariable varIndex = insertLocalStart("I", false, sectionStart);
        LocalVariable varObject = insertLocalStart(Type.getDescriptor(member.component), true, sectionStart);

        // Store the array in a local variable
        mv.visitVarInsn(ASTORE, varArray.index);

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
        data.loadDataItem(mv, varObject, member.component);

        // Inserts all tokens inside the loop
        add(token.content);

        // Unloads the variable
        data.unloadDataItem(mv, varObject);

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

    /**
     * Adds a collection loop
     */
    private void addCollectionLoop(SectionToken token, MemberType member, Label sectionStart) throws CompilerException {
        Label sectionEnd = new Label();
        Label loopStart = new Label();
        Label loopEnd = new Label();

        // Allocate variables
        LocalVariable varIterator = insertLocalStart("Ljava/util/Iterator;", false, sectionStart);
        LocalVariable varObject = insertLocalStart(OBJECT.getDescriptor(), false, sectionStart);

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
        data.loadDataItem(mv, varObject, member.component);

        // Inserts all tokens inside the loop
        add(token.content);

        // Unloads the variable
        data.unloadDataItem(mv, varObject);

        clearStack();

        mv.visitJumpInsn(GOTO, loopStart);
        mv.visitLabel(loopEnd);

        // COMPUTE_FRAMES is enabled, so we'll leave the frame construction to the ASM lib
        //mv.visitFrame(F_CHOP, 1, null, 0, null);

        mv.visitLabel(sectionEnd);

        insertLocalEnd(varIterator, sectionEnd);
        insertLocalEnd(varObject, sectionEnd);
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
        loadVarStack(builderVar);

        MemberType member = data.insertPartialGetter(mv, dataVar, partial);

        // partial.render(data)
        mv.visitMethodInsn(INVOKEINTERFACE, TEMPLATE.getInternalName(), "render",
                Type.getMethodDescriptor(STRING, member.clazzType), true);

        // builder.append(...)
        mv.visitMethodInsn(INVOKEVIRTUAL, BUILDER.getInternalName(), "append",
                Type.getMethodDescriptor(BUILDER, STRING), false);
    }

    /**
     * Converts the generated class into a byte array
     */
    public byte[] toByteArray() {
        return cw.toByteArray();
    }

}
