package lexer;

import lexer.tokens.IdentifierToken;
import lexer.tokens.LiteralToken;
import lexer.tokens.Minutiae;
import lexer.tokens.Token;
import lexer.tokens.TokenKind;

import java.util.ArrayList;
import java.util.List;

public class WinZigLexer extends AbstractLexer {
    public WinZigLexer(CharReader charReader) {
        super(charReader);
    }

    public Token read() {
        processLeadingMinutiae();

        charReader.startMarking();
        if (charReader.isEOF()) {
            return createToken(TokenKind.EOF_TOKEN);
        }

        char c = charReader.peek();
        charReader.advance();
        switch (c) {
            // Arithmetic Operators
            case LexerTerminals.PLUS:
                return createToken(TokenKind.PLUS_TOKEN);
            case LexerTerminals.MINUS:
                return createToken(TokenKind.MINUS_TOKEN);
            case LexerTerminals.MULTIPLY:
                return createToken(TokenKind.MULTIPLY_TOKEN);
            case LexerTerminals.DIVIDE:
                return createToken(TokenKind.DIVIDE_TOKEN);
            // Binary operators
            case LexerTerminals.EQUAL:
                return createToken(TokenKind.EQUAL_TOKEN);
            case LexerTerminals.LT:
                return processLt();
            case LexerTerminals.GT:
                return processGt();
            // Punctuations
            case LexerTerminals.OPEN_BRACKET:
                return createToken(TokenKind.OPEN_BRACKET_TOKEN);
            case LexerTerminals.CLOSE_BRACKET:
                return createToken(TokenKind.CLOSE_BRACKET_TOKEN);
            case LexerTerminals.SEMICOLON:
                return createToken(TokenKind.SEMICOLON_TOKEN);
            case LexerTerminals.COMMA:
                return createToken(TokenKind.COMMA_TOKEN);
            case LexerTerminals.COLON:
                return processColon();
            case LexerTerminals.DOT:
                return processDot();
            // Digits
            case LexerTerminals.DIGIT_0:
            case LexerTerminals.DIGIT_1:
            case LexerTerminals.DIGIT_2:
            case LexerTerminals.DIGIT_3:
            case LexerTerminals.DIGIT_4:
            case LexerTerminals.DIGIT_5:
            case LexerTerminals.DIGIT_6:
            case LexerTerminals.DIGIT_7:
            case LexerTerminals.DIGIT_8:
            case LexerTerminals.DIGIT_9:
                return processInteger();
            // Quotes
            case LexerTerminals.QUOTE:
                return processChar();
            case LexerTerminals.DOUBLE_QUOTE:
                return processString();
            default:
                if (isIdentifierInitialChar(c)) {
                    return processKeywordOrIdentifier();
                }
                throw new IllegalStateException();
        }
    }

    /*
     * -------------------------------
     * Other token creation methods
     * -------------------------------
     */

    private Token processColon() {
        char nextChar = charReader.peek();
        if (nextChar == LexerTerminals.EQUAL) {
            char nextNextChar = charReader.peek(1);
            if (nextNextChar == LexerTerminals.COLON) {
                charReader.advance(2);
                return createToken(TokenKind.SWAP_TOKEN);
            }
            charReader.advance();
            return createToken(TokenKind.ASSIGNMENT_TOKEN);
        }
        return createToken(TokenKind.COLON_TOKEN);
    }

    private Token processDot() {
        char nextChar = charReader.peek();
        if (nextChar == LexerTerminals.DOT) {
            charReader.advance();
            return createToken(TokenKind.DOUBLE_DOTS_TOKEN);
        }
        return createToken(TokenKind.SINGLE_DOT_TOKEN);
    }

    private Token processLt() {
        char nextChar = charReader.peek();
        if (nextChar == LexerTerminals.EQUAL) {
            charReader.advance();
            return createToken(TokenKind.LT_EQUAL_TOKEN);
        }
        if (nextChar == LexerTerminals.GT) {
            charReader.advance();
            return createToken(TokenKind.NOT_EQUAL_TOKEN);
        }
        return createToken(TokenKind.LT_TOKEN);
    }

    private Token processGt() {
        char nextChar = charReader.peek();
        if (nextChar == LexerTerminals.EQUAL) {
            charReader.advance();
            return createToken(TokenKind.GT_EQUAL_TOKEN);
        }
        return createToken(TokenKind.GT_TOKEN);
    }

    private Token processInteger() {
        while (!charReader.isEOF()) {
            char nextChar = charReader.peek();
            if (isDigit(nextChar)) {
                charReader.advance();
                continue;
            }
            break;
        }
        return createLiteralToken(TokenKind.INTEGER_LITERAL);
    }

