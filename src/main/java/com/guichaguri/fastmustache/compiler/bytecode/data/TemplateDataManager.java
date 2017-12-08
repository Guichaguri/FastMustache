package com.guichaguri.fastmustache.compiler.bytecode.data;

import com.guichaguri.fastmustache.compiler.CompilerException;
import com.guichaguri.fastmustache.data.ScopedData;
import com.guichaguri.fastmustache.template.MustacheType;
import com.guichaguri.fastmustache.template.TemplateData;
import java.util.LinkedList;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;
import static com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator.*;

/**
 * Generates the bytecode that gets data from a key using the {@link TemplateData} methods
 *
 * @author Guichaguri
 */
public class TemplateDataManager implements DataManager {

    public static final Type DATA = Type.getType(TemplateData.class);
    public static final Type SCOPED_DATA = Type.getType(ScopedData.class);

    private final MemberType array = new MemberType(TemplateData[].class, TemplateData.class, Type.getType(TemplateData[].class));
    private final LinkedList<Integer> vars = new LinkedList<>();

    @Override
    public Type getDataType() {
        return DATA;
    }

    @Override
    public MustacheType getType(String key) {
        return MustacheType.UNKNOWN;
    }

    @Override
    public void insertObjectGetter(MethodVisitor mv, int var, String key) throws CompilerException {
        insertStringGetter(mv, var, key, false);
    }

    @Override
    public void insertStringGetter(MethodVisitor mv, int var, String key, boolean escaped) {
        loadVar(mv, var);
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), escaped ? "get" : "getUnescaped",
                Type.getMethodDescriptor(STRING, STRING), true);
    }

    @Override
    public void insertBooleanGetter(MethodVisitor mv, int var, String key) {
        loadVar(mv, var);
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "getBoolean",
                Type.getMethodDescriptor(Type.BOOLEAN_TYPE, STRING), true);
    }

    @Override
    public MemberType insertArrayGetter(MethodVisitor mv, int var, String key) {
        loadVar(mv, var);
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "getArray",
                Type.getMethodDescriptor(Type.getType(TemplateData[].class), STRING), true);

        return array;
    }

    @Override
    public void loadDataItem(MethodVisitor mv, int var, Class<?> type) throws CompilerException {
        if(type != TemplateData.class) {
            throw new CompilerException("Can't parse a data item that is not a template data");
        }

        if(!vars.isEmpty()) {
            // TODO
            // d = new ScopedData(data, d);
            mv.visitTypeInsn(NEW, SCOPED_DATA.getInternalName());
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, vars.getLast());
            mv.visitVarInsn(ALOAD, var);
            mv.visitMethodInsn(INVOKESPECIAL, SCOPED_DATA.getInternalName(), "<init>",
                    Type.getMethodDescriptor(Type.VOID_TYPE, DATA, DATA), false);
            mv.visitVarInsn(ASTORE, var);
        }

        vars.add(var);
    }

    @Override
    public void unloadDataItem(MethodVisitor mv, int var) {
        vars.remove((Integer)var);
    }

    private void loadVar(MethodVisitor mv, int var) {
        if(!vars.isEmpty()) {
            var = vars.getLast();
        }

        mv.visitVarInsn(ALOAD, var);
    }
}
