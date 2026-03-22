package nl.han.ica.icss.parser;


import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.datastructures.HANStack;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	private AST ast;

	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}
	public AST getAST() {
		return ast;
	}
	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		Stylesheet sheet = new Stylesheet();
		currentContainer.push(sheet);
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		Stylesheet sheet = (Stylesheet) currentContainer.pop();
		ast.setRoot(sheet);
	}
	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule stylerule = new Stylerule();
		currentContainer.push(stylerule);
	}
	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule stylerule = (Stylerule) currentContainer.pop();
		currentContainer.peek().addChild(stylerule);
	}
	@Override
	public void enterSelector(ICSSParser.SelectorContext ctx) {
		ASTNode selector = parseSelector(ctx.getText());
		currentContainer.peek().addChild(selector);
	}
	private ASTNode parseSelector(String text) {
		if (text.startsWith("#")) {
			return new IdSelector(text);
		} else if (text.startsWith(".")) {
			return new ClassSelector(text);
		} else {
			return new TagSelector(text);
		}
	}
	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration declaration = new Declaration();
		currentContainer.push(declaration);
	}
	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration decl = (Declaration) currentContainer.pop();
		currentContainer.peek().addChild(decl);
	}
	@Override
	public void enterLiteral(ICSSParser.LiteralContext ctx) {
		ASTNode literal = parseLiteral(ctx.getText());
		if (literal != null) {
			currentContainer.peek().addChild(literal);
		}
	}
	private ASTNode parseLiteral(String text) {
		if (text.startsWith("#")) {
			return new ColorLiteral(text);
		} else if (text.endsWith("px")) {
			int value = Integer.parseInt(text.replace("px", ""));
			return new PixelLiteral(value);
		} else if (text.endsWith("%")) {
			int value = Integer.parseInt(text.replace("%", ""));
			return new PercentageLiteral(value);
		} else if (text.equals("TRUE") || text.equals("FALSE")) {
			return new BoolLiteral(text.equals("TRUE"));

		} else {
			return new ScalarLiteral(Integer.parseInt(text));
		}
	}
	@Override
	public void enterPropertyName(ICSSParser.PropertyNameContext ctx) {
		PropertyName propertyName = new PropertyName(ctx.getText());
		currentContainer.push(propertyName);
	}
	@Override
	public void exitPropertyName(ICSSParser.PropertyNameContext ctx) {
		PropertyName propertyName = (PropertyName) currentContainer.pop();
		currentContainer.peek().addChild(propertyName);
	}
	@Override
	public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) {
		VariableReference varRef = new VariableReference(ctx.getText());
		currentContainer.push(varRef);
	}
	@Override
	public void exitVariableReference(ICSSParser.VariableReferenceContext ctx) {
		VariableReference varRef = (VariableReference) currentContainer.pop();
		currentContainer.peek().addChild(varRef);
	}
	@Override
	public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment varAss = new VariableAssignment();
		currentContainer.push(varAss);
	}
	@Override
	public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment varAss = (VariableAssignment) currentContainer.pop();
		currentContainer.peek().addChild(varAss);
	}
	@Override
	public void enterExpression(ICSSParser.ExpressionContext ctx) {
		Operation operation = null;
		if (ctx.PLUS() != null) {
			operation = new AddOperation();
		} else if (ctx.MIN() != null) {
			operation = new SubtractOperation();
		} else if (ctx.MUL() != null) {
			operation = new MultiplyOperation();
		} else if (ctx.DIV() != null){
			operation = new DivideOperation();
		}
		if (operation != null) {
			currentContainer.push(operation);
		}
	}
	@Override
	public void exitExpression(ICSSParser.ExpressionContext ctx) {
		if(ctx.getChildCount() == 3) {
			Operation operation = (Operation) currentContainer.pop();
			currentContainer.peek().addChild(operation);
		}
	}
	@Override
	public void enterIfClause(ICSSParser.IfClauseContext ctx) {
		IfClause ifClause = new IfClause();
		currentContainer.push(ifClause);
	}

	@Override
	public void exitIfClause(ICSSParser.IfClauseContext ctx) {
		IfClause ifClause = (IfClause) currentContainer.pop();
		currentContainer.peek().addChild(ifClause);
	}

	@Override
	public void enterElseClause(ICSSParser.ElseClauseContext ctx) {
		ElseClause elseClause = new ElseClause();
		currentContainer.push(elseClause);
	}

	@Override
	public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
		ElseClause elseClause = (ElseClause) currentContainer.pop();
		currentContainer.peek().addChild(elseClause);
	}
}