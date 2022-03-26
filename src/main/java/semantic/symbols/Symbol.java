package semantic.symbols;

import semantic.attrs.SemanticType;

public abstract class Symbol {
    private final String name;
    private final SemanticType symbolType;

    protected Symbol(String name, SemanticType symbolType) {
        this.name = name;
        this.symbolType = symbolType;
    }

    public String getName() {
        return name;
    }

    public SemanticType getSymbolType() {
        return symbolType;
    }

    @Override
    public String toString() {
        return "[" + symbolType + "] " + name;
    }
}

