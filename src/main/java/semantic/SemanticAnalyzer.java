package semantic;

import lexer.tokens.TokenKind;
import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import parser.nodes.Node;
import parser.nodes.NodeKind;
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
import java.util.StringJoiner;

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

    public List<Instruction> generate(ASTNode astNode) {
        this.context = new Context();
        this.symbolTable = new SymbolTable();
        this.error = new ArrayList<>();
        this.code = new ArrayList<>();
        visit(astNode);
        if (!error.isEmpty()) {
            StringJoiner sj = new StringJoiner("\n");
            this.error.forEach(sj::add);
            sj.add("Compilation aborted due to semantic errors.");
            throw new IllegalStateException(sj.toString());
        }
        return code;
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

        // Constants cannot be defined twice in the same scope.
        if (symbolTable.alreadyDefinedInScope(identifier)) {
            addError("Variable '%s' is already defined.", identifier);
            return;
        }

        // Supports char/int literals only. Each is stored as integer regardless of type.
        // Constant can point to another constant, but it should be defined before this.
        // Basically, the value should be determinable at compile time.
        int constantValue = getConstantValue(valueNode);
        TypeSymbol constantTypeSymbol = getConstantType(valueNode);
        if (constantTypeSymbol == null) return;

        // Constants are not stored in the memory. The value is directly used.
        symbolTable.enterConstantSymbol(identifier, constantTypeSymbol, constantValue);
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
        context.newTypeLiteralNames.clear();
        visit(astNode.getChild(1)); // ListList

        // Define type and retrieve it to use for constant declaration.
        // User defined types will be simply a collection of names.
        // As implementation, they will be denoted as compile time constants.
        // So `type A = (p, q, r)` generated 3 constants; p, q, r.
        // However, the type of the constants generated will be A.
        TypeSymbol typeSymbol = symbolTable.enterTypeSymbol(typeName);

        // Define all the values that are assignable to this type as constants.
        List<String> newVars = context.newTypeLiteralNames;
        for (int i = 0; i < newVars.size(); i++) {
            String newVar = newVars.get(i);
            symbolTable.enterConstantSymbol(newVar, typeSymbol, i);
        }
    }

    @Override
    protected void visitLit(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // list
            context.newTypeLiteralNames.add(((IdentifierNode) astNode.getChild(i)).getIdentifierValue()); // Name
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
        context.paramTypeSymbols.clear();

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
        List<TypeSymbol> paramTypeSymbols = new ArrayList<>(context.paramTypeSymbols);
        context.activeFcnSymbol = symbolTable.enterFcnSymbol(functionName, functionEntryLabel,
                paramTypeSymbols, returnTypeSymbol);
        visit(astNode.getChild(6)); // Body
        context.activeFcnSymbol = null;

        // Always add a return instruction at the end of the function.
        // This is required to prevent the compiler from generating an error.
        // This will increase and decrease top, so no changes are required.
        addCode(InstructionMnemonic.LIT, 0);
        addCode(InstructionMnemonic.RTN, 1);

        symbolTable.endLocalScope();

        // Attach labels.
        attachLabel(functionEntryLabel, functionEntryPosition);
    }

    @Override
    protected void visitParams(ASTNode astNode) {
        // Parameter for each function incoming variable.
        // Following will be used to get the newly created parameters.
        context.newVarNames.clear();
        for (int i = 0; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Dcln
        }

        // Generate code for the new parameters.
        // All parameters are loaded from local frame.
        // Parameters will not have explicit value storage.
        // So, no instructions will be generated.
        for (String identifier : context.newVarNames) {
            VariableSymbol newVarSymbol = lookupVariable(identifier);
            if (newVarSymbol == null) continue;
            // Top is already increased by variable declaration.
            // So we do not need to change the top to denote the pushing of params.
            context.paramTypeSymbols.add(newVarSymbol.typeSymbol);
        }
    }

    // ---------------------------------------- Dcln -------------------------------------------------------------------

    @Override
    protected void visitDclns(ASTNode astNode) {
        // Find all the defined variables and add them to the scope.
        // Following will be used to get the newly created variables.
        context.newVarNames.clear();
        for (int i = 0; i < astNode.getSize(); i++) { // +
            visit(astNode.getChild(i)); // Dcln
        }

        // Generate code for the new variables.
        // All dclns are treated as new variables initialized with 0.
        // These will be explicitly stored, so instructions will be generated.
        for (String identifier : context.newVarNames) {
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
            context.newVarNames.add(identifier);
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
            if (context.exprTypeSymbol.isInteger()) {
                addCode(InstructionMnemonic.SOS, OperatingSystemOpType.OUTPUT);
                context.top--;
            } else if (context.exprTypeSymbol.isChar()) {
                addCode(InstructionMnemonic.SOS, OperatingSystemOpType.OUTPUTC);
                context.top--;
            } else if (context.exprTypeSymbol.isString()) {
                // Since the string expression is not added to the generated code,
                // we have to add them manually. Top will not change since adding and removing.
                for (int j = 0; j < context.stringExpression.length(); j++) {
                    addCode(InstructionMnemonic.LIT, (int) context.stringExpression.charAt(j));
                    addCode(InstructionMnemonic.SOS, OperatingSystemOpType.OUTPUTC);
                }
            } else {
                addError("Invalid type for output statement.");
            }

            // Assumption: For every value, except the last, manually insert a space.
            // This will not change top.
            if (i < astNode.getSize() - 1) {
                addCode(InstructionMnemonic.LIT, (int) ' ');
                addCode(InstructionMnemonic.SOS, OperatingSystemOpType.OUTPUTC);
            }
        }
        // New line at the end of output.
        addCode(InstructionMnemonic.SOS, OperatingSystemOpType.OUTPUTL);
    }

    @Override
    protected void visitIfStatement(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
        if (!context.exprTypeSymbol.isBoolean()) addError("Invalid type for if condition.");

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
        Label whileConditionLabel = new Label();
        Label whileBodyLabel = new Label();
        Label whileExitLabel = new Label();

        int whileConditionPosition = getNext();
        visit(astNode.getChild(0)); // Expression
        if (!context.exprTypeSymbol.isBoolean()) addError("Invalid type for while condition.");

        // Depending on the condition value, go to either the body or the end of while.
        // Top will decrease by one because we pop the condition value.
        addCode(InstructionMnemonic.COND, whileBodyLabel, whileExitLabel);
        context.top--;
        attachLabel(whileConditionLabel, whileConditionPosition);

        // Execute the body and go back to the condition.
        int whileBodyPosition = getNext();
        visit(astNode.getChild(1)); // Statement
        addCode(InstructionMnemonic.GOTO, whileConditionLabel);
        attachLabel(whileBodyLabel, whileBodyPosition);

        // With current implementation, the while statement is always followed by a NOP.
        // The while exit label is attached to the NOP.
        attachLabel(whileExitLabel, getNext());
    }

    @Override
    protected void visitRepeatStatement(ASTNode astNode) {
        Label repeatBodyLabel = new Label();
        Label repeatExitLabel = new Label();

        // Execute the body and go back to the condition.
        int repeatBodyPosition = getNext();
        for (int i = 0; i < astNode.getSize() - 1; i++) { // list
            visit(astNode.getChild(i)); // Statement
        }
        attachLabel(repeatBodyLabel, repeatBodyPosition);

        visit(astNode.getChild(astNode.getSize() - 1)); // Expression
        if (!context.exprTypeSymbol.isBoolean()) addError("Invalid type for repeat condition.");

        // Depending on the condition value, go to either the body or the end of while.
        // Top will decrease by one because we pop the condition value.
        addCode(InstructionMnemonic.COND, repeatExitLabel, repeatBodyLabel);
        context.top--;

        // With current implementation, the repeat statement is always followed by a NOP.
        // The repeat exit label is attached to the NOP.
        attachLabel(repeatExitLabel, getNext());
    }

    @Override
    protected void visitForStatement(ASTNode astNode) {
        // First statement is the initialization.
        // This is run only once.
        visit(astNode.getChild(0)); // ForStat

        Label forConditionLabel = new Label();
        Label forBodyLabel = new Label();
        Label forExitLabel = new Label();

        // Second statement is the condition.
        int forConditionPosition = getNext();
        visit(astNode.getChild(1)); // ForExp
        if (!context.exprTypeSymbol.isBoolean()) addError("Invalid type for loop condition.");

        // If the condition is true, go to the body.
        // Otherwise, go to the end of for.
        addCode(InstructionMnemonic.COND, forBodyLabel, forExitLabel);
        attachLabel(forConditionLabel, forConditionPosition);
        context.top--;

        // Third statement is the update.
        // But first we run the body, then we run the update.
        int forBodyPosition = getNext();
        visit(astNode.getChild(3)); // Statement
        visit(astNode.getChild(2)); // ForStat
        addCode(InstructionMnemonic.GOTO, forConditionLabel);
        attachLabel(forBodyLabel, forBodyPosition);

        // With current implementation, the for statement is always followed by a NOP.
        // The for exit label is attached to the NOP.
        attachLabel(forExitLabel, getNext());
    }

    @Override
    protected void visitLoopStatement(ASTNode astNode) {
        Label loopStartLabel = new Label();
        int loopStartPosition = getNext();

        // Execute the body and go back to the condition.
        for (int i = 0; i < astNode.getSize(); i++) { // list
            visit(astNode.getChild(i)); // Statement
        }
        addCode(InstructionMnemonic.GOTO, loopStartLabel);
        attachLabel(loopStartLabel, loopStartPosition);
    }

    @Override
    protected void visitCaseStatement(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression

        // Create a pseudo variable to keep track of expression.
        // If there is a previous case variable, we track it. (Can be nested case statements)
        VariableSymbol previousCaseVariableSymbol = context.currentCaseVariableSymbol;
        context.currentCaseVariableSymbol = new VariableSymbol("~generated~",
                context.exprTypeSymbol, context.top, false);

        // If the last node is of kind otherwise, there is an otherwise clause.
        // There will always be at least one node, so no checking is required.
        // Number of case clauses will depend on whether there is an otherwise clause.
        ASTNode lastNode = (ASTNode) astNode.getChild(astNode.getSize() - 1);
        boolean hasOtherwise = NodeKind.OTHERWISE_CLAUSE.equals(lastNode.getKind());
        int nCaseClauses = hasOtherwise ? astNode.getSize() - 2 : astNode.getSize() - 1;

        // Case clauses except expression and otherwise (if exists).
        // Add a label for each case and end of cases.
        // The end case label will be used by final case clause.
        List<Label> caseClausesLabels = new ArrayList<>();
        Label caseClausesEndLabel = new Label();
        Label caseOtherwiseLabel = hasOtherwise ? new Label() : caseClausesEndLabel;
        for (int i = 0; i < nCaseClauses; i++) {
            caseClausesLabels.add(new Label());
        }
        caseClausesLabels.add(caseOtherwiseLabel);

        // Create all the case clauses.
        // At the end of each clause, there will be a clause to exit the case.
        // Assumption: Cases break. After first case block, the second will not be checked.
        // So, only one block will execute. (The first one that matches)
        // Otherwise clause if non matches.
        for (int i = 0; i < nCaseClauses; i++) { // Caseclauses +
            int caseClausePosition = getNext();
            context.nextCaseLabel = caseClausesLabels.get(i + 1);
            visit(astNode.getChild(i + 1)); // Caseclause
            addCode(InstructionMnemonic.GOTO, caseClausesEndLabel);
            attachLabel(caseClausesLabels.get(i), caseClausePosition);
        }

        // Visit otherwise clause.
        int caseOtherwisePosition = getNext();
        visit(astNode.getChild(astNode.getSize() - 1)); // OtherwiseClause
        attachLabel(caseOtherwiseLabel, caseOtherwisePosition);

        // Restore previous case variable.
        context.currentCaseVariableSymbol = previousCaseVariableSymbol;

        // With current implementation, the case statement is always followed by a NOP.
        // The case clauses end label is attached to the NOP.
        attachLabel(caseClausesEndLabel, getNext());
    }

    @Override
    protected void visitReadStatement(ASTNode astNode) {
        for (int i = 0; i < astNode.getSize(); i++) { // list
            // Generate instruction to save in local/global variable.
            String identifier = ((IdentifierNode) astNode.getChild(i)).getIdentifierValue();
            VariableSymbol variableSymbol = lookupVariable(identifier);
            if (variableSymbol == null) continue;

            // Change instruction depending on the parameter type.
            // Here top increases by one each time.
            // But, since the next instruction saves the value, top will decrease again.
            // So overall, no change to the top.
            if (variableSymbol.typeSymbol.isInteger()) {
                addCode(InstructionMnemonic.SOS, OperatingSystemOpType.INPUT);
            } else if (variableSymbol.typeSymbol.isChar()) {
                addCode(InstructionMnemonic.SOS, OperatingSystemOpType.INPUTC);
            } else {
                addError("Invalid type for read statement: " + variableSymbol.typeSymbol);
                continue;
            }
            addCode(variableSymbol.isGlobal ? InstructionMnemonic.SGV : InstructionMnemonic.SLV,
                    variableSymbol.address);
        }
    }

    @Override
    protected void visitExitStatement(ASTNode astNode) {
        addCode(InstructionMnemonic.HALT);
    }

    @Override
    protected void visitReturnStatement(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
        FcnSymbol activeFcnSymbol = context.activeFcnSymbol;
        if (activeFcnSymbol == null) {
            addError("Return statement outside of function.");
            return;
        }
        if (typeMismatch(activeFcnSymbol.returnTypeSymbol, context.exprTypeSymbol)) return;

        addCode(InstructionMnemonic.RTN, 1);
        context.top--;
    }

    @Override
    protected void visitNullStatement(ASTNode astNode) {
        // Do nothing.
    }

    @Override
    protected void visitIntegerOutExp(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
        // Expression type does not change.
    }

    @Override
    protected void visitStringOutExp(ASTNode astNode) {
        String literalWithQuotes = ((IdentifierNode) astNode.getChild(0)).getIdentifierValue();
        context.stringExpression = literalWithQuotes.substring(1, literalWithQuotes.length() - 1); // StringNode
        context.exprTypeSymbol = SymbolTable.STRING_TYPE;
    }

    @Override
    protected void visitCaseClause(ASTNode astNode) {
        // This uses the puedo variable created using the case expression value.
        // Each case expression becomes equality checks combined with or operators.
        // Finally, depending on the expression result, this will go to case body or next case.

        Label caseBodyLabel = new Label();
        Label nextCaseLabel = context.nextCaseLabel;
        VariableSymbol caseVariableSymbol = context.currentCaseVariableSymbol;

        // Push n variable loads and equality operators.
        int nCaseExpressions = astNode.getSize() - 1;
        for (int i = 0; i < nCaseExpressions; i++) { // list
            Node caseExprBaseNode = astNode.getChild(i); // CaseExpression
            if (caseExprBaseNode instanceof IdentifierNode) {
                IdentifierNode caseExprNode = (IdentifierNode) caseExprBaseNode;
                TypeSymbol caseExprTypeSymbol = getConstantType(caseExprNode);
                if (caseExprTypeSymbol == null) continue;
                if (typeMismatch(caseVariableSymbol.typeSymbol, caseExprTypeSymbol)) continue;

                addCode(InstructionMnemonic.LLV, caseVariableSymbol.address);
                addCode(InstructionMnemonic.LIT, getConstantValue(caseExprNode));
                addCode(InstructionMnemonic.BOP, BinaryOpType.BEQ);
                context.top++;
            } else {
                // Process double dot case expression.
                // Top will increase by one.
                visit(caseExprBaseNode); // ..
            }
        }

        // Add n - 1 OR conditions to join all previous booleans.
        for (int i = 0; i < nCaseExpressions - 1; i++) {
            addCode(InstructionMnemonic.BOP, BinaryOpType.BOR);
            context.top--;
        }

        // Conditionally go to the body or the next case.
        addCode(InstructionMnemonic.COND, caseBodyLabel, nextCaseLabel);
        context.top--;
        // The code of body.
        int caseBodyPosition = getNext();
        visit(astNode.getChild(astNode.getSize() - 1)); // Statement
        attachLabel(caseBodyLabel, caseBodyPosition);
    }

    @Override
    protected void visitDoubleDotsClause(ASTNode astNode) {
        IdentifierNode caseExprNode1 = (IdentifierNode) astNode.getChild(0); // ConstValue
        IdentifierNode caseExprNode2 = (IdentifierNode) astNode.getChild(1); // ConstValue

        // Check if both case expressions are of correct type.
        VariableSymbol caseVariableSymbol = context.currentCaseVariableSymbol;
        if (typeMismatch(caseVariableSymbol.typeSymbol, getConstantType(caseExprNode1))) return;
        if (typeMismatch(caseVariableSymbol.typeSymbol, getConstantType(caseExprNode2))) return;
        int caseValue1 = getConstantValue(caseExprNode1);
        int caseValue2 = getConstantValue(caseExprNode2);

        // Check if case values are in order (small..big)
        if (caseValue1 > caseValue2) {
            addError("Case value range is not in order. First value should be smaller/equal to the second.");
        }

        // Generate caseValue1 <= caseVariable and caseVariableSymbol <= caseValue2 condition.
        // Top increases by one (last boolean value).
        addCode(InstructionMnemonic.LIT, caseValue1);
        addCode(InstructionMnemonic.LLV, caseVariableSymbol.address);
        addCode(InstructionMnemonic.BOP, BinaryOpType.BLE);
        addCode(InstructionMnemonic.LLV, caseVariableSymbol.address);
        addCode(InstructionMnemonic.LIT, caseValue2);
        addCode(InstructionMnemonic.BOP, BinaryOpType.BLE);
        addCode(InstructionMnemonic.BOP, BinaryOpType.BAND);
        context.top++;
    }

    @Override
    protected void visitOtherwiseClause(ASTNode astNode) {
        visit(astNode.getChild(0)); // Statement
    }

    @Override
    protected void visitAssignmentStatement(ASTNode astNode) {
        String identifier = ((IdentifierNode) astNode.getChild(0)).getIdentifierValue(); // Name
        visit(astNode.getChild(1)); // Expression

        // Assumption: Currently, if the variable is not defined,
        // the result of the expression is discarded.
        // But, no error will be reported.

        // If the variable does not exist, we ignore the assignment.
        // We have to use base lookup method to avoid writing the error.
        Symbol symbol = symbolTable.lookup(identifier);
        if (symbol == null) {
            // Variable does not exist. We ignore the assignment by popping it.
            addCode(InstructionMnemonic.POP, 1);
            context.top--;
            return;
        } else if (!(symbol instanceof VariableSymbol)) {
            // Symbol exists but is not a variable.
            addError("Expected a variable for the assignment, got " + symbol.symbolType + ".");
            return;
        }

        // Get the variable symbol and check its type.
        // The expression result should be assignable to the variable.
        VariableSymbol variableSymbol = (VariableSymbol) symbol;
        if (typeMismatch(variableSymbol.typeSymbol, context.exprTypeSymbol)) return;

        // Generate instruction to save in local/global variable.
        // This will pop the expression result.
        addCode(variableSymbol.isGlobal ? InstructionMnemonic.SGV : InstructionMnemonic.SLV,
                variableSymbol.address);
        context.top--;
    }

    @Override
    protected void visitSwapStatement(ASTNode astNode) {
        String identifier1 = ((IdentifierNode) astNode.getChild(0)).getIdentifierValue();
        String identifier2 = ((IdentifierNode) astNode.getChild(1)).getIdentifierValue();

        // Get the variable symbols and check their types.
        VariableSymbol variableSymbol1 = lookupVariable(identifier1);
        VariableSymbol variableSymbol2 = lookupVariable(identifier2);
        if (variableSymbol1 == null || variableSymbol2 == null) return;
        if (typeMismatch(variableSymbol1.typeSymbol, variableSymbol2.typeSymbol)) return;

        // Load var1 and var2. Then save var1 in var2 and var2 in var1.
        // Top will not change.
        addCode(variableSymbol1.isGlobal ? InstructionMnemonic.LGV : InstructionMnemonic.LLV,
                variableSymbol1.address);
        addCode(variableSymbol2.isGlobal ? InstructionMnemonic.LGV : InstructionMnemonic.LLV,
                variableSymbol2.address);
        addCode(variableSymbol1.isGlobal ? InstructionMnemonic.SGV : InstructionMnemonic.SLV,
                variableSymbol1.address);
        addCode(variableSymbol2.isGlobal ? InstructionMnemonic.SGV : InstructionMnemonic.SLV,
                variableSymbol2.address);
    }

    @Override
    protected void visitTrue(ASTNode astNode) {
        // Simply add truthy value to stack.
        addCode(InstructionMnemonic.LIT, 1);
        context.exprTypeSymbol = SymbolTable.BOOLEAN_TYPE;
        context.top++;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    protected void visitLtEqualExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BLE);
        context.exprTypeSymbol = SymbolTable.BOOLEAN_TYPE;
        // Two results are popped from the stack and one result is pushed.
        // So the top index is decreased by one for binary operators.
        context.top--;
    }

    @Override
    protected void visitLtExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BLT);
        context.exprTypeSymbol = SymbolTable.BOOLEAN_TYPE;
        context.top--;
    }

    @Override
    protected void visitGtEqualExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BGE);
        context.exprTypeSymbol = SymbolTable.BOOLEAN_TYPE;
        context.top--;
    }

    @Override
    protected void visitGtExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BGT);
        context.exprTypeSymbol = SymbolTable.BOOLEAN_TYPE;
        context.top--;
    }

    @Override
    protected void visitEqualsExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BEQ);
        context.exprTypeSymbol = SymbolTable.BOOLEAN_TYPE;
        context.top--;
    }

    @Override
    protected void visitNotEqualsExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BNE);
        context.exprTypeSymbol = SymbolTable.BOOLEAN_TYPE;
        context.top--;
    }

    @Override
    protected void visitAddExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        TypeSymbol leftTypeSymbol = context.exprTypeSymbol;
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BPLUS);
        // Expression type is the type of the left operand
        context.exprTypeSymbol = leftTypeSymbol;
        context.top--;
    }

    @Override
    protected void visitSubtractExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        TypeSymbol leftTypeSymbol = context.exprTypeSymbol;
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BMINUS);
        // Expression type is the type of the left operand
        context.exprTypeSymbol = leftTypeSymbol;
        context.top--;
    }

    @Override
    protected void visitOrExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        TypeSymbol firstType = context.exprTypeSymbol;
        visit(astNode.getChild(1)); // Term
        TypeSymbol secondType = context.exprTypeSymbol;
        if (isLogicalOperatorDefined(firstType, secondType)) {
            addCode(InstructionMnemonic.BOP, BinaryOpType.BOR);
        }
        context.exprTypeSymbol = SymbolTable.BOOLEAN_TYPE;
        context.top--;
    }

    @Override
    protected void visitMultiplyExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BMULT);
        context.exprTypeSymbol = SymbolTable.INTEGER_TYPE;
        context.top--;
    }

    @Override
    protected void visitDivideExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BDIV);
        context.exprTypeSymbol = SymbolTable.INTEGER_TYPE;
        context.top--;
    }

    @Override
    protected void visitAndExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        TypeSymbol firstType = context.exprTypeSymbol;
        visit(astNode.getChild(1)); // Term
        TypeSymbol secondType = context.exprTypeSymbol;
        if (isLogicalOperatorDefined(firstType, secondType)) {
            addCode(InstructionMnemonic.BOP, BinaryOpType.BAND);
        }
        context.exprTypeSymbol = SymbolTable.BOOLEAN_TYPE;
        context.top--;
    }

    @Override
    protected void visitModExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Term
        visit(astNode.getChild(1)); // Term
        addCode(InstructionMnemonic.BOP, BinaryOpType.BMOD);
        context.exprTypeSymbol = SymbolTable.INTEGER_TYPE;
        context.top--;
    }

    @Override
    protected void visitNegativeExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Primary
        if (isNumericOperatorDefined(context.exprTypeSymbol)) {
            addCode(InstructionMnemonic.UOP, UnaryOpType.UNEG);
        }
        context.exprTypeSymbol = SymbolTable.INTEGER_TYPE;
        // One result is popped from the stack and one result is pushed.
        // So the top index does not change for unary operators.
    }

    @Override
    protected void visitNotExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Primary
        if (isBooleanOperatorDefined(context.exprTypeSymbol)) {
            addCode(InstructionMnemonic.UOP, UnaryOpType.UNOT);
        }
        context.exprTypeSymbol = SymbolTable.BOOLEAN_TYPE;
    }

    @Override
    protected void visitEofExpression(ASTNode astNode) {
        addCode(InstructionMnemonic.SOS, OperatingSystemOpType.EOF);
        context.exprTypeSymbol = SymbolTable.BOOLEAN_TYPE;
        context.top++;
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
            typeSymbols.add(context.exprTypeSymbol);
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
        if (isSuccPredOperatorDefined(context.exprTypeSymbol)) {
            addCode(InstructionMnemonic.UOP, UnaryOpType.USUCC);
        }
        // Expression type does not change.
    }

    @Override
    protected void visitPredExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Primary
        if (isSuccPredOperatorDefined(context.exprTypeSymbol)) {
            addCode(InstructionMnemonic.UOP, UnaryOpType.UPRED);
        }
        // Expression type does not change.
    }

    @Override
    protected void visitChrExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
        // Simply change the expression type to char.
        context.exprTypeSymbol = SymbolTable.CHAR_TYPE;
    }

    @Override
    protected void visitOrdExpression(ASTNode astNode) {
        visit(astNode.getChild(0)); // Expression
        // Simply change the expression type to integer.
        context.exprTypeSymbol = SymbolTable.INTEGER_TYPE;
    }

    @Override
    public void visitIdentifier(IdentifierNode identifierNode) {
        // Simple literals.
        if (TokenKind.CHAR_LITERAL.equals(identifierNode.getKind())) {
            addCode(InstructionMnemonic.LIT, identifierNode.getIdentifierValue().codePointAt(1));
            context.exprTypeSymbol = SymbolTable.CHAR_TYPE;
            context.top++;
            return;
        }
        if (TokenKind.INTEGER_LITERAL.equals(identifierNode.getKind())) {
            addCode(InstructionMnemonic.LIT, Integer.parseInt(identifierNode.getIdentifierValue()));
            context.exprTypeSymbol = SymbolTable.INTEGER_TYPE;
            context.top++;
            return;
        }

        // If it is a variable, then we have to load it from the stack.
        // Variables are handled depending on whether they are global or local.
        Symbol symbol = symbolTable.lookup(identifierNode.getIdentifierValue());
        if (symbol instanceof VariableSymbol) {
            VariableSymbol variableSymbol = (VariableSymbol) symbol;
            addCode(variableSymbol.isGlobal ? InstructionMnemonic.LGV : InstructionMnemonic.LLV,
                    variableSymbol.address);
            context.exprTypeSymbol = variableSymbol.typeSymbol;
            context.top++;
            return;
        }
        // Constants are simply loaded to the stack.
        if (symbol instanceof ConstantSymbol) {
            ConstantSymbol constantSymbol = (ConstantSymbol) symbol;
            addCode(InstructionMnemonic.LIT, constantSymbol.value);
            context.exprTypeSymbol = constantSymbol.type;
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
        if (!isDefined)
            addError("Invalid types for logical operator. " + "Requires 'boolean'. Found '%s' and '%s'.",
                    firstType.name, secondType.name);
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
        if (fcnSymbol.paramTypeSymbols.size() != paramTypes.size()) {
            addError("Invalid number of parameters for function '%s'. Expected %d, found %d.",
                    fcnSymbol.name, fcnSymbol.paramTypeSymbols.size(), paramTypes.size());
            return false;
        }
        for (int i = 0; i < fcnSymbol.paramTypeSymbols.size(); i++) {
            if (typeMismatch(fcnSymbol.paramTypeSymbols.get(i), paramTypes.get(i))) return false;
        }
        return true;
    }

    private boolean typeMismatch(TypeSymbol firstType, TypeSymbol secondType) {
        if (firstType.isAssignable(secondType)) return false;
        addError("Type mismatch. Types are not compatible. Expected " +
                firstType + ", got " + secondType + ".");
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

    // -----------------------------------------------------------------------------------------------------------------

    public int getConstantValue(IdentifierNode constantValueNode) {
        String value = constantValueNode.getIdentifierValue();
        TokenKind tokenKind = constantValueNode.getKind();

        // Constant has simple literal values.
        if (tokenKind == TokenKind.CHAR_LITERAL) return value.codePointAt(1);
        if (tokenKind == TokenKind.INTEGER_LITERAL) return Integer.parseInt(value);

        // Constant is defined using another constant.
        ConstantSymbol constSymbol = lookupConstant(value);
        if (constSymbol == null) return 0;
        return constSymbol.value;
    }

    public TypeSymbol getConstantType(IdentifierNode constantValueNode) {
        String value = constantValueNode.getIdentifierValue();
        TokenKind tokenKind = constantValueNode.getKind();

        // Constant has simple literal values.
        if (tokenKind == TokenKind.CHAR_LITERAL) return SymbolTable.CHAR_TYPE;
        if (tokenKind == TokenKind.INTEGER_LITERAL) return SymbolTable.INTEGER_TYPE;

        // Constant is defined using another constant.
        ConstantSymbol constSymbol = lookupConstant(value);
        if (constSymbol == null) return null;
        return constSymbol.type;
    }
}
