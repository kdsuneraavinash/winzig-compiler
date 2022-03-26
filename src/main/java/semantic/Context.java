package semantic;

import semantic.attrs.SemanticType;

public class Context {
    public int next;
    public int top;
    public SemanticType type;

    public Context() {
        next = 0;
        top = 0;
        type = SemanticType.UNDEFINED;
    }
}
