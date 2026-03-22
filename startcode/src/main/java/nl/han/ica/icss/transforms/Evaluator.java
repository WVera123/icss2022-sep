package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;

public class Evaluator implements Transform {
    private IHANLinkedList<HashMap<String, Literal>> variableValues;
    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();
        transformNode(ast.root);
    }
    private void transformNode(ASTNode node) {
        boolean createsScope = node instanceof Stylesheet || node instanceof Stylerule;
        if (createsScope) {
            variableValues.addFirst(new HashMap<>());
        }

        // Evaluate expressions
        if (node instanceof VariableAssignment) {
            VariableAssignment assignment = (VariableAssignment) node;
            assignment.expression = evaluateExpression(assignment.expression);
            storeVariable(assignment.name.name, (Literal) assignment.expression);
        }
        else if (node instanceof Declaration) {
            Declaration declaration = (Declaration) node;
            declaration.expression = evaluateExpression(declaration.expression);
        }
        // Recursively transform children
        if (node instanceof Stylesheet) {
            Stylesheet sheet = (Stylesheet) node;
            ArrayList<ASTNode> newBody = new ArrayList<>();
            for (ASTNode child : sheet.getChildren()) {
                transformNode(child);
                // Only add it back to the tree if it isn't a variable assignment.
                if (!(child instanceof VariableAssignment)) {
                    newBody.add(child);
                }
            }
            sheet.body = newBody;
        }
        // Flatten if statements inside stylerules
        else if (node instanceof Stylerule) {
            Stylerule rule = (Stylerule) node;
            rule.body = flattenBody(rule.body);
        }
        // Exit scope
        if (createsScope) {
            variableValues.removeFirst();
        }
    }

    private ArrayList<ASTNode> flattenBody(ArrayList<ASTNode> originalBody) {
        ArrayList<ASTNode> flattened = new ArrayList<>();

        for (ASTNode child : originalBody) {
            if (child instanceof IfClause) {
                IfClause ifClause = (IfClause) child;
                // Evaluate the condition
                ifClause.conditionalExpression = evaluateExpression(ifClause.conditionalExpression);
                BoolLiteral condition = (BoolLiteral) ifClause.conditionalExpression;

                // Only traverse the branch that is 'active' based on the condition's value.
                if (condition.value) {
                    variableValues.addFirst(new HashMap<>());
                    ArrayList<ASTNode> evaluatedIfBody = flattenBody(ifClause.body);
                    variableValues.removeFirst();
                    flattened.addAll(evaluatedIfBody);
                }
                else if (ifClause.elseClause != null) {
                    variableValues.addFirst(new HashMap<>());
                    ArrayList<ASTNode> evaluatedElseBody = flattenBody(ifClause.elseClause.body);
                    variableValues.removeFirst();
                    flattened.addAll(evaluatedElseBody);
                }
            }
            else {
                transformNode(child);
                // Only add child if it isn't a variable assignment.
                if (!(child instanceof VariableAssignment)) {
                    flattened.add(child);
                }
            }
        }
        return flattened;
    }
    // If variable exists globally, update value. If not, create variable.
    private void storeVariable(String name, Literal value) {
        for (int i = 0; i < variableValues.getSize(); i++) {
            HashMap<String, Literal> scope = variableValues.get(i);
            if (scope.containsKey(name)) {
                scope.put(name, value);
                return;
            }
        }
        variableValues.getFirst().put(name, value);
    }

    private Literal evaluateExpression(Expression expr) {
        //When expression is a litral (10px) nothing needs to be done.
        if (expr instanceof Literal) {
            return (Literal) expr;
        }
        // When expression is a variable reference: lookup variable's value inside SymbolTable.
        if (expr instanceof VariableReference) {
            String varName = ((VariableReference) expr).name;
            for (int i = 0; i < variableValues.getSize(); i++) {
                HashMap<String, Literal> scope = variableValues.get(i);
                if (scope.containsKey(varName)) {
                    return scope.get(varName);
                }
            }
        }
        // When expression is an operation: do the math and return result instead of the equation.
        // https://aim-cni.github.io/app/docs/Week%206/Les%202/Lesprogramma#1-constant-folding
        if (expr instanceof Operation) {
            Operation op = (Operation) expr;
            Literal left = evaluateExpression(op.lhs);
            Literal right = evaluateExpression(op.rhs);
            return calculate(left, right, op);
        }

        return null;
    }

    private Literal calculate(Literal left, Literal right, Operation op) {
        int leftVal = extractValue(left);
        int rightVal = extractValue(right);
        int resultVal = 0;

        if (op instanceof AddOperation) resultVal = leftVal + rightVal;
        else if (op instanceof SubtractOperation) resultVal = leftVal - rightVal;
        else if (op instanceof MultiplyOperation) resultVal = leftVal * rightVal;
        else if (op instanceof DivideOperation) resultVal = leftVal / rightVal;

        if (left instanceof PixelLiteral || right instanceof PixelLiteral) {
            return new PixelLiteral(resultVal);
        }
        else if (left instanceof PercentageLiteral || right instanceof PercentageLiteral) {
            return new PercentageLiteral(resultVal);
        }
        else {
            return new ScalarLiteral(resultVal);
        }
    }
    // Method that gets value from any of the literal types
    private int extractValue(Literal literal) {
        if (literal instanceof PixelLiteral) return ((PixelLiteral) literal).value;
        if (literal instanceof PercentageLiteral) return ((PercentageLiteral) literal).value;
        if (literal instanceof ScalarLiteral) return ((ScalarLiteral) literal).value;
        return 0;
    }
}