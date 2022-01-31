package parser;


import common.CircularBuffer;
import lexer.AbstractLexer;
import lexer.tokens.SyntaxToken;

public class TokenReader {
    private final AbstractLexer lexer;
    private final CircularBuffer<SyntaxToken> fetchedTokens;

    public TokenReader(AbstractLexer lexer) {
        this.lexer = lexer;
        this.fetchedTokens = new CircularBuffer<>(10);
    }

    public SyntaxToken peek() {
        if (fetchedTokens.getSize() > 0) {
            return fetchedTokens.peek();
        }
        SyntaxToken nextToken = lexer.read();
        fetchedTokens.add(nextToken);
        return nextToken;
    }

    public SyntaxToken peek(int k) {
        while (fetchedTokens.getSize() <= k) {
            SyntaxToken nextToken = lexer.read();
            fetchedTokens.add(nextToken);
        }
        return fetchedTokens.peek(k);
    }

    public SyntaxToken read() {
        if (fetchedTokens.getSize() > 0) {
            return fetchedTokens.remove();
        }
        return lexer.read();
    }
}
