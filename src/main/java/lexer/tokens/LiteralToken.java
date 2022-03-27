package lexer.tokens;

import java.util.List;

public class LiteralToken extends Token {
    private final String value;

    public LiteralToken(TokenKind kind, String value, List<Minutiae> leading, List<Minutiae> trailing,
                        int startOffset, int endOffset) {
        super(kind, leading, trailing, startOffset, endOffset);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s{%s}", kind, value);
    }
}
