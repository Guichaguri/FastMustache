package com.guichaguri.fastmustache.compiler.bytecode.data;

import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator;
import com.guichaguri.fastmustache.compiler.bytecode.LocalVariable;
import org.objectweb.asm.MethodVisitor;
import java.util.LinkedList;

public class DataSourceContext {

    public final BytecodeGenerator generator;
    public final MethodVisitor mv;
    public final LinkedList<LocalVariable> vars = new LinkedList<>();

    public DataSourceContext(BytecodeGenerator generator, MethodVisitor mv) {
        this.generator = generator;
        this.mv = mv;
    }

}
