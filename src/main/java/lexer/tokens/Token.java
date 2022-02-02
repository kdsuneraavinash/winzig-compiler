package lexer.tokens;

import java.util.List;

public class Token {
    protected final TokenKind kind;
    protected final List<Minutiae> leading;
    protected final List<Minutiae> trailing;

    public Token(TokenKind kind, List<Minutiae> leading, List<Minutiae> trailing) {
        this.kind = kind;
        this.leading = leading;
        this.trailing = trailing;
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
}
