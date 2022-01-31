package parser;

import common.SyntaxKind;
import lexer.WinZigLexer;
import lexer.tokens.SyntaxToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;

public class WinZigParser extends AbstractParser {
    private Stack<Context> contextStack;
    private Stack<Node> nodeStack;

    public WinZigParser(WinZigLexer lexer) {
        super(lexer);
        this.contextStack = new Stack<>();
        this.nodeStack = new Stack<>();
    }

    @Override
    public Node parse() {
        return parseWinZig();
    }

    private Node parseWinZig() {
        Node node = new Node(SyntaxKind.PROGRAM);
        node.addChild(createNode(SyntaxKind.PROGRAM_KEYWORD));
        node.addChild(parseName());
        node.addChild(createNode(SyntaxKind.COLON_TOKEN));
        node.addChild(parseConsts());
        node.addChild(parseTypes());
        node.addChild(parseDclns());
        node.addChild(parseSubProgs());
        node.addChild(parseBody());
        node.addChild(parseName());
        node.addChild(createNode(SyntaxKind.SINGLE_DOT_TOKEN));
        return node;
    }

    private Node parseConsts() {
        Node node = new Node(SyntaxKind.CONSTS);
        if (isKind(SyntaxKind.CONST_KEYWORD)) {
            node.addChild(createNode(SyntaxKind.CONST_KEYWORD));
            node.addChildren(createNodeList(this::parseConst, SyntaxKind.COMMA_TOKEN));
            node.addChild(createNode(SyntaxKind.SEMICOLON_TOKEN));
        }
        return node;
    }

    private Node parseConst() {
        Node node = new Node(SyntaxKind.CONST);
        node.addChild(parseName());
        node.addChild(createNode(SyntaxKind.EQUAL_TOKEN));
        node.addChild(parseConstValue());
        return node;
    }

    private Node parseConstValue() {
        if (isKind(SyntaxKind.INTEGER_LITERAL)) {
            return createNode(SyntaxKind.INTEGER_LITERAL);
        }
        if (isKind(SyntaxKind.CHAR_LITERAL)) {
            return createNode(SyntaxKind.CHAR_LITERAL);
        }
        return parseName();
    }

    private Node parseTypes() {
        Node node = new Node(SyntaxKind.TYPES);
        if (isKind(SyntaxKind.TYPE_KEYWORD)) {
            node.addChild(createNode(SyntaxKind.TYPE_KEYWORD));
            node.addChild(parseType());
            node.addChild(createNode(SyntaxKind.SEMICOLON_TOKEN));
            // After types is always Dclns(nullable), SubProgs(nullable) and Body.
            // So must be followed by 'var', 'function' or 'begin' at the end.
            while (!isKind(SyntaxKind.VAR_KEYWORD)
                    && !isKind(SyntaxKind.FUNCTION_KEYWORD)
                    && !isKind(SyntaxKind.BEGIN_KEYWORD)) {
                node.addChild(parseType());
                node.addChild(createNode(SyntaxKind.SEMICOLON_TOKEN));
            }
        }
        return node;
    }

    private Node parseType() {
        Node node = new Node(SyntaxKind.TYPE);
        node.addChild(parseName());
        node.addChild(createNode(SyntaxKind.EQUAL_TOKEN));
        node.addChild(parseLitList());
        return node;
    }

    private Node parseLitList() {
        Node node = new Node(SyntaxKind.LIT);
        node.addChild(createNode(SyntaxKind.OPEN_BRACKET_TOKEN));
        node.addChildren(createNodeList(this::parseName, SyntaxKind.COMMA_TOKEN));
        node.addChild(createNode(SyntaxKind.CLOSE_BRACKET_TOKEN));
        return node;
    }

    private Node parseSubProgs() {
        Node node = new Node(SyntaxKind.SUB_PROGS);
        while (isKind(SyntaxKind.FUNCTION_KEYWORD)) {
            node.addChild(parseFcn());
        }
        return node;
    }

