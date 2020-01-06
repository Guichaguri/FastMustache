package com.guichaguri.fastmustache.compiler.bytecode;

import com.guichaguri.fastmustache.compiler.bytecode.data.MemberType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.*;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class CompilerUtils {

    /**
     * Looks for a local variable that matches a certain type
     * @param vars The list of variables
     * @param type The type
     * @return The local variable found or {@code null}
     */
    public static LocalVariable findLocalVariable(List<LocalVariable> vars, MemberType type) {
        String lambdaTypeDesc = type.clazzType.getDescriptor();

        // Looks for a variable with the type being the exact same with what we want
        for(int i = vars.size() - 1; i >= 0; i--) {
            LocalVariable var = vars.get(i);

            if (var.desc.equals(lambdaTypeDesc)) {
                return var;
            }
        }

        // Looks for a variable with the type compatible with what we want
        for(int i = vars.size() - 1; i >= 0; i--) {
            LocalVariable var = vars.get(i);

            if (var.descClass != null && var.descClass.isAssignableFrom(type.clazz)) {
                return var;
            }
        }

        return null;
    }

    public static void removeVariable(MethodNode node, List<LocalVariable> vars, LocalVariable var) {
        InsnList instructions = node.instructions;
        List<LocalVariableNode> locals = node.localVariables;
        int index = var.index;

        // Look for instructions for other variables after the one removed and relocate them
        for(int i = 0; i < instructions.size(); i++) {
            AbstractInsnNode inst = instructions.get(i);
            if (inst instanceof VarInsnNode) {
                VarInsnNode varNode = (VarInsnNode) inst;
                if (varNode.var > index) varNode.var--;
            }
        }


        // Look for local variables after the one removed and relocate them
        for(int i = vars.size() - 1; i >= 0; i--) {
            LocalVariable variable = vars.get(i);
            if (variable.index == index) {
                vars.remove(i);
            } else if (variable.index > index) {
                variable.index--;
            }
        }

        // Look for local variable nodes after the one removed and relocate them
        for(int i = locals.size() - 1; i >= 0; i--) {
            LocalVariableNode localNode = locals.get(i);
            if (localNode.index == index) {
                locals.remove(i);
            } else if (localNode.index > index) {
                localNode.index--;
            }
        }
    }

    public static void loadInteger(MethodVisitor mv, int number) {
        // Loads a constant instruction where possible
        switch (number) {
            case -1:
                mv.visitInsn(ICONST_M1);
                return;
            case 0:
                mv.visitInsn(ICONST_0);
                return;
            case 1:
                mv.visitInsn(ICONST_1);
                return;
            case 2:
                mv.visitInsn(ICONST_2);
                return;
            case 3:
                mv.visitInsn(ICONST_3);
                return;
            case 4:
                mv.visitInsn(ICONST_4);
                return;
            case 5:
                mv.visitInsn(ICONST_5);
                return;
        }

        if (number > Byte.MIN_VALUE && number < Byte.MAX_VALUE) {
            // Loads a byte into the stack
            mv.visitIntInsn(BIPUSH, number);
        } else if (number > Short.MIN_VALUE && number < Short.MAX_VALUE) {
            // Loads a short into the stack
            mv.visitIntInsn(SIPUSH, number);
        } else {
            // Loads an int into the stack
            mv.visitLdcInsn(number);
        }
    }

}
