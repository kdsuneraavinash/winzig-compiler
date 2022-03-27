package parser;

import lexer.WinZigLexer;
import lexer.tokens.TokenKind;
import parser.nodes.ASTNode;
import parser.nodes.Node;
import parser.nodes.NodeKind;

public class WinZigParser extends AbstractParser {
    public WinZigParser(WinZigLexer lexer) {
        super(lexer);
    }

    @Override
    public ASTNode parse() {
        parseWinZig();
        Node winZigNode = nodeStack.pop();
        if (winZigNode instanceof ASTNode) {
            if (!nodeStack.isEmpty()) addError(winZigNode, "Internal error: More items left in parser stack.");
            return (ASTNode) winZigNode;
        }
        // This should not reach.
        throw new IllegalStateException(String.format("[%s] but remaining %s.", winZigNode, nodeStack));
    }

    private void parseWinZig() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.PROGRAM_KEYWORD);
        itemCount += parseName();
        itemCount += parseToken(TokenKind.COLON_TOKEN);
        itemCount += parseConsts();
        itemCount += parseTypes();
        itemCount += parseDclns();
        itemCount += parseSubProgs();
        itemCount += parseBody();
        itemCount += parseName();
        itemCount += parseToken(TokenKind.SINGLE_DOT_TOKEN);
        buildTree(NodeKind.PROGRAM, itemCount);
    }

    private int parseConsts() {
        int itemCount = 0;
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == TokenKind.CONST_KEYWORD) {
            itemCount += parseToken(TokenKind.CONST_KEYWORD);
            itemCount += parseList(this::parseConst, TokenKind.COMMA_TOKEN);
            itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
        }
        return buildTree(NodeKind.CONSTS, itemCount);
    }

    private int parseConst() {
        int itemCount = 0;
        itemCount += parseName();
        itemCount += parseToken(TokenKind.EQUAL_TOKEN);
        itemCount += parseConstValue();
        return buildTree(NodeKind.CONST, itemCount);
    }

    private int parseConstValue() {
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == TokenKind.INTEGER_LITERAL) {
            return parseIdentifier(TokenKind.INTEGER_LITERAL);
        } else if (tokenKind == TokenKind.CHAR_LITERAL) {
            return parseIdentifier(TokenKind.CHAR_LITERAL);
        } else {
            return parseName();
        }
    }

    private int parseTypes() {
        int itemCount = 0;
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == TokenKind.TYPE_KEYWORD) {
            itemCount += parseToken(TokenKind.TYPE_KEYWORD);
            itemCount += parseType();
            itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
            // After types is always Dclns(nullable), SubProgs(nullable) and Body.
            // So must be followed by 'var', 'function' or 'begin' at the end.
            tokenKind = peekNextKind();
            while (tokenKind != TokenKind.VAR_KEYWORD
                    && tokenKind != TokenKind.FUNCTION_KEYWORD
                    && tokenKind != TokenKind.BEGIN_KEYWORD) {
                itemCount += parseType();
                itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
                tokenKind = peekNextKind();
            }
        }
        return buildTree(NodeKind.TYPES, itemCount);
    }

    private int parseType() {
        int itemCount = 0;
        itemCount += parseName();
        itemCount += parseToken(TokenKind.EQUAL_TOKEN);
        itemCount += parseLitList();
        return buildTree(NodeKind.TYPE, itemCount);
    }

    private int parseLitList() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.OPEN_BRACKET_TOKEN);
        itemCount += parseList(this::parseName, TokenKind.COMMA_TOKEN);
        itemCount += parseToken(TokenKind.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeKind.LIT, itemCount);
    }

    private int parseSubProgs() {
        int itemCount = 0;
        TokenKind tokenKind = peekNextKind();
        while (tokenKind == TokenKind.FUNCTION_KEYWORD) {
            itemCount += parseFcn();
            tokenKind = peekNextKind();
        }
        return buildTree(NodeKind.SUBPROGS, itemCount);
    }

    private int parseFcn() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.FUNCTION_KEYWORD);
        itemCount += parseName();
        itemCount += parseToken(TokenKind.OPEN_BRACKET_TOKEN);
        itemCount += parseParams();
        itemCount += parseToken(TokenKind.CLOSE_BRACKET_TOKEN);
        itemCount += parseToken(TokenKind.COLON_TOKEN);
        itemCount += parseName();
        itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
        itemCount += parseConsts();
        itemCount += parseTypes();
        itemCount += parseDclns();
        itemCount += parseBody();
        itemCount += parseName();
        itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
        return buildTree(NodeKind.FCN, itemCount);
    }

    private int parseParams() {
        int itemCount = 0;
        itemCount += parseList(this::parseDcln, TokenKind.SEMICOLON_TOKEN);
        return buildTree(NodeKind.PARAMS, itemCount);
    }

    private int parseDclns() {
        int itemCount = 0;
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == TokenKind.VAR_KEYWORD) {
            itemCount += parseToken(TokenKind.VAR_KEYWORD);
            itemCount += parseDcln();
            itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
            // Dclns is followed by SubProgs(nullable) and Body.
            // So must be followed by 'function' or 'begin' at the end.
            tokenKind = peekNextKind();
            while (tokenKind != TokenKind.FUNCTION_KEYWORD
                    && tokenKind != TokenKind.BEGIN_KEYWORD) {
                itemCount += parseDcln();
                itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
                tokenKind = peekNextKind();
            }
        }
        return buildTree(NodeKind.DCLNS, itemCount);
    }

    private int parseDcln() {
        int itemCount = 0;
        itemCount += parseList(this::parseName, TokenKind.COMMA_TOKEN);
        itemCount += parseToken(TokenKind.COLON_TOKEN);
        itemCount += parseName();
        return buildTree(NodeKind.VAR, itemCount);
    }

    private int parseBody() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.BEGIN_KEYWORD);
        itemCount += parseList(this::parseStatement, TokenKind.SEMICOLON_TOKEN);
        itemCount += parseToken(TokenKind.END_KEYWORD);
        return buildTree(NodeKind.BLOCK, itemCount);
    }

    private int parseStatement() {
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == TokenKind.OUTPUT_KEYWORD) {
            return parseOutputStatement();
        } else if (tokenKind == TokenKind.IF_KEYWORD) {
            return parseIfStatement();
        } else if (tokenKind == TokenKind.WHILE_KEYWORD) {
            return parseWhileStatement();
        } else if (tokenKind == TokenKind.REPEAT_KEYWORD) {
            return parseRepeatStatement();
        } else if (tokenKind == TokenKind.FOR_KEYWORD) {
            return parseForStatement();
        } else if (tokenKind == TokenKind.LOOP_KEYWORD) {
            return parseLoopStatement();
        } else if (tokenKind == TokenKind.CASE_KEYWORD) {
            return parseCaseStatement();
        } else if (tokenKind == TokenKind.READ_KEYWORD) {
            return parseReadStatement();
        } else if (tokenKind == TokenKind.EXIT_KEYWORD) {
            return parseExitStatement();
        } else if (tokenKind == TokenKind.RETURN_KEYWORD) {
            return parseReturnStatement();
        } else if (tokenKind == TokenKind.BEGIN_KEYWORD) {
            return parseBody();
        } else {
            // The second token of assignment is either ':=' or ':=:'.
            TokenKind peekKind = peekNextKind(1);
            if (peekKind == TokenKind.ASSIGNMENT_TOKEN || peekKind == TokenKind.SWAP_TOKEN) {
                return parseAssignmentStatement();
            } else {
                return buildTree(NodeKind.NULL_STATEMENT, 0);
            }
        }
    }

    private int parseOutputStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.OUTPUT_KEYWORD);
        itemCount += parseToken(TokenKind.OPEN_BRACKET_TOKEN);
        itemCount += parseList(this::parseOutExp, TokenKind.COMMA_TOKEN);
        itemCount += parseToken(TokenKind.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeKind.OUTPUT_STATEMENT, itemCount);
    }

    private int parseIfStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.IF_KEYWORD);
        itemCount += parseExpression();
        itemCount += parseToken(TokenKind.THEN_KEYWORD);
        itemCount += parseStatement();
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == TokenKind.ELSE_KEYWORD) {
            itemCount += parseToken(TokenKind.ELSE_KEYWORD);
            itemCount += parseStatement();
        }
        return buildTree(NodeKind.IF_STATEMENT, itemCount);
    }

    private int parseWhileStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.WHILE_KEYWORD);
        itemCount += parseExpression();
        itemCount += parseToken(TokenKind.DO_KEYWORD);
        itemCount += parseStatement();
        return buildTree(NodeKind.WHILE_STATEMENT, itemCount);
    }

    private int parseRepeatStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.REPEAT_KEYWORD);
        itemCount += parseList(this::parseStatement, TokenKind.SEMICOLON_TOKEN);
        itemCount += parseToken(TokenKind.UNTIL_KEYWORD);
        itemCount += parseExpression();
        return buildTree(NodeKind.REPEAT_STATEMENT, itemCount);
    }

    private int parseForStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.FOR_KEYWORD);
        itemCount += parseToken(TokenKind.OPEN_BRACKET_TOKEN);
        itemCount += parseForStat();
        itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
        itemCount += parseForExp();
        itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
        itemCount += parseForStat();
        itemCount += parseToken(TokenKind.CLOSE_BRACKET_TOKEN);
        itemCount += parseStatement();
        return buildTree(NodeKind.FOR_STATEMENT, itemCount);
    }

    private int parseLoopStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.LOOP_KEYWORD);
        itemCount += parseList(this::parseStatement, TokenKind.SEMICOLON_TOKEN);
        itemCount += parseToken(TokenKind.POOL_KEYWORD);
        return buildTree(NodeKind.LOOP_STATEMENT, itemCount);
    }

    private int parseCaseStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.CASE_KEYWORD);
        itemCount += parseExpression();
        itemCount += parseToken(TokenKind.OF_KEYWORD);
        itemCount += parseCaseClauses();
        itemCount += parseOtherwiseClause();
        itemCount += parseToken(TokenKind.END_KEYWORD);
        return buildTree(NodeKind.CASE_STATEMENT, itemCount);
    }

    private int parseReadStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.READ_KEYWORD);
        itemCount += parseToken(TokenKind.OPEN_BRACKET_TOKEN);
        itemCount += parseList(this::parseName, TokenKind.COMMA_TOKEN);
        itemCount += parseToken(TokenKind.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeKind.READ_STATEMENT, itemCount);
    }

    private int parseExitStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.EXIT_KEYWORD);
        return buildTree(NodeKind.EXIT_STATEMENT, itemCount);
    }

    private int parseReturnStatement() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.RETURN_KEYWORD);
        itemCount += parseExpression();
        return buildTree(NodeKind.RETURN_STATEMENT, itemCount);
    }

    private int parseOutExp() {
        int itemCount = 0;
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == TokenKind.STRING_LITERAL) {
            itemCount += parseStringNode();
            return buildTree(NodeKind.STRING_OUT_EXP, itemCount);
        } else {
            itemCount += parseExpression();
            return buildTree(NodeKind.INTEGER_OUT_EXP, itemCount);
        }
    }

    private int parseStringNode() {
        return parseIdentifier(TokenKind.STRING_LITERAL);
    }

    private int parseCaseClauses() {
        int itemCount = 0;
        itemCount += parseCaseClause();
        itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
        // Case clauses must be followed by otherwise(nullable) or 'end'.
        // So must be followed bt 'otherwise' or 'end'.
        TokenKind tokenKind = peekNextKind();
        while (tokenKind != TokenKind.OTHERWISE_KEYWORD
                && tokenKind != TokenKind.END_KEYWORD) {
            itemCount += parseCaseClause();
            itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
            tokenKind = peekNextKind();
        }
        return itemCount;
    }

    private int parseCaseClause() {
        int itemCount = 0;
        itemCount += parseList(this::parseCaseExpression, TokenKind.COMMA_TOKEN);
        itemCount += parseToken(TokenKind.COLON_TOKEN);
        itemCount += parseStatement();
        return buildTree(NodeKind.CASE_CLAUSE, itemCount);
    }

    private int parseCaseExpression() {
        int itemCount = 0;
        itemCount += parseConstValue();
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == TokenKind.DOUBLE_DOTS_TOKEN) {
            itemCount += parseToken(TokenKind.DOUBLE_DOTS_TOKEN);
            itemCount += parseConstValue();
            return buildTree(NodeKind.DOUBLE_DOTS_CLAUSE, itemCount);
        }
        return itemCount;
    }

    private int parseOtherwiseClause() {
        int itemCount = 0;
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == TokenKind.OTHERWISE_KEYWORD) {
            itemCount += parseToken(TokenKind.OTHERWISE_KEYWORD);
            itemCount += parseStatement();
            return buildTree(NodeKind.OTHERWISE_CLAUSE, itemCount);
        }
        return itemCount;
    }

    private int parseAssignmentStatement() {
        int itemCount = 0;
        itemCount += parseName();
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == TokenKind.ASSIGNMENT_TOKEN) {
            itemCount += parseToken(TokenKind.ASSIGNMENT_TOKEN);
            itemCount += parseExpression();
            return buildTree(NodeKind.ASSIGNMENT_STATEMENT, itemCount);
        } else {
            itemCount += parseToken(TokenKind.SWAP_TOKEN);
            itemCount += parseName();
            return buildTree(NodeKind.SWAP_STATEMENT, itemCount);
        }
    }

    private int parseForStat() {
        // ForStat is always followed by ';'.
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == TokenKind.SEMICOLON_TOKEN) {
            return buildTree(NodeKind.NULL_STATEMENT, 0);
        } else {
            return parseAssignmentStatement();
        }
    }

    private int parseForExp() {
        // ForStat is always followed by ';'.
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == TokenKind.SEMICOLON_TOKEN) {
            return buildTree(NodeKind.TRUE, 0);
        } else {
            return parseExpression();
        }
    }

    private int parseExpression() {
        int itemCount = 0;
        itemCount += parseTerm();
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == TokenKind.LT_EQUAL_TOKEN) {
            itemCount += parseToken(TokenKind.LT_EQUAL_TOKEN);
            itemCount += parseTerm();
            return buildTree(NodeKind.LT_EQUAL_EXPRESSION, itemCount);
        } else if (tokenKind == TokenKind.LT_TOKEN) {
            itemCount += parseToken(TokenKind.LT_TOKEN);
            itemCount += parseTerm();
            return buildTree(NodeKind.LT_EXPRESSION, itemCount);
        } else if (tokenKind == TokenKind.GT_EQUAL_TOKEN) {
            itemCount += parseToken(TokenKind.GT_EQUAL_TOKEN);
            itemCount += parseTerm();
            return buildTree(NodeKind.GT_EQUAL_EXPRESSION, itemCount);
        } else if (tokenKind == TokenKind.GT_TOKEN) {
            itemCount += parseToken(TokenKind.GT_TOKEN);
            itemCount += parseTerm();
            return buildTree(NodeKind.GT_EXPRESSION, itemCount);
        } else if (tokenKind == TokenKind.EQUAL_TOKEN) {
            itemCount += parseToken(TokenKind.EQUAL_TOKEN);
            itemCount += parseTerm();
            return buildTree(NodeKind.EQUALS_EXPRESSION, itemCount);
        } else if (tokenKind == TokenKind.NOT_EQUAL_TOKEN) {
            itemCount += parseToken(TokenKind.NOT_EQUAL_TOKEN);
            itemCount += parseTerm();
            return buildTree(NodeKind.NOT_EQUALS_EXPRESSION, itemCount);
        }
        return itemCount;
    }

    private int parseTerm() {
        int itemCount = 0;
        itemCount += parseFactor();
        TokenKind tokenKind = peekNextKind();
        while (tokenKind == TokenKind.PLUS_TOKEN
                || tokenKind == TokenKind.MINUS_TOKEN
                || tokenKind == TokenKind.OR_KEYWORD) {
            if (tokenKind == TokenKind.PLUS_TOKEN) {
                itemCount += parseToken(TokenKind.PLUS_TOKEN);
                itemCount += parseFactor();
                itemCount = buildTree(NodeKind.ADD_EXPRESSION, itemCount);
            } else if (tokenKind == TokenKind.MINUS_TOKEN) {
                itemCount += parseToken(TokenKind.MINUS_TOKEN);
                itemCount += parseFactor();
                itemCount = buildTree(NodeKind.SUBTRACT_EXPRESSION, itemCount);
            } else {
                itemCount += parseToken(TokenKind.OR_KEYWORD);
                itemCount += parseFactor();
                itemCount = buildTree(NodeKind.OR_EXPRESSION, itemCount);
            }
            tokenKind = peekNextKind();
        }
        return itemCount;
    }

    private int parseFactor() {
        int itemCount = 0;
        itemCount += parsePrimary();
        TokenKind tokenKind = peekNextKind();
        while (tokenKind == TokenKind.MULTIPLY_TOKEN
                || tokenKind == TokenKind.DIVIDE_TOKEN
                || tokenKind == TokenKind.AND_KEYWORD
                || tokenKind == TokenKind.MOD_KEYWORD) {
            if (tokenKind == TokenKind.MULTIPLY_TOKEN) {
                itemCount += parseToken(TokenKind.MULTIPLY_TOKEN);
                itemCount += parsePrimary();
                itemCount = buildTree(NodeKind.MULTIPLY_EXPRESSION, itemCount);
            } else if (tokenKind == TokenKind.DIVIDE_TOKEN) {
                itemCount += parseToken(TokenKind.DIVIDE_TOKEN);
                itemCount += parsePrimary();
                itemCount = buildTree(NodeKind.DIVIDE_EXPRESSION, itemCount);
            } else if (tokenKind == TokenKind.AND_KEYWORD) {
                itemCount += parseToken(TokenKind.AND_KEYWORD);
                itemCount += parsePrimary();
                itemCount = buildTree(NodeKind.AND_EXPRESSION, itemCount);
            } else {
                itemCount += parseToken(TokenKind.MOD_KEYWORD);
                itemCount += parsePrimary();
                itemCount = buildTree(NodeKind.MOD_EXPRESSION, itemCount);
            }
            tokenKind = peekNextKind();
        }
        return itemCount;
    }

    private int parsePrimary() {
        int itemCount = 0;
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == TokenKind.MINUS_TOKEN) {
            itemCount += parseNegativeExpression();
        } else if (tokenKind == TokenKind.PLUS_TOKEN) {
            itemCount += parseToken(TokenKind.PLUS_TOKEN);
            itemCount += parsePrimary();
        } else if (tokenKind == TokenKind.NOT_KEYWORD) {
            itemCount += parseNotExpression();
        } else if (tokenKind == TokenKind.EOF_KEYWORD) {
            itemCount += parseEofExpression();
        } else if (tokenKind == TokenKind.INTEGER_LITERAL) {
            itemCount += parseIdentifier(TokenKind.INTEGER_LITERAL);
        } else if (tokenKind == TokenKind.CHAR_LITERAL) {
            itemCount += parseIdentifier(TokenKind.CHAR_LITERAL);
        } else if (tokenKind == TokenKind.OPEN_BRACKET_TOKEN) {
            itemCount += parseToken(TokenKind.OPEN_BRACKET_TOKEN);
            itemCount += parseExpression();
            itemCount += parseToken(TokenKind.CLOSE_BRACKET_TOKEN);
        } else if (tokenKind == TokenKind.SUCC_KEYWORD) {
            itemCount += parseSuccExpression();
        } else if (tokenKind == TokenKind.PRED_KEYWORD) {
            itemCount += parsePredExpression();
        } else if (tokenKind == TokenKind.CHR_KEYWORD) {
            itemCount += parseChrExpression();
        } else if (tokenKind == TokenKind.ORD_KEYWORD) {
            itemCount += parseOrdExpression();
        } else {
            TokenKind nextNextKind = peekNextKind(1);
            // Is this correct?
            if (nextNextKind == TokenKind.OPEN_BRACKET_TOKEN) {
                itemCount += parseCallExpression();
            } else {
                itemCount += parseIdentifier(TokenKind.IDENTIFIER);
            }
        }
        return itemCount;
    }

    private int parseNegativeExpression() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.MINUS_TOKEN);
        itemCount += parsePrimary();
        return buildTree(NodeKind.NEGATIVE_EXPRESSION, itemCount);
    }

    private int parseNotExpression() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.NOT_KEYWORD);
        itemCount += parsePrimary();
        return buildTree(NodeKind.NOT_EXPRESSION, itemCount);
    }

    private int parseEofExpression() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.EOF_KEYWORD);
        return buildTree(NodeKind.EOF_EXPRESSION, itemCount);
    }

    private int parseCallExpression() {
        int itemCount = 0;
        itemCount += parseName();
        itemCount += parseToken(TokenKind.OPEN_BRACKET_TOKEN);
        itemCount += parseList(this::parseExpression, TokenKind.COMMA_TOKEN);
        itemCount += parseToken(TokenKind.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeKind.CALL_EXPRESSION, itemCount);
    }

    private int parseSuccExpression() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.SUCC_KEYWORD);
        itemCount += parseToken(TokenKind.OPEN_BRACKET_TOKEN);
        itemCount += parseExpression();
        itemCount += parseToken(TokenKind.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeKind.SUCC_EXPRESSION, itemCount);
    }

    private int parsePredExpression() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.PRED_KEYWORD);
        itemCount += parseToken(TokenKind.OPEN_BRACKET_TOKEN);
        itemCount += parseExpression();
        itemCount += parseToken(TokenKind.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeKind.PRED_EXPRESSION, itemCount);
    }

    private int parseChrExpression() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.CHR_KEYWORD);
        itemCount += parseToken(TokenKind.OPEN_BRACKET_TOKEN);
        itemCount += parseExpression();
        itemCount += parseToken(TokenKind.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeKind.CHR_EXPRESSION, itemCount);
    }

    private int parseOrdExpression() {
        int itemCount = 0;
        itemCount += parseToken(TokenKind.ORD_KEYWORD);
        itemCount += parseToken(TokenKind.OPEN_BRACKET_TOKEN);
        itemCount += parseExpression();
        itemCount += parseToken(TokenKind.CLOSE_BRACKET_TOKEN);
        return buildTree(NodeKind.ORD_EXPRESSION, itemCount);
    }

    private int parseName() {
        parseIdentifier(TokenKind.IDENTIFIER);
        return 1;
    }

}
