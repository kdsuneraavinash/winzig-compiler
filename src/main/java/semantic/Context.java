package semantic;

import semantic.attrs.Label;
import semantic.symbols.FcnSymbol;
import semantic.symbols.TypeSymbol;
import semantic.symbols.VariableSymbol;
import semantic.table.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class Context {
    // All the new variables.
    public List<String> newVarNames;
    // All the new literals of the current type.
    public List<String> newTypeLiteralNames;
    // String out expression in output statement.
    public String stringExpression;
    // The expression type of the last met expression.
    public TypeSymbol exprTypeSymbol;
    // Active function currently. Only set in function body.
    public FcnSymbol activeFcnSymbol;
    // All the new function parameters.
    public List<TypeSymbol> paramTypeSymbols;

    public VariableSymbol currentCaseVariableSymbol;
    public Label nextCaseLabel;
    // Top of the variable stack.
    public int top;

    public Context() {
        newVarNames = new ArrayList<>();
        newTypeLiteralNames = new ArrayList<>();
        paramTypeSymbols = new ArrayList<>();
        stringExpression = "";
        exprTypeSymbol = SymbolTable.UNDEFINED_TYPE;
        activeFcnSymbol = null;
        currentCaseVariableSymbol = null;
        nextCaseLabel = null;
        top = 0;
    }
}
