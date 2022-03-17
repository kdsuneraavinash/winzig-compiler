package parser.nodes;

import lexer.tokens.Token;

public class IdentifierNode extends TokenNode {
    private final Node child;

    public IdentifierNode(Token token) {
        super(token.getKind());
        this.child = new TokenNode(token);
    }

    public Node getChild() {
        return child;
    }

    @Override
    public String toString() {
        return String.format("%s(1)", value);
    }

    public String getValue() {
        return value;
    }
}
