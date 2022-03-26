package semantic.symbols;

import semantic.attrs.SemanticType;

public class ConstantSymbol extends Symbol {
    private final TypeSymbol type;
    private final int value;

    public ConstantSymbol(String name, TypeSymbol type, int value) {
        super(name, SemanticType.VARIABLE);
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s = %s", getSymbolType(), getName(), getType().getName(), getValue());
    }

    public TypeSymbol getType() {
        return type;
    }

    public int getValue() {
        return value;
    }
}
