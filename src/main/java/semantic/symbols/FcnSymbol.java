package semantic.symbols;

import semantic.attrs.Label;
import semantic.attrs.SymbolType;

public class FcnSymbol extends Symbol {
    public final Label label;
    public final TypeSymbol returnType;

    public FcnSymbol(String name, Label label, TypeSymbol returnType) {
        // TODO: Parameters
        super(name, SymbolType.FUNCTION, true);
        this.label = label;
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s->%s #%s", symbolType, name, returnType.name, label.getLabel());
    }
}
