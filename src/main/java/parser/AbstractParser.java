package parser;

import diagnostics.DiagnosticCollector;
import lexer.AbstractLexer;
import lexer.tokens.Token;
import lexer.tokens.TokenKind;
import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import parser.nodes.Node;
import parser.nodes.NodeKind;

import java.util.Stack;
import java.util.function.Supplier;

public abstract class AbstractParser extends DiagnosticCollector {
    protected final Stack<Node> nodeStack;
    protected final TokenReader tokenReader;

    protected AbstractParser(AbstractLexer lexer) {
        this.nodeStack = new Stack<>();
        this.tokenReader = new TokenReader(lexer);
    }

    public abstract ASTNode parse();

    protected int parseIdentifier(TokenKind kind) {
        Token nextToken = readToken(kind);
        nodeStack.push(new IdentifierNode(nextToken));
        return 1;
    }

    protected int parseToken(TokenKind kind) {
        readToken(kind);
        return 0;
    }

    private Token readToken(TokenKind kind) {
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == kind) {
            return tokenReader.read();
        } else {
            Token foundToken = tokenReader.peek(0);
            addError(foundToken, "Expected %s[%s] but found %s",
                    kind, kind.getValue(), foundToken);
            // Return the next token anyway.
            return foundToken;
        }
    }

    protected int parseList(Supplier<Integer> parser, TokenKind seperator) {
        int itemCount = 0;
        itemCount += parser.get();
        TokenKind tokenKind = peekNextKind();
        while (tokenKind == seperator) {
            itemCount += parseToken(seperator);
            itemCount += parser.get();
            tokenKind = peekNextKind();
        }
        return itemCount;
    }

    protected int buildTree(NodeKind kind, int childrenCount) {
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

    protected TokenKind peekNextKind() {
        return peekNextKind(0);
    }

    protected TokenKind peekNextKind(int skip) {
        return tokenReader.peek(skip).getKind();
    }

    @Override
    public String highlightedSegment(int startOffset, int endOffset) {
        return tokenReader.highlightedSegment(startOffset, endOffset);
    }
}
