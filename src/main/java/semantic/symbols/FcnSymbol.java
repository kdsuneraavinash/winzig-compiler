package semantic.symbols;

import semantic.attrs.Label;
import semantic.attrs.SymbolType;

import java.util.List;

public class FcnSymbol extends Symbol {
    public final Label label;
    public final List<TypeSymbol> paramTypeSymbols;
    public final TypeSymbol returnTypeSymbol;

    public FcnSymbol(String name, Label label, List<TypeSymbol> paramTypeSymbols, TypeSymbol returnTypeSymbol) {
        super(name, SymbolType.FUNCTION, true);
        this.label = label;
        this.paramTypeSymbols = paramTypeSymbols;
        this.returnTypeSymbol = returnTypeSymbol;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s(%s)->(%s) #%s", symbolType, name, paramTypeSymbols, returnTypeSymbol, label);
    }
}
