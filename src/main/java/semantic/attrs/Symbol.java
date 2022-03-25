package semantic.attrs;

public class Symbol {
    private final String name;
    private final int position;
    private final SemanticType type;
    private final boolean isConstant;

    public Symbol(String name, int position, SemanticType type, boolean isConstant) {
        this.name = name;
        this.position = position;
        this.type = type;
        this.isConstant = isConstant;
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

    public boolean isConstant() {
        return isConstant;
    }
}
