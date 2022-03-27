package lexer.tokens;

import java.util.List;

public class IdentifierToken extends Token {
    private final String value;

    public IdentifierToken(String value, List<Minutiae> leading, List<Minutiae> trailing,
                           int startOffset, int endOffset) {
        super(TokenKind.IDENTIFIER, leading, trailing, startOffset, endOffset);
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
