package lexer;

public class LexerTerminals {
    // Keywords
    public static final String PROGRAM_KEYWORD = "program";
    public static final String VAR_KEYWORD = "var";
    public static final String CONST_KEYWORD = "const";
    public static final String TYPE_KEYWORD = "type";
    public static final String FUNCTION_KEYWORD = "function";
    public static final String RETURN_KEYWORD = "return";
    public static final String BEGIN_KEYWORD = "begin";
    public static final String END_KEYWORD = "end";
    public static final String OUTPUT_KEYWORD = "output";
    public static final String IF_KEYWORD = "if";
    public static final String THEN_KEYWORD = "then";
    public static final String ELSE_KEYWORD = "else";
    public static final String WHILE_KEYWORD = "while";
    public static final String DO_KEYWORD = "do";
    public static final String CASE_KEYWORD = "case";
    public static final String OF_KEYWORD = "of";
    public static final String OTHERWISE_KEYWORD = "otherwise";
    public static final String REPEAT_KEYWORD = "repeat";
    public static final String FOR_KEYWORD = "for";
    public static final String UNTIL_KEYWORD = "until";
    public static final String LOOP_KEYWORD = "loop";
    public static final String POOL_KEYWORD = "pool";
    public static final String EXIT_KEYWORD = "exit";
    public static final String MOD_KEYWORD = "mod";
    public static final String AND_KEYWORD = "and";
    public static final String OR_KEYWORD = "or";
    public static final String NOT_KEYWORD = "not";
    public static final String READ_KEYWORD = "read";
    public static final String SUCC_KEYWORD = "succ";
    public static final String PRED_KEYWORD = "pred";
    public static final String CHR_KEYWORD = "chr";
    public static final String ORD_KEYWORD = "ord";
    public static final String EOF_KEYWORD = "eof";

    // Punctuations
    public static final char OPEN_BRACE = '{';
    public static final char CLOSE_BRACE = '}';
    public static final char COLON = ':';
    public static final char SEMICOLON = ';';
    public static final char DOT = '.';
    public static final char COMMA = ',';
    public static final char EQUAL = '=';
    public static final char GT = '>';
    public static final char LT = '<';
    public static final char OPEN_BRACKET = '(';
    public static final char CLOSE_BRACKET = ')';
    public static final char PLUS = '+';
    public static final char MINUS = '-';
    public static final char MULTIPLY = '*';
    public static final char DIVIDE = '/';
    public static final char QUOTE = '\'';
    public static final char DOUBLE_QUOTE = '\"';
    public static final char HASH = '#';

    // Digits
    public static final char DIGIT_0 = '0';
    public static final char DIGIT_1 = '1';
    public static final char DIGIT_2 = '2';
    public static final char DIGIT_3 = '3';
    public static final char DIGIT_4 = '4';
    public static final char DIGIT_5 = '5';
    public static final char DIGIT_6 = '6';
    public static final char DIGIT_7 = '7';
    public static final char DIGIT_8 = '8';
    public static final char DIGIT_9 = '9';

    // White Spaces
    public static final char NEWLINE = '\n';
    public static final char CARRIAGE_RETURN = '\r';
    public static final char SPACE = 0x20;
    public static final char TAB = 0x9;
    public static final char FORM_FEED = 0x0C;
}
