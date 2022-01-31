package parser;

import common.SyntaxKind;
import lexer.tokens.SyntaxToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Node {
    private final SyntaxKind kind;
    private final String value;
    private final List<Node> children;

    public Node(SyntaxToken token) {
        this.kind = token.getKind();
        this.value = token.getValue();
        this.children = new ArrayList<>();
    }

    public Node(SyntaxKind kind) {
        this.kind = kind;
        this.value = kind.getValue();
        this.children = new ArrayList<>();
    }

    public void addChild(Node node) {
        this.children.add(node);
    }

    public void addChildren(Collection<Node> nodes) {
        this.children.addAll(nodes);
    }

    public List<Node> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return String.format("%s{%s}", kind, value);
    }
}
