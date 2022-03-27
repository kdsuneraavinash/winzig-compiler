package semantic;

import semantic.attrs.Label;
import semantic.symbols.ConstantSymbol;
import semantic.symbols.FcnSymbol;
import semantic.symbols.Symbol;
import semantic.symbols.TypeSymbol;
import semantic.symbols.VariableSymbol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class SymbolTable {
    public static final TypeSymbol INTEGER_TYPE = new TypeSymbol("integer", true);
    public static final TypeSymbol CHAR_TYPE = new TypeSymbol("char", true);
    public static final TypeSymbol BOOLEAN_TYPE = new TypeSymbol("boolean", true);
    public static final TypeSymbol STRING_TYPE = new TypeSymbol("~STRING~", true); // Internal use only
    public static final TypeSymbol UNDEFINED_TYPE = new TypeSymbol("~UNDEFINED~", true); // Internal use only
    public static final ConstantSymbol FALSE_CONSTANT = new ConstantSymbol("false", BOOLEAN_TYPE, 0, true);
    public static final ConstantSymbol TRUE_CONSTANT = new ConstantSymbol("true", BOOLEAN_TYPE, 1, true);
    private static final Map<String, Symbol> BUILT_IN_SYMBOLS = Map.of(
            "integer", INTEGER_TYPE,
            "char", CHAR_TYPE,
            "boolean", BOOLEAN_TYPE,
            "false", FALSE_CONSTANT,
            "true", TRUE_CONSTANT
    );

    private final Map<String, Symbol> globalSymbols;
    private final Map<String, Symbol> localSymbols;
    private boolean isInLocalScope;

    public SymbolTable() {
        this.globalSymbols = new HashMap<>();
        this.localSymbols = new HashMap<>();
        this.isInLocalScope = false;
    }

    /**
     * Begin a new local scope.
     * Will create a new symbol table for the local scope.
     */
    public void beginLocalScope() {
        assert !this.isInLocalScope;
        this.localSymbols.clear();
        this.isInLocalScope = true;
    }

    /**
     * End the local scope.
     * Will return to the global scope and restore the top.
     */
    public void endLocalScope() {
        assert this.isInLocalScope;
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
    public void enterVariableSymbol(String name, int address, TypeSymbol type) {
        VariableSymbol variableSymbol = new VariableSymbol(name, type, address, !isInLocalScope);
        if (isInLocalScope) localSymbols.put(name, variableSymbol);
        else globalSymbols.put(name, variableSymbol);
    }

    /**
     * Enter a new constant symbol.
     *
     * @param name  the name of the constant.
     * @param type  the type of the constant.
     * @param value the value of the constant.
     */
    public void enterConstantSymbol(String name, TypeSymbol type, int value) {
        ConstantSymbol constantSymbol = new ConstantSymbol(name, type, value, !isInLocalScope);
        if (isInLocalScope) localSymbols.put(name, constantSymbol);
        else globalSymbols.put(name, constantSymbol);
    }

    /**
     * Enter a new type symbol.
     *
     * @param name the name of the type.
     */
    public TypeSymbol enterTypeSymbol(String name) {
        TypeSymbol typeSymbol = new TypeSymbol(name, !isInLocalScope);
        if (isInLocalScope) localSymbols.put(name, typeSymbol);
        else globalSymbols.put(name, typeSymbol);
        return typeSymbol;
    }

    /**
     * Enter a new function symbol.
     *
     * @param name       the name of the function.
     * @param label      the label of the function.
     * @param returnType the return type of the function.
     */
    public FcnSymbol enterFcnSymbol(String name, Label label, List<TypeSymbol> paramTypes, TypeSymbol returnType) {
        FcnSymbol fcnSymbol = new FcnSymbol(name, label, paramTypes, returnType);
        globalSymbols.put(name, new FcnSymbol(name, label, paramTypes, returnType));
        return fcnSymbol;
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Get the symbol of the given name.
     * This will first look in the built-in definitions,
     * then local scope, then in the global scope.
     * If the symbol is not found, null is returned.
     *
     * @param name the name of the symbol.
     * @return the symbol of the given name.
     */
    public Symbol lookup(String name) {
        if (BUILT_IN_SYMBOLS.containsKey(name)) return BUILT_IN_SYMBOLS.get(name);
        if (isInLocalScope && localSymbols.containsKey(name)) return localSymbols.get(name);
        if (globalSymbols.containsKey(name)) return globalSymbols.get(name);
        return null;
    }

    /**
     * Get whether the given symbol is already defined.
     * This will first look in the built-in definitions.
     * If in local scope, this will check the local scope then.
     * Otherwise, the global scope will be checked.
     *
     * @param name the name of the symbol.
     * @return whether the given symbol is already defined.
     */
    public boolean alreadyDefinedInScope(String name) {
        if (BUILT_IN_SYMBOLS.containsKey(name)) return true;
        if (isInLocalScope) return localSymbols.containsKey(name);
        return globalSymbols.containsKey(name);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add("Symbols: ");
        globalSymbols.forEach((key, value) -> sj.add("\t" + value));
        return sj.toString();
    }
}
