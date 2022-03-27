package lexer.tokens;

public enum TokenKind {
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
    INTEGER_LITERAL("<integer>"),
    CHAR_LITERAL("<char>"),
    STRING_LITERAL("<string>"),
    IDENTIFIER("<identifier>"),

    // Minutiae
    WHITESPACE_MINUTIAE,
    END_OF_LINE_MINUTIAE,
    MULTILINE_COMMENT_MINUTIAE,
    COMMENT_MINUTIAE,

    // Error Token
    ERROR;

    private final String value;

    TokenKind(String value) {
        this.value = value;
    }

    TokenKind() {
        this("");
    }

    public String getValue() {
        return value;
    }
}
