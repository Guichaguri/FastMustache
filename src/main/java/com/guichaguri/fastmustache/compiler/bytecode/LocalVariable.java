package com.guichaguri.fastmustache.compiler.bytecode;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class LocalVariable {

    public final int index;
    public final String desc;
    public Label start;
    public Label end;
    public boolean declared;

    public LocalVariable(int index, String desc, boolean declared) {
        this.index = index;
        this.desc = desc;
        this.declared = declared;
    }

    public void load(MethodVisitor mv) {
        int opcode;

        if (desc.equals("I")) {
            opcode = ILOAD;
        } else if (desc.equals("F")) {
            opcode = FLOAD;
        } else if (desc.equals("D")) {
            opcode = DLOAD;
        } else {
            opcode = ALOAD;
        }

        mv.visitVarInsn(opcode, index);
    }

    public void store(MethodVisitor mv) {
        int opcode;

        if (desc.equals("I")) {
            opcode = ISTORE;
        } else if (desc.equals("F")) {
            opcode = FSTORE;
        } else if (desc.equals("D")) {
            opcode = DSTORE;
        } else {
            opcode = ASTORE;
        }

        mv.visitVarInsn(opcode, index);
    }

    public void pop(MethodVisitor mv) {
        // doubles and longs have need POP2 as they are based on 64 bits instead of 32 bits
        mv.visitInsn(desc.equals("D") || desc.equals("J") ? POP2 : POP);
    }

}
