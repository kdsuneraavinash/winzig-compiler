package semantic;

import semantic.attrs.SemanticType;
import semantic.attrs.Symbol;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private final Scope parent;
    private final Map<String, Symbol> symbols;
    public int next;
    public int top;
    public SemanticType type;

    public Scope() {
        this(null);
    }

    public Scope(Scope parent) {
        this.parent = parent;
        symbols = new HashMap<>();
        this.next = 1;
        this.top = 0;
        this.type = SemanticType.NOT_SET;
    }

    /**
     * Enter a symbol to the current scope.
     *
     * @param symbol Symbol to enter.
     */
    public void enter(Symbol symbol) {
        symbols.put(symbol.getName(), symbol);
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
        if (symbols.containsKey(name)) return true;
        if (parent != null) return parent.isDefined(name);
        return false;
    }

    @Override
    public String toString() {


        return "Scope{" +
                "parent=" + parent +
                ", symbols=" + symbols +
                ", next=" + next +
                ", top=" + top +
                ", type=" + type +
                '}';
    }
}
