package com.guichaguri.fastmustache.compiler.bytecode.sections;

import com.guichaguri.fastmustache.compiler.CompilerException;
import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator;
import org.objectweb.asm.MethodVisitor;

/**
 * Generates the bytecode for a mustache section
 *
 * @author Guichaguri
 */
public interface Section {

    void insertSectionStart(MethodVisitor mv, BytecodeGenerator generator) throws CompilerException;

    void insertSectionEnd(MethodVisitor mv, BytecodeGenerator generator) throws CompilerException;

}
