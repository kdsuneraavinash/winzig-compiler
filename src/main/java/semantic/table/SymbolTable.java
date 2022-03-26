package semantic.table;

import semantic.attrs.Label;
import semantic.symbols.ConstantSymbol;
import semantic.symbols.FcnSymbol;
import semantic.symbols.Symbol;
import semantic.symbols.TypeSymbol;
import semantic.symbols.VariableSymbol;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class SymbolTable {
    public static final TypeSymbol INTEGER_TYPE = new TypeSymbol("integer", true);
    public static final TypeSymbol CHAR_TYPE = new TypeSymbol("char", true);
    public static final TypeSymbol BOOLEAN_TYPE = new TypeSymbol("boolean", true);
    public static final TypeSymbol UNDEFINED_TYPE = new TypeSymbol("", true);

    private final Map<String, Symbol> globalSymbols;
    private final Map<String, Symbol> localSymbols;
    private int globalTop;
    private int localTop;
    private boolean isInLocalScope;

    public SymbolTable() {
        this.globalSymbols = new HashMap<>();
        this.globalSymbols.put("integer", INTEGER_TYPE);
        this.globalSymbols.put("char", CHAR_TYPE);
        this.globalSymbols.put("boolean", BOOLEAN_TYPE);
        this.localSymbols = new HashMap<>();
        this.globalTop = 0;
        this.localTop = 0;
        this.isInLocalScope = false;
    }

    /**
     * Begin a new local scope.
     * Will create a new symbol table for the local scope.
     */
    public void beginLocalScope() {
        assert !this.isInLocalScope;
        this.localTop = 0;
        this.localSymbols.clear();
        this.isInLocalScope = true;
    }

    /**
     * End the local scope.
     * Will return to the global scope and restore the top.
     */
    public void endLocalScope() {
        assert this.isInLocalScope;
        this.globalTop += this.localTop;
        this.localTop = 0;
        this.localSymbols.clear();
        this.isInLocalScope = false;
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Enter a new variable symbol.
     *
     * @param name the name of the variable.
     * @param type the type of the variable.
     */
    public void enterVariableSymbol(String name, TypeSymbol type) {
        if (isInLocalScope) localSymbols.put(name, new VariableSymbol(name, type, ++localTop, false));
        else globalSymbols.put(name, new VariableSymbol(name, type, ++globalTop, true));
    }

    /**
     * Enter a new constant symbol.
     *
     * @param name  the name of the constant.
     * @param type  the type of the constant.
     * @param value the value of the constant.
     */
    public void enterConstantSymbol(String name, TypeSymbol type, int value) {
        if (isInLocalScope) localSymbols.put(name, new ConstantSymbol(name, type, value, false));
        else globalSymbols.put(name, new ConstantSymbol(name, type, value, true));
    }

    /**
     * Enter a new type symbol.
     *
     * @param name the name of the type.
     */
    public void enterTypeSymbol(String name) {
        if (isInLocalScope) localSymbols.put(name, new TypeSymbol(name, false));
        else globalSymbols.put(name, new TypeSymbol(name, true));
    }

    /**
     * Enter a new function symbol.
     *
     * @param name       the name of the function.
     * @param label      the label of the function.
     * @param returnType the return type of the function.
     */
    public void enterFcnSymbol(String name, Label label, TypeSymbol returnType) {
        assert !isInLocalScope;
        globalSymbols.put(name, new FcnSymbol(name, label, returnType));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Get the symbol of the given name.
     * This will first look in the local scope, then in the global scope.
     * If the symbol is not found, null is returned.
     *
     * @param name the name of the symbol.
     * @return the symbol of the given name.
     */
    public Symbol lookup(String name) {
        if (isInLocalScope && localSymbols.containsKey(name)) return localSymbols.get(name);
        if (globalSymbols.containsKey(name)) return globalSymbols.get(name);
        return null;
    }

    /**
     * Get whether the given symbol is already defined.
     * If in local scope, this will check the local scope only.
     * Otherwise, the global scope will be checked.
     *
     * @param name the name of the symbol.
     * @return whether the given symbol is already defined.
     */
    public boolean alreadyDefinedInScope(String name) {
        if (isInLocalScope) return localSymbols.containsKey(name);
        return globalSymbols.containsKey(name);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add("Top:     " + globalTop);
        sj.add("Symbols: ");
        globalSymbols.forEach((key, value) -> sj.add("\t" + value));
        return sj.toString();
    }
}
