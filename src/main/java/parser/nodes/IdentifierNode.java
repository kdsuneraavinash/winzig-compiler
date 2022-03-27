package parser.nodes;

import lexer.tokens.Token;

public class IdentifierNode extends TokenNode {
    private final TokenNode child;

    public IdentifierNode(Token token) {
        super(token.getKind(), token.getKind().getValue(), token);
        this.child = new TokenNode(token);
    }

    public String getIdentifierValue() {
        return child.getValue();
    }

    @Override
    public String toString() {
        return String.format("%s(1)", value);
    }

    public String getValue() {
        return value;
    }
}
