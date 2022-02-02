package common.nodes;

import common.SyntaxKind;

public class IdentifierNode extends Node {
    private final Node child;

    public IdentifierNode(Node child) {
        super(SyntaxKind.IDENTIFIER_EXPRESSION);
        this.child = child;
    }

    public Node getChild() {
        return child;
    }

    @Override
    public String toString() {
        return String.format("%s(1)", value);
    }
}
