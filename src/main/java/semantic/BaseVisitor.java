package semantic;

import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import parser.nodes.Node;

public abstract class BaseVisitor {
    protected Node currentNode;

    protected BaseVisitor() {
        this.currentNode = null;
    }

    public void visit(Node node) {
        this.currentNode = node;
        if (node instanceof IdentifierNode) {
            visitIdentifier((IdentifierNode) node);
            return;
        } else if (node instanceof ASTNode) {
            ASTNode astNode = (ASTNode) node;
            switch (astNode.getKind()) {
                case PROGRAM:
                    visitProgram(astNode);
                    return;
                case CONSTS:
                    visitConsts(astNode);
                    return;
                case CONST:
                    visitConst(astNode);
                    return;
                case TYPES:
                    visitTypes(astNode);
                    return;
                case TYPE:
                    visitType(astNode);
                    return;
                case LIT:
                    visitLit(astNode);
                    return;
                case SUBPROGS:
                    visitSubprogs(astNode);
                    return;
                case FCN:
                    visitFcn(astNode);
                    return;
                case PARAMS:
                    visitParams(astNode);
                    return;
                case DCLNS:
                    visitDclns(astNode);
                    return;
                case VAR:
                    visitVar(astNode);
                    return;
                case BLOCK:
                    visitBlock(astNode);
                    return;
                case OUTPUT_STATEMENT:
                    visitOutputStatement(astNode);
                    return;
                case IF_STATEMENT:
                    visitIfStatement(astNode);
                    return;
                case WHILE_STATEMENT:
                    visitWhileStatement(astNode);
                    return;
                case REPEAT_STATEMENT:
                    visitRepeatStatement(astNode);
                    return;
                case FOR_STATEMENT:
                    visitForStatement(astNode);
                    return;
                case LOOP_STATEMENT:
                    visitLoopStatement(astNode);
                    return;
                case CASE_STATEMENT:
                    visitCaseStatement(astNode);
                    return;
                case READ_STATEMENT:
                    visitReadStatement(astNode);
                    return;
                case EXIT_STATEMENT:
                    visitExitStatement(astNode);
                    return;
                case RETURN_STATEMENT:
                    visitReturnStatement(astNode);
                    return;
                case NULL_STATEMENT:
                    visitNullStatement(astNode);
                    return;
                case INTEGER_OUT_EXP:
                    visitIntegerOutExp(astNode);
                    return;
                case STRING_OUT_EXP:
                    visitStringOutExp(astNode);
                    return;
                case CASE_CLAUSE:
                    visitCaseClause(astNode);
                    return;
                case DOUBLE_DOTS_CLAUSE:
                    visitDoubleDotsClause(astNode);
                    return;
                case OTHERWISE_CLAUSE:
                    visitOtherwiseClause(astNode);
                    return;
                case ASSIGNMENT_STATEMENT:
                    visitAssignmentStatement(astNode);
                    return;
                case SWAP_STATEMENT:
                    visitSwapStatement(astNode);
                    return;
                case TRUE:
                    visitTrue(astNode);
                    return;
                case LT_EQUAL_EXPRESSION:
                    visitLtEqualExpression(astNode);
                    return;
                case LT_EXPRESSION:
                    visitLtExpression(astNode);
                    return;
                case GT_EQUAL_EXPRESSION:
                    visitGtEqualExpression(astNode);
                    return;
                case GT_EXPRESSION:
                    visitGtExpression(astNode);
                    return;
                case EQUALS_EXPRESSION:
                    visitEqualsExpression(astNode);
                    return;
                case NOT_EQUALS_EXPRESSION:
                    visitNotEqualsExpression(astNode);
                    return;
                case ADD_EXPRESSION:
                    visitAddExpression(astNode);
                    return;
                case SUBTRACT_EXPRESSION:
                    visitSubtractExpression(astNode);
                    return;
                case OR_EXPRESSION:
                    visitOrExpression(astNode);
                    return;
                case MULTIPLY_EXPRESSION:
                    visitMultiplyExpression(astNode);
                    return;
                case DIVIDE_EXPRESSION:
                    visitDivideExpression(astNode);
                    return;
                case AND_EXPRESSION:
                    visitAndExpression(astNode);
                    return;
                case MOD_EXPRESSION:
                    visitModExpression(astNode);
                    return;
                case NEGATIVE_EXPRESSION:
                    visitNegativeExpression(astNode);
                    return;
                case NOT_EXPRESSION:
                    visitNotExpression(astNode);
                    return;
                case EOF_EXPRESSION:
                    visitEofExpression(astNode);
                    return;
                case CALL_EXPRESSION:
                    visitCallExpression(astNode);
                    return;
                case SUCC_EXPRESSION:
                    visitSuccExpression(astNode);
                    return;
                case PRED_EXPRESSION:
                    visitPredExpression(astNode);
                    return;
                case CHR_EXPRESSION:
                    visitChrExpression(astNode);
                    return;
                case ORD_EXPRESSION:
                    visitOrdExpression(astNode);
                    return;
                default:
                    throw new IllegalArgumentException("Unknown AST node type: " + astNode.getKind());
            }
        }
        throw new IllegalStateException("Unknown node type: " + node.toString());
    }

