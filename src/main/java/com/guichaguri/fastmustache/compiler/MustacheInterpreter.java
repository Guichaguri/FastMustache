package com.guichaguri.fastmustache.compiler;

import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Guichaguri
 */
public class MustacheInterpreter {

    protected final CompilerOptions options = CompilerOptions.DEFAULT;
    protected final MustacheCompiler compiler;
    protected final BufferedReader reader;

    protected String currentLine;
    protected int currentLineNumber = -1;
    protected int currentPos;

    public MustacheInterpreter(MustacheCompiler compiler, BufferedReader reader) {
        this.compiler = compiler;
        this.reader = reader;
    }

    protected boolean nextLine() throws IOException {
        currentLine = reader.readLine();
        currentLineNumber++;
        currentPos = 0;

        return currentLine != null && !currentLine.isEmpty();
    }

    protected void parse() throws IOException, CompilerException {
        StringBuilder builder = new StringBuilder();
        String delimiterLeft = options.getDelimiterLeft();
        String delimiterRight = options.getDelimiterRight();

        if(!nextLine()) return;

        while(true) {
            int pos1 = currentLine.indexOf(delimiterLeft, currentPos);

            if(pos1 == -1) {
                builder.append(currentLine.substring(currentPos, currentLine.length()));

                if(nextLine()) continue;
                break;
            }

            int pos2 = currentLine.indexOf(delimiterRight, currentPos);//TODO make it work with }}}

            if(pos2 == -1) {
                builder.append(currentLine.substring(currentPos, currentLine.length()));

                if(nextLine()) continue;
                break;
            }

            builder.append(currentLine.substring(currentPos, pos1));
            compiler.getGenerator().insertString(builder.toString());

            parseTag(currentLine.substring(pos1 + delimiterLeft.length(), pos2));

            builder = new StringBuilder();
            currentPos = pos2 + delimiterRight.length();
        }

        if(builder.length() > 0) {
            compiler.getGenerator().insertString(builder.toString());
        }
    }

    protected void parseTag(String tag) throws CompilerException {
        BytecodeGenerator generator = compiler.getGenerator();
        char c = tag.charAt(0);

        if(c == '!') {
            // Comment
        } else if(c == '#') {
            // Condition Open
            generator.insertSectionStart(tag.substring(1).trim(), false);
        } else if(c == '^') {
            // Inverted Condition Open
            generator.insertSectionStart(tag.substring(1).trim(), true);
        } else if(c == '/') {
            // Condition Close
            generator.insertSectionEnd(tag.substring(1).trim());
        } else if(c == '>') {
            // Partial
            //TODO
        } else if(c == '=' && tag.charAt(tag.length() - 1) == '=') {
            // Set Delimiter
            updateDelimiters(tag.substring(1, tag.length() - 1).trim());
        } else if(c == '&') {
            // Unescaped Variable
            generator.insertVariable(tag.substring(1).trim(), false);
        } else if(c == '{' && tag.charAt(tag.length() - 1) == '}') {
            // Unescaped Variable
            generator.insertVariable(tag.substring(1, tag.length() - 1).trim(), false);
        } else {
            // Variable
            generator.insertVariable(tag.trim(), true);
        }
    }

    protected void updateDelimiters(String d) throws CompilerException {
        int space = d.indexOf(' ');

        if(space == -1) {
            d = d.trim();
            options.setDelimiterLeft(d);
            options.setDelimiterRight(d);
        } else {
            options.setDelimiterLeft(d.substring(0, space).trim());
            options.setDelimiterRight(d.substring(space + 1).trim());
        }
    }

}
