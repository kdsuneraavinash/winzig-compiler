package parser;

import lexer.WinZigLexer;
import lexer.tokens.Token;
import lexer.tokens.TokenKind;
import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import parser.nodes.Node;
import parser.nodes.NodeKind;

import java.util.Stack;
import java.util.function.Supplier;

public class WinZigParser extends AbstractParser {
    private final Stack<Node> nodeStack;

    public WinZigParser(WinZigLexer lexer) {
        super(lexer);
        this.nodeStack = new Stack<>();
    }

    @Override
    public ASTNode parse() {
        parseWinZig();
        Node winZigNode = nodeStack.pop();
        if (winZigNode instanceof ASTNode) {
            if (nodeStack.isEmpty()) {
                return (ASTNode) winZigNode;
            }
            throw new IllegalStateException("Expected ASTNode which was not found");
        }
        String errorMessage = String.format("[%s] but remaining %s", winZigNode, nodeStack);
        throw new IllegalStateException(errorMessage);
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
        if (nextTokenIs(TokenKind.CONST_KEYWORD)) {
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
        if (nextTokenIs(TokenKind.INTEGER_LITERAL)) {
            return parseIdentifier(TokenKind.INTEGER_LITERAL);
        } else if (nextTokenIs(TokenKind.CHAR_LITERAL)) {
            return parseIdentifier(TokenKind.CHAR_LITERAL);
        } else {
            return parseName();
        }
    }

    private int parseTypes() {
        int itemCount = 0;
        if (nextTokenIs(TokenKind.TYPE_KEYWORD)) {
            itemCount += parseToken(TokenKind.TYPE_KEYWORD);
            itemCount += parseType();
            itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
            // After types is always Dclns(nullable), SubProgs(nullable) and Body.
            // So must be followed by 'var', 'function' or 'begin' at the end.
            while (!nextTokenIs(TokenKind.VAR_KEYWORD, TokenKind.FUNCTION_KEYWORD, TokenKind.BEGIN_KEYWORD)) {
                itemCount += parseType();
                itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
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
        while (nextTokenIs(TokenKind.FUNCTION_KEYWORD)) {
            itemCount += parseFcn();
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
        if (nextTokenIs(TokenKind.VAR_KEYWORD)) {
            itemCount += parseToken(TokenKind.VAR_KEYWORD);
            itemCount += parseDcln();
            itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
            // Dclns is followed by SubProgs(nullable) and Body.
            // So must be followed by 'function' or 'begin' at the end.
            while (!nextTokenIs(TokenKind.FUNCTION_KEYWORD, TokenKind.BEGIN_KEYWORD)) {
                itemCount += parseDcln();
                itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
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
            TokenKind peekKind = peekNextNextKind();
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
        if (nextTokenIs(TokenKind.ELSE_KEYWORD)) {
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
        if (nextTokenIs(TokenKind.STRING_LITERAL)) {
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
        while (!nextTokenIs(TokenKind.OTHERWISE_KEYWORD, TokenKind.END_KEYWORD)) {
            itemCount += parseCaseClause();
            itemCount += parseToken(TokenKind.SEMICOLON_TOKEN);
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
        if (nextTokenIs(TokenKind.DOUBLE_DOTS_TOKEN)) {
            itemCount += parseToken(TokenKind.DOUBLE_DOTS_TOKEN);
            itemCount += parseConstValue();
            return buildTree(NodeKind.DOUBLE_DOTS_CLAUSE, itemCount);
        }
        return itemCount;
    }

    private int parseOtherwiseClause() {
        int itemCount = 0;
        if (nextTokenIs(TokenKind.OTHERWISE_KEYWORD)) {
            itemCount += parseToken(TokenKind.OTHERWISE_KEYWORD);
            itemCount += parseStatement();
            return buildTree(NodeKind.OTHERWISE_CLAUSE, itemCount);
        }
        return itemCount;
    }

    private int parseAssignmentStatement() {
        int itemCount = 0;
        itemCount += parseName();
        if (nextTokenIs(TokenKind.ASSIGNMENT_TOKEN)) {
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
        if (nextTokenIs(TokenKind.SEMICOLON_TOKEN)) {
            return buildTree(NodeKind.NULL_STATEMENT, 0);
        } else {
            return parseAssignmentStatement();
        }
    }

    private int parseForExp() {
        // ForStat is always followed by ';'.
        if (nextTokenIs(TokenKind.SEMICOLON_TOKEN)) {
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
        while (nextTokenIs(TokenKind.PLUS_TOKEN,
                TokenKind.MINUS_TOKEN, TokenKind.OR_KEYWORD)) {
            TokenKind tokenKind = peekNextKind();
            if (tokenKind == TokenKind.PLUS_TOKEN) {
                itemCount += parseToken(TokenKind.PLUS_TOKEN);
                itemCount += parseFactor();
                itemCount = buildTree(NodeKind.ADD_EXPRESSION, itemCount);
            } else if (tokenKind == TokenKind.MINUS_TOKEN) {
                itemCount += parseToken(TokenKind.MINUS_TOKEN);
                itemCount += parseFactor();
                itemCount = buildTree(NodeKind.SUBTRACT_EXPRESSION, itemCount);
            } else if (tokenKind == TokenKind.OR_KEYWORD) {
                itemCount += parseToken(TokenKind.OR_KEYWORD);
                itemCount += parseFactor();
                itemCount = buildTree(NodeKind.OR_EXPRESSION, itemCount);
            }
        }
        return itemCount;
    }

    private int parseFactor() {
        int itemCount = 0;
        itemCount += parsePrimary();
        while (nextTokenIs(TokenKind.MULTIPLY_TOKEN, TokenKind.DIVIDE_TOKEN,
                TokenKind.AND_KEYWORD, TokenKind.MOD_KEYWORD)) {
            TokenKind tokenKind = peekNextKind();
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
            } else if (tokenKind == TokenKind.MOD_KEYWORD) {
                itemCount += parseToken(TokenKind.MOD_KEYWORD);
                itemCount += parsePrimary();
                itemCount = buildTree(NodeKind.MOD_EXPRESSION, itemCount);
            }
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
            TokenKind nextNextKind = peekNextNextKind();
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

    private int parseIdentifier(TokenKind kind) {
        if (nextTokenIs(kind)) {
            Token nextToken = tokenReader.read();
            nodeStack.push(new IdentifierNode(nextToken));
            return 1;
        } else {
            TokenKind foundKind = peekNextKind();
            String message = String.format("Expected %s[%s] but found %s[%s]",
                    kind, kind.getValue(), foundKind, foundKind.getValue());
            throw new IllegalStateException(message);
        }
    }

    private int parseToken(TokenKind kind) {
        if (nextTokenIs(kind)) {
            tokenReader.read();
            return 0;
        } else {
            TokenKind foundKind = peekNextKind();
            String message = String.format("Expected %s[%s] but found %s[%s]",
                    kind, kind.getValue(), foundKind, foundKind.getValue());
            throw new IllegalStateException(message);
        }
    }

    private int parseList(Supplier<Integer> parser, TokenKind seperator) {
        int itemCount = 0;
        itemCount += parser.get();
        while (nextTokenIs(seperator)) {
            itemCount += parseToken(seperator);
            itemCount += parser.get();
        }
        return itemCount;
    }

    private int buildTree(NodeKind kind, int childrenCount) {
        Stack<Node> children = new Stack<>();
        for (int i = 0; i < childrenCount; i++) {
            children.push(nodeStack.pop());
        }
        ASTNode node = new ASTNode(kind);
        for (int i = 0; i < childrenCount; i++) {
            node.addChild(children.pop());
        }
        nodeStack.push(node);
        return 1;
    }


    private boolean nextTokenIs(TokenKind... kinds) {
        TokenKind nextKind = peekNextKind();
        for (TokenKind kind : kinds) {
            if (kind.equals(nextKind)) {
                return true;
            }
        }
        return false;
    }

    private TokenKind peekNextNextKind() {
        return tokenReader.peek(1).getKind();
    }

    private TokenKind peekNextKind() {
        return tokenReader.peek().getKind();
    }
}
