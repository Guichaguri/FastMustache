package com.guichaguri.fastmustache.compiler.bytecode;

import com.guichaguri.fastmustache.compiler.CompilerException;
import com.guichaguri.fastmustache.compiler.MustacheCompiler;
import com.guichaguri.fastmustache.compiler.bytecode.data.DataManager;
import com.guichaguri.fastmustache.compiler.bytecode.data.MemberType;
import com.guichaguri.fastmustache.compiler.bytecode.sections.ConditionSection;
import com.guichaguri.fastmustache.compiler.bytecode.sections.LoopSection;
import com.guichaguri.fastmustache.compiler.bytecode.sections.ObjectConditionSection;
import com.guichaguri.fastmustache.compiler.bytecode.sections.Section;
import com.guichaguri.fastmustache.template.MustacheType;
import com.guichaguri.fastmustache.template.Template;
import com.guichaguri.fastmustache.template.SimpleTemplate;
import com.guichaguri.fastmustache.template.TemplateUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static com.guichaguri.fastmustache.compiler.bytecode.data.TemplateDataManager.DATA;
import static org.objectweb.asm.Opcodes.*;

/**
 * Generates bytecode through the interpreter
 *
 * @author Guichaguri
 */
public class BytecodeGenerator {

    public static final Type TEMPLATE = Type.getType(SimpleTemplate.class);
    public static final Type OBJECT_TEMPLATE = Type.getType(Template.class);
    public static final Type UTILS = Type.getType(TemplateUtils.class);
    public static final Type BUILDER = Type.getType(StringBuilder.class);
    public static final Type STRING = Type.getType(String.class);
    public static final Type OBJECT = Type.getType(Object.class);

    public final MustacheCompiler compiler;
    public final BytecodeGenerator parent;
    public final ClassWriter cw;
    public final DataManager data;
    public final Map<String, Section> sections = new HashMap<>();

    public final String className;
    public final String classDesc;
    public final String sectionName;
    public final int varThis;
    public final int varData;
    public final int varBuilder;

    protected Label start, end;
    protected MethodVisitor mv;
    protected boolean builderLoaded = true;
    protected boolean needLocal = false;
    private int localIndex = 0;

    public BytecodeGenerator(MustacheCompiler compiler, ClassWriter cw, DataManager data, String className, String classDesc) {
        this(compiler, null, null, cw, data, className, classDesc);
    }

    public BytecodeGenerator(MustacheCompiler compiler, BytecodeGenerator parent, String sectionName, ClassWriter cw,
                             DataManager data, String className, String classDesc) {
        this.compiler = compiler;
        this.parent = parent;
        this.sectionName = sectionName;
        this.cw = cw;
        this.data = data;
        this.className = className;
        this.classDesc = classDesc;

        if(parent == null) {
            varThis = 0;
            varData = 1;
            varBuilder = 2;
            localIndex = 3;
        } else {
            varThis = -1;
            varData = 0;
            varBuilder = 1;
            localIndex = 2;
        }
    }

    public int getNextLocal() {
        return localIndex++;
    }

    public void freeLocals(int amount) {
        localIndex -= amount;
    }

    public void insertMethodStart(String methodName) {
        start = new Label();
        end = new Label();

        int access = parent == null ? ACC_PUBLIC : ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC;

        mv = cw.visitMethod(access, methodName, Type.getMethodDescriptor(STRING, data.getDataType()), null, null);
        mv.visitCode();

        // new StringBuilder()
        mv.visitLabel(start);
        mv.visitTypeInsn(NEW, BUILDER.getInternalName());
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, BUILDER.getInternalName(), "<init>", "()V", false);

