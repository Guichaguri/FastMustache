package com.guichaguri.fastmustache.compiler.parser.tokens;

import com.guichaguri.fastmustache.compiler.CompilerException;
import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator2;
import com.guichaguri.fastmustache.compiler.bytecode.data.DataManager;
import com.guichaguri.fastmustache.compiler.bytecode.sections.LoopSection;
import com.guichaguri.fastmustache.compiler.bytecode.sections.Section;
import com.guichaguri.fastmustache.template.MustacheType;

import java.util.List;

/**
 * Represents a section (condition, loop, lambda)
 */
public class SectionToken extends MustacheToken {

    public String variable;
    public boolean inverted;
    public List<MustacheToken> content;

    @Override
    public void add(BytecodeGenerator2 generator, DataManager data) throws CompilerException {
        MustacheType type = data.getType(variable);
        Section section;

        if(type == MustacheType.ARRAY) {
            generator.addArrayLoop(this);
            // TODO collection
        } else if(type == MustacheType.LAMBDA) {

        } else if(type == MustacheType.BOOLEAN) {
            generator.addCondition(this);
        } else {
            generator.addArrayLoop(this);
        }

        // TODO
    }

    @Override
    public String toString() {
        return "SectionToken{" +
                "variable='" + variable + '\'' +
                ", inverted=" + inverted +
                ", content=" + content +
                '}';
    }
}
