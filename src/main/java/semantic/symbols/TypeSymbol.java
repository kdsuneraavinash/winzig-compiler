package semantic.symbols;

import semantic.attrs.SymbolType;
import semantic.table.SymbolTable;

public class TypeSymbol extends Symbol {
    public TypeSymbol(String name, boolean isGlobal) {
        super(name, SymbolType.TYPE, isGlobal);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", symbolType, name);
    }

    public boolean isInteger() {
        return equals(SymbolTable.INTEGER_TYPE);
    }

    public boolean isChar() {
        return equals(SymbolTable.CHAR_TYPE);
    }

    public boolean isBoolean() {
        return equals(SymbolTable.BOOLEAN_TYPE);
    }
}
