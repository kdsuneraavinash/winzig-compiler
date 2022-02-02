package common.nodes;

import common.SyntaxKind;

public class IdentifierNode extends Node {
    private final Node child;

    public IdentifierNode(SyntaxKind kind, Node child) {
        super(kind);
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
