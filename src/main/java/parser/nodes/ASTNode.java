package parser.nodes;

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
}
