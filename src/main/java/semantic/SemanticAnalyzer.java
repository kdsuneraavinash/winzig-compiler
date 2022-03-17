package semantic;

import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import semantic.attrs.CodeSegment;
import semantic.attrs.ErrorSegment;
import semantic.attrs.Instruction;
import semantic.attrs.SemanticType;

public class SemanticAnalyzer extends BaseVisitor {
    private final SymbolTable symbolTable;

    public SemanticAnalyzer() {
        symbolTable = new SymbolTable();
    }

    public void analyze(ASTNode astNode) {
        visit(astNode, SemanticContext.empty());
    }

    @Override
    protected SemanticContext visitProgram(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Name
        context = visit(astNode.getChild(1), context); // Consts
        context = visit(astNode.getChild(2), context); // Types
        context = visit(astNode.getChild(3), context); // Dclns
        context = visit(astNode.getChild(4), context); // SubProgs
        context = visit(astNode.getChild(5), context); // Body
        context = visit(astNode.getChild(6), context); // Name
        return context;
    }

    @Override
    protected SemanticContext visitConsts(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        for (int i = 0; i < astNode.getSize(); i++) { // list
            context = visit(astNode.getChild(i), context); // Const
        }
        return context;
    }

    @Override
    protected SemanticContext visitConst(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Name
        context = visit(astNode.getChild(1), context); // ConstValue
        return context;
    }

    @Override
    protected SemanticContext visitTypes(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        for (int i = 0; i < astNode.getSize(); i++) { // +
            context = visit(astNode.getChild(i), context); // Type
        }
        return context;
    }

    @Override
    protected SemanticContext visitType(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Name
        context = visit(astNode.getChild(1), context); // ListList
        return context;
    }

    @Override
    protected SemanticContext visitLit(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        for (int i = 0; i < astNode.getSize(); i++) { // list
            context = visit(astNode.getChild(i), context); // Name
        }
        return context;
    }

    @Override
    protected SemanticContext visitSubprogs(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        for (int i = 0; i < astNode.getSize(); i++) { // *
            context = visit(astNode.getChild(i), context); // Fcn
        }
        return context;
    }

    @Override
    protected SemanticContext visitFcn(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Name
        context = visit(astNode.getChild(1), context); // Params
        context = visit(astNode.getChild(2), context); // Name
        context = visit(astNode.getChild(3), context); // Consts
        context = visit(astNode.getChild(4), context); // Types
        context = visit(astNode.getChild(5), context); // Dclns
        context = visit(astNode.getChild(6), context); // Body
        context = visit(astNode.getChild(7), context); // Name
        return context;
    }

    @Override
    protected SemanticContext visitParams(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        for (int i = 0; i < astNode.getSize(); i++) { // list
            context = visit(astNode.getChild(i), context); // Dcln
        }
        return context;
    }

    @Override
    protected SemanticContext visitDclns(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        for (int i = 0; i < astNode.getSize(); i++) { // +
            context = visit(astNode.getChild(i), context); // Dcln
        }
        return context;
    }

    @Override
    protected SemanticContext visitVar(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        for (int i = 0; i < astNode.getSize() - 1; i++) { // list
            context = visit(astNode.getChild(i), context); // Name
        }
        context = visit(astNode.getChild(astNode.getSize() - 1), context); // Name
        return context;
    }

    @Override
    protected SemanticContext visitBlock(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        for (int i = 0; i < astNode.getSize(); i++) { // list
            context = visit(astNode.getChild(i), context); // Statement
        }
        return context;
    }

    @Override
    protected SemanticContext visitOutputStatement(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        for (int i = 0; i < astNode.getSize(); i++) { // list
            context = visit(astNode.getChild(i), context); // OutExp
        }
        return context;
    }

    @Override
    protected SemanticContext visitIfStatement(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Expression
        context = visit(astNode.getChild(1), context); // Statement
        if (astNode.getSize() == 3) { // ?
            context = visit(astNode.getChild(2), context); // Statement
        }
        return context;
    }

    @Override
    protected SemanticContext visitWhileStatement(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Expression
        context = visit(astNode.getChild(1), context); // Statement
        return context;
    }

    @Override
    protected SemanticContext visitRepeatStatement(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        for (int i = 0; i < astNode.getSize() - 1; i++) { // list
            context = visit(astNode.getChild(i), context); // Statement
        }
        context = visit(astNode.getChild(astNode.getSize() - 1), context); // Expression
        return context;
    }

    @Override
    protected SemanticContext visitForStatement(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // ForStat
        context = visit(astNode.getChild(1), context); // ForExp
        context = visit(astNode.getChild(2), context); // ForStat
        context = visit(astNode.getChild(3), context); // Statement
        return context;
    }

    @Override
    protected SemanticContext visitLoopStatement(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        for (int i = 0; i < astNode.getSize(); i++) { // list
            context = visit(astNode.getChild(i), context); // Statement
        }
        return context;
    }

    @Override
    protected SemanticContext visitCaseStatement(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Expression
        for (int i = 1; i < astNode.getSize() - 1; i++) { // Caseclauses +
            context = visit(astNode.getChild(i), context); // Caseclause
        }
        context = visit(astNode.getChild(astNode.getSize() - 1), context); // OtherwiseClause
        return context;
    }

