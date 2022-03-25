package semantic;

import lexer.tokens.TokenKind;
import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import semantic.attrs.Instruction;
import semantic.attrs.InstructionMnemonic;
import semantic.attrs.SemanticType;
import semantic.attrs.Symbol;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer extends BaseVisitor {
    private Scope scope;
    private List<String> error;
    private List<Instruction> code;

    public void analyze(ASTNode astNode) {
        this.scope = new Scope();
        this.error = new ArrayList<>();
        this.code = new ArrayList<>();
        visit(astNode);
        System.out.println("----------------------------------------------------");
        System.out.println(String.join("\n", error));
        System.out.println("----------------------------------------------------");
        for (Instruction line : this.code) {
            System.out.println(line);
        }
        System.out.println("----------------------------------------------------");
        System.out.println(this.scope);
    }

    @Override
    protected void visitProgram(ASTNode astNode) {
        visit(astNode.getChild(0)); // Name
        visit(astNode.getChild(1)); // Consts
        visit(astNode.getChild(2)); // Types
        visit(astNode.getChild(3)); // Dclns
        visit(astNode.getChild(4)); // SubProgs
        visit(astNode.getChild(5)); // Body
        visit(astNode.getChild(6)); // Name
    }

    @Override
    protected void visitConsts(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Const
        }
    }

    @Override
    protected void visitConst(ASTNode astNode) {
        IdentifierNode identifierNode = (IdentifierNode) astNode.getChild(0); // Name
        IdentifierNode valueNode = (IdentifierNode) astNode.getChild(1); // ConstValue
        String identifier = identifierNode.getIdentifierValue();
        String value = valueNode.getIdentifierValue();
        TokenKind tokenKind = valueNode.getKind();

        // Constants cannot be defined twice, even if they have in nested scopes.
        if (scope.isDefined(identifier)) {
            error.add("Variable already defined: " + identifier);
            return;
        }

        // Supports char/int literals only. Each is stored as integer regardless of type.
        int registerValue;
        if (tokenKind == TokenKind.CHAR_LITERAL) {
            registerValue = value.codePointAt(1);
        } else if (tokenKind == TokenKind.INTEGER_LITERAL) {
            registerValue = Integer.parseInt(value);
        } else {
            error.add("Type mismatched. expected int/char, found: " + tokenKind);
            return;
        }

        // Define the constant at the top and mark the symbol as constant.
        scope.type = SemanticType.CONSTANT;
        code.add(new Instruction(InstructionMnemonic.LIT, registerValue));
        scope.enter(new Symbol(identifier, ++scope.top, scope.type));
        scope.next++;
    }

    @Override
    protected void visitTypes(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // +
            visit(astNode.getChild(i)); // Type
        }
    }

    @Override
    protected void visitType(ASTNode astNode) {
        // TODO: ???
        visit(astNode.getChild(0)); // Name
        visit(astNode.getChild(1)); // ListList
    }

    @Override
    protected void visitLit(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Name
        }
    }

    @Override
    protected void visitSubprogs(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // *
            visit(astNode.getChild(i)); // Fcn
        }
    }

    @Override
    protected void visitFcn(ASTNode astNode) {
        visit(astNode.getChild(0)); // Name
        visit(astNode.getChild(1)); // Params
        visit(astNode.getChild(2)); // Name
        visit(astNode.getChild(3)); // Consts
        visit(astNode.getChild(4)); // Types
        visit(astNode.getChild(5)); // Dclns
        visit(astNode.getChild(6)); // Body
        visit(astNode.getChild(7)); // Name
    }

    @Override
    protected void visitParams(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Dcln
        }
    }

    @Override
    protected void visitDclns(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // +
            visit(astNode.getChild(i)); // Dcln
        }
    }

    @Override
    protected void visitVar(ASTNode astNode) {
        List<IdentifierNode> identifierNodes = new ArrayList<>();
        for (int i = 0; i < astNode.getSize() - 1; i++) { // list
            identifierNodes.add((IdentifierNode) astNode.getChild(i)); // Name
        }
        IdentifierNode typeNode = (IdentifierNode) astNode.getChild(astNode.getSize() - 1); // Name (Type)
        String type = typeNode.getIdentifierValue();

        // The data type should be defined first.
        if (!scope.isDefined(type)) {
            error.add("Undefined type found: " + type);
            return;
        }
        // The data should be of 'TYPE' type.
        Symbol typeSymbol = scope.lookup(type);
        if (!SemanticType.TYPE.equals(typeSymbol.getType())) {
            error.add("Expected " + type + " to be a type, found: " + typeSymbol.getType());
            return;
        }

        // Define the variables at the top and mark the symbol as variable.
        scope.type = SemanticType.VARIABLE;
        for (IdentifierNode identifierNode : identifierNodes) {
            String identifier = identifierNode.getIdentifierValue();
            if (scope.isDefined(identifier)) {
                // TODO: Does not handle multiple variables with the same name even even if on a top scope.
                error.add("Variable " + identifier + " already defined.");
                continue;
            }
            code.add(new Instruction(InstructionMnemonic.LIT, "0"));
            scope.enter(new Symbol(identifier, ++scope.top, scope.type));
            scope.next++;
        }
    }

    @Override
    protected void visitBlock(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Statement
        }
    }

    @Override
    protected void visitOutputStatement(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // OutExp
        }
    }

    @Override
    protected void visitIfStatement(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
        visit(astNode.getChild(1)); // Statement
        if (astNode.getSize() == 3) { // ?
            visit(astNode.getChild(2)); // Statement
        }
    }

    @Override
    protected void visitWhileStatement(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
        visit(astNode.getChild(1)); // Statement
    }

    @Override
    protected void visitRepeatStatement(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize() - 1; i++) { // list
            visit(astNode.getChild(i)); // Statement
        }
        visit(astNode.getChild(astNode.getSize() - 1)); // Expression
    }

    @Override
    protected void visitForStatement(ASTNode astNode) {
        visit(astNode.getChild(0)); // ForStat
        visit(astNode.getChild(1)); // ForExp
        visit(astNode.getChild(2)); // ForStat
        visit(astNode.getChild(3)); // Statement
    }

    @Override
    protected void visitLoopStatement(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Statement
        }
    }

    @Override
    protected void visitCaseStatement(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
        for (int i = 1; i < astNode.getSize() - 1; i++) { // Caseclauses +
            visit(astNode.getChild(i)); // Caseclause
        }
        visit(astNode.getChild(astNode.getSize() - 1)); // OtherwiseClause
    }

    @Override
    protected void visitReadStatement(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Name
        }
    }

    @Override
    protected void visitExitStatement(ASTNode astNode) {
    }

    @Override
    protected void visitReturnStatement(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
    }

    @Override
    protected void visitNullStatement(ASTNode astNode) {
    }

    @Override
    protected void visitIntegerOutExp(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
    }

    @Override
    protected void visitStringOutExp(ASTNode astNode) {
        visit(astNode.getChild(0)); // StringNode
    }

    @Override
    protected void visitCaseClause(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize() - 1; i++) { // list
            visit(astNode.getChild(i)); // CaseExpression
        }
        visit(astNode.getChild(astNode.getSize() - 1)); // Statement
    }

    @Override
    protected void visitDoubleDotsClause(ASTNode astNode) {
        visit(astNode.getChild(0)); // ConstValue
        visit(astNode.getChild(1)); // ConstValue
    }

    @Override
    protected void visitOtherwiseClause(ASTNode astNode) {
        visit(astNode.getChild(0)); // Statement
    }

    @Override
    protected void visitAssignmentStatement(ASTNode astNode) {
        visit(astNode.getChild(0)); // Name
        visit(astNode.getChild(1)); // Expression
    }

    @Override
    protected void visitSwapStatement(ASTNode astNode) {
        visit(astNode.getChild(0)); // Name
        visit(astNode.getChild(1)); // Name
    }

    @Override
    protected void visitTrue(ASTNode astNode) {
    }

    @Override
    protected void visitLtEqualExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
    }

    @Override
    protected void visitLtExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
    }

    @Override
    protected void visitGtEqualExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
    }

    @Override
    protected void visitGtExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
    }

    @Override
    protected void visitEqualsExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
    }

    @Override
    protected void visitNotEqualsExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
    }

    @Override
    protected void visitAddExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Factor
    }

    @Override
    protected void visitSubtractExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Factor
    }

    @Override
    protected void visitOrExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Factor
    }

    @Override
    protected void visitMultiplyExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Factor
        visit(astNode.getChild(1)); // Primary
    }

    @Override
    protected void visitDivideExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Factor
        visit(astNode.getChild(1)); // Primary
    }

    @Override
    protected void visitAndExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Factor
        visit(astNode.getChild(1)); // Primary
    }

    @Override
    protected void visitModExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Factor
        visit(astNode.getChild(1)); // Primary
    }

    @Override
    protected void visitNegativeExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Primary
    }

    @Override
    protected void visitNotExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Primary
    }

    @Override
    protected void visitEofExpression(ASTNode astNode) {
    }

    @Override
    protected void visitCallExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Name
        for (int i = 1; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Expression
        }
    }

    @Override
    protected void visitSuccExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
    }

    @Override
    protected void visitPredExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
    }

    @Override
    protected void visitChrExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
    }

    @Override
    protected void visitOrdExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
    }

    @Override
    public void visitIdentifier(IdentifierNode identifierNode) {
    }
}