        builderLoaded = true;
    }

    public void insertMethodEnd() {
        if(!builderLoaded && needLocal) {
            mv.visitVarInsn(ALOAD, 2);
            builderLoaded = true;
        }

        // return builder.toString()
        mv.visitMethodInsn(INVOKEVIRTUAL, BUILDER.getInternalName(), "toString", Type.getMethodDescriptor(STRING), false);
        mv.visitInsn(ARETURN);
        mv.visitLabel(end);

        if(parent == null && varThis >= 0) {
            mv.visitLocalVariable("this", classDesc, null, start, end, varThis);
        }

        mv.visitLocalVariable("data", data.getDataType().getDescriptor(), null, start, end, varData);//TODO

        if(needLocal) {
            mv.visitLocalVariable("builder", BUILDER.getDescriptor(), null, start, end, varBuilder);
        }

        mv.visitMaxs(6, needLocal ? 7 : 2);//TODO
        mv.visitEnd();
    }

    public void insertString(String string) throws CompilerException {
        if(string.length() == 0) return;

        if(!builderLoaded && needLocal) {
            mv.visitVarInsn(ALOAD, varBuilder);
            builderLoaded = true;
        }

        String desc;

        if(string.length() == 1) {
            // Uses append(char)
            int c = (int)string.charAt(0);
            if(c < Byte.MAX_VALUE) {
                mv.visitIntInsn(BIPUSH, c);
            } else {
                mv.visitLdcInsn(c);
            }
            desc = Type.getMethodDescriptor(BUILDER, Type.CHAR_TYPE);
        } else {
            // Uses append(String)
            mv.visitLdcInsn(string);
            desc = Type.getMethodDescriptor(BUILDER, STRING);
        }

        // builder.append(...)
        mv.visitMethodInsn(INVOKEVIRTUAL, BUILDER.getInternalName(), "append", desc, false);
    }

    public void insertVariable(String key, boolean escaped) throws CompilerException {
        if(!builderLoaded && needLocal) {
            mv.visitVarInsn(ALOAD, varBuilder);
            builderLoaded = true;
        }

        data.insertStringGetter(mv, varData, key, escaped);

        // builder.append(...)
        mv.visitMethodInsn(INVOKEVIRTUAL, BUILDER.getInternalName(), "append", Type.getMethodDescriptor(BUILDER, STRING), false);
    }

    public void insertSectionStart(String key, boolean inverted) throws CompilerException {
        MustacheType type = data.getType(key);
        System.out.println(type);

        if(type == MustacheType.BOOLEAN) {
            // Insert "if"
            insertConditionStart(key, inverted);
        } else if(type == MustacheType.ARRAY) {
            // Insert "for"
            insertLoopStart(key, inverted);
        } else if(type == MustacheType.UNKNOWN) {
            // Unknown or not available -- Insert lambda
            insertDefaultSectionStart(key, inverted);
        } else {
            // Anything else -- Check if it's null
            insertObjectConditionStart(key, inverted);
        }
    }

    public void insertSectionEnd(String key) throws CompilerException {
        MustacheType type = data.getType(key);
        System.out.println(type);

        if(type == MustacheType.BOOLEAN) {
            // Close "if"
            insertConditionEnd(key);
        } else if(type == MustacheType.ARRAY) {
            // Close "for"
            insertLoopEnd(key);
        } else if(type == MustacheType.UNKNOWN) {
            // Unknown or not available -- Close lambda
            insertDefaultSectionEnd(key);
        } else {
            // Anything else -- Close null if
            insertObjectConditionEnd(key);
        }
    }

    protected void insertDefaultSectionStart(String key, boolean inverted) throws CompilerException {
        if(!builderLoaded && needLocal) {
            mv.visitVarInsn(ALOAD, varBuilder);
            builderLoaded = true;
        }

        if(data.getDataType() != DATA) {
            throw new CompilerException("The default section generator can't be used within a custom data type");
        }

        String methodName = "lambda$render$" + compiler.getNextLambdaId();

        mv.visitVarInsn(ALOAD, varData);
        mv.visitLdcInsn(key);

        Type methodType = Type.getMethodType(STRING, DATA);

        Handle lambda = new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;"
                        + "Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);
        Handle method = new Handle(H_INVOKESTATIC, className, methodName, methodType.getDescriptor(), false);

        mv.visitInvokeDynamicInsn("render", Type.getMethodDescriptor(TEMPLATE), lambda, methodType, method, methodType);

        mv.visitInsn(inverted ? ICONST_1 : ICONST_0);

        mv.visitMethodInsn(INVOKESTATIC, UTILS.getInternalName(), "renderSection", "(" + BUILDER.getDescriptor() +
                DATA.getDescriptor() + STRING.getDescriptor() + TEMPLATE.getDescriptor() +"Z)" + BUILDER.getDescriptor(), false);

        BytecodeGenerator gen = new BytecodeGenerator(compiler, this, key, cw, data, className, classDesc);
        compiler.setGenerator(gen);

        gen.insertMethodStart(methodName);
    }

    protected void insertDefaultSectionEnd(String key) throws CompilerException {
        if(parent == null) {
            throw new CompilerException("Can't close a section that is not open.");
        } else if(!key.equals(sectionName)) {
            throw new CompilerException("You need to close " + sectionName + " first.");
        }

        insertMethodEnd();
        compiler.setGenerator(parent);
    }

    protected void insertObjectConditionStart(String key, boolean inverted) throws CompilerException {
        if(!needLocal) {
            // Store it as a local variable, as we'll be working with another stack
            mv.visitVarInsn(ASTORE, varBuilder);
            builderLoaded = false;
            needLocal = true;
        }
        // TODO check when needLocal is true and builderLoaded is also true

        data.insertObjectGetter(mv, varData, key);

        ObjectConditionSection condition = new ObjectConditionSection(inverted);
        condition.insertSectionStart(mv, this);
        sections.put(key, condition);
    }

    protected void insertObjectConditionEnd(String key) throws CompilerException {
        Section section = sections.remove(key);

        if(section == null || !(section instanceof ConditionSection)) {
            throw new CompilerException("Condition section " + key + " is not open");
        }

        builderLoaded = false;
        section.insertSectionEnd(mv, this);
    }

    protected void insertConditionStart(String key, boolean inverted) throws CompilerException {
        if(!needLocal) {
            // Store it as a local variable, as we'll be working with another stack
            mv.visitVarInsn(ASTORE, varBuilder);
            builderLoaded = false;
            needLocal = true;
        }
        // TODO check when needLocal is true and builderLoaded is also true

        data.insertBooleanGetter(mv, varData, key);

        ConditionSection condition = new ConditionSection(inverted);
        condition.insertSectionStart(mv, this);
        sections.put(key, condition);
    }

    protected void insertConditionEnd(String key) throws CompilerException {
        Section section = sections.remove(key);

        if(section == null || !(section instanceof ConditionSection)) {
            throw new CompilerException("Condition section " + key + " is not open");
        }

        builderLoaded = false;
        section.insertSectionEnd(mv, this);
    }

    protected void insertLoopStart(String key, boolean inverted) throws CompilerException {
        if(!needLocal) {
            // Store it as a local variable, as we'll be working with another stack
            mv.visitVarInsn(ASTORE, varBuilder);
            builderLoaded = false;
            needLocal = true;
        }
        // TODO check when needLocal is true and builderLoaded is also true

        MemberType type = data.insertArrayGetter(mv, varData, key);
        Section section;

        if(inverted) {
            if(type.clazz.isArray()) {
                // array.length
                mv.visitInsn(ARRAYLENGTH);

                // Inverted conditions for ints check if the number == 0
                section = new ConditionSection(true);
            } else if(Collection.class.isAssignableFrom(type.clazz)) {
                // collection.isEmpty()
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "isEmpty", "()Z", true);

                section = new ConditionSection(false);
            } else {
                throw new CompilerException(type + " is not a collection nor an array.");
            }
        } else {
            section = new LoopSection(type);
        }

        section.insertSectionStart(mv, this);
        sections.put(key, section);
    }

    protected void insertLoopEnd(String key) throws CompilerException {
        Section section = sections.remove(key);

        if(section == null || !(section instanceof LoopSection || section instanceof ConditionSection)) {
            throw new CompilerException("Loop section " + key + " is not open");
        }

        builderLoaded = false;
        section.insertSectionEnd(mv, this);
    }

}
