package semantic;

import semantic.attrs.SemanticType;

import java.util.ArrayList;
import java.util.List;

public class Context {
    public int top;
    public SemanticType type;
    public List<String> newVars;

    public Context() {
        top = 0;
        type = SemanticType.UNDEFINED;
        newVars = new ArrayList<>();
    }
}
