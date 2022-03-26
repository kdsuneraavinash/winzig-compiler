package semantic.symbols;

import semantic.attrs.SemanticType;

public abstract class Symbol {
    private final String name;
    private final SemanticType type;

    protected Symbol(String name, SemanticType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public SemanticType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "[" + type + "] " + name;
    }
}

