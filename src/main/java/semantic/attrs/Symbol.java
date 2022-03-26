package semantic.attrs;

public class Symbol {
    private final String name;
    private final int position;
    private final SemanticType type;

    public Symbol(String name, int position, SemanticType type) {
        this.name = name;
        this.position = position;
        this.type = type;
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

    @Override
    public String toString() {
        String str = "[" + type + "] " + name;
        if (position != -1) str += "@" + position;
        return str;
    }
}
