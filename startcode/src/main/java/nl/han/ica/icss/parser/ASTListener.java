package nl.han.ica.icss.parser;


import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.datastructures.HANStack;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
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
		currentContainer.peek().addChild(stylerule);
		currentContainer.push(stylerule);
	}
	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		currentContainer.pop();
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
}