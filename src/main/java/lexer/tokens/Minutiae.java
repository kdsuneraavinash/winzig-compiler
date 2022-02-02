package lexer.tokens;

public class Minutiae {
    private final TokenKind kind;
    private final String content;

    public Minutiae(TokenKind kind, String content) {
        this.kind = kind;
        this.content = content;
    }

    @Override
    public String toString() {
        return String.format("%s{%s}", kind, content);
    }
}
