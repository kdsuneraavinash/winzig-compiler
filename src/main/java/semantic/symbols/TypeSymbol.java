package semantic.symbols;

import semantic.attrs.SymbolType;

public class TypeSymbol extends Symbol {
    public TypeSymbol(String name, boolean isGlobal) {
        super(name, SymbolType.TYPE, isGlobal);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", symbolType, name);
    }
}