    private Node parseFcn() {
        Node node = new Node(SyntaxKind.FCN);
        node.addChild(createNode(SyntaxKind.FUNCTION_KEYWORD));
        node.addChild(parseName());
        node.addChild(createNode(SyntaxKind.OPEN_BRACKET_TOKEN));
        node.addChild(parseParams());
        node.addChild(createNode(SyntaxKind.CLOSE_BRACKET_TOKEN));
        node.addChild(createNode(SyntaxKind.COLON_TOKEN));
        node.addChild(parseName());
        node.addChild(createNode(SyntaxKind.SEMICOLON_TOKEN));
        node.addChild(parseConsts());
        node.addChild(parseTypes());
        node.addChild(parseDclns());
        node.addChild(parseBody());
        node.addChild(parseName());
        node.addChild(createNode(SyntaxKind.SEMICOLON_TOKEN));
        return node;
    }

    private Node parseParams() {
        Node node = new Node(SyntaxKind.PARAMS);
        node.addChildren(createNodeList(this::parseDcln, SyntaxKind.SEMICOLON_TOKEN));
        return node;
    }

    private Node parseDclns() {
        Node node = new Node(SyntaxKind.DCLNS);
        if (isKind(SyntaxKind.VAR_KEYWORD)) {
            node.addChild(createNode(SyntaxKind.VAR_KEYWORD));
            node.addChild(parseDcln());
            node.addChild(createNode(SyntaxKind.SEMICOLON_TOKEN));
            // Dclns is followed by SubProgs(nullable) and Body.
            // So must be followed by 'function' or 'begin' at the end.
            while (!isKind(SyntaxKind.FUNCTION_KEYWORD)
                    && !isKind(SyntaxKind.BEGIN_KEYWORD)) {
                node.addChild(parseDcln());
                node.addChild(createNode(SyntaxKind.SEMICOLON_TOKEN));
            }
        }
        return node;
    }

    private Node parseDcln() {
        Node node = new Node(SyntaxKind.VAR);
        node.addChildren(createNodeList(this::parseName, SyntaxKind.COMMA_TOKEN));
        node.addChild(createNode(SyntaxKind.COLON_TOKEN));
        node.addChild(parseName());
        return node;
    }

    private Node parseBody() {
        Node node = new Node(SyntaxKind.BLOCK);
        node.addChild(createNode(SyntaxKind.BEGIN_KEYWORD));
        node.addChildren(createNodeList(this::parseStatement, SyntaxKind.SEMICOLON_TOKEN));
        node.addChild(createNode(SyntaxKind.END_KEYWORD));
        return node;
    }

    private Node parseStatement() {
        switch (peekKind(0)) {
            case OUTPUT_KEYWORD:
                return parseOutputStatement();
            case IF_KEYWORD:
                return parseIfStatement();
            case WHILE_KEYWORD:
                return parseWhileStatement();
            case REPEAT_KEYWORD:
                return parseRepeatStatement();
            case FOR_KEYWORD:
                return parseForStatement();
            case LOOP_KEYWORD:
                return parseLoopStatement();
            case CASE_KEYWORD:
                return parseCaseStatement();
            case READ_KEYWORD:
                return parseReadStatement();
            case EXIT_KEYWORD:
                return parseExitStatement();
            case RETURN_KEYWORD:
                return parseReturnStatement();
            case BEGIN_KEYWORD:
                return parseBody();
            default:
                // The second token of assignment is either ':=' or ':=:'.
                SyntaxKind peekKind = peekKind(1);
                if (SyntaxKind.ASSIGNMENT_TOKEN.equals(peekKind)
                        || SyntaxKind.SWAP_TOKEN.equals(peekKind)) {
                    return parseAssignmentStatement();
                }
                return new Node(SyntaxKind.NULL_STATEMENT);
        }
    }

    private Node parseOutputStatement() {
        Node node = new Node(SyntaxKind.OUTPUT_STATEMENT);
        node.addChild(createNode(SyntaxKind.OUTPUT_KEYWORD));
        node.addChild(createNode(SyntaxKind.OPEN_BRACKET_TOKEN));
        node.addChildren(createNodeList(this::parseOutExp, SyntaxKind.COMMA_TOKEN));
        node.addChild(createNode(SyntaxKind.CLOSE_BRACKET_TOKEN));
        return node;
    }

