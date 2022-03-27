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

        // Go to entry point and skip sub program section.
        // Otherwise, will enter sub program first since they will be in the top.
        // The entry point label will be attached to the correct position afterwards.
        Label entryPointLabel = new Label();
        addCode(InstructionMnemonic.GOTO, entryPointLabel);
        visit(astNode.getChild(4)); // SubProgs

        // Entry point.
        int entryPointPosition = getNext();
        visit(astNode.getChild(5)); // Body
        addCode(InstructionMnemonic.HALT);
        attachLabel(entryPointLabel, entryPointPosition);
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
        // Constant can point to another constant, but it should be defined before this.
        // Basically, the value should be determinable at compile time.
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

        // Constants are not stored in the memory. The value is directly used.
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
        String typeName = ((IdentifierNode) astNode.getChild(0)).getIdentifierValue(); // Name

        // Following is used to find the literals that are defined in the type.
        // We clear it, so it would not have previously calculated values.
        context.newTypeLits.clear();
        visit(astNode.getChild(1)); // ListList

        // Define type and retrieve it to use for constant declaration.
        // User defined types will be simply a collection of names.
        // As implementation, they will be denoted as compile time constants.
        // So `type A = (p, q, r)` generated 3 constants; p, q, r.
        // However, the type of the constants generated will be A.
        symbolTable.enterTypeSymbol(typeName);
        TypeSymbol typeSymbol = lookupType(typeName);
        if (typeSymbol == null) return;

        // Define all the values that are assignable to this type as constants.
        List<String> newVars = context.newTypeLits;
        for (int i = 0; i < newVars.size(); i++) {
            String newVar = newVars.get(i);
            symbolTable.enterConstantSymbol(newVar, typeSymbol, i);
        }
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
        // Stores the current global top.
        // Inside the functions, all the addresses will be relative to function start position.
        // After the function definitions are over, the global will need to be restored.
        int globalTop = context.top;
        for (int i = 0; i < astNode.getSize(); i++) { // *
            context.top = 0;
            visit(astNode.getChild(i)); // Fcn
        }
        context.top = globalTop;
    }

    @Override
    protected void visitFcn(ASTNode astNode) {
        String functionName = ((IdentifierNode) astNode.getChild(0)).getIdentifierValue(); // Name
        String returnTypeName = ((IdentifierNode) astNode.getChild(2)).getIdentifierValue(); // Name
        if (doesEndTokenMismatch(functionName, astNode.getChild(7))) return;

        int functionEntryPosition = getNext();
        // Get the return type from the function definition.
        // The return value is required to determine the type of the values of calls.
        TypeSymbol returnTypeSymbol = lookupType(returnTypeName);
        if (returnTypeSymbol == null) return;

        // The param types will be used to synthesize the parameters of the function.
        // This will be used for compile time checking of parameters to function calls.
        context.paramTypes.clear();

        // Begins a new scope. (A new local symbol table)
        symbolTable.beginLocalScope();
        visit(astNode.getChild(1)); // Params
        visit(astNode.getChild(3)); // Consts
        visit(astNode.getChild(4)); // Types
        visit(astNode.getChild(5)); // Dclns

        // Before entering body, create function symbols to enable recursive calls.
        // The entry point label is used to generate the function call instructions.
        // Param types are copied to remove side effects of clearing the array.
        // Even if this is entered while in local scope, functions are always global.
        Label functionEntryLabel = new Label();
        List<TypeSymbol> paramTypes = new ArrayList<>(context.paramTypes);
        symbolTable.enterFcnSymbol(functionName, functionEntryLabel, paramTypes, returnTypeSymbol);

        // Then enter body.
        visit(astNode.getChild(6)); // Body
        symbolTable.endLocalScope();

        // Attach labels.
        attachLabel(functionEntryLabel, functionEntryPosition);
    }

    @Override
    protected void visitParams(ASTNode astNode) {
        // Parameter for each function incoming variable.
        // Following will be used to get the newly created parameters.
        context.newVars.clear();
        for (int i = 0; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Dcln
        }

        // Generate code for the new parameters.
        // All parameters are loaded from local frame.
        // Parameters will not have explicit value storage.
        // So, no instructions will be generated.
        for (String identifier : context.newVars) {
            VariableSymbol newVarSymbol = lookupVariable(identifier);
            if (newVarSymbol == null) continue;
            // Top is already increased by variable declaration.
            // So we do not need to change the top to denote the pushing of params.
            context.paramTypes.add(newVarSymbol.type);
        }
    }

    // ---------------------------------------- Dcln -------------------------------------------------------------------

    @Override
    protected void visitDclns(ASTNode astNode) {
        // Find all the defined variables and add them to the scope.
        // Following will be used to get the newly created variables.
        context.newVars.clear();
        for (int i = 0; i < astNode.getSize(); i++) { // +
            visit(astNode.getChild(i)); // Dcln
        }

        // Generate code for the new variables.
        // All dclns are treated as new variables initialized with 0.
        // These will be explicitly stored, so instructions will be generated.
        for (String identifier : context.newVars) {
            VariableSymbol newVarSymbol = lookupVariable(identifier);
            if (newVarSymbol == null) continue;
            // Top is already increased by variable declaration.
            // So we do not need to change the top value.
            addCode(InstructionMnemonic.LIT, 0);
        }
    }

    @Override
    protected void visitVar(ASTNode astNode) {
        List<IdentifierNode> identifierNodes = new ArrayList<>();
        for (int i = 0; i < astNode.getSize() - 1; i++) { // list
            identifierNodes.add((IdentifierNode) astNode.getChild(i)); // Name
        }
        IdentifierNode typeNode = (IdentifierNode) astNode.getChild(astNode.getSize() - 1); // Name (Type)

        // Get the type symbol for the variable from symbol table.
        TypeSymbol typeSymbol = lookupType(typeNode.getIdentifierValue());
        if (typeSymbol == null) return;

        // Define new variables in the symbol table.
        // Here, no new machine instructions are generated.
        // The above node (either dcln or param) is expected to deal with them.
        // The variables will simply be added to the symbol table.
        for (IdentifierNode identifierNode : identifierNodes) {
            String identifier = identifierNode.getIdentifierValue();
            if (symbolTable.alreadyDefinedInScope(identifier)) {
                addError("Variable '%s' already defined.", identifier);
                continue;
            }
            symbolTable.enterVariableSymbol(identifier, ++context.top, typeSymbol);
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

            // Generate correct output depending on the expression type.
            // TODO: Handle the case of string output.
            if (context.expressionType.isInteger()) {
                addCode(InstructionMnemonic.SOS, OperatingSystemOpType.OUTPUT);
            } else if (context.expressionType.isChar()) {
                addCode(InstructionMnemonic.SOS, OperatingSystemOpType.OUTPUTC);
            } else {
                addError("Invalid type for output statement.");
                continue;
            }
            context.top--;
        }
        // New line at the end of output.
        addCode(InstructionMnemonic.SOS, OperatingSystemOpType.OUTPUTL);
    }

    @Override
    protected void visitIfStatement(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
        if (!context.expressionType.isBoolean()) addError("Invalid type for if statement.");

        Label thenEntryLabel = new Label();
        Label elseEntryLabel = astNode.getSize() == 3 ? new Label() : null;
        Label ifExitLabel = new Label();

        // Evaluate the condition. (Pops one value off the stack)
        // Here, depending on the else clause, go to either else or end of if.
        Label thenExitLabel = Objects.requireNonNullElse(elseEntryLabel, ifExitLabel);
        addCode(InstructionMnemonic.COND, thenEntryLabel, thenExitLabel);
        context.top--;

        // Then statement.
        // After then, go to the end of statement.
        int thenEntryPosition = getNext();
        visit(astNode.getChild(1)); // Statement
        addCode(InstructionMnemonic.GOTO, ifExitLabel);
        attachLabel(thenEntryLabel, thenEntryPosition);

        if (elseEntryLabel != null) { // ?
            int elseStartPosition = getNext();
            visit(astNode.getChild(2)); // Statement
            attachLabel(elseEntryLabel, elseStartPosition);
        }
        // With current implementation, the if statement is always followed by a NOP.
        // The if exit label is attached to the NOP.
        attachLabel(ifExitLabel, getNext());
    }

    @Override
    protected void visitWhileStatement(ASTNode astNode) {
        // TODO: Implement this.
        visit(astNode.getChild(0)); // Expression
        visit(astNode.getChild(1)); // Statement
        throw new UnsupportedOperationException("while");
    }

    @Override
    protected void visitRepeatStatement(ASTNode astNode) {
        // TODO: Implement this.
        for (int i = 0; i < astNode.getSize() - 1; i++) { // list
            visit(astNode.getChild(i)); // Statement
        }
        visit(astNode.getChild(astNode.getSize() - 1)); // Expression
        throw new UnsupportedOperationException("repeat");
    }

    @Override
    protected void visitForStatement(ASTNode astNode) {
        // TODO: Implement this.
        visit(astNode.getChild(0)); // ForStat
        visit(astNode.getChild(1)); // ForExp
        visit(astNode.getChild(2)); // ForStat
        visit(astNode.getChild(3)); // Statement
        throw new UnsupportedOperationException("for");
    }

    @Override
    protected void visitLoopStatement(ASTNode astNode) {
        // TODO: Implement this.
        for (int i = 0; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Statement
        }
        throw new UnsupportedOperationException("loop");
    }

    @Override
    protected void visitCaseStatement(ASTNode astNode) {
        // TODO: Implement this.
        visit(astNode.getChild(0)); // Expression
        for (int i = 1; i < astNode.getSize() - 1; i++) { // Caseclauses +
            visit(astNode.getChild(i)); // Caseclause
        }
        visit(astNode.getChild(astNode.getSize() - 1)); // OtherwiseClause
        throw new UnsupportedOperationException("case");
    }

    @Override
    protected void visitReadStatement(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // list
            // Change instruction depending on the parameter type.
            // Here top increases by one each time.
            // But, since the next instruction saves the value, top will decrease again.
            // So overall, no change to the top.
            if (context.expressionType.isInteger()) {
                addCode(InstructionMnemonic.SOS, OperatingSystemOpType.INPUT);
            } else if (context.expressionType.isChar()) {
                addCode(InstructionMnemonic.SOS, OperatingSystemOpType.INPUTC);
            } else {
                addError("Invalid type for read statement.");
                continue;
            }
            // Generate instruction to save in local/global variable.
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
        // TODO: Implement this.
        throw new UnsupportedOperationException("exit");
    }

    @Override
    protected void visitReturnStatement(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
        addCode(InstructionMnemonic.RTN, 1);
        // TODO: Check if the return type is correct.
        context.top--;
        // Expression type does not change from the child.
    }

    @Override
    protected void visitNullStatement(ASTNode astNode) {
        // Do nothing.
    }

    @Override
    protected void visitIntegerOutExp(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
    }

    @Override
    protected void visitStringOutExp(ASTNode astNode) {
        // TODO: Implement this.
        visit(astNode.getChild(0)); // StringNode
        throw new UnsupportedOperationException("string_out");
    }

    @Override
    protected void visitCaseClause(ASTNode astNode) {
        // TODO: Implement this.
        for (int i = 0; i < astNode.getSize() - 1; i++) { // list
            visit(astNode.getChild(i)); // CaseExpression
        }
        visit(astNode.getChild(astNode.getSize() - 1)); // Statement
        throw new UnsupportedOperationException("case_clause");
    }

    @Override
    protected void visitDoubleDotsClause(ASTNode astNode) {
        // TODO: Implement this.
        visit(astNode.getChild(0)); // ConstValue
        visit(astNode.getChild(1)); // ConstValue
        throw new UnsupportedOperationException("double_dots_clause");
    }

    @Override
    protected void visitOtherwiseClause(ASTNode astNode) {
        // TODO: Implement this.
        visit(astNode.getChild(0)); // Statement
        throw new UnsupportedOperationException("otherwise_clause");
    }

    @Override
    protected void visitAssignmentStatement(ASTNode astNode) {
        String identifier = ((IdentifierNode) astNode.getChild(0)).getIdentifierValue(); // Name
        visit(astNode.getChild(1)); // Expression

        // Get the variable symbol and check its type.
        // The expression result should be assignable to the variable.
        VariableSymbol variableSymbol = lookupVariable(identifier);
        if (variableSymbol == null) return;
        if (!variableSymbol.type.isAssignable(context.expressionType)) {
            addError("Invalid type for assignment statement: expected " + variableSymbol.type +
                    ", got " + context.expressionType);
            return;
        }

        // Generate instruction to save in local/global variable.
        // This will pop the expression result.
        if (variableSymbol.isGlobal) {
            addCode(InstructionMnemonic.SGV, variableSymbol.address);
        } else {
            addCode(InstructionMnemonic.SLV, variableSymbol.address);
        }
        context.top--;
    }

    @Override
    protected void visitSwapStatement(ASTNode astNode) {
        // TODO: Implement this.
        visit(astNode.getChild(0)); // Name
        visit(astNode.getChild(1)); // Name
        throw new UnsupportedOperationException("swap");
    }

    @Override
    protected void visitTrue(ASTNode astNode) {
        // TODO: Implement this.
        throw new UnsupportedOperationException("true");
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    protected void visitLtEqualExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BLE);
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
        // Two results are popped from the stack and one result is pushed.
        // So the top index is decreased by one for binary operators.
        context.top--;
    }

    @Override
    protected void visitLtExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BLT);
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
        context.top--;
    }

    @Override
    protected void visitGtEqualExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BGE);
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
        context.top--;
    }

    @Override
    protected void visitGtExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BGT);
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
        context.top--;
    }

    @Override
    protected void visitEqualsExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BEQ);
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
        context.top--;
    }

    @Override
    protected void visitNotEqualsExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BNE);
        context.expressionType = SymbolTable.BOOLEAN_TYPE;
        context.top--;
    }

    @Override
    protected void visitAddExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        TypeSymbol leftTypeSymbol = context.expressionType;
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BPLUS);
        // Expression type is the type of the left operand
        context.expressionType = leftTypeSymbol;
        context.top--;
    }

    @Override
    protected void visitSubtractExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        TypeSymbol leftTypeSymbol = context.expressionType;
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BMINUS);
        // Expression type is the type of the left operand
        context.expressionType = leftTypeSymbol;
        context.top--;
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
        context.top--;
    }

    @Override
    protected void visitMultiplyExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BMULT);
        context.expressionType = SymbolTable.INTEGER_TYPE;
        context.top--;
    }

    @Override
    protected void visitDivideExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BDIV);
        context.expressionType = SymbolTable.INTEGER_TYPE;
        context.top--;
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
        context.top--;
    }

    @Override
    protected void visitModExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BMOD);
        context.expressionType = SymbolTable.INTEGER_TYPE;
        context.top--;
    }

    @Override
    protected void visitNegativeExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Primary
        if (isNumericOperatorDefined(context.expressionType)) {
            addCode(InstructionMnemonic.UOP, UnaryOpType.UNEG);
        }
        context.expressionType = SymbolTable.INTEGER_TYPE;
        // One result is popped from the stack and one result is pushed.
        // So the top index does not change for unary operators.
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
        // TODO: Implement this.
        throw new UnsupportedOperationException("eof_expression");
    }

    @Override
    protected void visitCallExpression(ASTNode astNode) {
        String fcnName = ((IdentifierNode) astNode.getChild(0)).getIdentifierValue();

        // Check if the function is defined.
        FcnSymbol fcnSymbol = lookupFcn(fcnName);
        if (fcnSymbol == null) return;

        // Push return value storage to the stack first.
        // This will increase the stack by one.
        addCode(InstructionMnemonic.LIT, 0);
        context.top++;
        // After the function is called, we have to restore the top.
        int top = context.top;

        // Push parameters and check if the parameters are correct.
        List<TypeSymbol> typeSymbols = new ArrayList<>();
        for (int i = 1; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Expression
            typeSymbols.add(context.expressionType);
        }
        if (!isFunctionAssignable(fcnSymbol, typeSymbols)) return;

        // Restore the top and call the function.
        context.top = top;
        addCode(InstructionMnemonic.CODE, fcnSymbol.label);
        addCode(InstructionMnemonic.CALL, context.top);
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
            context.top++;
            return;
        }
        if (TokenKind.INTEGER_LITERAL.equals(identifierNode.getKind())) {
            addCode(InstructionMnemonic.LIT, Integer.parseInt(identifierNode.getIdentifierValue()));
            context.expressionType = SymbolTable.INTEGER_TYPE;
            context.top++;
            return;
        }

        // If it is a variable, then we have to load it from the stack.
        // Variables are handled depending on whether they are global or local.
        Symbol symbol = symbolTable.lookup(identifierNode.getIdentifierValue());
        if (symbol instanceof VariableSymbol) {
            VariableSymbol variableSymbol = (VariableSymbol) symbol;
            if (variableSymbol.isGlobal) {
                addCode(InstructionMnemonic.LGV, variableSymbol.address);
            } else {
                addCode(InstructionMnemonic.LLV, variableSymbol.address);
            }
            context.expressionType = variableSymbol.type;
            context.top++;
            return;
        }
        // Constants are simply loaded to the stack.
        if (symbol instanceof ConstantSymbol) {
            ConstantSymbol constantSymbol = (ConstantSymbol) symbol;
            addCode(InstructionMnemonic.LIT, constantSymbol.value);
            context.expressionType = constantSymbol.type;
            context.top++;
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

    private boolean isFunctionAssignable(FcnSymbol fcnSymbol, List<TypeSymbol> paramTypes) {
        if (fcnSymbol.paramTypes.size() != paramTypes.size()) {
            addError("Invalid number of parameters for function '%s'. Expected %d, found %d.",
                    fcnSymbol.name, fcnSymbol.paramTypes.size(), paramTypes.size());
            return false;
        }
        for (int i = 0; i < fcnSymbol.paramTypes.size(); i++) {
            if (!fcnSymbol.paramTypes.get(i).isAssignable(paramTypes.get(i))) {
                addError("Invalid argument type for function '%s'. Expected '%s', found '%s'.",
                        fcnSymbol.name, fcnSymbol.paramTypes.get(i).name, paramTypes.get(i).name);
                return false;
            }
        }
        return true;
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
