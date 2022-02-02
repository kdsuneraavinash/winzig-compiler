package lexer.tokens;

public class MinutiaeToken {
    private final TokenKind kind;
    private final String content;

    public MinutiaeToken(TokenKind kind, String content) {
        this.kind = kind;
        this.content = content;
    }

    @Override
    public String toString() {
        return String.format("%s{%s}", kind, content);
    }
}
