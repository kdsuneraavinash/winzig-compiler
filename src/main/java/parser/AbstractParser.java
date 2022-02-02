package parser;

import lexer.AbstractLexer;
import lexer.tokens.Token;
import lexer.tokens.TokenKind;
import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import parser.nodes.Node;
import parser.nodes.NodeKind;

import java.util.Stack;
import java.util.function.Supplier;

public abstract class AbstractParser {
    protected final Stack<Node> nodeStack;
    protected final TokenReader tokenReader;

    protected AbstractParser(AbstractLexer lexer) {
        this.nodeStack = new Stack<>();
        this.tokenReader = new TokenReader(lexer);
    }

    public abstract ASTNode parse();

    protected int parseIdentifier(TokenKind kind) {
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == kind) {
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

    protected int parseToken(TokenKind kind) {
        TokenKind tokenKind = peekNextKind();
        if (tokenKind == kind) {
            tokenReader.read();
            return 0;
        } else {
            TokenKind foundKind = peekNextKind();
            String message = String.format("Expected %s[%s] but found %s[%s]",
                    kind, kind.getValue(), foundKind, foundKind.getValue());
            throw new IllegalStateException(message);
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
}
