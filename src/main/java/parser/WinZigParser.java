package parser;

import common.SyntaxKind;
import common.nodes.ASTNode;
import common.nodes.IdentifierNode;
import common.nodes.Node;
import lexer.WinZigLexer;
import lexer.tokens.SyntaxToken;

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
        itemCount += parseToken(SyntaxKind.PROGRAM_KEYWORD);
        itemCount += parseName();
        itemCount += parseToken(SyntaxKind.COLON_TOKEN);
        itemCount += parseConsts();
        itemCount += parseTypes();
        itemCount += parseDclns();
        itemCount += parseSubProgs();
        itemCount += parseBody();
        itemCount += parseName();
        itemCount += parseToken(SyntaxKind.SINGLE_DOT_TOKEN);
        buildTree(SyntaxKind.PROGRAM, itemCount);
    }

    private int parseConsts() {
        int itemCount = 0;
        if (nextTokenIs(SyntaxKind.CONST_KEYWORD)) {
            itemCount += parseToken(SyntaxKind.CONST_KEYWORD);
            itemCount += parseList(this::parseConst, SyntaxKind.COMMA_TOKEN);
            itemCount += parseToken(SyntaxKind.SEMICOLON_TOKEN);
        }
        return buildTree(SyntaxKind.CONSTS, itemCount);
    }

    private int parseConst() {
        int itemCount = 0;
        itemCount += parseName();
        itemCount += parseToken(SyntaxKind.EQUAL_TOKEN);
        itemCount += parseConstValue();
        return buildTree(SyntaxKind.CONST, itemCount);
    }

    private int parseConstValue() {
        if (nextTokenIs(SyntaxKind.INTEGER_LITERAL)) {
            return parseToken(SyntaxKind.INTEGER_LITERAL);
        } else if (nextTokenIs(SyntaxKind.CHAR_LITERAL)) {
            return parseToken(SyntaxKind.CHAR_LITERAL);
        } else {
            return parseName();
        }
    }

    private int parseTypes() {
        int itemCount = 0;
        if (nextTokenIs(SyntaxKind.TYPE_KEYWORD)) {
            itemCount += parseToken(SyntaxKind.TYPE_KEYWORD);
            itemCount += parseType();
            itemCount += parseToken(SyntaxKind.SEMICOLON_TOKEN);
            // After types is always Dclns(nullable), SubProgs(nullable) and Body.
            // So must be followed by 'var', 'function' or 'begin' at the end.
            while (!nextTokenIs(SyntaxKind.VAR_KEYWORD, SyntaxKind.FUNCTION_KEYWORD, SyntaxKind.BEGIN_KEYWORD)) {
                itemCount += parseType();
                itemCount += parseToken(SyntaxKind.SEMICOLON_TOKEN);
            }
        }
        return buildTree(SyntaxKind.TYPES, itemCount);
    }

    private int parseType() {
        int itemCount = 0;
        itemCount += parseName();
        itemCount += parseToken(SyntaxKind.EQUAL_TOKEN);
        itemCount += parseLitList();
        return buildTree(SyntaxKind.TYPE, itemCount);
    }

    private int parseLitList() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        itemCount += parseList(this::parseName, SyntaxKind.COMMA_TOKEN);
        itemCount += parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        return buildTree(SyntaxKind.LIT, itemCount);
    }

    private int parseSubProgs() {
        int itemCount = 0;
        while (nextTokenIs(SyntaxKind.FUNCTION_KEYWORD)) {
            itemCount += parseFcn();
        }
        return buildTree(SyntaxKind.SUBPROGS, itemCount);
    }

    private int parseFcn() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.FUNCTION_KEYWORD);
        itemCount += parseName();
        itemCount += parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        itemCount += parseParams();
        itemCount += parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        itemCount += parseToken(SyntaxKind.COLON_TOKEN);
        itemCount += parseName();
        itemCount += parseToken(SyntaxKind.SEMICOLON_TOKEN);
        itemCount += parseConsts();
        itemCount += parseTypes();
        itemCount += parseDclns();
        itemCount += parseBody();
        itemCount += parseName();
        itemCount += parseToken(SyntaxKind.SEMICOLON_TOKEN);
        return buildTree(SyntaxKind.FCN, itemCount);
    }

    private int parseParams() {
        int itemCount = 0;
        itemCount += parseList(this::parseDcln, SyntaxKind.SEMICOLON_TOKEN);
        return buildTree(SyntaxKind.PARAMS, itemCount);
    }

    private int parseDclns() {
        int itemCount = 0;
        if (nextTokenIs(SyntaxKind.VAR_KEYWORD)) {
            itemCount += parseToken(SyntaxKind.VAR_KEYWORD);
            itemCount += parseDcln();
            itemCount += parseToken(SyntaxKind.SEMICOLON_TOKEN);
            // Dclns is followed by SubProgs(nullable) and Body.
            // So must be followed by 'function' or 'begin' at the end.
            while (!nextTokenIs(SyntaxKind.FUNCTION_KEYWORD, SyntaxKind.BEGIN_KEYWORD)) {
                itemCount += parseDcln();
                itemCount += parseToken(SyntaxKind.SEMICOLON_TOKEN);
            }
        }
        return buildTree(SyntaxKind.DCLNS, itemCount);
    }

    private int parseDcln() {
        int itemCount = 0;
        itemCount += parseList(this::parseName, SyntaxKind.COMMA_TOKEN);
        itemCount += parseToken(SyntaxKind.COLON_TOKEN);
        itemCount += parseName();
        return buildTree(SyntaxKind.VAR, itemCount);
    }

    private int parseBody() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.BEGIN_KEYWORD);
        itemCount += parseList(this::parseStatement, SyntaxKind.SEMICOLON_TOKEN);
        itemCount += parseToken(SyntaxKind.END_KEYWORD);
        return buildTree(SyntaxKind.BLOCK, itemCount);
    }

    private int parseStatement() {
        SyntaxKind syntaxKind = peekKind(0);
        if (syntaxKind == SyntaxKind.OUTPUT_KEYWORD) {
            return parseOutputStatement();
        } else if (syntaxKind == SyntaxKind.IF_KEYWORD) {
            return parseIfStatement();
        } else if (syntaxKind == SyntaxKind.WHILE_KEYWORD) {
            return parseWhileStatement();
        } else if (syntaxKind == SyntaxKind.REPEAT_KEYWORD) {
            return parseRepeatStatement();
        } else if (syntaxKind == SyntaxKind.FOR_KEYWORD) {
            return parseForStatement();
        } else if (syntaxKind == SyntaxKind.LOOP_KEYWORD) {
            return parseLoopStatement();
        } else if (syntaxKind == SyntaxKind.CASE_KEYWORD) {
            return parseCaseStatement();
        } else if (syntaxKind == SyntaxKind.READ_KEYWORD) {
            return parseReadStatement();
        } else if (syntaxKind == SyntaxKind.EXIT_KEYWORD) {
            return parseExitStatement();
        } else if (syntaxKind == SyntaxKind.RETURN_KEYWORD) {
            return parseReturnStatement();
        } else if (syntaxKind == SyntaxKind.BEGIN_KEYWORD) {
            return parseBody();
        } else {
            // The second token of assignment is either ':=' or ':=:'.
            SyntaxKind peekKind = peekKind(1);
            if (peekKind == SyntaxKind.ASSIGNMENT_TOKEN || peekKind == SyntaxKind.SWAP_TOKEN) {
                return parseAssignmentStatement();
            } else {
                return buildTree(SyntaxKind.NULL_STATEMENT, 0);
            }
        }
    }

    private int parseOutputStatement() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.OUTPUT_KEYWORD);
        itemCount += parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        itemCount += parseList(this::parseOutExp, SyntaxKind.COMMA_TOKEN);
        itemCount += parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        return buildTree(SyntaxKind.OUTPUT_STATEMENT, itemCount);
    }

    private int parseIfStatement() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.IF_KEYWORD);
        itemCount += parseExpression();
        itemCount += parseToken(SyntaxKind.THEN_KEYWORD);
        itemCount += parseStatement();
        if (nextTokenIs(SyntaxKind.ELSE_KEYWORD)) {
            itemCount += parseToken(SyntaxKind.ELSE_KEYWORD);
            itemCount += parseStatement();
        }
        return buildTree(SyntaxKind.IF_STATEMENT, itemCount);
    }

    private int parseWhileStatement() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.WHILE_KEYWORD);
        itemCount += parseExpression();
        itemCount += parseToken(SyntaxKind.DO_KEYWORD);
        itemCount += parseStatement();
        return buildTree(SyntaxKind.WHILE_STATEMENT, itemCount);
    }

    private int parseRepeatStatement() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.REPEAT_KEYWORD);
        itemCount += parseList(this::parseStatement, SyntaxKind.SEMICOLON_TOKEN);
        itemCount += parseToken(SyntaxKind.UNTIL_KEYWORD);
        itemCount += parseExpression();
        return buildTree(SyntaxKind.REPEAT_STATEMENT, itemCount);
    }

    private int parseForStatement() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.FOR_KEYWORD);
        itemCount += parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        itemCount += parseForStat();
        itemCount += parseToken(SyntaxKind.SEMICOLON_TOKEN);
        itemCount += parseForExp();
        itemCount += parseToken(SyntaxKind.SEMICOLON_TOKEN);
        itemCount += parseForStat();
        itemCount += parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        itemCount += parseStatement();
        return buildTree(SyntaxKind.FOR_STATEMENT, itemCount);
    }

    private int parseLoopStatement() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.LOOP_KEYWORD);
        itemCount += parseList(this::parseStatement, SyntaxKind.SEMICOLON_TOKEN);
        itemCount += parseToken(SyntaxKind.POOL_KEYWORD);
        return buildTree(SyntaxKind.LOOP_STATEMENT, itemCount);
    }

    private int parseCaseStatement() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.CASE_KEYWORD);
        itemCount += parseExpression();
        itemCount += parseToken(SyntaxKind.OF_KEYWORD);
        itemCount += parseCaseClauses();
        itemCount += parseOtherwiseClause();
        itemCount += parseToken(SyntaxKind.END_KEYWORD);
        return buildTree(SyntaxKind.CASE_STATEMENT, itemCount);
    }

    private int parseReadStatement() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.READ_KEYWORD);
        itemCount += parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        itemCount += parseList(this::parseName, SyntaxKind.COMMA_TOKEN);
        itemCount += parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        return buildTree(SyntaxKind.READ_STATEMENT, itemCount);
    }

    private int parseExitStatement() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.EXIT_KEYWORD);
        return buildTree(SyntaxKind.EXIT_STATEMENT, itemCount);
    }

    private int parseReturnStatement() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.RETURN_KEYWORD);
        itemCount += parseExpression();
        return buildTree(SyntaxKind.RETURN_STATEMENT, itemCount);
    }

    private int parseOutExp() {
        int itemCount = 0;
        if (nextTokenIs(SyntaxKind.STRING_LITERAL)) {
            itemCount += parseStringNode();
            return buildTree(SyntaxKind.STRING_OUT_EXP, itemCount);
        } else {
            itemCount += parseExpression();
            return buildTree(SyntaxKind.INTEGER_OUT_EXP, itemCount);
        }
    }

    private int parseStringNode() {
        return parseToken(SyntaxKind.STRING_LITERAL);
    }

    private int parseCaseClauses() {
        int itemCount = 0;
        itemCount += parseCaseClause();
        itemCount += parseToken(SyntaxKind.SEMICOLON_TOKEN);
        // Case clauses must be followed by otherwise(nullable) or 'end'.
        // So must be followed bt 'otherwise' or 'end'.
        while (!nextTokenIs(SyntaxKind.OTHERWISE_KEYWORD, SyntaxKind.END_KEYWORD)) {
            itemCount += parseCaseClause();
            itemCount += parseToken(SyntaxKind.SEMICOLON_TOKEN);
        }
        return itemCount;
    }

    private int parseCaseClause() {
        int itemCount = 0;
        itemCount += parseList(this::parseCaseExpression, SyntaxKind.COMMA_TOKEN);
        itemCount += parseToken(SyntaxKind.COLON_TOKEN);
        itemCount += parseStatement();
        return buildTree(SyntaxKind.CASE_CLAUSE, itemCount);
    }

    private int parseCaseExpression() {
        int itemCount = 0;
        itemCount += parseConstValue();
        if (nextTokenIs(SyntaxKind.DOUBLE_DOTS_TOKEN)) {
            itemCount += parseToken(SyntaxKind.DOUBLE_DOTS_TOKEN);
            itemCount += parseConstValue();
            return buildTree(SyntaxKind.DOUBLE_DOTS_CLAUSE, itemCount);
        }
        return itemCount;
    }

    private int parseOtherwiseClause() {
        int itemCount = 0;
        if (nextTokenIs(SyntaxKind.OTHERWISE_KEYWORD)) {
            itemCount += parseToken(SyntaxKind.OTHERWISE_KEYWORD);
            itemCount += parseStatement();
            return buildTree(SyntaxKind.OTHERWISE_CLAUSE, itemCount);
        }
        return itemCount;
    }

    private int parseAssignmentStatement() {
        int itemCount = 0;
        itemCount += parseName();
        if (nextTokenIs(SyntaxKind.ASSIGNMENT_TOKEN)) {
            itemCount += parseToken(SyntaxKind.ASSIGNMENT_TOKEN);
            itemCount += parseExpression();
            return buildTree(SyntaxKind.ASSIGNMENT_STATEMENT, itemCount);
        } else {
            itemCount += parseToken(SyntaxKind.SWAP_TOKEN);
            itemCount += parseName();
            return buildTree(SyntaxKind.SWAP_STATEMENT, itemCount);
        }
    }

    private int parseForStat() {
        // ForStat is always followed by ';'.
        if (nextTokenIs(SyntaxKind.SEMICOLON_TOKEN)) {
            return buildTree(SyntaxKind.NULL_STATEMENT, 0);
        } else {
            return parseAssignmentStatement();
        }
    }

    private int parseForExp() {
        // ForStat is always followed by ';'.
        if (nextTokenIs(SyntaxKind.SEMICOLON_TOKEN)) {
            return buildTree(SyntaxKind.TRUE, 0);
        } else {
            return parseExpression();
        }
    }

    private int parseExpression() {
        int itemCount = 0;
        itemCount += parseTerm();
        SyntaxKind syntaxKind = peekKind(0);
        if (syntaxKind == SyntaxKind.LT_EQUAL_TOKEN) {
            itemCount += parseToken(SyntaxKind.LT_EQUAL_TOKEN);
            itemCount += parseTerm();
            return buildTree(SyntaxKind.LT_EQUAL_EXPRESSION, itemCount);
        } else if (syntaxKind == SyntaxKind.LT_TOKEN) {
            itemCount += parseToken(SyntaxKind.LT_TOKEN);
            itemCount += parseTerm();
            return buildTree(SyntaxKind.LT_EXPRESSION, itemCount);
        } else if (syntaxKind == SyntaxKind.GT_EQUAL_TOKEN) {
            itemCount += parseToken(SyntaxKind.GT_EQUAL_TOKEN);
            itemCount += parseTerm();
            return buildTree(SyntaxKind.GT_EQUAL_EXPRESSION, itemCount);
        } else if (syntaxKind == SyntaxKind.GT_TOKEN) {
            itemCount += parseToken(SyntaxKind.GT_TOKEN);
            itemCount += parseTerm();
            return buildTree(SyntaxKind.GT_EXPRESSION, itemCount);
        } else if (syntaxKind == SyntaxKind.EQUAL_TOKEN) {
            itemCount += parseToken(SyntaxKind.EQUAL_TOKEN);
            itemCount += parseTerm();
            return buildTree(SyntaxKind.EQUALS_EXPRESSION, itemCount);
        } else if (syntaxKind == SyntaxKind.NOT_EQUAL_TOKEN) {
            itemCount += parseToken(SyntaxKind.NOT_EQUAL_TOKEN);
            itemCount += parseTerm();
            return buildTree(SyntaxKind.NOT_EQUALS_EXPRESSION, itemCount);
        }
        return itemCount;
    }

    private int parseTerm() {
        int itemCount = 0;
        itemCount += parseFactor();
        while (nextTokenIs(SyntaxKind.PLUS_TOKEN,
                SyntaxKind.MINUS_TOKEN, SyntaxKind.OR_KEYWORD)) {
            SyntaxKind syntaxKind = peekKind(0);
            if (syntaxKind == SyntaxKind.PLUS_TOKEN) {
                itemCount += parseToken(SyntaxKind.PLUS_TOKEN);
                itemCount += parseFactor();
                itemCount = buildTree(SyntaxKind.ADD_EXPRESSION, itemCount);
            } else if (syntaxKind == SyntaxKind.MINUS_TOKEN) {
                itemCount += parseToken(SyntaxKind.MINUS_TOKEN);
                itemCount += parseFactor();
                itemCount = buildTree(SyntaxKind.SUBTRACT_EXPRESSION, itemCount);
            } else if (syntaxKind == SyntaxKind.OR_KEYWORD) {
                itemCount += parseToken(SyntaxKind.OR_KEYWORD);
                itemCount += parseFactor();
                itemCount = buildTree(SyntaxKind.OR_EXPRESSION, itemCount);
            }
        }
        return itemCount;
    }

    private int parseFactor() {
        int itemCount = 0;
        itemCount += parsePrimary();
        while (nextTokenIs(SyntaxKind.MULTIPLY_TOKEN, SyntaxKind.DIVIDE_TOKEN,
                SyntaxKind.AND_KEYWORD, SyntaxKind.MOD_KEYWORD)) {
            SyntaxKind syntaxKind = peekKind(0);
            if (syntaxKind == SyntaxKind.MULTIPLY_TOKEN) {
                itemCount += parseToken(SyntaxKind.MULTIPLY_TOKEN);
                itemCount += parsePrimary();
                itemCount = buildTree(SyntaxKind.MULTIPLY_EXPRESSION, itemCount);
            } else if (syntaxKind == SyntaxKind.DIVIDE_TOKEN) {
                itemCount += parseToken(SyntaxKind.DIVIDE_TOKEN);
                itemCount += parsePrimary();
                itemCount = buildTree(SyntaxKind.DIVIDE_EXPRESSION, itemCount);
            } else if (syntaxKind == SyntaxKind.AND_KEYWORD) {
                itemCount += parseToken(SyntaxKind.AND_KEYWORD);
                itemCount += parsePrimary();
                itemCount = buildTree(SyntaxKind.AND_EXPRESSION, itemCount);
            } else if (syntaxKind == SyntaxKind.MOD_KEYWORD) {
                itemCount += parseToken(SyntaxKind.MOD_KEYWORD);
                itemCount += parsePrimary();
                itemCount = buildTree(SyntaxKind.MOD_EXPRESSION, itemCount);
            }
        }
        return itemCount;
    }

    private int parsePrimary() {
        int itemCount = 0;
        SyntaxKind syntaxKind = peekKind(0);
        if (syntaxKind == SyntaxKind.MINUS_TOKEN) {
            itemCount += parseNegativeExpression();
        } else if (syntaxKind == SyntaxKind.PLUS_TOKEN) {
            itemCount += parseToken(SyntaxKind.PLUS_TOKEN);
            itemCount += parsePrimary();
        } else if (syntaxKind == SyntaxKind.NOT_KEYWORD) {
            itemCount += parseNotExpression();
        } else if (syntaxKind == SyntaxKind.EOF_TOKEN) {
            itemCount += parseEofExpression();
        } else if (syntaxKind == SyntaxKind.INTEGER_LITERAL) {
            itemCount += parseToken(SyntaxKind.INTEGER_LITERAL);
        } else if (syntaxKind == SyntaxKind.CHAR_LITERAL) {
            itemCount += parseToken(SyntaxKind.CHAR_LITERAL);
        } else if (syntaxKind == SyntaxKind.OPEN_BRACKET_TOKEN) {
            itemCount += parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
            itemCount += parseExpression();
            itemCount += parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        } else if (syntaxKind == SyntaxKind.SUCC_KEYWORD) {
            itemCount += parseSuccExpression();
        } else if (syntaxKind == SyntaxKind.PRED_KEYWORD) {
            itemCount += parsePredExpression();
        } else if (syntaxKind == SyntaxKind.CHR_KEYWORD) {
            itemCount += parseChrExpression();
        } else if (syntaxKind == SyntaxKind.ORD_KEYWORD) {
            itemCount += parseOrdExpression();
        } else {
            SyntaxKind nextNextKind = peekKind(1);
            // Is this correct?
            if (nextNextKind == SyntaxKind.OPEN_BRACKET_TOKEN) {
                itemCount += parseCallExpression();
            } else {
                itemCount += parseToken(SyntaxKind.IDENTIFIER);
            }
        }
        return itemCount;
    }

    private int parseNegativeExpression() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.MINUS_TOKEN);
        itemCount += parsePrimary();
        return buildTree(SyntaxKind.NEGATIVE_EXPRESSION, itemCount);
    }

    private int parseNotExpression() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.NOT_KEYWORD);
        itemCount += parsePrimary();
        return buildTree(SyntaxKind.NOT_EXPRESSION, itemCount);
    }

    private int parseEofExpression() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.EOF_KEYWORD);
        return buildTree(SyntaxKind.EOF_EXPRESSION, itemCount);
    }

    private int parseCallExpression() {
        int itemCount = 0;
        itemCount += parseName();
        itemCount += parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        itemCount += parseList(this::parseExpression, SyntaxKind.COMMA_TOKEN);
        itemCount += parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        return buildTree(SyntaxKind.CALL_EXPRESSION, itemCount);
    }

    private int parseSuccExpression() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.SUCC_KEYWORD);
        itemCount += parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        itemCount += parseExpression();
        itemCount += parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        return buildTree(SyntaxKind.SUCC_EXPRESSION, itemCount);
    }

    private int parsePredExpression() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.PRED_KEYWORD);
        itemCount += parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        itemCount += parseExpression();
        itemCount += parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        return buildTree(SyntaxKind.PRED_EXPRESSION, itemCount);
    }

    private int parseChrExpression() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.CHR_KEYWORD);
        itemCount += parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        itemCount += parseExpression();
        itemCount += parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        return buildTree(SyntaxKind.CHR_EXPRESSION, itemCount);
    }

    private int parseOrdExpression() {
        int itemCount = 0;
        itemCount += parseToken(SyntaxKind.ORD_KEYWORD);
        itemCount += parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        itemCount += parseExpression();
        itemCount += parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        return buildTree(SyntaxKind.ORD_EXPRESSION, itemCount);
    }

    private int parseName() {
        parseToken(SyntaxKind.IDENTIFIER);
        nodeStack.push(new IdentifierNode(nodeStack.pop()));
        return 1;
    }

    private int parseToken(SyntaxKind kind) {
        if (nextTokenIs(kind)) {
            SyntaxToken token = tokenReader.read();
            nodeStack.push(new Node(token));
            return 1;
        } else {
            SyntaxKind foundKind = peekKind(0);
            String message = String.format("Expected %s[%s] but found %s[%s]",
                    kind, kind.getValue(), foundKind, foundKind.getValue());
            throw new IllegalStateException(message);
        }
    }

    private int parseList(Supplier<Integer> parser, SyntaxKind seperator) {
        int itemCount = 0;
        itemCount += parser.get();
        while (nextTokenIs(seperator)) {
            itemCount += parseToken(seperator);
            itemCount += parser.get();
        }
        return itemCount;
    }

    private int buildTree(SyntaxKind kind, int childrenCount) {
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


    private boolean nextTokenIs(SyntaxKind... kinds) {
        SyntaxKind nextKind = peekKind(0);
        for (SyntaxKind kind : kinds) {
            if (kind.equals(nextKind)) {
                return true;
            }
        }
        return false;
    }

    private SyntaxKind peekKind(int skip) {
        return tokenReader.peek(skip).getKind();
    }
}