    @Override
    protected SemanticContext visitReadStatement(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        for (int i = 0; i < astNode.getSize(); i++) { // list
            context = visit(astNode.getChild(i), context); // Name
        }
        return context;
    }

    @Override
    protected SemanticContext visitExitStatement(ASTNode astNode, SemanticContext inheritedContext) {
        return SemanticContext.from(inheritedContext);
    }

    @Override
    protected SemanticContext visitReturnStatement(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Expression
        return context;
    }

    @Override
    protected SemanticContext visitNullStatement(ASTNode astNode, SemanticContext inheritedContext) {
        return SemanticContext.from(inheritedContext);
    }

    @Override
    protected SemanticContext visitIntegerOutExp(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Expression
        return context;
    }

    @Override
    protected SemanticContext visitStringOutExp(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // StringNode
        return context;
    }

    @Override
    protected SemanticContext visitCaseClause(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        for (int i = 0; i < astNode.getSize() - 1; i++) { // list
            context = visit(astNode.getChild(i), context); // CaseExpression
        }
        context = visit(astNode.getChild(astNode.getSize() - 1), context); // Statement
        return context;
    }

    @Override
    protected SemanticContext visitDoubleDotsClause(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // ConstValue
        context = visit(astNode.getChild(1), context); // ConstValue
        return context;
    }

    @Override
    protected SemanticContext visitOtherwiseClause(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Statement
        return context;
    }

    @Override
    protected SemanticContext visitAssignmentStatement(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Name
        context = visit(astNode.getChild(1), context); // Expression
        return context;
    }

    @Override
    protected SemanticContext visitSwapStatement(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Name
        context = visit(astNode.getChild(1), context); // Name
        return context;
    }

    @Override
    protected SemanticContext visitTrue(ASTNode astNode, SemanticContext inheritedContext) {
        return SemanticContext.from(inheritedContext);
    }

    @Override
    protected SemanticContext visitLtEqualExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Term
        context = visit(astNode.getChild(1), context); // Term
        return context;
    }

    @Override
    protected SemanticContext visitLtExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Term
        context = visit(astNode.getChild(1), context); // Term
        return context;
    }

    @Override
    protected SemanticContext visitGtEqualExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Term
        context = visit(astNode.getChild(1), context); // Term
        return context;
    }

    @Override
    protected SemanticContext visitGtExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Term
        context = visit(astNode.getChild(1), context); // Term
        return context;
    }

    @Override
    protected SemanticContext visitEqualsExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Term
        context = visit(astNode.getChild(1), context); // Term
        return context;
    }

    @Override
    protected SemanticContext visitNotEqualsExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Term
        context = visit(astNode.getChild(1), context); // Term
        return context;
    }

    @Override
    protected SemanticContext visitAddExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Term
        context = visit(astNode.getChild(1), context); // Factor
        return context;
    }

    @Override
    protected SemanticContext visitSubtractExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Term
        context = visit(astNode.getChild(1), context); // Factor
        return context;
    }

    @Override
    protected SemanticContext visitOrExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Term
        context = visit(astNode.getChild(1), context); // Factor
        return context;
    }

    @Override
    protected SemanticContext visitMultiplyExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Factor
        context = visit(astNode.getChild(1), context); // Primary
        return context;
    }

    @Override
    protected SemanticContext visitDivideExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Factor
        context = visit(astNode.getChild(1), context); // Primary
        return context;
    }

    @Override
    protected SemanticContext visitAndExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Factor
        context = visit(astNode.getChild(1), context); // Primary
        return context;
    }

    @Override
    protected SemanticContext visitModExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Factor
        context = visit(astNode.getChild(1), context); // Primary
        return context;
    }

    @Override
    protected SemanticContext visitNegativeExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Primary
        return context;
    }

    @Override
    protected SemanticContext visitNotExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Primary
        return context;
    }

    @Override
    protected SemanticContext visitEofExpression(ASTNode astNode, SemanticContext inheritedContext) {
        return SemanticContext.from(inheritedContext);
    }

    @Override
    protected SemanticContext visitCallExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Name
        for (int i = 1; i < astNode.getSize(); i++) { // list
            context = visit(astNode.getChild(i), context); // Expression
        }
        return context;
    }

    @Override
    protected SemanticContext visitSuccExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Expression
        return context;
    }

    @Override
    protected SemanticContext visitPredExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Expression
        return context;
    }

    @Override
    protected SemanticContext visitChrExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Expression
        return context;
    }

    @Override
    protected SemanticContext visitOrdExpression(ASTNode astNode, SemanticContext inheritedContext) {
        SemanticContext context = SemanticContext.from(inheritedContext);
        context = visit(astNode.getChild(0), context); // Expression
        return context;
    }

    @Override
    public SemanticContext visitIdentifier(IdentifierNode identifierNode, SemanticContext inheritedContext) {
        String identifier = identifierNode.getValue();
        CodeSegment code = CodeSegment.gen(inheritedContext.code, Instruction.LOAD, symbolTable.lookup(identifier));
        int next = inheritedContext.next + 1;
        int top = inheritedContext.top + 1;
        ErrorSegment error = symbolTable.lookup(identifier) == 0
                ? ErrorSegment.gen(inheritedContext.error, "Identifier un-initialized")
                : inheritedContext.error;

        return new SemanticContext(code, error, next, top, SemanticType.INTEGER_TYPE);
    }
}
