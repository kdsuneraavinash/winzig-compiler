package semantic.symbols;

import semantic.attrs.SemanticType;

public class TypeSymbol extends Symbol {
    public TypeSymbol(String name) {
        super(name, SemanticType.TYPE);
    }

    @Override
    public String toString() {
        return "[" + getSymbolType() + "] " + getName();
    }
}
