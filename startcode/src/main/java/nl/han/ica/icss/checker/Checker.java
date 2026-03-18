package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;

import static nl.han.ica.icss.checker.TypeEvaluator.getExpressionType;

public class Checker {
    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        checkNode(ast.root);
    }
    //CH06
    private void checkNode(ASTNode node) {
        boolean createsScope = node instanceof Stylesheet || node instanceof Stylerule || node instanceof IfClause || node instanceof ElseClause;
        if (createsScope) {
            variableTypes.addFirst(new HashMap<>());
        }
        //Specific checks
        if (node instanceof VariableAssignment) {
            checkVariableAssignment((VariableAssignment) node);
        } else if (node instanceof VariableReference) {
            checkVariableReference((VariableReference) node);
        } else if (node instanceof Operation) {
            checkOperation((Operation) node);
        } else if (node instanceof Declaration) {
            checkDeclaration((Declaration) node);
        } else if (node instanceof IfClause) {
            checkIfClause((IfClause) node);
        }
        // Recursively check children
        for (ASTNode child : node.getChildren()) {
            checkNode(child);
        }
        if (createsScope) {
            variableTypes.removeFirst();
        }
    }
    private void checkVariableAssignment(VariableAssignment assignment) {
        ExpressionType type = getExpressionType(assignment.expression, variableTypes);
        variableTypes.getFirst().put(assignment.name.name, type);
    }
    // Check if variable is defined in any active scope
    private void checkVariableReference(VariableReference reference) {
        ExpressionType type = getExpressionType(reference, variableTypes);
        if (type == ExpressionType.UNDEFINED) {
            reference.setError("Variable '" + reference.name + "' is not defined or used outside its scope.");
        }
    }
    private void checkOperation(Operation operation) {
        ExpressionType leftType = getExpressionType(operation.lhs, variableTypes);
        ExpressionType rightType = getExpressionType(operation.rhs, variableTypes);
        // CH03
        if (leftType == ExpressionType.COLOR || rightType == ExpressionType.COLOR) {
            operation.setError("Colors are not allowed in operations.");
            return;
        }
        // CH02
        if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if (leftType != rightType) {
                operation.setError("Operands of addition and subtraction must have the exact same type.");
            }
        }
        // CH02
        if (operation instanceof MultiplyOperation || operation instanceof DivideOperation) {
            if (leftType != ExpressionType.SCALAR && rightType != ExpressionType.SCALAR) {
                operation.setError("Multiplication and division require at least one scalar operand.");
            }
        }
    }
    // CH04
    private void checkDeclaration(Declaration declaration) {
        String propertyName = declaration.property.name;
        ExpressionType valueType = getExpressionType(declaration.expression, variableTypes);

        if (propertyName.equals("width") || propertyName.equals("height")) {
            if (valueType != ExpressionType.PIXEL && valueType != ExpressionType.PERCENTAGE) {
                declaration.setError("The property '" + propertyName + "' requires a Pixel or Percentage value.");
            }
        } else if (propertyName.equals("color") || propertyName.equals("background-color")) {
            if (valueType != ExpressionType.COLOR) {
                declaration.setError("The property '" + propertyName + "' requires a Color value.");
            }
        }
    }
    //CH05
    private void checkIfClause(IfClause ifClause) {
        ExpressionType condType = getExpressionType(ifClause.conditionalExpression, variableTypes);
        if (condType != ExpressionType.BOOL) {
            ifClause.setError("The condition of an if-statement must be a boolean.");
        }
    }
}