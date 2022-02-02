package parser;

import parser.nodes.ASTNode;
import lexer.AbstractLexer;

public abstract class AbstractParser {
    protected final TokenReader tokenReader;

    protected AbstractParser(AbstractLexer lexer) {
        this.tokenReader = new TokenReader(lexer);
    }

    public abstract ASTNode parse();
}
