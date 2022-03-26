package semantic.symbols;

import semantic.attrs.Label;
import semantic.attrs.SemanticType;

public class FcnSymbol extends Symbol {
    private final Label label;
    private final TypeSymbol returnType;

    public FcnSymbol(String name, Label label, TypeSymbol returnType) {
        // TODO: Parameters
        super(name, SemanticType.FUNCTION);
        this.label = label;
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return "[" + getSymbolType() + "] " + getName() + "->" + returnType.getName() + " #" + label.getLabel();
    }
}
