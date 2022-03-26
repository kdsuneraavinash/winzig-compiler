package semantic;

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
    private final Map<String, Symbol> globalSymbols;
    private final Map<String, Symbol> localSymbols;
    private int top;
    private boolean isLocal;

    public SymbolTable() {
        this.globalSymbols = new HashMap<>();
        this.localSymbols = new HashMap<>();
        this.top = 0;
        this.isLocal = false;
        enterTypeSymbol("integer");
        enterTypeSymbol("char");
        enterTypeSymbol("boolean");
    }

    public int startLocal() {
        this.isLocal = true;
        this.localSymbols.clear();
        int globalTop = this.top;
        this.top = 0;
        return globalTop;
    }

    public void endLocal(int globalTop) {
        this.top += globalTop;
        this.localSymbols.clear();
        this.isLocal = false;
    }

    public void enterVarSymbol(String name, TypeSymbol type) {
        if (isLocal) localSymbols.put(name, new VariableSymbol(name, type, ++top, false));
        else globalSymbols.put(name, new VariableSymbol(name, type, ++top, true));
    }

    public void enterConstSymbol(String name, TypeSymbol type, int value) {
        if (isLocal) localSymbols.put(name, new ConstantSymbol(name, type, value));
        else globalSymbols.put(name, new ConstantSymbol(name, type, value));
    }

    public void enterTypeSymbol(String name) {
        globalSymbols.put(name, new TypeSymbol(name));
    }

    public void enterFcnSymbol(String name, Label label, TypeSymbol returnType) {
        globalSymbols.put(name, new FcnSymbol(name, label, returnType));
    }

    public Symbol lookup(String name) {
        if (localSymbols.containsKey(name)) return localSymbols.get(name);
        if (globalSymbols.containsKey(name)) return globalSymbols.get(name);
        return null;
    }

    public boolean alreadyDefinedInScope(String name) {
        if (isLocal) return localSymbols.containsKey(name);
        return globalSymbols.containsKey(name);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add("Top: " + top);
        sj.add("Symbols: ");
        for (Map.Entry<String, Symbol> entry : globalSymbols.entrySet()) {
            sj.add("\t" + entry.getValue());
        }
        for (Map.Entry<String, Symbol> entry : localSymbols.entrySet()) {
            sj.add("\t" + entry.getValue());
        }
        return sj.toString();
    }
}
