package lexer;

import diagnostics.DiagnosticCollector;
import diagnostics.Highlightable;
import lexer.tokens.Minutiae;
import lexer.tokens.Token;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLexer extends DiagnosticCollector {
    protected final CharReader charReader;
    protected List<Minutiae> leadingMinutiae;

    public AbstractLexer(CharReader charReader) {
        this.charReader = charReader;
        this.leadingMinutiae = new ArrayList<>();
    }

    public abstract Token read();

    protected List<Minutiae> getLeadingMinutiae() {
        List<Minutiae> minutiae = leadingMinutiae;
        this.leadingMinutiae = new ArrayList<>();
        return minutiae;
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

    @Override
    public String highlightedSegment(Highlightable highlightable) {
        return charReader.highlightedSegment(highlightable);
    }
}
