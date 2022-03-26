package semantic;

import semantic.attrs.Label;
import semantic.attrs.SemanticType;
import semantic.attrs.Symbol;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class SymbolTable {
    private final SymbolTable parent;
    private final Map<String, Symbol> symbols;
    private int top;

    public SymbolTable() {
        this(null);
        enterDclnSymbol("integer", SemanticType.TYPE);
        enterDclnSymbol("char", SemanticType.TYPE);
        enterDclnSymbol("boolean", SemanticType.TYPE);
    }

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
        symbols = new HashMap<>();
        this.top = 0;
    }

    public void extendTop(SymbolTable child) {
        this.top += child.top;
    }

    public void enterVarSymbol(String name, SemanticType type) {
        symbols.put(name, new Symbol(name, type, ++top, null));
    }

    public void enterDclnSymbol(String name, SemanticType type) {
        symbols.put(name, new Symbol(name, type, -1, null));
    }

    public void enterFcnSymbol(String name, SemanticType type, Label label) {
        symbols.put(name, new Symbol(name, type, -1, label));
    }

    /**
     * Get a symbol from the current scope.
     *
     * @param name Symbol name.
     * @return Symbol.
     */
    public Symbol lookup(String name) {
        if (symbols.containsKey(name)) return symbols.get(name);
        if (parent != null) return parent.lookup(name);
        return null;
    }

    /**
     * Check if a symbol is in the current scope.
     *
     * @param name Symbol name.
     * @return True if the symbol is in the current scope.
     */
    public boolean isDefined(String name) {
        return isDefined(name, true);
    }

    /**
     * Check if a symbol is in the current scope.
     *
     * @param name        Symbol name.
     * @param checkParent If true, check the parent scope.
     * @return True if the symbol is in the current scope.
     */
    public boolean isDefined(String name, boolean checkParent) {
        if (symbols.containsKey(name)) return true;
        if (checkParent && parent != null) return parent.isDefined(name);
        return false;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add("Top: " + top);
        sj.add("Symbols: ");
        for (Map.Entry<String, Symbol> entry : symbols.entrySet()) {
            sj.add("\t" + entry.getValue());
        }
        return sj.toString();
    }
}
