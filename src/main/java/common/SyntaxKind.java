package common;

public enum SyntaxKind {
    // Keywords
    PROGRAM_KEYWORD("program"),
    VAR_KEYWORD("var"),
    CONST_KEYWORD("const"),
    TYPE_KEYWORD("type"),
    FUNCTION_KEYWORD("function"),
    RETURN_KEYWORD("return"),
    BEGIN_KEYWORD("begin"),
    END_KEYWORD("end"),
    OUTPUT_KEYWORD("output"),
    IF_KEYWORD("if"),
    THEN_KEYWORD("then"),
    ELSE_KEYWORD("else"),
    WHILE_KEYWORD("while"),
    DO_KEYWORD("do"),
    CASE_KEYWORD("case"),
    OF_KEYWORD("of"),
    OTHERWISE_KEYWORD("otherwise"),
    REPEAT_KEYWORD("repeat"),
    FOR_KEYWORD("for"),
    UNTIL_KEYWORD("until"),
    LOOP_KEYWORD("loop"),
    POOL_KEYWORD("pool"),
    EXIT_KEYWORD("exit"),
    MOD_KEYWORD("mod"),
    AND_KEYWORD("and"),
    OR_KEYWORD("or"),
    NOT_KEYWORD("not"),
    READ_KEYWORD("read"),
    SUCC_KEYWORD("succ"),
    PRED_KEYWORD("pred"),
    CHR_KEYWORD("chr"),
    ORD_KEYWORD("ord"),
    EOF_KEYWORD("eof"),

    // Tokens
    SWAP_TOKEN(":=:"),
    ASSIGNMENT_TOKEN(":="),
    LT_EQUAL_TOKEN("<="),
    NOT_EQUAL_TOKEN("<>"),
    LT_TOKEN("<"),
    GT_EQUAL_TOKEN(">="),
    GT_TOKEN(">"),
    PLUS_TOKEN("+"),
    MINUS_TOKEN("-"),
    MULTIPLY_TOKEN("*"),
    DIVIDE_TOKEN("/"),
    EQUAL_TOKEN("="),
    OPEN_BRACKET_TOKEN("("),
    CLOSE_BRACKET_TOKEN(")"),
    SEMICOLON_TOKEN(";"),
    DOUBLE_DOTS_TOKEN(".."),
    SINGLE_DOT_TOKEN("."),
    COLON_TOKEN(":"),
    COMMA_TOKEN(","),
    EOF_TOKEN,

    // Literals
    INTEGER_LITERAL,
    CHAR_LITERAL,
    STRING_LITERAL,
    IDENTIFIER,

    // Minutiae
    WHITESPACE_MINUTIAE,
    END_OF_LINE_MINUTIAE,
    MULTILINE_COMMENT_MINUTIAE,
    COMMENT_MINUTIAE,

    PROGRAM("program"),
    CONSTS("consts"),
    CONST("const"),
    TYPES("types"),
    TYPE("type"),
    LIT("lit"),
    SUB_PROGS("subprogs"),
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
    OTHERWISE_CLAUSE(".."),

    ASSIGNMENT_STATEMENT("assign"),
    SWAP_STATEMENT("swap"),

    TRUE("true"),

    NEGATIVE_EXPRESSION("-"),
    ;

    private final String value;

    SyntaxKind(String value) {
        this.value = value;
    }

    SyntaxKind() {
        this("");
    }

    public String getValue() {
        return value;
    }
}
