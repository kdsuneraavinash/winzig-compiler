package lexer;

import lexer.tokens.MinutiaeToken;
import lexer.tokens.SyntaxToken;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLexer {
    protected final CharReader charReader;
    protected List<MinutiaeToken> leadingMinutiae;

    public AbstractLexer(CharReader charReader) {
        this.charReader = charReader;
        this.leadingMinutiae = new ArrayList<>();
    }

    public boolean isEOF() {
        return charReader.isEOF();
    }

    public abstract SyntaxToken read();

    protected List<MinutiaeToken> getLeadingMinutiae() {
        List<MinutiaeToken> minutiaeTokens = leadingMinutiae;
        this.leadingMinutiae = new ArrayList<>();
        return minutiaeTokens;
    }

    protected static boolean isDigit(int c) {
        return ('0' <= c && c <= '9');
    }

    protected static boolean isAlphaChar(int c) {
        return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z');
    }

    protected static boolean isUnderscore(int c) {
        return (c == '_');
    }

    protected static boolean isIdentifierInitialChar(int c) {
        return isAlphaChar(c) || isUnderscore(c);
    }

    protected static boolean isIdentifierChar(int c) {
        return isAlphaChar(c) || isUnderscore(c) || isDigit(c);
    }
}
