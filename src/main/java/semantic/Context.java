package semantic;

import semantic.symbols.TypeSymbol;

import java.util.ArrayList;
import java.util.List;

public class Context {
    public TypeSymbol expressionType;
    public List<String> newVars;

    public Context() {
        expressionType = null;
        newVars = new ArrayList<>();
    }
}
