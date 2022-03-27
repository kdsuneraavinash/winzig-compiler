package semantic.symbols;

import semantic.attrs.SymbolType;

public class VariableSymbol extends Symbol {
    public final int address;
    public final TypeSymbol typeSymbol;

    public VariableSymbol(String name, TypeSymbol typeSymbol, int address, boolean isGlobal) {
        super(name, SymbolType.VARIABLE, isGlobal);
        this.typeSymbol = typeSymbol;
        this.address = address;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s@%d", symbolType, name, typeSymbol.name, address);
    }
}