    private Node parseIfStatement() {
        Node node = new Node(SyntaxKind.IF_STATEMENT);
        node.addChild(createNode(SyntaxKind.IF_KEYWORD));
        node.addChild(parseExpression());
        node.addChild(createNode(SyntaxKind.THEN_KEYWORD));
        node.addChild(parseStatement());
        if (isKind(SyntaxKind.ELSE_KEYWORD)) {
            node.addChild(createNode(SyntaxKind.ELSE_KEYWORD));
            node.addChild(parseStatement());
        }
        return node;
    }

    private Node parseWhileStatement() {
        Node node = new Node(SyntaxKind.WHILE_STATEMENT);
        node.addChild(createNode(SyntaxKind.WHILE_KEYWORD));
        node.addChild(parseExpression());
        node.addChild(createNode(SyntaxKind.DO_KEYWORD));
        node.addChild(parseStatement());
        return node;
    }

    private Node parseRepeatStatement() {
        Node node = new Node(SyntaxKind.REPEAT_STATEMENT);
        node.addChild(createNode(SyntaxKind.REPEAT_KEYWORD));
        node.addChildren(createNodeList(this::parseStatement, SyntaxKind.SEMICOLON_TOKEN));
        node.addChild(createNode(SyntaxKind.UNTIL_KEYWORD));
        node.addChild(parseExpression());
        return node;
    }

    private Node parseForStatement() {
        Node node = new Node(SyntaxKind.FOR_STATEMENT);
        node.addChild(createNode(SyntaxKind.FOR_KEYWORD));
        node.addChild(createNode(SyntaxKind.OPEN_BRACKET_TOKEN));
        node.addChild(parseForStat());
        node.addChild(createNode(SyntaxKind.SEMICOLON_TOKEN));
        node.addChild(parseForExp());
        node.addChild(createNode(SyntaxKind.SEMICOLON_TOKEN));
        node.addChild(parseForStat());
        node.addChild(createNode(SyntaxKind.CLOSE_BRACKET_TOKEN));
        node.addChild(parseStatement());
        return node;
    }

    private Node parseLoopStatement() {
        Node node = new Node(SyntaxKind.LOOP_STATEMENT);
        node.addChild(createNode(SyntaxKind.LOOP_KEYWORD));
        node.addChildren(createNodeList(this::parseStatement, SyntaxKind.SEMICOLON_TOKEN));
        node.addChild(createNode(SyntaxKind.POOL_KEYWORD));
        return node;
    }

    private Node parseCaseStatement() {
        Node node = new Node(SyntaxKind.CASE_STATEMENT);
        node.addChild(createNode(SyntaxKind.CASE_KEYWORD));
        node.addChild(parseExpression());
        node.addChild(createNode(SyntaxKind.OF_KEYWORD));
        // Expanded CaseClauses
        node.addChild(parseCaseClause());
        node.addChild(createNode(SyntaxKind.SEMICOLON_TOKEN));
        // Case clauses must be followed by otherwise(nullable) or 'end'.
        // So must be followed bt 'otherwise' or 'end'.
        while (!isKind(SyntaxKind.OTHERWISE_KEYWORD)
                && !isKind(SyntaxKind.END_KEYWORD)) {
            node.addChild(parseCaseClause());
            node.addChild(createNode(SyntaxKind.SEMICOLON_TOKEN));
        }
        node.addChild(parseOtherwiseClause());
        node.addChild(createNode(SyntaxKind.END_KEYWORD));
        return node;
    }

    private Node parseReadStatement() {
        Node node = new Node(SyntaxKind.READ_STATEMENT);
        node.addChild(createNode(SyntaxKind.READ_KEYWORD));
        node.addChild(createNode(SyntaxKind.OPEN_BRACKET_TOKEN));
        node.addChildren(createNodeList(this::parseName, SyntaxKind.COMMA_TOKEN));
        node.addChild(createNode(SyntaxKind.CLOSE_BRACKET_TOKEN));
        return node;
    }

    private Node parseExitStatement() {
        Node node = new Node(SyntaxKind.EXIT_STATEMENT);
        node.addChild(createNode(SyntaxKind.EXIT_KEYWORD));
        return node;
    }

    private Node parseReturnStatement() {
        Node node = new Node(SyntaxKind.RETURN_STATEMENT);
        node.addChild(createNode(SyntaxKind.RETURN_KEYWORD));
        node.addChild(parseExpression());
        return node;
    }

