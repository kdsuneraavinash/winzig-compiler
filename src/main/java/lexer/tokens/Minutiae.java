package lexer.tokens;

import diagnostics.Highlightable;

public class Minutiae implements Highlightable {
    private final TokenKind kind;
    private final String content;
    // Variables required for highlighted code for diagnostics.
    protected final int startOffset;
    protected final int endOffset;

    public Minutiae(TokenKind kind, String content, int startOffset, int endOffset) {
        this.kind = kind;
        this.content = content;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    @Override
    public String toString() {
        return String.format("%s{%s}", kind, content);
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
