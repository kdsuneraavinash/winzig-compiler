package parser;


import lexer.AbstractLexer;
import lexer.tokens.Token;

public class TokenReader {
    private final AbstractLexer lexer;
    private final CircularBuffer<Token> fetchedTokens;

    public TokenReader(AbstractLexer lexer) {
        this.lexer = lexer;
        this.fetchedTokens = new CircularBuffer<>(10);
    }

    public Token peek(int k) {
        while (fetchedTokens.getSize() <= k) {
            Token nextToken = lexer.read();
            fetchedTokens.add(nextToken);
        }
        return fetchedTokens.peek(k);
    }

    public Token read() {
        if (fetchedTokens.getSize() > 0) {
            return fetchedTokens.remove();
        }
        return lexer.read();
    }
}