    private Node parseOutExp() {
        if (isKind(SyntaxKind.STRING_LITERAL)) {
            Node node = new Node(SyntaxKind.STRING_OUT_EXP);
            node.addChild(parseStringNode());
            return node;
        }
        Node node = new Node(SyntaxKind.INTEGER_OUT_EXP);
        node.addChild(parseExpression());
        return node;
    }

    private Node parseStringNode() {
        return createNode(SyntaxKind.STRING_LITERAL);
    }

    private Node parseCaseClause() {
        Node node = new Node(SyntaxKind.CASE_CLAUSE);
        node.addChildren(createNodeList(this::parseCaseExpression, SyntaxKind.COMMA_TOKEN));
        node.addChild(createNode(SyntaxKind.COLON_TOKEN));
        node.addChild(parseStatement());
        return node;
    }

    private Node parseCaseExpression() {
        Node constValueNode = parseConstValue();
        if (isKind(SyntaxKind.DOUBLE_DOTS_TOKEN)) {
            Node node = new Node(SyntaxKind.DOUBLE_DOTS_CLAUSE);
            node.addChild(constValueNode);
            node.addChild(createNode(SyntaxKind.DOUBLE_DOTS_TOKEN));
            node.addChild(parseConstValue());
            return node;
        }
        return constValueNode;
    }

    private Node parseOtherwiseClause() {
        Node node = new Node(SyntaxKind.OTHERWISE_CLAUSE);
        if (isKind(SyntaxKind.OTHERWISE_KEYWORD)) {
            node.addChild(createNode(SyntaxKind.OTHERWISE_KEYWORD));
            node.addChild(parseStatement());
        }
        return node;
    }

    private Node parseAssignmentStatement() {
        SyntaxKind peekKind = peekKind(1);
        if (SyntaxKind.ASSIGNMENT_TOKEN.equals(peekKind)) {
            Node node = new Node(SyntaxKind.ASSIGNMENT_STATEMENT);
            node.addChild(parseName());
            node.addChild(createNode(SyntaxKind.ASSIGNMENT_TOKEN));
            node.addChild(parseExpression());
            return node;
        }
        Node node = new Node(SyntaxKind.SWAP_STATEMENT);
        node.addChild(parseName());
        node.addChild(createNode(SyntaxKind.ASSIGNMENT_TOKEN));
        node.addChild(parseName());
        return node;
    }

    private Node parseForStat() {
        // ForStat is always followed by ';'.
        if (isKind(SyntaxKind.SEMICOLON_TOKEN)) {
            return new Node(SyntaxKind.NULL_STATEMENT);
        }
        return parseAssignmentStatement();
    }

    private Node parseForExp() {
        // ForExp is always followed by ';'.
        if (isKind(SyntaxKind.SEMICOLON_TOKEN)) {
            return new Node(SyntaxKind.TRUE);
        }
        return parseExpression();
    }

    private Node parseExpression() {
    }

    private Node parseName() {
        return createNode(SyntaxKind.IDENTIFIER);
    }

    private void startContext(SyntaxKind kind) {
        contextStack.push(new Context(kind));
    }

    private void endContext() {
        Context context = contextStack.pop();
        Stack<Node> children = new Stack<>();
        for (int i = 0; i < context.children; i++) {
            children.push(nodeStack.pop());
        }
        Node node = new Node(context.kind);
        for (int i = 0; i < context.children; i++) {
            node.addChild(children.pop());
        }
        nodeStack.push(node);
    }

    private Collection<Node> createNodeList(Supplier<Node> parser, SyntaxKind seperator) {
        List<Node> nodeList = new ArrayList<>();
        nodeList.add(parser.get());
        while (isKind(seperator)) {
            nodeList.add(createNode(seperator));
            nodeList.add(parser.get());
        }
        return nodeList;
    }

    private Node createNode(SyntaxKind kind) {
        if (isKind(kind)) {
            SyntaxToken token = tokenReader.read();
            return new Node(token);
        }
        throw new IllegalStateException();
    }

    private boolean isKind(SyntaxKind kind) {
        return kind.equals(peekKind(0));
    }

    private SyntaxKind peekKind(int skip) {
        return tokenReader.peek(skip).getKind();
    }

    private static class Context {
        private final SyntaxKind kind;
        private int children;

        private Context(SyntaxKind kind) {
            this.kind = kind;
            this.children = 0;
        }
    }
}
