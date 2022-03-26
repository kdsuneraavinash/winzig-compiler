package semantic.symbols;

import semantic.attrs.SymbolType;

import java.util.Objects;

public abstract class Symbol {
    public final String name;
    public final SymbolType symbolType;
    public final boolean isGlobal;

    protected Symbol(String name, SymbolType symbolType, boolean isGlobal) {
        this.name = name;
        this.symbolType = symbolType;
        this.isGlobal = isGlobal;
    }

    @Override
    public String toString() {
        return "[" + symbolType + "] " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Symbol)) return false;
        Symbol symbol = (Symbol) o;
        return isGlobal == symbol.isGlobal
                && name.equals(symbol.name)
                && symbolType == symbol.symbolType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, symbolType, isGlobal);
    }
}

