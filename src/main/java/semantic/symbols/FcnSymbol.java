package semantic.symbols;

import semantic.attrs.Label;
import semantic.attrs.SymbolType;

import java.util.List;

public class FcnSymbol extends Symbol {
    public final Label label;
    public final List<TypeSymbol> paramTypes;
    public final TypeSymbol returnType;

    public FcnSymbol(String name, Label label, List<TypeSymbol> paramTypes, TypeSymbol returnType) {
        // TODO: Parameters
        super(name, SymbolType.FUNCTION, true);
        this.label = label;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s(%s)->(%s) #%s", symbolType, name, paramTypes, returnType, label);
    }
}
