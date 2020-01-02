package com.guichaguri.fastmustache.compiler.bytecode;

import com.guichaguri.fastmustache.compiler.bytecode.data.DataSource;
import com.guichaguri.fastmustache.compiler.parser.tokens.MustacheToken;
import com.guichaguri.fastmustache.compiler.parser.tokens.TextToken;
import com.guichaguri.fastmustache.template.CompilerOptions;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import java.util.List;

import static com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator2.*;
import static org.objectweb.asm.Opcodes.*;

public class MustacheCompiler {

    private final ClassWriter cw;
    private final Type classType;
    private final Type dataType;

    public MustacheCompiler(String className, String templateName, Type dataType) {
        this.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        this.dataType = dataType;

        String internalName = className.replace('.', '/');
        classType = Type.getObjectType(internalName);

        String signature = OBJECT.getDescriptor() + "L" +
                TEMPLATE.getInternalName() + "<" + dataType.getDescriptor() + ">;";

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, internalName, signature,
                OBJECT.getInternalName(),
                new String[]{TEMPLATE.getInternalName()});

        cw.visitSource(templateName, null);
    }

    public Type getClassType() {
        return classType;
    }

    public Type getDataType() {
        return dataType;
    }

    ClassWriter getClassWriter() {
        return cw;
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
        mv.visitLocalVariable("this", classType.getDescriptor(), null, start, end, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /**
     * Inserts the bridge render(Object) method
     */
    public void insertObjectRender() {
        Label start = new Label();
        Label end = new Label();

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "render",
                Type.getMethodDescriptor(STRING, OBJECT), null, null);
        mv.visitCode();
        mv.visitLabel(start);

        // return render((T) obj)
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, dataType.getInternalName());
        mv.visitMethodInsn(INVOKEVIRTUAL, classType.getInternalName(), "render", Type.getMethodDescriptor(STRING, dataType), false);
        mv.visitInsn(ARETURN);

        mv.visitLabel(end);
        mv.visitLocalVariable("this", classType.getDescriptor(), null, start, end, 0);
        mv.visitLocalVariable("obj", dataType.getDescriptor(), null, start, end, 1);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    /**
     * Inserts the render(T) method
     * @param options The compiler options
     * @param data The data manager
     * @param tokens The token list
     * @throws CompilerException Thrown when an error occurs while generating the instructions
     */
    public void insertRender(CompilerOptions options, DataSource data, List<MustacheToken> tokens) throws CompilerException {
        BytecodeGenerator2 generator = new BytecodeGenerator2(this, options, data);
        generator.start(TextToken.getMinimumLength(tokens));
        generator.add(tokens);
        generator.end();
    }

    /**
     * Converts the generated class into a byte array
     */
    public byte[] toByteArray() {
        return cw.toByteArray();
    }

}