    private Token processChar() {
        char nextChar = charReader.peek();
        if (nextChar != LexerTerminals.QUOTE) {
            char nextNextChar = charReader.peek(1);
            if (nextNextChar == LexerTerminals.QUOTE) {
                charReader.advance(2);
                return createLiteralToken(TokenKind.CHAR_LITERAL);
            }
        }
        throw new IllegalStateException();
    }

    private Token processString() {
        while (!charReader.isEOF()) {
            char nextChar = charReader.peek();
            charReader.advance();
            if (nextChar == LexerTerminals.DOUBLE_QUOTE) {
                return createLiteralToken(TokenKind.STRING_LITERAL);
            }
        }
        throw new IllegalStateException();
    }

    private Token processKeywordOrIdentifier() {
        while (!charReader.isEOF()) {
            char nextChar = charReader.peek();
            if (isIdentifierChar(nextChar)) {
                charReader.advance();
                continue;
            }
            break;
        }
        String s = charReader.getMarkedChars();
        switch (s) {
            case LexerTerminals.PROGRAM_KEYWORD:
                return createToken(TokenKind.PROGRAM_KEYWORD);
            case LexerTerminals.VAR_KEYWORD:
                return createToken(TokenKind.VAR_KEYWORD);
            case LexerTerminals.CONST_KEYWORD:
                return createToken(TokenKind.CONST_KEYWORD);
            case LexerTerminals.TYPE_KEYWORD:
                return createToken(TokenKind.TYPE_KEYWORD);
            case LexerTerminals.FUNCTION_KEYWORD:
                return createToken(TokenKind.FUNCTION_KEYWORD);
            case LexerTerminals.RETURN_KEYWORD:
                return createToken(TokenKind.RETURN_KEYWORD);
            case LexerTerminals.BEGIN_KEYWORD:
                return createToken(TokenKind.BEGIN_KEYWORD);
            case LexerTerminals.END_KEYWORD:
                return createToken(TokenKind.END_KEYWORD);
            case LexerTerminals.OUTPUT_KEYWORD:
                return createToken(TokenKind.OUTPUT_KEYWORD);
            case LexerTerminals.IF_KEYWORD:
                return createToken(TokenKind.IF_KEYWORD);
            case LexerTerminals.THEN_KEYWORD:
                return createToken(TokenKind.THEN_KEYWORD);
            case LexerTerminals.ELSE_KEYWORD:
                return createToken(TokenKind.ELSE_KEYWORD);
            case LexerTerminals.WHILE_KEYWORD:
                return createToken(TokenKind.WHILE_KEYWORD);
            case LexerTerminals.DO_KEYWORD:
                return createToken(TokenKind.DO_KEYWORD);
            case LexerTerminals.CASE_KEYWORD:
                return createToken(TokenKind.CASE_KEYWORD);
            case LexerTerminals.OF_KEYWORD:
                return createToken(TokenKind.OF_KEYWORD);
            case LexerTerminals.OTHERWISE_KEYWORD:
                return createToken(TokenKind.OTHERWISE_KEYWORD);
            case LexerTerminals.REPEAT_KEYWORD:
                return createToken(TokenKind.REPEAT_KEYWORD);
            case LexerTerminals.FOR_KEYWORD:
                return createToken(TokenKind.FOR_KEYWORD);
            case LexerTerminals.UNTIL_KEYWORD:
                return createToken(TokenKind.UNTIL_KEYWORD);
            case LexerTerminals.LOOP_KEYWORD:
                return createToken(TokenKind.LOOP_KEYWORD);
            case LexerTerminals.POOL_KEYWORD:
                return createToken(TokenKind.POOL_KEYWORD);
            case LexerTerminals.EXIT_KEYWORD:
                return createToken(TokenKind.EXIT_KEYWORD);
            case LexerTerminals.MOD_KEYWORD:
                return createToken(TokenKind.MOD_KEYWORD);
            case LexerTerminals.AND_KEYWORD:
                return createToken(TokenKind.AND_KEYWORD);
            case LexerTerminals.OR_KEYWORD:
                return createToken(TokenKind.OR_KEYWORD);
            case LexerTerminals.NOT_KEYWORD:
                return createToken(TokenKind.NOT_KEYWORD);
            case LexerTerminals.READ_KEYWORD:
                return createToken(TokenKind.READ_KEYWORD);
            case LexerTerminals.SUCC_KEYWORD:
                return createToken(TokenKind.SUCC_KEYWORD);
            case LexerTerminals.PRED_KEYWORD:
                return createToken(TokenKind.PRED_KEYWORD);
            case LexerTerminals.CHR_KEYWORD:
                return createToken(TokenKind.CHR_KEYWORD);
            case LexerTerminals.ORD_KEYWORD:
                return createToken(TokenKind.ORD_KEYWORD);
            case LexerTerminals.EOF_KEYWORD:
                return createToken(TokenKind.EOF_KEYWORD);
            default:
                return createIdentifierToken();
        }
    }

