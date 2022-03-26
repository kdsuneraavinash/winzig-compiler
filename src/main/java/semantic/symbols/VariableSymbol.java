package semantic.symbols;

import semantic.attrs.SemanticType;

public class VariableSymbol extends Symbol {
    private final int position;
    private final boolean isGlobal;
    private final boolean isConstant;

    public VariableSymbol(String name, int position, boolean isConstant, boolean isGlobal) {
        super(name, SemanticType.VARIABLE);
        this.position = position;
        this.isGlobal = isGlobal;
        this.isConstant = isConstant;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getType()).append("] ").append(getName());
        sb.append("@").append(position);
        if (!isGlobal) sb.append(" (local)");
        return sb.toString();
    }

    public int getPosition() {
        return position;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public boolean isConstant() {
        return isConstant;
    }
}
