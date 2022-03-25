package parser.nodes;

import lexer.tokens.Token;
import lexer.tokens.TokenKind;

public class TokenNode implements Node {
    protected final TokenKind kind;
    protected final String value;

    public TokenNode(Token token) {
        this.kind = token.getKind();
        this.value = token.getValue();
    }

    protected TokenNode(TokenKind kind) {
        this.kind = kind;
        this.value = kind.getValue();
    }

    public TokenKind getKind() {
        return kind;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s(0)", value);
    }
}
