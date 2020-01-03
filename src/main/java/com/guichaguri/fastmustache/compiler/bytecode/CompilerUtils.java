package com.guichaguri.fastmustache.compiler.bytecode;

import com.guichaguri.fastmustache.compiler.bytecode.data.MemberType;
import org.objectweb.asm.tree.*;
import java.util.List;

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

}
