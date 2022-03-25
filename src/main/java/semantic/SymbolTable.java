package semantic;

import semantic.attrs.Symbol;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Symbol> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
    }

    public void enter(Symbol symbol) {
        if (symbols.containsKey(symbol.getName())) {
            throw new IllegalStateException("Redefinition of symbol " + symbol.getName());
        }
        symbols.put(symbol.getName(), symbol);
    }

    public Symbol lookup(String name) {
        if (!symbols.containsKey(name)) {
            throw new IllegalStateException("Undefined symbol " + name);
        }
        return symbols.getOrDefault(name, null);
    }

    public boolean isDefined(String name) {
        return symbols.containsKey(name);
    }
}
