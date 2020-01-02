package com.guichaguri.fastmustache.compiler.bytecode.data;

import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator2;
import com.guichaguri.fastmustache.compiler.bytecode.LocalVariable;
import org.objectweb.asm.MethodVisitor;
import java.util.LinkedList;

public class DataSourceContext {

    public final BytecodeGenerator2 generator;
    public final MethodVisitor mv;
    public final LinkedList<LocalVariable> vars = new LinkedList<>();

    public DataSourceContext(BytecodeGenerator2 generator, MethodVisitor mv) {
        this.generator = generator;
        this.mv = mv;
    }

    public DataSourceContext(BytecodeGenerator2 generator, MethodVisitor mv, LocalVariable var) {
        this(generator, mv);
        vars.add(var);
    }

}
