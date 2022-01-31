package parser;

import common.SyntaxKind;
import lexer.WinZigLexer;
import lexer.tokens.SyntaxToken;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class WinZigParser extends AbstractParser {
    private final Stack<AtomicInteger> contextStack;
    private final Stack<Node> nodeStack;

    public WinZigParser(WinZigLexer lexer) {
        super(lexer);
        this.contextStack = new Stack<>();
        this.nodeStack = new Stack<>();
    }

    @Override
    public Node parse() {
        parseWinZig();
        Node winZigNode = nodeStack.pop();
        if (nodeStack.isEmpty()) {
            return winZigNode;
        }
        throw new IllegalStateException(nodeStack.toString());
    }

    private void parseWinZig() {
        startContext();
        parseToken(SyntaxKind.PROGRAM_KEYWORD);
        parseName();
        parseToken(SyntaxKind.COLON_TOKEN);
        parseConsts();
        parseTypes();
        parseDclns();
        parseSubProgs();
        parseBody();
        parseName();
        parseToken(SyntaxKind.SINGLE_DOT_TOKEN);
        endContext(SyntaxKind.PROGRAM);
    }

    private void parseConsts() {
        startContext();
        if (nextTokenIs(SyntaxKind.CONST_KEYWORD)) {
            parseToken(SyntaxKind.CONST_KEYWORD);
            parseList(this::parseConst, SyntaxKind.COMMA_TOKEN);
            parseToken(SyntaxKind.SEMICOLON_TOKEN);
        }
        endContext(SyntaxKind.CONSTS);
    }

    private void parseConst() {
        startContext();
        parseName();
        parseToken(SyntaxKind.EQUAL_TOKEN);
        parseConstValue();
        endContext(SyntaxKind.CONST);
    }

    private void parseConstValue() {
        if (nextTokenIs(SyntaxKind.INTEGER_LITERAL)) {
            parseToken(SyntaxKind.INTEGER_LITERAL);
        } else if (nextTokenIs(SyntaxKind.CHAR_LITERAL)) {
            parseToken(SyntaxKind.CHAR_LITERAL);
        } else {
            parseName();
        }
    }

    private void parseTypes() {
        startContext();
        if (nextTokenIs(SyntaxKind.TYPE_KEYWORD)) {
            parseToken(SyntaxKind.TYPE_KEYWORD);
            parseType();
            parseToken(SyntaxKind.SEMICOLON_TOKEN);
            // After types is always Dclns(nullable), SubProgs(nullable) and Body.
            // So must be followed by 'var', 'function' or 'begin' at the end.
            while (!nextTokenIs(SyntaxKind.VAR_KEYWORD,
                    SyntaxKind.FUNCTION_KEYWORD,
                    SyntaxKind.BEGIN_KEYWORD)) {
                parseType();
                parseToken(SyntaxKind.SEMICOLON_TOKEN);
            }
        }
        endContext(SyntaxKind.TYPES);
    }

    private void parseType() {
        startContext();
        parseName();
        parseToken(SyntaxKind.EQUAL_TOKEN);
        parseLitList();
        endContext(SyntaxKind.TYPE);
    }

    private void parseLitList() {
        startContext();
        parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        parseList(this::parseName, SyntaxKind.COMMA_TOKEN);
        parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        endContext(SyntaxKind.LIT);
    }

    private void parseSubProgs() {
        startContext();
        while (nextTokenIs(SyntaxKind.FUNCTION_KEYWORD)) {
            parseFcn();
        }
        endContext(SyntaxKind.SUBPROGS);
    }

    private void parseFcn() {
        startContext();
        parseToken(SyntaxKind.FUNCTION_KEYWORD);
        parseName();
        parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        parseParams();
        parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        parseToken(SyntaxKind.COLON_TOKEN);
        parseName();
        parseToken(SyntaxKind.SEMICOLON_TOKEN);
        parseConsts();
        parseTypes();
        parseDclns();
        parseBody();
        parseName();
        parseToken(SyntaxKind.SEMICOLON_TOKEN);
        endContext(SyntaxKind.FCN);
    }

    private void parseParams() {
        startContext();
        parseList(this::parseDcln, SyntaxKind.SEMICOLON_TOKEN);
        endContext(SyntaxKind.PARAMS);
    }

    private void parseDclns() {
        startContext();
        if (nextTokenIs(SyntaxKind.VAR_KEYWORD)) {
            parseToken(SyntaxKind.VAR_KEYWORD);
            parseDcln();
            parseToken(SyntaxKind.SEMICOLON_TOKEN);
            // Dclns is followed by SubProgs(nullable) and Body.
            // So must be followed by 'function' or 'begin' at the end.
            while (!nextTokenIs(SyntaxKind.FUNCTION_KEYWORD, SyntaxKind.BEGIN_KEYWORD)) {
                parseDcln();
                parseToken(SyntaxKind.SEMICOLON_TOKEN);
            }
        }
        endContext(SyntaxKind.DCLNS);
    }

    private void parseDcln() {
        startContext();
        parseList(this::parseName, SyntaxKind.COMMA_TOKEN);
        parseToken(SyntaxKind.COLON_TOKEN);
        parseName();
        endContext(SyntaxKind.VAR);
    }

    private void parseBody() {
        startContext();
        parseToken(SyntaxKind.BEGIN_KEYWORD);
        parseList(this::parseStatement, SyntaxKind.SEMICOLON_TOKEN);
        parseToken(SyntaxKind.END_KEYWORD);
        endContext(SyntaxKind.BLOCK);
    }

    private void parseStatement() {
        switch (peekKind(0)) {
            case OUTPUT_KEYWORD:
                parseOutputStatement();
                break;
            case IF_KEYWORD:
                parseIfStatement();
                break;
            case WHILE_KEYWORD:
                parseWhileStatement();
                break;
            case REPEAT_KEYWORD:
                parseRepeatStatement();
                break;
            case FOR_KEYWORD:
                parseForStatement();
                break;
            case LOOP_KEYWORD:
                parseLoopStatement();
                break;
            case CASE_KEYWORD:
                parseCaseStatement();
                break;
            case READ_KEYWORD:
                parseReadStatement();
                break;
            case EXIT_KEYWORD:
                parseExitStatement();
                break;
            case RETURN_KEYWORD:
                parseReturnStatement();
                break;
            case BEGIN_KEYWORD:
                parseBody();
                break;
            default:
                // The second token of assignment is either ':=' or ':=:'.
                SyntaxKind peekKind = peekKind(1);
                if (peekKind == SyntaxKind.ASSIGNMENT_TOKEN
                        || peekKind == SyntaxKind.SWAP_TOKEN) {
                    parseAssignmentStatement();
                } else {
                    startContext();
                    endContext(SyntaxKind.NULL_STATEMENT);
                }
                break;
        }
    }

    private void parseOutputStatement() {
        startContext();
        parseToken(SyntaxKind.OUTPUT_KEYWORD);
        parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        parseList(this::parseOutExp, SyntaxKind.COMMA_TOKEN);
        parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        endContext(SyntaxKind.OUTPUT_STATEMENT);
    }

    private void parseIfStatement() {
        startContext();
        parseToken(SyntaxKind.IF_KEYWORD);
        parseExpression();
        parseToken(SyntaxKind.THEN_KEYWORD);
        parseStatement();
        if (nextTokenIs(SyntaxKind.ELSE_KEYWORD)) {
            parseToken(SyntaxKind.ELSE_KEYWORD);
            parseStatement();
        }
        endContext(SyntaxKind.IF_STATEMENT);
    }

    private void parseWhileStatement() {
        startContext();
        parseToken(SyntaxKind.WHILE_KEYWORD);
        parseExpression();
        parseToken(SyntaxKind.DO_KEYWORD);
        parseStatement();
        endContext(SyntaxKind.WHILE_STATEMENT);
    }

    private void parseRepeatStatement() {
        startContext();
        parseToken(SyntaxKind.REPEAT_KEYWORD);
        parseList(this::parseStatement, SyntaxKind.SEMICOLON_TOKEN);
        parseToken(SyntaxKind.UNTIL_KEYWORD);
        parseExpression();
        endContext(SyntaxKind.REPEAT_STATEMENT);
    }

    private void parseForStatement() {
        startContext();
        parseToken(SyntaxKind.FOR_KEYWORD);
        parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        parseForStat();
        parseToken(SyntaxKind.SEMICOLON_TOKEN);
        parseForExp();
        parseToken(SyntaxKind.SEMICOLON_TOKEN);
        parseForStat();
        parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        parseStatement();
        endContext(SyntaxKind.FOR_STATEMENT);
    }

    private void parseLoopStatement() {
        startContext();
        parseToken(SyntaxKind.LOOP_KEYWORD);
        parseList(this::parseStatement, SyntaxKind.SEMICOLON_TOKEN);
        parseToken(SyntaxKind.POOL_KEYWORD);
        endContext(SyntaxKind.LOOP_STATEMENT);
    }

    private void parseCaseStatement() {
        startContext();
        parseToken(SyntaxKind.CASE_KEYWORD);
        parseExpression();
        parseToken(SyntaxKind.OF_KEYWORD);
        parseCaseClauses();
        parseOtherwiseClause();
        parseToken(SyntaxKind.END_KEYWORD);
        endContext(SyntaxKind.CASE_STATEMENT);
    }

    private void parseReadStatement() {
        startContext();
        parseToken(SyntaxKind.READ_KEYWORD);
        parseToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        parseList(this::parseName, SyntaxKind.COMMA_TOKEN);
        parseToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        endContext(SyntaxKind.READ_STATEMENT);
    }

    private void parseExitStatement() {
        startContext();
        parseToken(SyntaxKind.EXIT_KEYWORD);
        endContext(SyntaxKind.EXIT_STATEMENT);
    }

    private void parseReturnStatement() {
        startContext();
        parseToken(SyntaxKind.RETURN_KEYWORD);
        parseExpression();
        endContext(SyntaxKind.RETURN_STATEMENT);
    }

    private void parseOutExp() {
        startContext();
        if (nextTokenIs(SyntaxKind.STRING_LITERAL)) {
            parseStringNode();
            endContext(SyntaxKind.STRING_OUT_EXP);
        } else {
            parseExpression();
            endContext(SyntaxKind.INTEGER_OUT_EXP);
        }
    }

    private void parseStringNode() {
        parseToken(SyntaxKind.STRING_LITERAL);
    }

    private void parseCaseClauses() {
        parseCaseClause();
        parseToken(SyntaxKind.SEMICOLON_TOKEN);
        // Case clauses must be followed by otherwise(nullable) or 'end'.
        // So must be followed bt 'otherwise' or 'end'.
        while (!nextTokenIs(SyntaxKind.OTHERWISE_KEYWORD, SyntaxKind.END_KEYWORD)) {
            parseCaseClause();
            parseToken(SyntaxKind.SEMICOLON_TOKEN);
        }
    }

    private void parseCaseClause() {
        startContext();
        parseList(this::parseCaseExpression, SyntaxKind.COMMA_TOKEN);
        parseToken(SyntaxKind.COLON_TOKEN);
        parseStatement();
        endContext(SyntaxKind.CASE_CLAUSE);
    }

    private void parseCaseExpression() {
        startContext();
        parseConstValue();
        if (nextTokenIs(SyntaxKind.DOUBLE_DOTS_TOKEN)) {
            parseToken(SyntaxKind.DOUBLE_DOTS_TOKEN);
            parseConstValue();
            endContext(SyntaxKind.DOUBLE_DOTS_CLAUSE);
        } else {
            abortContext();
        }
    }

    private void parseOtherwiseClause() {
        if (nextTokenIs(SyntaxKind.OTHERWISE_KEYWORD)) {
            startContext();
            parseToken(SyntaxKind.OTHERWISE_KEYWORD);
            parseStatement();
            endContext(SyntaxKind.OTHERWISE_CLAUSE);
        }
    }

    private void parseAssignmentStatement() {
        startContext();
        parseName();
        if (nextTokenIs(SyntaxKind.ASSIGNMENT_TOKEN)) {
            parseToken(SyntaxKind.ASSIGNMENT_TOKEN);
            parseExpression();
            endContext(SyntaxKind.ASSIGNMENT_STATEMENT);
        } else {
            parseToken(SyntaxKind.SWAP_TOKEN);
            parseName();
            endContext(SyntaxKind.SWAP_STATEMENT);
        }
    }

    private void parseForStat() {
        // ForStat is always followed by ';'.
        if (nextTokenIs(SyntaxKind.SEMICOLON_TOKEN)) {
            startContext();
            endContext(SyntaxKind.NULL_STATEMENT);
        } else {
            parseAssignmentStatement();
        }
    }

    private void parseForExp() {
        // ForStat is always followed by ';'.
        if (nextTokenIs(SyntaxKind.SEMICOLON_TOKEN)) {
            startContext();
            endContext(SyntaxKind.TRUE);
        } else {
            parseExpression();
        }
    }

    private void parseExpression() {
    }

    private void parseName() {
        parseToken(SyntaxKind.IDENTIFIER);
    }

    private void parseToken(SyntaxKind kind) {
        if (nextTokenIs(kind)) {
            SyntaxToken token = tokenReader.read();
            nodeStack.push(new Node(token));
            contextStack.peek().getAndIncrement();
        } else {
            String message = String.format("Expected %s but found %s", kind, peekKind(0));
            throw new IllegalStateException(message);
        }
    }

    private void parseList(Runnable parser, SyntaxKind seperator) {
        parser.run();
        while (nextTokenIs(seperator)) {
            parseToken(seperator);
            parser.run();
        }
    }

    private void startContext() {
        contextStack.push(new AtomicInteger(0));
    }

    private void endContext(SyntaxKind kind) {
        int childrenCount = contextStack.pop().get();
        Stack<Node> children = new Stack<>();
        for (int i = 0; i < childrenCount; i++) {
            children.push(nodeStack.pop());
        }
        Node node = new Node(kind);
        for (int i = 0; i < childrenCount; i++) {
            node.addChild(children.pop());
        }
        nodeStack.push(node);
        if (!contextStack.isEmpty()) {
            contextStack.peek().getAndIncrement();
        }
    }

    private void abortContext() {
        int childrenCount = contextStack.pop().get();
        contextStack.peek().addAndGet(childrenCount);
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
