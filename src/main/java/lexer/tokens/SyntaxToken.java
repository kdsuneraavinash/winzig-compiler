package lexer.tokens;

import java.util.List;

public class SyntaxToken {
    protected final TokenKind kind;
    protected final List<MinutiaeToken> leading;
    protected final List<MinutiaeToken> trailing;

    public SyntaxToken(TokenKind kind, List<MinutiaeToken> leading, List<MinutiaeToken> trailing) {
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
        // return String.format("%s{leading=%s, trailing=%s}", kind, leading, trailing);
    }
}
