package semantic;

import diagnostics.DiagnosticCollector;
import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import parser.nodes.Node;

public abstract class BaseVisitor extends DiagnosticCollector {
    protected Node currentNode;

    protected BaseVisitor() {
        this.currentNode = null;
    }

    public void visit(Node node) {
        Node currentNode = this.currentNode;
        this.currentNode = node;
        if (node instanceof IdentifierNode) {
            visitIdentifier((IdentifierNode) node);
        } else if (node instanceof ASTNode) {
            ASTNode astNode = (ASTNode) node;
            switch (astNode.getKind()) {
                case PROGRAM:
                    visitProgram(astNode);
                    break;
                case CONSTS:
                    visitConsts(astNode);
                    break;
                case CONST:
                    visitConst(astNode);
                    break;
                case TYPES:
                    visitTypes(astNode);
                    break;
                case TYPE:
                    visitType(astNode);
                    break;
                case LIT:
                    visitLit(astNode);
                    break;
                case SUBPROGS:
                    visitSubprogs(astNode);
                    break;
                case FCN:
                    visitFcn(astNode);
                    break;
                case PARAMS:
                    visitParams(astNode);
                    break;
                case DCLNS:
                    visitDclns(astNode);
                    break;
                case VAR:
                    visitVar(astNode);
                    break;
                case BLOCK:
                    visitBlock(astNode);
                    break;
                case OUTPUT_STATEMENT:
                    visitOutputStatement(astNode);
                    break;
                case IF_STATEMENT:
                    visitIfStatement(astNode);
                    break;
                case WHILE_STATEMENT:
                    visitWhileStatement(astNode);
                    break;
                case REPEAT_STATEMENT:
                    visitRepeatStatement(astNode);
                    break;
                case FOR_STATEMENT:
                    visitForStatement(astNode);
                    break;
                case LOOP_STATEMENT:
                    visitLoopStatement(astNode);
                    break;
                case CASE_STATEMENT:
                    visitCaseStatement(astNode);
                    break;
                case READ_STATEMENT:
                    visitReadStatement(astNode);
                    break;
                case EXIT_STATEMENT:
                    visitExitStatement(astNode);
                    break;
                case RETURN_STATEMENT:
                    visitReturnStatement(astNode);
                    break;
                case NULL_STATEMENT:
                    visitNullStatement(astNode);
                    break;
                case INTEGER_OUT_EXP:
                    visitIntegerOutExp(astNode);
                    break;
                case STRING_OUT_EXP:
                    visitStringOutExp(astNode);
                    break;
                case CASE_CLAUSE:
                    visitCaseClause(astNode);
                    break;
                case DOUBLE_DOTS_CLAUSE:
                    visitDoubleDotsClause(astNode);
                    break;
                case OTHERWISE_CLAUSE:
                    visitOtherwiseClause(astNode);
                    break;
                case ASSIGNMENT_STATEMENT:
                    visitAssignmentStatement(astNode);
                    break;
                case SWAP_STATEMENT:
                    visitSwapStatement(astNode);
                    break;
                case TRUE:
                    visitTrue(astNode);
                    break;
                case LT_EQUAL_EXPRESSION:
                    visitLtEqualExpression(astNode);
                    break;
                case LT_EXPRESSION:
                    visitLtExpression(astNode);
                    break;
                case GT_EQUAL_EXPRESSION:
                    visitGtEqualExpression(astNode);
                    break;
                case GT_EXPRESSION:
                    visitGtExpression(astNode);
                    break;
                case EQUALS_EXPRESSION:
                    visitEqualsExpression(astNode);
                    break;
                case NOT_EQUALS_EXPRESSION:
                    visitNotEqualsExpression(astNode);
                    break;
                case ADD_EXPRESSION:
                    visitAddExpression(astNode);
                    break;
                case SUBTRACT_EXPRESSION:
                    visitSubtractExpression(astNode);
                    break;
                case OR_EXPRESSION:
                    visitOrExpression(astNode);
                    break;
                case MULTIPLY_EXPRESSION:
                    visitMultiplyExpression(astNode);
                    break;
                case DIVIDE_EXPRESSION:
                    visitDivideExpression(astNode);
                    break;
                case AND_EXPRESSION:
                    visitAndExpression(astNode);
                    break;
                case MOD_EXPRESSION:
                    visitModExpression(astNode);
                    break;
                case NEGATIVE_EXPRESSION:
                    visitNegativeExpression(astNode);
                    break;
                case NOT_EXPRESSION:
                    visitNotExpression(astNode);
                    break;
                case EOF_EXPRESSION:
                    visitEofExpression(astNode);
                    break;
                case CALL_EXPRESSION:
                    visitCallExpression(astNode);
                    break;
                case SUCC_EXPRESSION:
                    visitSuccExpression(astNode);
                    break;
                case PRED_EXPRESSION:
                    visitPredExpression(astNode);
                    break;
                case CHR_EXPRESSION:
                    visitChrExpression(astNode);
                    break;
                case ORD_EXPRESSION:
                    visitOrdExpression(astNode);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown AST node type: " + astNode.getKind());
            }
        } else {
            throw new IllegalStateException("Unknown node type: " + node.toString());
        }
        this.currentNode = currentNode;
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
