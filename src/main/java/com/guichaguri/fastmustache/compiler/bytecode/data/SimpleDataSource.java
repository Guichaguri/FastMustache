package com.guichaguri.fastmustache.compiler.bytecode.data;

import com.guichaguri.fastmustache.compiler.bytecode.CompilerException;
import com.guichaguri.fastmustache.compiler.bytecode.LocalVariable;
import com.guichaguri.fastmustache.data.ScopedData;
import com.guichaguri.fastmustache.template.MustacheType;
import com.guichaguri.fastmustache.template.TemplateData;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import java.util.LinkedList;

import static com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator2.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * Generates the bytecode that gets data from a key using the {@link TemplateData} methods
 *
 * @author Guichaguri
 */
public class SimpleDataSource implements DataSource {

    public static final Type DATA = Type.getType(TemplateData.class);
    public static final Type DATA_ARRAY = Type.getType(TemplateData[].class);
    public static final Type SCOPED_DATA = Type.getType(ScopedData.class);

    private final MemberType ARRAY_TYPE = new MemberType(TemplateData[].class, TemplateData.class, DATA_ARRAY);
    private final MemberType DATA_TYPE = new MemberType(TemplateData.class, DATA);
    private final LinkedList<LocalVariable> vars = new LinkedList<>();

    @Override
    public Type getDataType() {
        return DATA;
    }

    @Override
    public MustacheType getType(String key) {
        return MustacheType.UNKNOWN;
    }

    @Override
    public void insertObjectGetter(MethodVisitor mv, LocalVariable var, String key) {
        loadVar(mv, var);

        // data.get(key)
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "get",
                Type.getMethodDescriptor(OBJECT, STRING), true);
    }

    @Override
    public MemberType insertDataGetter(MethodVisitor mv, LocalVariable var, String key) {
        loadVar(mv, var);

        // data.getData(key)
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "getData",
                Type.getMethodDescriptor(DATA, STRING), true);

        return DATA_TYPE;
    }

    @Override
    public void insertStringGetter(MethodVisitor mv, LocalVariable var, String key, boolean escaped) {
        loadVar(mv, var);

        // data.getEscaped(key) or data.getUnescaped(key)
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), escaped ? "getEscaped" : "getUnescaped",
                Type.getMethodDescriptor(STRING, STRING), true);
    }

    @Override
    public void insertBooleanGetter(MethodVisitor mv, LocalVariable var, String key) {
        loadVar(mv, var);

        // data.getBoolean(key)
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "getBoolean",
                Type.getMethodDescriptor(Type.BOOLEAN_TYPE, STRING), true);
    }

    @Override
    public void insertTypeGetter(MethodVisitor mv, LocalVariable var, String key) {
        loadVar(mv, var);

        // data.getType(key).ordinal()
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "getType",
                Type.getMethodDescriptor(MUSTACHE_TYPE, STRING), true);
        mv.visitMethodInsn(INVOKEVIRTUAL, MUSTACHE_TYPE.getInternalName(), "ordinal",
                Type.getMethodDescriptor(Type.INT_TYPE), false);
    }

    @Override
    public MemberType insertArrayGetter(MethodVisitor mv, LocalVariable var, String key) {
        loadVar(mv, var);

        // data.getArray(key)
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "getArray",
                Type.getMethodDescriptor(DATA_ARRAY, STRING), true);

        return ARRAY_TYPE;
    }

    @Override
    public MemberType insertPartialGetter(MethodVisitor mv, LocalVariable var, String key) {
        // Loads the partial into the stack
        loadVar(mv, var);

        // data.getPartial(key)
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "getPartial",
                Type.getMethodDescriptor(SIMPLE_TEMPLATE, STRING), true);

        // Loads the data into the stack
        loadVar(mv, var);

        return DATA_TYPE;
    }

    @Override
    public void loadDataItem(MethodVisitor mv, LocalVariable var, Class<?> type) throws CompilerException {
        if(type != TemplateData.class) {
            throw new CompilerException("Can't parse a data item that is not a template data");
        }

        if(!vars.isEmpty()) {
            // TODO
            // d = new ScopedData(data, d);
            mv.visitTypeInsn(NEW, SCOPED_DATA.getInternalName());
            mv.visitInsn(DUP);
            vars.getLast().load(mv);
            var.load(mv);
            mv.visitMethodInsn(INVOKESPECIAL, SCOPED_DATA.getInternalName(), "<init>",
                    Type.getMethodDescriptor(Type.VOID_TYPE, DATA, DATA), false);
            var.store(mv);
        }

        vars.add(var);
    }

    @Override
    public void unloadDataItem(MethodVisitor mv, LocalVariable var) {
        vars.remove(var);
    }

    private void loadVar(MethodVisitor mv, LocalVariable var) {
        if(!vars.isEmpty()) {
            var = vars.getLast();
        }

        var.load(mv);
    }
}
