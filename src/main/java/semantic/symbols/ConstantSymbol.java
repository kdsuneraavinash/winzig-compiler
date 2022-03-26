package semantic.symbols;

import semantic.attrs.SymbolType;

public class ConstantSymbol extends Symbol {
    public final TypeSymbol type;
    public final int value;

    public ConstantSymbol(String name, TypeSymbol type, int value, boolean isGlobal) {
        super(name, SymbolType.CONSTANT, isGlobal);
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s = %s", symbolType, name, type.name, value);
    }
}
