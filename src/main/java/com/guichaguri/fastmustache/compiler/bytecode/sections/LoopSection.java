package com.guichaguri.fastmustache.compiler.bytecode.sections;

import com.guichaguri.fastmustache.compiler.CompilerException;
import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator;
import com.guichaguri.fastmustache.compiler.bytecode.data.MemberType;
import java.util.Collection;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator.BUILDER;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author Guichaguri
 */
public class LoopSection implements Section {

    private final MemberType member;

    private final boolean collection;
    private final Label loopStart = new Label();
    private final Label loopEnd = new Label();

    private int varArray;
    private int varObject;
    private int varLength; // Only for arrays
    private int varIndex; // Only for arrays

    public LoopSection(MemberType member) throws CompilerException {
        this.member = member;

        if(member.clazz.isArray()) {
            this.collection = false;
        } else if(Collection.class.isAssignableFrom(member.clazz)) {
            this.collection = true;
        } else {
            throw new CompilerException(member + " is not a collection nor an array.");
        }
    }

    private void insertCollectionStart(MethodVisitor mv) {
        // collection.iterator()
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "iterator", "()Ljava/util/Iterator;", true);
        mv.visitVarInsn(ASTORE, varArray);

        mv.visitLabel(loopStart);

        // Append a new frame preserving the same locals from the last one
        mv.visitFrame(F_APPEND, 2, new Object[]{BUILDER.getInternalName(), "java/util/Iterator"}, 0, null);

        // if(iterator.hasNext()) break;
        mv.visitVarInsn(ALOAD, varArray);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
        mv.visitJumpInsn(IFEQ, loopEnd);

        // (String)iterator.next()
        mv.visitVarInsn(ALOAD, varArray);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(member.component));
        mv.visitVarInsn(ASTORE, varObject);
    }

    private void insertCollectionEnd(MethodVisitor mv) {
        mv.visitInsn(POP);
        mv.visitJumpInsn(GOTO, loopStart);
        mv.visitLabel(loopEnd);
        mv.visitFrame(F_CHOP, 1, null, 0, null);
    }

    private void insertArrayStart(MethodVisitor mv, BytecodeGenerator generator) {
        // Store the array in a local variable
        mv.visitVarInsn(ASTORE, varArray);

        // array.length
        mv.visitVarInsn(ALOAD, varArray);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitVarInsn(ISTORE, varLength);

        // i = 0
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, varIndex);

        mv.visitLabel(loopStart);

        // Create a compressed frame
        mv.visitFrame(F_FULL, 6, new Object[]{generator.className,
                generator.data.getDataType().getInternalName(), BUILDER.getInternalName(),
                member.clazzType.getInternalName(), INTEGER, INTEGER}, 0, new Object[0]);

        // if(index >= length) break;
        mv.visitVarInsn(ILOAD, varIndex);
        mv.visitVarInsn(ILOAD, varLength);
        mv.visitJumpInsn(IF_ICMPGE, loopEnd);

        // array[i]
        mv.visitVarInsn(ALOAD, varArray);
        mv.visitVarInsn(ILOAD, varIndex);
        mv.visitInsn(AALOAD);
        mv.visitVarInsn(ASTORE, varObject);
    }

    private void insertArrayEnd(MethodVisitor mv) {
        mv.visitInsn(POP);
        mv.visitIincInsn(varIndex, 1);
        mv.visitJumpInsn(GOTO, loopStart);
        mv.visitLabel(loopEnd);
        mv.visitFrame(F_CHOP, 3, null, 0, null);
    }

    @Override
    public void insertSectionStart(MethodVisitor mv, BytecodeGenerator generator) throws CompilerException {
        this.varArray = generator.getNextLocal();

        if(collection) {
            this.varObject = generator.getNextLocal();
            insertCollectionStart(mv);
        } else {
            this.varLength = generator.getNextLocal();
            this.varIndex = generator.getNextLocal();
            this.varObject = generator.getNextLocal();
            insertArrayStart(mv, generator);
        }

        generator.data.loadDataItem(mv, varObject, member.component);
    }

    @Override
    public void insertSectionEnd(MethodVisitor mv, BytecodeGenerator generator) {
        generator.data.unloadDataItem(mv, varObject);

        if(collection) {
            insertCollectionEnd(mv);
            // Free the array and object locals
            generator.freeLocals(2);
        } else {
            insertArrayEnd(mv);
            // Free the array, object, length and index locals
            generator.freeLocals(4);
        }
    }
}
