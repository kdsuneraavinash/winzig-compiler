package common.nodes;

import common.SyntaxKind;

import java.util.ArrayList;
import java.util.List;

public class ASTNode extends Node {
    private final List<Node> children;

    public ASTNode(SyntaxKind kind) {
        super(kind);
        this.children = new ArrayList<>();
    }

    public void addChild(Node node) {
        this.children.add(node);
    }

    public List<Node> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", value, children.size());
    }
}