    /*
     * -------------------------------
     * Minutiae creation methods
     * -------------------------------
     */

    private void processLeadingMinutiae() {
        processSyntaxMinutiae(this.leadingMinutiae, true);
    }

    private List<Minutiae> processTrailingMinutiae() {
        List<Minutiae> minutiae = new ArrayList<>();
        processSyntaxMinutiae(minutiae, false);
        return minutiae;
    }

    private void processSyntaxMinutiae(List<Minutiae> minutiae, boolean isLeading) {
        while (!charReader.isEOF()) {
            charReader.startMarking();
            char c = charReader.peek();
            switch (c) {
                case LexerTerminals.SPACE:
                case LexerTerminals.TAB:
                case LexerTerminals.FORM_FEED:
                    minutiae.add(processWhitespaceMinutiae());
                    break;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                    minutiae.add(processEndOfLineMinutiae());
                    if (isLeading) {
                        break;
                    }
                    return;
                case LexerTerminals.OPEN_BRACE:
                    minutiae.add(processMultilineCommentMinutiae());
                    break;
                case LexerTerminals.HASH:
                    minutiae.add(processCommentMinutiae());
                    break;
                default:
                    return;
            }
        }
    }

    private Minutiae processWhitespaceMinutiae() {
        while (!charReader.isEOF()) {
            char c = charReader.peek();
            switch (c) {
                case LexerTerminals.SPACE:
                case LexerTerminals.TAB:
                case LexerTerminals.FORM_FEED:
                    charReader.advance();
                    continue;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                default:
                    break;
            }
            break;
        }

        return createMinutiae(TokenKind.WHITESPACE_MINUTIAE);
    }

    private Minutiae processEndOfLineMinutiae() {
        char c = charReader.peek();
        switch (c) {
            case LexerTerminals.NEWLINE:
                charReader.advance();
                return createMinutiae(TokenKind.END_OF_LINE_MINUTIAE);
            case LexerTerminals.CARRIAGE_RETURN:
                charReader.advance();
                if (charReader.peek() == LexerTerminals.NEWLINE) {
                    charReader.advance();
                }
                return createMinutiae(TokenKind.END_OF_LINE_MINUTIAE);
            default:
                throw new IllegalStateException();
        }
    }

    private Minutiae processCommentMinutiae() {
        while (!charReader.isEOF()) {
            char nextChar = charReader.peek();
            if (nextChar == LexerTerminals.NEWLINE || nextChar == LexerTerminals.CARRIAGE_RETURN) {
                break;
            }
            charReader.advance();
        }
        return createMinutiae(TokenKind.COMMENT_MINUTIAE);
    }

    private Minutiae processMultilineCommentMinutiae() {
        while (!charReader.isEOF()) {
            char nextChar = charReader.peek();
            charReader.advance();
            if (nextChar == LexerTerminals.CLOSE_BRACE) {
                return createMinutiae(TokenKind.MULTILINE_COMMENT_MINUTIAE);
            }
        }
        throw new IllegalStateException();
    }

    /*
     * -------------------------------
     * Token creation methods
     * -------------------------------
     */

    private Token createToken(TokenKind kind) {
        int startOffset = charReader.getMarkStartOffset();
        int endOffset = charReader.getOffset();
        return new Token(kind, getLeadingMinutiae(), processTrailingMinutiae(),
                startOffset, endOffset);
    }

    private Token createIdentifierToken() {
        int startOffset = charReader.getMarkStartOffset();
        int endOffset = charReader.getOffset();
        return new IdentifierToken(charReader.getMarkedChars(), getLeadingMinutiae(), processTrailingMinutiae(),
                startOffset, endOffset);
    }

    private Token createLiteralToken(TokenKind kind) {
        int startOffset = charReader.getMarkStartOffset();
        int endOffset = charReader.getOffset();
        return new LiteralToken(kind, charReader.getMarkedChars(), getLeadingMinutiae(), processTrailingMinutiae(),
                startOffset, endOffset);
    }

    private Minutiae createMinutiae(TokenKind kind) {
        return new Minutiae(kind, charReader.getMarkedChars());
    }
}
