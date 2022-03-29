package lexer.tokens;

import diagnostics.Highlightable;

import java.util.List;

public class Token implements Highlightable {
    protected final TokenKind kind;
    protected final List<Minutiae> leading;
    protected final List<Minutiae> trailing;
    // Variables required for highlighted code for diagnostics.
    protected final int startOffset;
    protected final int endOffset;

    public Token(TokenKind kind, List<Minutiae> leading, List<Minutiae> trailing,
                 int startOffset, int endOffset) {
        this.kind = kind;
        this.leading = leading;
        this.trailing = trailing;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public TokenKind getKind() {
        return kind;
    }

    public String getValue() {
        return kind.getValue();
    }

    @Override
    public String toString() {
        return kind.toString();
    }

    @Override
    public int getStartOffset() {
        return startOffset;
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }
}
