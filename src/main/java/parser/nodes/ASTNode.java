package parser.nodes;

import diagnostics.TextHighlighter;

import java.util.ArrayList;
import java.util.List;

public class ASTNode implements Node {
    private final NodeKind kind;
    private final List<Node> children;

    public ASTNode(NodeKind kind) {
        this.kind = kind;
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
        return String.format("%s(%s)", kind.getValue(), children.size());
    }

    public NodeKind getKind() {
        return kind;
    }

    public Node getChild(int i) {
        return children.get(i);
    }

    public int getSize() {
        return children.size();
    }

    @Override
    public String highlighted(TextHighlighter highlighter) {
        if (children.size() > 0) {
            return children.get(0).highlighted(highlighter);
        }
        return "";
    }
}
