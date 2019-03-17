package com.guichaguri.fastmustache.parser;

import com.guichaguri.fastmustache.template.CompilerOptions;
import com.guichaguri.fastmustache.parser.tokens.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a Mustache layout into tokens
 */
public class MustacheParser {

    private final CompilerOptions options;
    private final BufferedReader reader;

    private String delimiterLeft;
    private String delimiterRight;
    private boolean defaultDelimiters;

    private String currentLine;
    private int currentLineNumber = -1;
    private int currentPos;

    private SectionToken currentSection;
    private ArrayList<MustacheToken> tokens = new ArrayList<>();

    public MustacheParser(CompilerOptions options, BufferedReader reader) {
        this.options = options;
        this.reader = reader;
        this.delimiterLeft = this.options.getDelimiterLeft();
        this.delimiterRight = this.options.getDelimiterRight();
        this.defaultDelimiters = delimiterLeft.equals(CompilerOptions.DEFAULT.getDelimiterLeft()) &&
                delimiterRight.equals(CompilerOptions.DEFAULT.getDelimiterRight());
    }

    private boolean nextLine() throws IOException {
        currentLine = reader.readLine();
        currentLineNumber++;
        currentPos = 0;

        return currentLine != null && !currentLine.isEmpty();
    }

    public List<MustacheToken> parse() throws IOException {
        if(!nextLine()) return tokens;

        StringBuilder text = new StringBuilder();

        while(true) {
            int d1 = currentLine.indexOf(delimiterLeft, currentPos);

            if(d1 != -1) {
                int start = d1 + delimiterLeft.length();

                text.append(currentLine, currentPos, d1);
                addText(text.toString());
                text = new StringBuilder();

                parseDelimiter(start);
            } else {
                text.append(currentLine.substring(currentPos)).append('\n');
                if(!nextLine()) break;
            }
        }

        if(currentSection != null) {
            throw new ParseException("Section not closed", currentSection.line, currentSection.position);
        }

        if(text.length() > 0) {
            addText(text.toString());
        }

        return tokens;
    }

    private void parseDelimiter(int start) throws IOException {
        String tagContent;
        int d2 = currentLine.indexOf(delimiterRight, start);

        if(d2 != -1) {
            tagContent = currentLine.substring(start, d2);
        } else {
            int lineNum = currentLineNumber;
            StringBuilder content = new StringBuilder();
            content.append(currentLine.substring(start));

            while(true) {
                if(!nextLine()) {
                    throw new ParseException("Tag not closed", lineNum, start);
                }

                d2 = currentLine.indexOf(delimiterRight, currentPos);

                if(d2 == -1) {
                    content.append(currentLine);
                } else {
                    content.append(currentLine, 0, d2);
                    break;
                }
            }

            tagContent = content.toString();
        }

        currentPos = d2 + delimiterRight.length();

        boolean tripleDelimiter = defaultDelimiters &&
                tagContent.charAt(0) == '{' &&
                currentLine.length() > currentPos + 1 &&
                currentLine.charAt(currentPos + 1) == '}';

        if(tripleDelimiter) {
            tagContent = tagContent.substring(1);
            currentPos++;
        }

        parseTag(tagContent.trim(), tripleDelimiter);
    }

    private void parseTag(String tag, boolean tripleDelimiter) {
        char symbol = tag.charAt(0);

        if(symbol == '!') {
            // Comment
            return;
        }

        if(symbol == '#') {
            // Open Section

            currentSection = openSection(tag.substring(1).trim(), false);

        } else if(symbol == '^') {
            // Open Inverted Section

            currentSection = openSection(tag.substring(1).trim(), true);

        } else if(symbol == '/') {
            // Close Section

            if(currentSection == null) {
                throw new ParseException("Invalid section close", currentLineNumber, currentPos);
            }

            SectionToken section = currentSection;
            String sectionName = tag.substring(1).trim();

            if(!section.variable.equals(sectionName)) {
                throw new ParseException("Unknown section name", currentLineNumber, currentPos);
            }

            currentSection = section.parent;
            addToken(section);

        } else if(symbol == '>') {
            // Partial

            addPartial(tag.substring(1).trim());

        } else if(symbol == '=' && tag.charAt(tag.length() - 1) == '=') {
            // Change delimiters

            String[] delimiters = tag.substring(1, tag.length() - 2).trim().split(" ", 2);

            if(delimiters.length < 2) {
                throw new ParseException("Couldn't parse the delimiters", currentLineNumber, currentPos);
            }

            delimiterLeft = delimiters[0].trim();
            delimiterRight = delimiters[1].trim();
            defaultDelimiters = delimiterLeft.equals(CompilerOptions.DEFAULT.getDelimiterLeft()) &&
                    delimiterRight.equals(CompilerOptions.DEFAULT.getDelimiterRight());

            if(delimiterLeft.isEmpty() || delimiterRight.isEmpty()) {
                throw new ParseException("The delimiters cannot be empty", currentLineNumber, currentPos);
            }

        } else if(symbol == '&') {
            // Unescaped Variable

            addVariable(tag.substring(1).trim(), false);

        } else if(tripleDelimiter) {
            // Unescaped Variable

            addVariable(tag, false);

        } else {
            // Regular Variable

            addVariable(tag, options.isEscapingEnabled());

        }
    }

    private void addToken(MustacheToken token) {
        if(currentSection != null) {
            currentSection.content.add(token);
        } else {
            tokens.add(token);
        }
    }

    private void addText(String text) {
        if(text.isEmpty()) return;

        TextToken token = new TextToken();
        token.line = currentLineNumber;
        token.position = currentPos;
        token.parent = currentSection;
        token.text = text;
        addToken(token);
    }

    private SectionToken openSection(String variable, boolean inverted) {
        if(variable.isEmpty()) {
            throw new ParseException("The section name cannot be empty", currentLineNumber, currentPos);
        }

        SectionToken token = new SectionToken();
        token.line = currentLineNumber;
        token.position = currentPos;
        token.parent = currentSection;
        token.variable = variable.trim();
        token.inverted = inverted;
        token.content = new ArrayList<>();
        return token;
    }

    private void addVariable(String variable, boolean escaped) {
        if(variable.isEmpty()) {
            throw new ParseException("The tag cannot be empty", currentLineNumber, currentPos);
        }

        VariableToken token = new VariableToken();
        token.line = currentLineNumber;
        token.position = currentPos;
        token.parent = currentSection;
        token.variable = variable;
        token.escaped = escaped;
        addToken(token);
    }

    private void addPartial(String partial) {
        if(partial.isEmpty()) {
            throw new ParseException("The partial cannot be empty", currentLineNumber, currentPos);
        }

        PartialToken token = new PartialToken();
        token.line = currentLineNumber;
        token.position = currentPos;
        token.parent = currentSection;
        token.partial = partial;
        addToken(token);
    }

}
