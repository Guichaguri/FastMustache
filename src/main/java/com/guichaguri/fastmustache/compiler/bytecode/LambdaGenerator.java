package com.guichaguri.fastmustache.compiler.bytecode;

import com.guichaguri.fastmustache.compiler.bytecode.data.DataSource;
import com.guichaguri.fastmustache.compiler.bytecode.data.MemberType;
import com.guichaguri.fastmustache.template.CompilerOptions;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class LambdaGenerator extends BytecodeGenerator2 {

    protected MethodNode methodNode;

    protected List<LambdaArgument> additionalArguments;
    protected List<LambdaArgument> mainArguments;

    protected LambdaArgument builderArg;
    protected LambdaArgument dataArg;

    public LambdaGenerator(MustacheCompiler compiler, CompilerOptions options, DataSource data) {
        super(compiler, options, data);
    }

    public void startLambda(BytecodeGenerator2 generator, MemberType lambdaType, String methodName) throws CompilerException {
        mv = methodNode = new MethodNode(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, methodName, null, null, null);

        mv.visitCode();
        mv.visitLabel(start);

        List<LocalVariable> originalVars = generator.data.getDataContext(generator.context);
        insertArguments(generator, lambdaType, originalVars);

        name = methodName;
        context = data.createContext(this, mv, dataVar);

        for(LocalVariable var : originalVars) {
            // Finds the argument index to load in the same order as the original ones
            LambdaArgument arg = findArgument(additionalArguments, var);
            if (arg == null) arg = findArgument(mainArguments, var);

            if (arg != null) {
                data.loadDataItem(context, arg.variable);
            }
        }
    }

    public void endLambda() throws CompilerException {
        if (methodNode == null) {
            throw new CompilerException("A lambda section is not started");
        }

        clearStack();
        mv.visitInsn(RETURN);
        mv.visitLabel(end);

        // Looks into all additional arguments and remove the ones that are not being used
        for(int i = additionalArguments.size() - 1; i >= 0; i--) {
            LambdaArgument arg = additionalArguments.get(i);
            if (arg.variable.usages > 0) continue;

            additionalArguments.remove(i);

            // Shifts all other variables after the argument to keep it sequential
            CompilerUtils.removeVariable(methodNode, locals, arg.variable);
        }

        // Updates the descriptor to match the new argument list
        methodNode.desc = Type.getMethodDescriptor(Type.VOID_TYPE, getMethodArgumentTypes());

        for(LocalVariable var : locals) {
            if (!var.declared) continue;

            mv.visitLocalVariable("var" + var.index, var.desc, null,
                    var.start, var.end == null ? end : var.end, var.index);
        }

        mv.visitMaxs(10, locals.size());//TODO
        mv.visitEnd();

        // Writes the class
        methodNode.accept(compiler.getClassWriter());
    }

    public Type[] getAdditionalTypes() {
        int size = additionalArguments.size();
        Type[] types = new Type[size];

        for(int i = 0; i < size; i++) {
            types[i] = additionalArguments.get(i).type;
        }

        return types;
    }

    public Type[] getMethodArgumentTypes() {
        int additionalSize = additionalArguments.size();
        int mainSize = mainArguments.size();
        Type[] types = new Type[additionalSize + mainSize];

        for(int i = 0; i < additionalSize; i++) {
            types[i] = additionalArguments.get(i).type;
        }

        for(int i = 0; i < mainSize; i++) {
            types[additionalSize + i] = mainArguments.get(i).type;
        }

        return types;
    }

    public String getMethodDescriptor() {
        return methodNode.desc;
    }

    /**
     * Adds all data variables as arguments, including the {@link StringBuilder}
     */
    private void insertArguments(BytecodeGenerator2 generator, MemberType lambdaType, List<LocalVariable> originalVars) throws CompilerException {
        LocalVariable originalDataVar = CompilerUtils.findLocalVariable(originalVars, lambdaType);

        if (originalDataVar == null) {
            throw new CompilerException("No data variable with the type " + lambdaType.clazz + " found in the context");
        }

        additionalArguments = new ArrayList<>(originalVars.size() - 1);
        mainArguments = new ArrayList<>(2);

        for(LocalVariable var : originalVars) {
            // Skips the data variable that we want to pass as the last argument
            if (var == originalDataVar) continue;
            additionalArguments.add(insertArgumentLocal(var, Type.getType(var.desc), start));
        }

        builderArg = insertArgumentLocal(generator.builderVar, BUILDER, start);
        dataArg = insertArgumentLocal(originalDataVar, data.getDataType(), start);

        mainArguments.add(builderArg);
        mainArguments.add(dataArg);

        builderVar = builderArg.variable;
        dataVar = dataArg.variable;
    }

    private LambdaArgument insertArgumentLocal(LocalVariable var, Type type, Label start) {
        LocalVariable local = new LocalVariable(locals.size(), var.desc, var.descClass, true);
        local.start = start;
        locals.add(local);

        LambdaArgument argument = new LambdaArgument();
        argument.original = var;
        argument.variable = local;
        argument.type = type;
        return argument;
    }

    private LambdaArgument findArgument(List<LambdaArgument> list, LocalVariable original) {
        for(LambdaArgument arg : list) {
            if (arg.original == original) return arg;
        }
        return null;
    }


    protected static class LambdaArgument {
        protected LocalVariable original;
        protected LocalVariable variable;
        protected Type type;
    }

}
