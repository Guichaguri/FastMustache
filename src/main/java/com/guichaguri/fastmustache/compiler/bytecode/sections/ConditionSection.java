package com.guichaguri.fastmustache.compiler.bytecode.sections;

import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;
import static com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator.*;

/**
 * @author Guichaguri
 */
public class ConditionSection implements Section {

    protected final boolean inverted;
    protected final Label ifEnd = new Label();

    public ConditionSection(boolean inverted) {
        this.inverted = inverted;
    }

    @Override
    public void insertSectionStart(MethodVisitor mv, BytecodeGenerator generator) {
        mv.visitJumpInsn(inverted ? IFNE : IFEQ, ifEnd);
    }

    @Override
    public void insertSectionEnd(MethodVisitor mv, BytecodeGenerator generator) {
        mv.visitInsn(POP);
        mv.visitLabel(ifEnd);
        mv.visitFrame(F_APPEND, 1, new Object[]{BUILDER.getInternalName()}, 0, null);
    }
}
