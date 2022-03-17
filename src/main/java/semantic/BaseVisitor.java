package semantic;

import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import parser.nodes.Node;

public abstract class BaseVisitor {
    public SemanticContext visit(Node node, SemanticContext inheritedContext) {
        System.out.println(node);
        if (node instanceof IdentifierNode) {
            return visitIdentifier((IdentifierNode) node, inheritedContext);
        } else if (node instanceof ASTNode) {
            ASTNode astNode = (ASTNode) node;
            switch (astNode.getKind()) {
                case PROGRAM:
                    return visitProgram(astNode, inheritedContext);
                case CONSTS:
                    return visitConsts(astNode, inheritedContext);
                case CONST:
                    return visitConst(astNode, inheritedContext);
                case TYPES:
                    return visitTypes(astNode, inheritedContext);
                case TYPE:
                    return visitType(astNode, inheritedContext);
                case LIT:
                    return visitLit(astNode, inheritedContext);
                case SUBPROGS:
                    return visitSubprogs(astNode, inheritedContext);
                case FCN:
                    return visitFcn(astNode, inheritedContext);
                case PARAMS:
                    return visitParams(astNode, inheritedContext);
                case DCLNS:
                    return visitDclns(astNode, inheritedContext);
                case VAR:
                    return visitVar(astNode, inheritedContext);
                case BLOCK:
                    return visitBlock(astNode, inheritedContext);
                case OUTPUT_STATEMENT:
                    return visitOutputStatement(astNode, inheritedContext);
                case IF_STATEMENT:
                    return visitIfStatement(astNode, inheritedContext);
                case WHILE_STATEMENT:
                    return visitWhileStatement(astNode, inheritedContext);
                case REPEAT_STATEMENT:
                    return visitRepeatStatement(astNode, inheritedContext);
                case FOR_STATEMENT:
                    return visitForStatement(astNode, inheritedContext);
                case LOOP_STATEMENT:
                    return visitLoopStatement(astNode, inheritedContext);
                case CASE_STATEMENT:
                    return visitCaseStatement(astNode, inheritedContext);
                case READ_STATEMENT:
                    return visitReadStatement(astNode, inheritedContext);
                case EXIT_STATEMENT:
                    return visitExitStatement(astNode, inheritedContext);
                case RETURN_STATEMENT:
                    return visitReturnStatement(astNode, inheritedContext);
                case NULL_STATEMENT:
                    return visitNullStatement(astNode, inheritedContext);
                case INTEGER_OUT_EXP:
                    return visitIntegerOutExp(astNode, inheritedContext);
                case STRING_OUT_EXP:
                    return visitStringOutExp(astNode, inheritedContext);
                case CASE_CLAUSE:
                    return visitCaseClause(astNode, inheritedContext);
                case DOUBLE_DOTS_CLAUSE:
                    return visitDoubleDotsClause(astNode, inheritedContext);
                case OTHERWISE_CLAUSE:
                    return visitOtherwiseClause(astNode, inheritedContext);
                case ASSIGNMENT_STATEMENT:
                    return visitAssignmentStatement(astNode, inheritedContext);
                case SWAP_STATEMENT:
                    return visitSwapStatement(astNode, inheritedContext);
                case TRUE:
                    return visitTrue(astNode, inheritedContext);
                case LT_EQUAL_EXPRESSION:
                    return visitLtEqualExpression(astNode, inheritedContext);
                case LT_EXPRESSION:
                    return visitLtExpression(astNode, inheritedContext);
                case GT_EQUAL_EXPRESSION:
                    return visitGtEqualExpression(astNode, inheritedContext);
                case GT_EXPRESSION:
                    return visitGtExpression(astNode, inheritedContext);
                case EQUALS_EXPRESSION:
                    return visitEqualsExpression(astNode, inheritedContext);
                case NOT_EQUALS_EXPRESSION:
                    return visitNotEqualsExpression(astNode, inheritedContext);
                case ADD_EXPRESSION:
                    return visitAddExpression(astNode, inheritedContext);
                case SUBTRACT_EXPRESSION:
                    return visitSubtractExpression(astNode, inheritedContext);
                case OR_EXPRESSION:
                    return visitOrExpression(astNode, inheritedContext);
                case MULTIPLY_EXPRESSION:
                    return visitMultiplyExpression(astNode, inheritedContext);
                case DIVIDE_EXPRESSION:
                    return visitDivideExpression(astNode, inheritedContext);
                case AND_EXPRESSION:
                    return visitAndExpression(astNode, inheritedContext);
                case MOD_EXPRESSION:
                    return visitModExpression(astNode, inheritedContext);
                case NEGATIVE_EXPRESSION:
                    return visitNegativeExpression(astNode, inheritedContext);
                case NOT_EXPRESSION:
                    return visitNotExpression(astNode, inheritedContext);
                case EOF_EXPRESSION:
                    return visitEofExpression(astNode, inheritedContext);
                case CALL_EXPRESSION:
                    return visitCallExpression(astNode, inheritedContext);
                case SUCC_EXPRESSION:
                    return visitSuccExpression(astNode, inheritedContext);
                case PRED_EXPRESSION:
                    return visitPredExpression(astNode, inheritedContext);
                case CHR_EXPRESSION:
                    return visitChrExpression(astNode, inheritedContext);
                case ORD_EXPRESSION:
                    return visitOrdExpression(astNode, inheritedContext);
            }
        }
        throw new IllegalStateException("Unknown node type: " + node.toString());
    }

    protected abstract SemanticContext visitProgram(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitConsts(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitConst(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitTypes(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitType(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitLit(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitSubprogs(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitFcn(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitParams(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitDclns(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitVar(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitBlock(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitOutputStatement(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitIfStatement(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitWhileStatement(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitRepeatStatement(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitForStatement(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitLoopStatement(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitCaseStatement(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitReadStatement(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitExitStatement(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitReturnStatement(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitNullStatement(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitIntegerOutExp(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitStringOutExp(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitCaseClause(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitDoubleDotsClause(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitOtherwiseClause(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitAssignmentStatement(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitSwapStatement(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitTrue(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitLtEqualExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitLtExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitGtEqualExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitGtExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitEqualsExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitNotEqualsExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitAddExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitSubtractExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitOrExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitMultiplyExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitDivideExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitAndExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitModExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitNegativeExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitNotExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitEofExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitCallExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitSuccExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitPredExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitChrExpression(ASTNode astNode, SemanticContext inheritedContext);

    protected abstract SemanticContext visitOrdExpression(ASTNode astNode, SemanticContext inheritedContext);

    public abstract SemanticContext visitIdentifier(IdentifierNode identifierNode, SemanticContext inheritedContext);
}
