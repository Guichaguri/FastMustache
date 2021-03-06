package com.guichaguri.fastmustache.compiler.bytecode;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class LocalVariable {

    public int index;
    public final String desc;
    public final Class<?> descClass; // Can be null
    public Label start;
    public Label end;
    public boolean declared;
    public int usages = 0; // Number of times that this variable was loaded or stored

    public LocalVariable(int index, String desc, Class<?> descClass, boolean declared) {
        this.index = index;
        this.desc = desc;
        this.descClass = descClass;
        this.declared = declared;
    }

    public void load(MethodVisitor mv) {
        int opcode;

        if (desc.equals("I") || desc.equals("S") || desc.equals("B") || desc.equals("C") || desc.equals("Z")) {
            // short, byte, char and boolean values are all treated as ints in the bytecode
            opcode = ILOAD; // int
        } else if (desc.equals("F")) {
            opcode = FLOAD; // float
        } else if (desc.equals("D")) {
            opcode = DLOAD; // double
        } else if (desc.equals("J")) {
            opcode = LLOAD; // long
        } else {
            opcode = ALOAD; // reference
        }

        mv.visitVarInsn(opcode, index);
        usages++;
    }

    public void store(MethodVisitor mv) {
        int opcode;

        if (desc.equals("I") || desc.equals("S") || desc.equals("B") || desc.equals("C") || desc.equals("Z")) {
            // short, byte, char and boolean values are all treated as ints in the bytecode
            opcode = ISTORE; // int
        } else if (desc.equals("F")) {
            opcode = FSTORE; // float
        } else if (desc.equals("D")) {
            opcode = DSTORE; // double
        } else if (desc.equals("J")) {
            opcode = LSTORE; // long
        } else {
            opcode = ASTORE; // reference
        }

        mv.visitVarInsn(opcode, index);
        usages++;
    }

    public void pop(MethodVisitor mv) {
        // double and long values require POP2 to pop two values from the stack as they are based on 64 bits instead of 32 bits
        mv.visitInsn(desc.equals("D") || desc.equals("J") ? POP2 : POP);
    }

}
