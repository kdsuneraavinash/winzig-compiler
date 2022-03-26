package semantic;

import semantic.symbols.TypeSymbol;
import semantic.table.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class Context {
    public TypeSymbol expressionType;
    public List<String> newVars;
    public List<String> newTypeLits;

    public Context() {
        expressionType = SymbolTable.UNDEFINED_TYPE;
        newVars = new ArrayList<>();
        newTypeLits = new ArrayList<>();
    }
}
