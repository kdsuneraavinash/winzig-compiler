package parser.nodes;

public enum NodeKind {
    PROGRAM("program"),
    CONSTS("consts"),
    CONST("const"),
    TYPES("types"),
    TYPE("type"),
    LIT("lit"),
    SUBPROGS("subprogs"),
    FCN("fcn"),
    PARAMS("params"),
    DCLNS("dclns"),
    VAR("var"),
    BLOCK("block"),

    OUTPUT_STATEMENT("output"),
    IF_STATEMENT("if"),
    WHILE_STATEMENT("while"),
    REPEAT_STATEMENT("repeat"),
    FOR_STATEMENT("for"),
    LOOP_STATEMENT("loop"),
    CASE_STATEMENT("case"),
    READ_STATEMENT("read"),
    EXIT_STATEMENT("exit"),
    RETURN_STATEMENT("return"),
    NULL_STATEMENT("<null>"),

    INTEGER_OUT_EXP("integer"),
    STRING_OUT_EXP("string"),

    CASE_CLAUSE("case_clause"),
    DOUBLE_DOTS_CLAUSE(".."),
    OTHERWISE_CLAUSE("otherwise"),

    ASSIGNMENT_STATEMENT("assign"),
    SWAP_STATEMENT("swap"),

    TRUE("true"),

    LT_EQUAL_EXPRESSION("<="),
    LT_EXPRESSION("<"),
    GT_EQUAL_EXPRESSION(">="),
    GT_EXPRESSION(">"),
    EQUALS_EXPRESSION("="),
    NOT_EQUALS_EXPRESSION("<>"),


    ADD_EXPRESSION("+"),
    SUBTRACT_EXPRESSION("-"),
    OR_EXPRESSION("or"),

    MULTIPLY_EXPRESSION("*"),
    DIVIDE_EXPRESSION("/"),
    AND_EXPRESSION("and"),
    MOD_EXPRESSION("mod"),

    NEGATIVE_EXPRESSION("-"),
    NOT_EXPRESSION("not"),
    EOF_EXPRESSION("eof"),
    CALL_EXPRESSION("call"),
    SUCC_EXPRESSION("succ"),
    PRED_EXPRESSION("pred"),
    CHR_EXPRESSION("chr"),
    ORD_EXPRESSION("ord");

    private final String value;

    NodeKind(String value) {
        this.value = value;
    }

    NodeKind() {
        this("");
    }

    public String getValue() {
        return value;
    }
}
