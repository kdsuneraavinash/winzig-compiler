package semantic.symbols;

import semantic.attrs.SymbolType;

public class VariableSymbol extends Symbol {
    public final int address;
    public final TypeSymbol type;

    public VariableSymbol(String name, TypeSymbol type, int address, boolean isGlobal) {
        super(name, SymbolType.VARIABLE, isGlobal);
        this.type = type;
        this.address = address;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s@%d", symbolType, name, type.name, address);
    }
}
