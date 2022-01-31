package lexer.tokens;

import common.SyntaxKind;

public class MinutiaeToken {
    private final SyntaxKind kind;
    private final String content;

    public MinutiaeToken(SyntaxKind kind, String content) {
        this.kind = kind;
        this.content = content;
    }

    @Override
    public String toString() {
        return String.format("%s{%s}", kind, content);
    }
}
