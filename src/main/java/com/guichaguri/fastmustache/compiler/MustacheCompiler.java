package com.guichaguri.fastmustache.compiler;

import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator;
import com.guichaguri.fastmustache.compiler.bytecode.data.ClassDataManager;
import com.guichaguri.fastmustache.compiler.bytecode.data.DataManager;
import com.guichaguri.fastmustache.compiler.bytecode.data.SimpleDataManager;
import com.guichaguri.fastmustache.compiler.bytecode.data.TypedDataManager;
import com.guichaguri.fastmustache.template.MustacheType;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Guichaguri
 */
public class MustacheCompiler {

    private final MustacheParser parser;
    private final String templateName;
    private BytecodeGenerator generator;

    private int lambdaIdCounter = 0;

    public MustacheCompiler(String templateName, Reader reader) {
        this.parser = new MustacheParser(this, reader);
        this.templateName = templateName;
    }

    public byte[] compileSimple(String className, Map<String, MustacheType> types) throws IOException, CompilerException {
        ClassWriter cw = new ClassWriter(0);

        String internalName = className.replace('.', '/');
        String classDesc = "L" + internalName + ";";

        cw.visit(52, ACC_PUBLIC + ACC_SUPER,
                internalName, null,
                BytecodeGenerator.OBJECT.getInternalName(),
                new String[]{BytecodeGenerator.SIMPLE_TEMPLATE.getInternalName()});

        cw.visitSource(templateName, null);

        createConstructor(cw, classDesc);

        DataManager data;

        if(types != null && !types.isEmpty()) {
            data = new TypedDataManager(types);
        } else {
            data = new SimpleDataManager();
        }

        generator = new BytecodeGenerator(this, cw, data, className, classDesc);

        generator.insertMethodStart("render");
        parser.parse();
        generator.insertMethodEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    public byte[] compile(String className, Class<?> clazz) throws IOException, CompilerException {
        ClassWriter cw = new ClassWriter(0);

        Type clazzType = Type.getType(clazz);

        String internalName = className.replace('.', '/');
        String classDesc = "L" + internalName + ";";

        String signature = BytecodeGenerator.OBJECT.getDescriptor() + "L" +
                BytecodeGenerator.OBJECT_TEMPLATE.getInternalName() + "<" + clazzType.getDescriptor() + ">;";

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, internalName, signature,
                BytecodeGenerator.OBJECT.getInternalName(),
                new String[]{BytecodeGenerator.OBJECT_TEMPLATE.getInternalName()});

        cw.visitSource(templateName, null);

        createConstructor(cw, classDesc);
        createObjectRender(cw, internalName, classDesc, clazzType);

        DataManager getter = new ClassDataManager(clazz, clazzType);
        generator = new BytecodeGenerator(this, cw, getter, internalName, classDesc);

        generator.insertMethodStart("render");
        parser.parse();
        generator.insertMethodEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    private void createConstructor(ClassWriter cw, String classDesc) {
        Label start = new Label();
        Label end = new Label();

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitLabel(start);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, BytecodeGenerator.OBJECT.getInternalName(), "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitLabel(end);
        mv.visitLocalVariable("this", classDesc, null, start, end, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void createObjectRender(ClassWriter cw, String className, String classDesc, Type data) {
        Label start = new Label();
        Label end = new Label();

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "render",
                Type.getMethodDescriptor(BytecodeGenerator.STRING, BytecodeGenerator.OBJECT), null, null);
        mv.visitCode();
        mv.visitLabel(start);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, data.getInternalName());
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "render", Type.getMethodDescriptor(BytecodeGenerator.STRING, data), false);
        mv.visitInsn(ARETURN);
        mv.visitLabel(end);
        mv.visitLocalVariable("this", classDesc, null, start, end, 0);
        //mv.visitLocalVariable("obj", data.getDescriptor(), null, start, end, 1);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    public String getTemplateName() {
        return templateName;
    }

    public BytecodeGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(BytecodeGenerator generator) {
        this.generator = generator;
    }

    public int getNextLambdaId() {
        return lambdaIdCounter++;
    }
}
