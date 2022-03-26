package semantic;

import lexer.tokens.TokenKind;
import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import semantic.attrs.Instruction;
import semantic.attrs.InstructionMnemonic;
import semantic.attrs.Label;
import semantic.attrs.SemanticType;
import semantic.symbols.Symbol;
import semantic.symbols.TypeSymbol;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer extends BaseVisitor {
    private SymbolTable symbolTable;

    private Context context;
    private List<String> error;
    private List<Instruction> code;

    private void addCode(InstructionMnemonic mnemonic, Object... register) {
        code.add(new Instruction(mnemonic, register));
    }

    private void addError(String message, Object... args) {
        error.add(String.format(message, args));
    }

    private Label getLabel(int position) {
        // Subtracting 1 because position is 1-indexed.
        int zPosition = position - 1;
        if (code.size() <= zPosition) {
            // Add NOP if nothing in the function (?)
            addCode(InstructionMnemonic.NOP);
        }
        return code.get(zPosition).getLabel();
    }

    private int getNext() {
        return code.size() + 1;
    }

    // ---------------------------------------- Program ----------------------------------------------------------------

    public void analyze(ASTNode astNode) {
        this.context = new Context();
        this.symbolTable = new SymbolTable();
        this.error = new ArrayList<>();
        this.code = new ArrayList<>();
        visit(astNode);
        System.out.println("----------------------------------------------------");
        for (String line : this.error) {
            System.out.println(line);
        }
        System.out.println("----------------------------------------------------");
        for (Instruction line : this.code) {
            System.out.println(line);
        }
        System.out.println("----------------------------------------------------");
        System.out.println(this.symbolTable);
    }

    // ---------------------------------------- Program ----------------------------------------------------------------

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

    // ---------------------------------------- Consts -----------------------------------------------------------------

    @Override
    protected void visitConsts(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Const
        }
        context.type = SemanticType.DECLARATION;
    }

    @Override
    protected void visitConst(ASTNode astNode) {
        IdentifierNode identifierNode = (IdentifierNode) astNode.getChild(0); // Name
        IdentifierNode valueNode = (IdentifierNode) astNode.getChild(1); // ConstValue
        String identifier = identifierNode.getIdentifierValue();
        String value = valueNode.getIdentifierValue();
        TokenKind tokenKind = valueNode.getKind();

        // Constants cannot be defined twice in the same scope.
        if (symbolTable.alreadyDefinedInScope(identifier)) {
            addError("Variable '%s' is already defined.", identifier);
            return;
        }

        // Supports char/int literals only. Each is stored as integer regardless of type.
        int registerValue;
        if (tokenKind == TokenKind.CHAR_LITERAL) {
            registerValue = value.codePointAt(1);
        } else if (tokenKind == TokenKind.INTEGER_LITERAL) {
            registerValue = Integer.parseInt(value);
        } else {
            addError("Type mismatched. expected int/char, found %s", tokenKind);
            return;
        }

        // Define the constant at the top and mark the symbol as constant.
        addCode(InstructionMnemonic.LIT, registerValue);
        symbolTable.enterVarSymbol(identifier, true);
    }

    // ---------------------------------------- Types ------------------------------------------------------------------

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

    // ---------------------------------------- SubProgs ---------------------------------------------------------------

    @Override
    protected void visitSubprogs(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // *
            visit(astNode.getChild(i)); // Fcn
        }
    }

    @Override
    protected void visitFcn(ASTNode astNode) {
        // Process the function.
        String functionName = ((IdentifierNode) astNode.getChild(0)).getIdentifierValue(); // Name
        String functionName2 = ((IdentifierNode) astNode.getChild(7)).getIdentifierValue(); // Name
        IdentifierNode returnTypeNode = (IdentifierNode) astNode.getChild(2); // Name

        if (!functionName.equals(functionName2)) {
            addError("Function name of '%s' does not have a proper end.", functionName);
            return;
        }
        Symbol returnTypeSymbol = symbolTable.lookup(returnTypeNode.getIdentifierValue());
        if (returnTypeSymbol == null) {
            addError("Function return type '%s' is not defined.", returnTypeNode.getIdentifierValue());
            return;
        }
        if (!(returnTypeSymbol instanceof TypeSymbol)) {
            addError("Expected function return type to be a TYPE, but found %s.", returnTypeSymbol.getType());
            return;
        }

        // Load local context
        int globalTop = symbolTable.startLocal();
        int fcnStart = getNext();
        visit(astNode.getChild(1)); // Params
        visit(astNode.getChild(3)); // Consts
        visit(astNode.getChild(4)); // Types
        visit(astNode.getChild(5)); // Dclns
        visit(astNode.getChild(6)); // Body

        // Load global context
        symbolTable.endLocal(globalTop);
        // Add label to function start pos and define function as a symbol.
        symbolTable.enterFcnSymbol(functionName, getLabel(fcnStart), (TypeSymbol) returnTypeSymbol);
        context.type = SemanticType.DECLARATION;
    }

    @Override
    protected void visitParams(ASTNode astNode) {
        // Parameter for each function incoming variable.
        context.newVars.clear();
        for (int i = 0; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Dcln
        }
        // Generate code for the new parameters.
        // All parameters are loaded from local frame.
        List<String> newVars = context.newVars;
        for (int i = 0; i < newVars.size(); i++) {
            String identifier = newVars.get(i);
            if (symbolTable.alreadyDefinedInScope(identifier)) {
                addError("Variable '%s' already defined.", identifier);
                continue;
            }
            addCode(InstructionMnemonic.LLV, i + 1);
            symbolTable.enterVarSymbol(identifier, false);
        }
        context.newVars.clear();
        context.type = SemanticType.DECLARATION;
    }

    // ---------------------------------------- Dcln -------------------------------------------------------------------

    @Override
    protected void visitDclns(ASTNode astNode) {
        // Find all the defined variables and add them to the scope.
        context.newVars.clear();
        for (int i = 0; i < astNode.getSize(); i++) { // +
            visit(astNode.getChild(i)); // Dcln
        }
        // Generate code for the new variables.
        // All dclns are treated as new variables initialized with 0.
        for (String identifier : context.newVars) {
            if (symbolTable.alreadyDefinedInScope(identifier)) {
                addError("Variable '%s' already defined in the current scope.", identifier);
                continue;
            }
            addCode(InstructionMnemonic.LIT, 0);
            symbolTable.enterVarSymbol(identifier, false);
        }
        context.newVars.clear();
        context.type = SemanticType.DECLARATION;
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
        Symbol typeSymbol = symbolTable.lookup(type);
        if (typeSymbol == null) {
            addError("'%s' type is not not defined", type);
            return;
        }
        // The data should be of 'TYPE' type.
        if (!SemanticType.TYPE.equals(typeSymbol.getType())) {
            addError("Expected '%s' to be a type, but was a %s", type, typeSymbol.getType());
            return;
        }

        // Synthesize all the new variable names.
        for (IdentifierNode identifierNode : identifierNodes) {
            context.newVars.add(identifierNode.getIdentifierValue());
        }
    }

    // ---------------------------------------- Statements -------------------------------------------------------------

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
