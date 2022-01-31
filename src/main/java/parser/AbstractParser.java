package parser;

import lexer.AbstractLexer;

public abstract class AbstractParser {
    protected final TokenReader tokenReader;

    protected AbstractParser(AbstractLexer lexer) {
        this.tokenReader = new TokenReader(lexer);
    }

    public abstract Node parse();
}
