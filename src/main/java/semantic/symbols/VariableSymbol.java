package semantic.symbols;

import semantic.attrs.SemanticType;

public class VariableSymbol extends Symbol {
    private final int position;
    private final boolean isGlobal;
    private final TypeSymbol type;

    public VariableSymbol(String name, TypeSymbol type, int position, boolean isGlobal) {
        super(name, SemanticType.VARIABLE);
        this.type = type;
        this.position = position;
        this.isGlobal = isGlobal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getSymbolType()).append("] ").append(getName())
                .append(": ").append(getType().getName())
                .append("@").append(position);
        if (!isGlobal) sb.append(" (local)");
        return sb.toString();
    }

    public int getPosition() {
        return position;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public TypeSymbol getType() {
        return type;
    }
}
