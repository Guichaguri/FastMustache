package com.guichaguri.fastmustache.compiler.bytecode.sections;

import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;

/**
 * Checks whether a object is null
 * @author Guichaguri
 */
public class ObjectConditionSection extends ConditionSection {
    public ObjectConditionSection(boolean inverted) {
        super(inverted);
    }

    @Override
    public void insertSectionStart(MethodVisitor mv, BytecodeGenerator generator) {
        mv.visitJumpInsn(inverted ? IFNULL : IFNONNULL, ifEnd);
    }
}
