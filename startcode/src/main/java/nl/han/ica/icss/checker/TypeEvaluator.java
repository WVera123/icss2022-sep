package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Literal;
import nl.han.ica.icss.ast.Operation;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.DivideOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;

public class TypeEvaluator {

    public static ExpressionType getExpressionType(Expression expr, IHANLinkedList<HashMap<String, ExpressionType>> variableTypes) {
        if (expr instanceof Literal) {
            return getLiteralType((Literal) expr);
        } else if (expr instanceof VariableReference) {
            return getReferenceType((VariableReference) expr, variableTypes);
        } else if (expr instanceof Operation) {
            return getOperationType((Operation) expr, variableTypes);
        }
        return ExpressionType.UNDEFINED;
    }
    private static ExpressionType getLiteralType(Literal literal) {
        if (literal instanceof PixelLiteral) return ExpressionType.PIXEL;
        if (literal instanceof PercentageLiteral) return ExpressionType.PERCENTAGE;
        if (literal instanceof ColorLiteral) return ExpressionType.COLOR;
        if (literal instanceof ScalarLiteral) return ExpressionType.SCALAR;
        if (literal instanceof BoolLiteral) return ExpressionType.BOOL;

        return ExpressionType.UNDEFINED;
    }
    private static ExpressionType getReferenceType(VariableReference ref, IHANLinkedList<HashMap<String, ExpressionType>> variableTypes) {
        String varName = ref.name;

        for (int i = 0; i < variableTypes.getSize(); i++) {
            HashMap<String, ExpressionType> scope = variableTypes.get(i);
            if (scope.containsKey(varName)) {
                return scope.get(varName);
            }
        }
        return ExpressionType.UNDEFINED;
    }

    private static ExpressionType getOperationType(Operation op, IHANLinkedList<HashMap<String, ExpressionType>> variableTypes) {
        ExpressionType left = getExpressionType(op.lhs, variableTypes);
        ExpressionType right = getExpressionType(op.rhs, variableTypes);

        if (op instanceof AddOperation || op instanceof SubtractOperation) {
            return left;
        }
        if (op instanceof MultiplyOperation || op instanceof DivideOperation) {
            if (left == ExpressionType.SCALAR) return right;
            return left;
        }

        return ExpressionType.UNDEFINED;
    }
}