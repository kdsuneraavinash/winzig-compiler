package semantic.attrs;

public class Symbol {
    private final String name;
    private final int position;
    private final SemanticType type;
    private final Label label;

    public Symbol(String name, SemanticType type, int position, Label label) {
        this.name = name;
        this.type = type;
        this.position = position;
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public SemanticType getType() {
        return type;
    }

    public Label getLabel() {
        return label;
    }

    @Override
    public String toString() {
        String str = "[" + type + "] " + name;
        if (position != -1) str += "@" + position;
        if (label != null) str += "#" + label.getLabel();
        return str;
    }
}
