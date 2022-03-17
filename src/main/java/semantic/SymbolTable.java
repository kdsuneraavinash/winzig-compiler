package semantic;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Integer> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
    }

    public void enter(String symbol, int position) {
        symbols.put(symbol, position);
    }

    public int lookup(String symbol) {
        return symbols.getOrDefault(symbol, 0);
    }
}
