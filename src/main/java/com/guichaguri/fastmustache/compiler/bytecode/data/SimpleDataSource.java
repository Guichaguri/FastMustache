package com.guichaguri.fastmustache.compiler.bytecode.data;

import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator2;
import com.guichaguri.fastmustache.compiler.bytecode.CompilerException;
import com.guichaguri.fastmustache.compiler.bytecode.LocalVariable;
import com.guichaguri.fastmustache.data.ScopedData;
import com.guichaguri.fastmustache.template.MustacheType;
import com.guichaguri.fastmustache.template.TemplateData;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.List;

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

    private final MemberType ARRAY_TYPE = new MemberType(TemplateData[].class, DATA_ARRAY);
    private final MemberType DATA_TYPE = new MemberType(TemplateData.class, DATA);

    @Override
    public Type getDataType() {
        return DATA;
    }

    @Override
    public Class<?> getDataClass() {
        return TemplateData.class;
    }

    @Override
    public MustacheType getType(DataSourceContext context, String key) {
        return MustacheType.UNKNOWN;
    }

    @Override
    public void insertObjectGetter(DataSourceContext context, String key) {
        MethodVisitor mv = context.mv;

        // Loads the last data variable into the stack
        context.vars.getLast().load(mv);

        // data.get(key)
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "get",
                Type.getMethodDescriptor(OBJECT, STRING), true);
    }

    @Override
    public MemberType insertDataGetter(DataSourceContext context, String key) {
        MethodVisitor mv = context.mv;

        // Loads the last data variable
        context.vars.getLast().load(mv);

        // data.getData(key)
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "getData",
                Type.getMethodDescriptor(DATA, STRING), true);

        return DATA_TYPE;
    }

    @Override
    public void insertStringGetter(DataSourceContext context, String key, boolean escaped) {
        MethodVisitor mv = context.mv;

        // Loads the last data variable into the stack
        context.vars.getLast().load(mv);

        // data.getEscaped(key) or data.getUnescaped(key)
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), escaped ? "getEscaped" : "getUnescaped",
                Type.getMethodDescriptor(STRING, STRING), true);
    }

    @Override
    public void insertBooleanGetter(DataSourceContext context, String key) {
        MethodVisitor mv = context.mv;

        // Loads the last data variable into the stack
        context.vars.getLast().load(mv);

        // data.getBoolean(key)
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "getBoolean",
                Type.getMethodDescriptor(Type.BOOLEAN_TYPE, STRING), true);
    }

    @Override
    public void insertTypeGetter(DataSourceContext context, String key) {
        MethodVisitor mv = context.mv;

        // Loads the last data variable into the stack
        context.vars.getLast().load(mv);

        // data.getType(key).ordinal()
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "getType",
                Type.getMethodDescriptor(MUSTACHE_TYPE, STRING), true);
        mv.visitMethodInsn(INVOKEVIRTUAL, MUSTACHE_TYPE.getInternalName(), "ordinal",
                Type.getMethodDescriptor(Type.INT_TYPE), false);
    }

    @Override
    public MemberType insertArrayGetter(DataSourceContext context, String key) {
        MethodVisitor mv = context.mv;

        // Loads the last data variable into the stack
        context.vars.getLast().load(mv);

        // data.getArray(key)
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "getArray",
                Type.getMethodDescriptor(DATA_ARRAY, STRING), true);

        return ARRAY_TYPE;
    }

    @Override
    public MemberType insertLambdaGetter(DataSourceContext context, String key) {
        MethodVisitor mv = context.mv;

        // Loads the last data variable into the stack
        context.vars.getLast().load(mv);

        // data.getLambda(key)
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "getLambda",
                Type.getMethodDescriptor(LAMBDA, STRING), true);

        return DATA_TYPE;
    }

    @Override
    public void insertPartialGetter(DataSourceContext context, String key) {
        MethodVisitor mv = context.mv;
        LocalVariable var = context.vars.getLast();

        // Loads the last data variable into the stack
        var.load(mv);

        // data.getPartial(key)
        mv.visitLdcInsn(key);
        mv.visitMethodInsn(INVOKEINTERFACE, DATA.getInternalName(), "getPartial",
                Type.getMethodDescriptor(SIMPLE_TEMPLATE, STRING), true);

        // Loads the data into the stack
        var.load(mv);
    }

    @Override
    public void loadDataItem(DataSourceContext context, LocalVariable var) throws CompilerException {
        if(var.descClass != TemplateData.class) {
            throw new CompilerException("Can't parse a data item that is not a template data");
        }

        if(!context.vars.isEmpty()) {
            MethodVisitor mv = context.mv;
            LocalVariable topVar = context.vars.getLast();

            // d = new ScopedData(data, d);
            mv.visitTypeInsn(NEW, SCOPED_DATA.getInternalName());
            mv.visitInsn(DUP);
            topVar.load(mv);
            var.load(mv);
            mv.visitMethodInsn(INVOKESPECIAL, SCOPED_DATA.getInternalName(), "<init>",
                    Type.getMethodDescriptor(Type.VOID_TYPE, DATA, DATA), false);
            var.store(mv);
        }

        context.vars.add(var);
    }

    @Override
    public void unloadDataItem(DataSourceContext context, LocalVariable var) {
        context.vars.remove(var);
    }

    @Override
    public List<LocalVariable> getDataContext(DataSourceContext context) {
        if (context.vars.isEmpty()) return Collections.emptyList();
        return Collections.singletonList(context.vars.getLast());
    }

}
