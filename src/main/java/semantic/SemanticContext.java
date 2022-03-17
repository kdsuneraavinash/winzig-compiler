package semantic;

import semantic.attrs.CodeSegment;
import semantic.attrs.ErrorSegment;
import semantic.attrs.SemanticType;

public class SemanticContext {
    public final CodeSegment code;
    public final ErrorSegment error;
    public final int next;
    public final int top;
    public final SemanticType type;

    public SemanticContext(CodeSegment code, ErrorSegment error, int next, int top, SemanticType type) {
        this.code = code;
        this.error = error;
        this.next = next;
        this.top = top;
        this.type = type;
    }

    public static SemanticContext from(SemanticContext context) {
        return new SemanticContext(context.code, context.error, context.next, context.top, context.type);
    }

    public static SemanticContext empty() {
        return new SemanticContext(CodeSegment.empty(), ErrorSegment.empty(), 1, 0, SemanticType.NOT_SET);
    }
}