    protected abstract void visitProgram(ASTNode astNode);

    protected abstract void visitConsts(ASTNode astNode);

    protected abstract void visitConst(ASTNode astNode);

    protected abstract void visitTypes(ASTNode astNode);

    protected abstract void visitType(ASTNode astNode);

    protected abstract void visitLit(ASTNode astNode);

    protected abstract void visitSubprogs(ASTNode astNode);

    protected abstract void visitFcn(ASTNode astNode);

    protected abstract void visitParams(ASTNode astNode);

    protected abstract void visitDclns(ASTNode astNode);

    protected abstract void visitVar(ASTNode astNode);

    protected abstract void visitBlock(ASTNode astNode);

    protected abstract void visitOutputStatement(ASTNode astNode);

    protected abstract void visitIfStatement(ASTNode astNode);

    protected abstract void visitWhileStatement(ASTNode astNode);

    protected abstract void visitRepeatStatement(ASTNode astNode);

    protected abstract void visitForStatement(ASTNode astNode);

    protected abstract void visitLoopStatement(ASTNode astNode);

    protected abstract void visitCaseStatement(ASTNode astNode);

    protected abstract void visitReadStatement(ASTNode astNode);

    protected abstract void visitExitStatement(ASTNode astNode);

    protected abstract void visitReturnStatement(ASTNode astNode);

    protected abstract void visitNullStatement(ASTNode astNode);

    protected abstract void visitIntegerOutExp(ASTNode astNode);

    protected abstract void visitStringOutExp(ASTNode astNode);

    protected abstract void visitCaseClause(ASTNode astNode);

    protected abstract void visitDoubleDotsClause(ASTNode astNode);

    protected abstract void visitOtherwiseClause(ASTNode astNode);

    protected abstract void visitAssignmentStatement(ASTNode astNode);

    protected abstract void visitSwapStatement(ASTNode astNode);

    protected abstract void visitTrue(ASTNode astNode);

    protected abstract void visitLtEqualExpression(ASTNode astNode);

    protected abstract void visitLtExpression(ASTNode astNode);

    protected abstract void visitGtEqualExpression(ASTNode astNode);

    protected abstract void visitGtExpression(ASTNode astNode);

    protected abstract void visitEqualsExpression(ASTNode astNode);

    protected abstract void visitNotEqualsExpression(ASTNode astNode);

    protected abstract void visitAddExpression(ASTNode astNode);

    protected abstract void visitSubtractExpression(ASTNode astNode);

    protected abstract void visitOrExpression(ASTNode astNode);

    protected abstract void visitMultiplyExpression(ASTNode astNode);

    protected abstract void visitDivideExpression(ASTNode astNode);

    protected abstract void visitAndExpression(ASTNode astNode);

    protected abstract void visitModExpression(ASTNode astNode);

    protected abstract void visitNegativeExpression(ASTNode astNode);

    protected abstract void visitNotExpression(ASTNode astNode);

    protected abstract void visitEofExpression(ASTNode astNode);

    protected abstract void visitCallExpression(ASTNode astNode);

    protected abstract void visitSuccExpression(ASTNode astNode);

    protected abstract void visitPredExpression(ASTNode astNode);

    protected abstract void visitChrExpression(ASTNode astNode);

    protected abstract void visitOrdExpression(ASTNode astNode);

    public abstract void visitIdentifier(IdentifierNode identifierNode);
}
