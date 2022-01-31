package lexer;

import common.SyntaxKind;
import lexer.tokens.IdentifierToken;
import lexer.tokens.LiteralToken;
import lexer.tokens.MinutiaeToken;
import lexer.tokens.SyntaxToken;

import java.util.ArrayList;
import java.util.List;

public class WinZigLexer extends AbstractLexer {
    public WinZigLexer(CharReader charReader) {
        super(charReader);
    }

    public SyntaxToken read() {
        processLeadingMinutiae();

        charReader.startMarking();
        if (charReader.isEOF()) {
            return createToken(SyntaxKind.EOF_TOKEN);
        }

        char c = charReader.peek();
        charReader.advance();
        switch (c) {
            // Arithmetic Operators
            case LexerTerminals.PLUS:
                return createToken(SyntaxKind.PLUS_TOKEN);
            case LexerTerminals.MINUS:
                return createToken(SyntaxKind.MINUS_TOKEN);
            case LexerTerminals.MULTIPLY:
                return createToken(SyntaxKind.MULTIPLY_TOKEN);
            case LexerTerminals.DIVIDE:
                return createToken(SyntaxKind.DIVIDE_TOKEN);
            // Binary operators
            case LexerTerminals.EQUAL:
                return createToken(SyntaxKind.EQUAL_TOKEN);
            case LexerTerminals.LT:
                return processLt();
            case LexerTerminals.GT:
                return processGt();
            // Punctuations
            case LexerTerminals.OPEN_BRACKET:
                return createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
            case LexerTerminals.CLOSE_BRACKET:
                return createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
            case LexerTerminals.SEMICOLON:
                return createToken(SyntaxKind.SEMICOLON_TOKEN);
            case LexerTerminals.COMMA:
                return createToken(SyntaxKind.COMMA_TOKEN);
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

    private SyntaxToken processColon() {
        char nextChar = charReader.peek();
        if (nextChar == LexerTerminals.EQUAL) {
            char nextNextChar = charReader.peek(1);
            if (nextNextChar == LexerTerminals.COLON) {
                charReader.advance(2);
                return createToken(SyntaxKind.SWAP_TOKEN);
            }
            charReader.advance();
            return createToken(SyntaxKind.ASSIGNMENT_TOKEN);
        }
        return createToken(SyntaxKind.COLON_TOKEN);
    }

    private SyntaxToken processDot() {
        char nextChar = charReader.peek();
        if (nextChar == LexerTerminals.DOT) {
            charReader.advance();
            return createToken(SyntaxKind.DOUBLE_DOTS_TOKEN);
        }
        return createToken(SyntaxKind.SINGLE_DOT_TOKEN);
    }

    private SyntaxToken processLt() {
        char nextChar = charReader.peek();
        if (nextChar == LexerTerminals.EQUAL) {
            charReader.advance();
            return createToken(SyntaxKind.LT_EQUAL_TOKEN);
        }
        if (nextChar == LexerTerminals.GT) {
            charReader.advance();
            return createToken(SyntaxKind.NOT_EQUAL_TOKEN);
        }
        return createToken(SyntaxKind.LT_TOKEN);
    }

    private SyntaxToken processGt() {
        char nextChar = charReader.peek();
        if (nextChar == LexerTerminals.EQUAL) {
            charReader.advance();
            return createToken(SyntaxKind.GT_EQUAL_TOKEN);
        }
        return createToken(SyntaxKind.GT_TOKEN);
    }

    private SyntaxToken processInteger() {
        while (!charReader.isEOF()) {
            char nextChar = charReader.peek();
            if (isDigit(nextChar)) {
                charReader.advance();
                continue;
            }
            break;
        }
        return createLiteralToken(SyntaxKind.INTEGER_LITERAL);
    }

    private SyntaxToken processChar() {
        char nextChar = charReader.peek();
        if (nextChar != LexerTerminals.QUOTE) {
            char nextNextChar = charReader.peek(1);
            if (nextNextChar == LexerTerminals.QUOTE) {
                charReader.advance(2);
                return createLiteralToken(SyntaxKind.CHAR_LITERAL);
            }
        }
        throw new IllegalStateException();
    }

    private SyntaxToken processString() {
        while (!charReader.isEOF()) {
            char nextChar = charReader.peek();
            charReader.advance();
            if (nextChar == LexerTerminals.DOUBLE_QUOTE) {
                return createLiteralToken(SyntaxKind.STRING_LITERAL);
            }
        }
        throw new IllegalStateException();
    }

    private SyntaxToken processKeywordOrIdentifier() {
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
                return createToken(SyntaxKind.PROGRAM_KEYWORD);
            case LexerTerminals.VAR_KEYWORD:
                return createToken(SyntaxKind.VAR_KEYWORD);
            case LexerTerminals.CONST_KEYWORD:
                return createToken(SyntaxKind.CONST_KEYWORD);
            case LexerTerminals.TYPE_KEYWORD:
                return createToken(SyntaxKind.TYPE_KEYWORD);
            case LexerTerminals.FUNCTION_KEYWORD:
                return createToken(SyntaxKind.FUNCTION_KEYWORD);
            case LexerTerminals.RETURN_KEYWORD:
                return createToken(SyntaxKind.RETURN_KEYWORD);
            case LexerTerminals.BEGIN_KEYWORD:
                return createToken(SyntaxKind.BEGIN_KEYWORD);
            case LexerTerminals.END_KEYWORD:
                return createToken(SyntaxKind.END_KEYWORD);
            case LexerTerminals.OUTPUT_KEYWORD:
                return createToken(SyntaxKind.OUTPUT_KEYWORD);
            case LexerTerminals.IF_KEYWORD:
                return createToken(SyntaxKind.IF_KEYWORD);
            case LexerTerminals.THEN_KEYWORD:
                return createToken(SyntaxKind.THEN_KEYWORD);
            case LexerTerminals.ELSE_KEYWORD:
                return createToken(SyntaxKind.ELSE_KEYWORD);
            case LexerTerminals.WHILE_KEYWORD:
                return createToken(SyntaxKind.WHILE_KEYWORD);
            case LexerTerminals.DO_KEYWORD:
                return createToken(SyntaxKind.DO_KEYWORD);
            case LexerTerminals.CASE_KEYWORD:
                return createToken(SyntaxKind.CASE_KEYWORD);
            case LexerTerminals.OF_KEYWORD:
                return createToken(SyntaxKind.OF_KEYWORD);
            case LexerTerminals.OTHERWISE_KEYWORD:
                return createToken(SyntaxKind.OTHERWISE_KEYWORD);
            case LexerTerminals.REPEAT_KEYWORD:
                return createToken(SyntaxKind.REPEAT_KEYWORD);
            case LexerTerminals.FOR_KEYWORD:
                return createToken(SyntaxKind.FOR_KEYWORD);
            case LexerTerminals.UNTIL_KEYWORD:
                return createToken(SyntaxKind.UNTIL_KEYWORD);
            case LexerTerminals.LOOP_KEYWORD:
                return createToken(SyntaxKind.LOOP_KEYWORD);
            case LexerTerminals.POOL_KEYWORD:
                return createToken(SyntaxKind.POOL_KEYWORD);
            case LexerTerminals.EXIT_KEYWORD:
                return createToken(SyntaxKind.EXIT_KEYWORD);
            case LexerTerminals.MOD_KEYWORD:
                return createToken(SyntaxKind.MOD_KEYWORD);
            case LexerTerminals.AND_KEYWORD:
                return createToken(SyntaxKind.AND_KEYWORD);
            case LexerTerminals.OR_KEYWORD:
                return createToken(SyntaxKind.OR_KEYWORD);
            case LexerTerminals.NOT_KEYWORD:
                return createToken(SyntaxKind.NOT_KEYWORD);
            case LexerTerminals.READ_KEYWORD:
                return createToken(SyntaxKind.READ_KEYWORD);
            case LexerTerminals.SUCC_KEYWORD:
                return createToken(SyntaxKind.SUCC_KEYWORD);
            case LexerTerminals.PRED_KEYWORD:
                return createToken(SyntaxKind.PRED_KEYWORD);
            case LexerTerminals.CHR_KEYWORD:
                return createToken(SyntaxKind.CHR_KEYWORD);
            case LexerTerminals.ORD_KEYWORD:
                return createToken(SyntaxKind.ORD_KEYWORD);
            case LexerTerminals.EOF_KEYWORD:
                return createToken(SyntaxKind.EOF_KEYWORD);
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

    private List<MinutiaeToken> processTrailingMinutiae() {
        List<MinutiaeToken> minutiaeTokens = new ArrayList<>();
        processSyntaxMinutiae(minutiaeTokens, false);
        return minutiaeTokens;
    }

    private void processSyntaxMinutiae(List<MinutiaeToken> minutiaeTokens, boolean isLeading) {
        while (!charReader.isEOF()) {
            charReader.startMarking();
            char c = charReader.peek();
            switch (c) {
                case LexerTerminals.SPACE:
                case LexerTerminals.TAB:
                case LexerTerminals.FORM_FEED:
                    minutiaeTokens.add(processWhitespaceMinutiae());
                    break;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                    minutiaeTokens.add(processEndOfLineMinutiae());
                    if (isLeading) {
                        break;
                    }
                    return;
                case LexerTerminals.OPEN_BRACE:
                    minutiaeTokens.add(processMultilineCommentMinutiae());
                    break;
                case LexerTerminals.HASH:
                    minutiaeTokens.add(processCommentMinutiae());
                    break;
                default:
                    return;
            }
        }
    }

    private MinutiaeToken processWhitespaceMinutiae() {
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

        return createMinutiae(SyntaxKind.WHITESPACE_MINUTIAE);
    }

    private MinutiaeToken processEndOfLineMinutiae() {
        char c = charReader.peek();
        switch (c) {
            case LexerTerminals.NEWLINE:
                charReader.advance();
                return createMinutiae(SyntaxKind.END_OF_LINE_MINUTIAE);
            case LexerTerminals.CARRIAGE_RETURN:
                charReader.advance();
                if (charReader.peek() == LexerTerminals.NEWLINE) {
                    charReader.advance();
                }
                return createMinutiae(SyntaxKind.END_OF_LINE_MINUTIAE);
            default:
                throw new IllegalStateException();
        }
    }

    private MinutiaeToken processCommentMinutiae() {
        while (!charReader.isEOF()) {
            char nextChar = charReader.peek();
            if (nextChar == LexerTerminals.NEWLINE || nextChar == LexerTerminals.CARRIAGE_RETURN) {
                break;
            }
            charReader.advance();
        }
        return createMinutiae(SyntaxKind.COMMENT_MINUTIAE);
    }

    private MinutiaeToken processMultilineCommentMinutiae() {
        while (!charReader.isEOF()) {
            char nextChar = charReader.peek();
            charReader.advance();
            if (nextChar == LexerTerminals.CLOSE_BRACE) {
                return createMinutiae(SyntaxKind.MULTILINE_COMMENT_MINUTIAE);
            }
        }
        throw new IllegalStateException();
    }

    /*
     * -------------------------------
     * Token creation methods
     * -------------------------------
     */

    private SyntaxToken createToken(SyntaxKind kind) {
        return new SyntaxToken(kind, getLeadingMinutiae(), processTrailingMinutiae());
    }

    private SyntaxToken createIdentifierToken() {
        return new IdentifierToken(charReader.getMarkedChars(), getLeadingMinutiae(), processTrailingMinutiae());
    }

    private SyntaxToken createLiteralToken(SyntaxKind kind) {
        return new LiteralToken(kind, charReader.getMarkedChars(), getLeadingMinutiae(), processTrailingMinutiae());
    }

    private MinutiaeToken createMinutiae(SyntaxKind kind) {
        return new MinutiaeToken(kind, charReader.getMarkedChars());
    }
}
