package com.guichaguri.fastmustache.compiler;

import com.guichaguri.fastmustache.compiler.bytecode.BytecodeGenerator;
import com.guichaguri.fastmustache.compiler.options.CompilerOptions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Parses a Mustache template and generates bytecode through a {@link BytecodeGenerator}
 *
 * @author Guichaguri
 */
public class MustacheParser {

    protected final CompilerOptions options = CompilerOptions.DEFAULT;
    protected final MustacheCompiler compiler;
    protected final BufferedReader reader;

    protected String currentLine;
    protected int currentLineNumber = -1;
    protected int currentPos;

    public MustacheParser(MustacheCompiler compiler, Reader reader) {
        this.compiler = compiler;
        this.reader = reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
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
        boolean defDelimiter = options.isUsingDefaultDelimiters();

        if(!nextLine()) return;

        while(true) {
            int pos1 = currentLine.indexOf(delimiterLeft, currentPos);

            if(pos1 == -1) {
                builder.append(currentLine.substring(currentPos, currentLine.length()));

                if(nextLine()) continue;
                break;
            }

            boolean tripleMustache = defDelimiter && currentLine.charAt(pos1 + delimiterLeft.length()) == '{';
            int pos2 = currentLine.indexOf(delimiterRight, currentPos);

            if(pos2 == -1 || (tripleMustache && currentLine.charAt(pos2 + delimiterRight.length()) != '}')) {
                builder.append(currentLine.substring(currentPos, currentLine.length()));

                if(nextLine()) continue;
                break;
            }

            int leftLength = delimiterLeft.length();
            int rightLength = delimiterLeft.length();

            if(tripleMustache) {
                leftLength++;
                rightLength++;
            }

            builder.append(currentLine.substring(currentPos, pos1));
            compiler.getGenerator().insertString(builder.toString());

            parseTag(currentLine.substring(pos1 + leftLength, pos2), tripleMustache);

            builder = new StringBuilder();
            currentPos = pos2 + rightLength;
        }

        if(builder.length() > 0) {
            compiler.getGenerator().insertString(builder.toString());
        }
    }

    private void parseTag(String tag, boolean tripleMustache) throws CompilerException, IOException {
        BytecodeGenerator generator = compiler.getGenerator();

        if(tripleMustache) {
            // Unescaped Variable
            generator.insertVariable(tag.trim(), false);
            return;
        }

        char c = tag.charAt(0);

        if(c == '!') {
            // Comment
        } else if(c == '#') {
            // Section Open
            generator.insertSectionStart(tag.substring(1).trim(), false);
        } else if(c == '^') {
            // Inverted Section Open
            generator.insertSectionStart(tag.substring(1).trim(), true);
        } else if(c == '/') {
            // Section Close
            generator.insertSectionEnd(tag.substring(1).trim());
        } else if(c == '>') {
            // Partial
            parsePartial(tag.substring(1).trim());
        } else if(c == '=' && tag.charAt(tag.length() - 1) == '=') {
            // Set Delimiter
            updateDelimiters(tag.substring(1, tag.length() - 1).trim());
        } else if(c == '&') {
            // Unescaped Variable
            generator.insertVariable(tag.substring(1).trim(), false);
        } else {
            // Variable
            generator.insertVariable(tag.trim(), true);
        }
    }

    private void parsePartial(String tag) throws IOException, CompilerException {
        Reader reader = options.getResolver().resolve(compiler.getTemplateName(), tag);

        if(reader == null) {
            throw new CompilerException("The partial " + tag + " couldn't be resolved.");
        }

        new MustacheParser(compiler, reader).parse();
    }

    private void updateDelimiters(String d) throws CompilerException {
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
