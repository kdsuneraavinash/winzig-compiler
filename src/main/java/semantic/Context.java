package semantic;

import semantic.symbols.FcnSymbol;
import semantic.symbols.TypeSymbol;
import semantic.table.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class Context {
    public List<String> newVarNames;
    public List<String> newTypeLiteralNames;
    public String stringExpression;
    public TypeSymbol exprTypeSymbol;
    public FcnSymbol activeFcnSymbol;
    public List<TypeSymbol> paramTypeSymbols;
    public int top;

    public Context() {
        newVarNames = new ArrayList<>();
        newTypeLiteralNames = new ArrayList<>();
        paramTypeSymbols = new ArrayList<>();
        stringExpression = "";
        exprTypeSymbol = SymbolTable.UNDEFINED_TYPE;
        activeFcnSymbol = null;
        top = 0;
    }
}
