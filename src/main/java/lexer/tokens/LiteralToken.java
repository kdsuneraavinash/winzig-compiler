package lexer.tokens;

import java.util.List;

public class LiteralToken extends SyntaxToken {
    private final String value;

    public LiteralToken(TokenKind kind, String value, List<MinutiaeToken> leading, List<MinutiaeToken> trailing) {
        super(kind, leading, trailing);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s{%s}", kind, value);
        // return String.format("%s{value=%s, leading=%s, trailing=%s}", kind, value, leading, trailing);
    }
}
