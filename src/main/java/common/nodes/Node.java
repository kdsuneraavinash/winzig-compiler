package common.nodes;

import common.SyntaxKind;
import lexer.tokens.SyntaxToken;

public class Node {
    protected final SyntaxKind kind;
    protected final String value;

    public Node(SyntaxToken token) {
        this.kind = token.getKind();
        this.value = token.getValue();
    }

    protected Node(SyntaxKind kind) {
        this.kind = kind;
        this.value = kind.getValue();
    }

    @Override
    public String toString() {
        return String.format("%s(0)", value);
    }
}
