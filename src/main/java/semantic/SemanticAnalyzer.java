package semantic;

import lexer.tokens.TokenKind;
import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import parser.nodes.Node;
import semantic.attrs.BinaryOpType;
import semantic.attrs.Instruction;
import semantic.attrs.InstructionMnemonic;
import semantic.attrs.Label;
import semantic.attrs.OperatingSystemOpType;
import semantic.attrs.UnaryOpType;
import semantic.symbols.ConstantSymbol;
import semantic.symbols.FcnSymbol;
import semantic.symbols.Symbol;
import semantic.symbols.TypeSymbol;
import semantic.symbols.VariableSymbol;
import semantic.table.SymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    private void attachLabel(Label label, int position) {
        // Subtracting 1 because position is 1-indexed.
        int zPosition = position - 1;
        if (code.size() <= zPosition) {
            // Add NOP if nothing in the function (?)
            addCode(InstructionMnemonic.NOP);
        }
        code.get(zPosition).attachLabel(label);

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
        String programName = ((IdentifierNode) astNode.getChild(0)).getIdentifierValue(); // Name
        if (doesEndTokenMismatch(programName, astNode.getChild(6))) return;

        visit(astNode.getChild(1)); // Consts
        visit(astNode.getChild(2)); // Types
        visit(astNode.getChild(3)); // Dclns
        visit(astNode.getChild(4)); // SubProgs
        visit(astNode.getChild(5)); // Body
    }

    // ---------------------------------------- Consts -----------------------------------------------------------------

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

        // Constants cannot be defined twice in the same scope.
        if (symbolTable.alreadyDefinedInScope(identifier)) {
            addError("Variable '%s' is already defined.", identifier);
            return;
        }

        // Supports char/int literals only. Each is stored as integer regardless of type.
        int constantValue;
        TypeSymbol constantType;
        if (tokenKind == TokenKind.CHAR_LITERAL) {
            constantValue = value.codePointAt(1);
            constantType = SymbolTable.CHAR_TYPE;
        } else if (tokenKind == TokenKind.INTEGER_LITERAL) {
            constantValue = Integer.parseInt(value);
            constantType = SymbolTable.INTEGER_TYPE;
        } else {
            // Constant is defined using another constant.
            ConstantSymbol constSymbol = lookupConstant(value);
            if (constSymbol == null) return;
            constantValue = constSymbol.value;
            constantType = constSymbol.type;
        }

        symbolTable.enterConstantSymbol(identifier, constantType, constantValue);
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
        context.newTypeLits.clear();

        String typeName = ((IdentifierNode) astNode.getChild(0)).getIdentifierValue(); // Name
        visit(astNode.getChild(1)); // ListList

        // Define type and retrieve it to use for constant declaration.
        symbolTable.enterTypeSymbol(typeName);
        TypeSymbol typeSymbol = lookupType(typeName);
        if (typeSymbol == null) return;

        // Define all the values that are assignable to this type as constants.
        List<String> newVars = context.newTypeLits;
        for (int i = 0; i < newVars.size(); i++) {
            String newVar = newVars.get(i);
            symbolTable.enterConstantSymbol(newVar, typeSymbol, i);
        }

        context.newTypeLits.clear();
    }

    @Override
    protected void visitLit(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // list
            context.newTypeLits.add(((IdentifierNode) astNode.getChild(i)).getIdentifierValue()); // Name
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
        String functionName = ((IdentifierNode) astNode.getChild(0)).getIdentifierValue(); // Name
        String returnTypeName = ((IdentifierNode) astNode.getChild(2)).getIdentifierValue(); // Name
        if (doesEndTokenMismatch(functionName, astNode.getChild(7))) return;

        TypeSymbol returnTypeSymbol = lookupType(returnTypeName);
        if (returnTypeSymbol == null) return;

        symbolTable.beginLocalScope();
        int fcnStart = getNext();
        visit(astNode.getChild(1)); // Params
        visit(astNode.getChild(3)); // Consts
        visit(astNode.getChild(4)); // Types
        visit(astNode.getChild(5)); // Dclns
        visit(astNode.getChild(6)); // Body
        symbolTable.endLocalScope();
        // Add label to function start pos and define function as a symbol.
        Label fcnLabel = new Label();
        attachLabel(fcnLabel, fcnStart);
        symbolTable.enterFcnSymbol(functionName, fcnLabel, returnTypeSymbol);
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
        for (String identifier : context.newVars) {
            VariableSymbol newVarSymbol = lookupVariable(identifier);
            if (newVarSymbol == null) continue;
            addCode(InstructionMnemonic.LLV, newVarSymbol.address);
        }
        context.newVars.clear();
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
            VariableSymbol newVarSymbol = lookupVariable(identifier);
            if (newVarSymbol == null) continue;
            addCode(InstructionMnemonic.LIT, 0);
        }
        context.newVars.clear();
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
        TypeSymbol typeSymbol = lookupType(type);
        if (typeSymbol == null) return;

        // Define new variables in the symbol table.
        for (IdentifierNode identifierNode : identifierNodes) {
            String identifier = identifierNode.getIdentifierValue();
            if (symbolTable.alreadyDefinedInScope(identifier)) {
                addError("Variable '%s' already defined.", identifier);
                continue;
            }
            symbolTable.enterVariableSymbol(identifier, typeSymbol);
            context.newVars.add(identifier);
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
            if (context.expressionType.isInteger()) {
                addCode(InstructionMnemonic.SOS, OperatingSystemOpType.OUTPUT);
            } else if (context.expressionType.isChar()) {
                addCode(InstructionMnemonic.SOS, OperatingSystemOpType.OUTPUTC);
            } else {
                addError("Invalid type for output statement.");
            }
        }
        addCode(InstructionMnemonic.SOS, OperatingSystemOpType.OUTPUTL);
    }

    @Override
    protected void visitIfStatement(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
        if (!context.expressionType.isBoolean()) {
            addError("Invalid type for if statement.");
        }
        Label thenStartLabel = new Label();
        Label elseStartLabel = astNode.getSize() == 3 ? new Label() : null;
        Label ifEndLabel = new Label();

        // Evaluate the condition.
        addCode(InstructionMnemonic.COND, thenStartLabel, Objects.requireNonNullElse(elseStartLabel, ifEndLabel));
        // Then statement.
        int thenStart = getNext();
        visit(astNode.getChild(1)); // Statement
        addCode(InstructionMnemonic.GOTO, ifEndLabel);
        attachLabel(thenStartLabel, thenStart);
        // Else statement.
        if (elseStartLabel != null) { // ?
            int elseStart = getNext();
            visit(astNode.getChild(2)); // Statement
            attachLabel(elseStartLabel, elseStart);
        }
        // End if.
        attachLabel(ifEndLabel, getNext());
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
            if (context.expressionType.isInteger()) {
                addCode(InstructionMnemonic.SOS, OperatingSystemOpType.INPUT);
            } else if (context.expressionType.isChar()) {
                addCode(InstructionMnemonic.SOS, OperatingSystemOpType.INPUTC);
            } else {
                addError("Invalid type for read statement.");
                continue;
            }
            String identifier = ((IdentifierNode) astNode.getChild(i)).getIdentifierValue();
            VariableSymbol variableSymbol = lookupVariable(identifier);
            if (variableSymbol == null) continue;
            if (variableSymbol.isGlobal) {
                addCode(InstructionMnemonic.SGV, variableSymbol.address);
            } else {
                addCode(InstructionMnemonic.SLV, variableSymbol.address);
            }
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
        addCode(InstructionMnemonic.BOP, BinaryOpType.BLE);
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
    }

    @Override
    protected void visitLtExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BLT);
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
    }

    @Override
    protected void visitGtEqualExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BGE);
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
    }

    @Override
    protected void visitGtExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BGT);
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
    }

    @Override
    protected void visitEqualsExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BEQ);
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
    }

    @Override
    protected void visitNotEqualsExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BNE);
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
    }

    @Override
    protected void visitAddExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BPLUS);
        context.expressionType = SymbolTable.INTEGER_TYPE;
    }

    @Override
    protected void visitSubtractExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BMINUS);
        context.expressionType = SymbolTable.INTEGER_TYPE;
    }

    @Override
    protected void visitOrExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        TypeSymbol firstType = context.expressionType;
        visit(astNode.getChild(1)); // Term
        TypeSymbol secondType = context.expressionType;
        if (isLogicalOperatorDefined(firstType, secondType)) {
            addCode(InstructionMnemonic.BOP, BinaryOpType.BOR);
        }
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
    }

    @Override
    protected void visitMultiplyExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BMULT);
        context.expressionType = SymbolTable.INTEGER_TYPE;
    }

    @Override
    protected void visitDivideExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BDIV);
        context.expressionType = SymbolTable.INTEGER_TYPE;
    }

    @Override
    protected void visitAndExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        TypeSymbol firstType = context.expressionType;
        visit(astNode.getChild(1)); // Term
        TypeSymbol secondType = context.expressionType;
        if (isLogicalOperatorDefined(firstType, secondType)) {
            addCode(InstructionMnemonic.BOP, BinaryOpType.BAND);
        }
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
    }

    @Override
    protected void visitModExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BMOD);
        context.expressionType = SymbolTable.INTEGER_TYPE;
    }

    @Override
    protected void visitNegativeExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Primary
        if (isNumericOperatorDefined(context.expressionType)) {
            addCode(InstructionMnemonic.UOP, UnaryOpType.UNEG);
        }
        context.expressionType = SymbolTable.INTEGER_TYPE;
    }

    @Override
    protected void visitNotExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Primary
        if (isBooleanOperatorDefined(context.expressionType)) {
            addCode(InstructionMnemonic.UOP, UnaryOpType.UNOT);
        }
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
    }

    @Override
    protected void visitEofExpression(ASTNode astNode) {
        // TODO
    }

    @Override
    protected void visitCallExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Name
        for (int i = 1; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Expression
        }
        // TODO
    }

    @Override
    protected void visitSuccExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Primary
        if (isSuccPredOperatorDefined(context.expressionType)) {
            addCode(InstructionMnemonic.UOP, UnaryOpType.USUCC);
        }
        // Expression type does not change.
    }

    @Override
    protected void visitPredExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Primary
        if (isSuccPredOperatorDefined(context.expressionType)) {
            addCode(InstructionMnemonic.UOP, UnaryOpType.UPRED);
        }
        // Expression type does not change.
    }

    @Override
    protected void visitChrExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
        // Simply change the expression type to char.
        context.expressionType = SymbolTable.CHAR_TYPE;
    }

    @Override
    protected void visitOrdExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
        // Simply change the expression type to integer.
        context.expressionType = SymbolTable.INTEGER_TYPE;
    }

    @Override
    public void visitIdentifier(IdentifierNode identifierNode) {
        // Simple literals.
        if (TokenKind.CHAR_LITERAL.equals(identifierNode.getKind())) {
            addCode(InstructionMnemonic.LIT, identifierNode.getIdentifierValue().codePointAt(1));
            context.expressionType = SymbolTable.CHAR_TYPE;
            return;
        }
        if (TokenKind.INTEGER_LITERAL.equals(identifierNode.getKind())) {
            addCode(InstructionMnemonic.LIT, Integer.parseInt(identifierNode.getIdentifierValue()));
            context.expressionType = SymbolTable.INTEGER_TYPE;
            return;
        }

        Symbol symbol = symbolTable.lookup(identifierNode.getIdentifierValue());
        // Variables are handled depending on whether they are global or local.
        if (symbol instanceof VariableSymbol) {
            VariableSymbol variableSymbol = (VariableSymbol) symbol;
            if (variableSymbol.isGlobal) {
                addCode(InstructionMnemonic.LGV, variableSymbol.address);
            } else {
                addCode(InstructionMnemonic.LLV, variableSymbol.address);
            }
            context.expressionType = variableSymbol.type;
            return;
        }
        // Constants are simply loaded to the stack.
        if (symbol instanceof ConstantSymbol) {
            ConstantSymbol constantSymbol = (ConstantSymbol) symbol;
            addCode(InstructionMnemonic.LIT, constantSymbol.value);
            context.expressionType = constantSymbol.type;
            return;
        }
        addError("Identifier '%s' is not defined.", identifierNode.getIdentifierValue());
    }

    // -----------------------------------------------------------------------------------------------------------------

    private boolean doesEndTokenMismatch(String name, Node endNode) {
        if (!name.equals(((IdentifierNode) endNode).getIdentifierValue())) {
            addError("Begin and end clauses have mismatching names.", name);
            return true;
        }
        return false;
    }

    private boolean isLogicalOperatorDefined(TypeSymbol firstType, TypeSymbol secondType) {
        boolean isDefined = firstType.isBoolean() && secondType.isBoolean();
        if (!isDefined) addError("Invalid types for logical operator. " +
                "Requires 'boolean'. Found '%s' and '%s'.", firstType.name, secondType.name);
        return isDefined;
    }

    private boolean isBooleanOperatorDefined(TypeSymbol typeSymbol) {
        boolean isDefined = typeSymbol.isBoolean();
        if (!isDefined) addError("Invalid type for boolean operator. Requires 'boolean'. Found '%s'.",
                typeSymbol.name);
        return isDefined;
    }

    private boolean isNumericOperatorDefined(TypeSymbol typeSymbol) {
        boolean isDefined = typeSymbol.isInteger();
        if (!isDefined) addError("Invalid type for numeric operator. Requires 'integer'. Found '%s'.",
                typeSymbol.name);
        return isDefined;
    }

    private boolean isSuccPredOperatorDefined(TypeSymbol typeSymbol) {
        boolean isDefined = typeSymbol.isInteger() || typeSymbol.isChar() || typeSymbol.isCustom();
        if (!isDefined) addError("Invalid type for succ/pred operations. Found '%s'.", typeSymbol.name);
        return isDefined;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public VariableSymbol lookupVariable(String name) {
        Symbol symbol = symbolTable.lookup(name);
        if (symbol instanceof VariableSymbol) return (VariableSymbol) symbol;
        addError("Variable '%s' is not defined.", name);
        return null;
    }

    public ConstantSymbol lookupConstant(String name) {
        Symbol symbol = symbolTable.lookup(name);
        if (symbol instanceof ConstantSymbol) return (ConstantSymbol) symbol;
        addError("Constant '%s' is not defined.", name);
        return null;
    }

    public TypeSymbol lookupType(String name) {
        Symbol symbol = symbolTable.lookup(name);
        if (symbol instanceof TypeSymbol) return (TypeSymbol) symbol;
        addError("Type '%s' is not defined.", name);
        return null;
    }

    public FcnSymbol lookupFcn(String name) {
        Symbol symbol = symbolTable.lookup(name);
        if (symbol instanceof FcnSymbol) return (FcnSymbol) symbol;
        addError("Function '%s' is not defined.", name);
        return null;
    }
}
