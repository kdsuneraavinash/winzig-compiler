package parser.nodes;

import lexer.tokens.Token;
import lexer.tokens.TokenKind;

public class TokenNode implements Node {
    protected final TokenKind kind;
    protected final String value;
    protected final Token token;

    public TokenNode(Token token) {
        this(token.getKind(), token.getValue(), token);
    }

    protected TokenNode(TokenKind kind, String value, Token token) {
        this.kind = kind;
        this.value = value;
        this.token = token;
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

    @Override
    public int getStartOffset() {
        return token.getStartOffset();
    }

    @Override
    public int getEndOffset() {
        return token.getEndOffset();
    }
}
