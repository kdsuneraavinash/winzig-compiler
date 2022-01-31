package lexer.tokens;

import common.SyntaxKind;

import java.util.List;

public class SyntaxToken {
    protected final SyntaxKind kind;
    protected final List<MinutiaeToken> leading;
    protected final List<MinutiaeToken> trailing;

    public SyntaxToken(SyntaxKind kind, List<MinutiaeToken> leading, List<MinutiaeToken> trailing) {
        this.kind = kind;
        this.leading = leading;
        this.trailing = trailing;
    }

    public SyntaxKind getKind() {
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
