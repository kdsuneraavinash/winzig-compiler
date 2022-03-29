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
    public int getStartOffset() {
        if (getSize() > 0) {
            return children.get(0).getStartOffset();
        }
        return -1;
    }

    @Override
    public int getEndOffset() {
        if (getSize() > 0) {
            return children.get(getSize() - 1).getEndOffset();
        }
        return -1;
    }
}
